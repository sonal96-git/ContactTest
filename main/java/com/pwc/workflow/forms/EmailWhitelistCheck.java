package com.pwc.workflow.forms;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.pwc.util.MXUtils;
import com.pwc.workflow.WorkFlowConstants;


/**
 * Process Step for MX Validation Step in Form Submission Workflow, which validates if visitor's email (if provided),
 * email-domain exists in whitelist in Referene Data or not, if not it checks in MX Lookup Service.
 *
 * @author  jayati
 */
@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for checking whitelisted Emails",
    Constants.SERVICE_VENDOR + "= PwC",
   "process.label=" + "PwC workflow for checking whitelisted Emails"    
})
@Designate(ocd = EmailWhitelistCheck.Config.class)
public class EmailWhitelistCheck implements WorkflowProcess {

	private static final String ERROR_NOT_FOUND = "error-not-found";
	public static final Logger log = LoggerFactory.getLogger(EmailWhitelistCheck.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    // private static final String ENDPOINT = "pwc.mxlookup.endpoint";
    public static final Gson GSON = new Gson();
    public static final String EMAIL_REGEX_PATTERN = "^[^@]+([@]{1})[0-9a-zA-Z\\\\._-]+([\\\\.]{1})[0-9a-zA-Z\\\\._-]+$";
    private static String mxEndpoint = "https://dns-api.org";
    
    @ObjectClassDefinition(name = "PwC workflow for checking whitelisted Emails", 
    		description = "PwC workflow for checking whitelisted Emails")
    @interface Config {
        @AttributeDefinition(name = "Endpoint", 
                            description = "Endpoint",
                            type = AttributeType.STRING)
        public String endPoint() default "https://dns-api.org";
    }
    
    /**
     * Execution logic for Process Step in MX Validation in Form Submission workflow, which identifies if form data
     * contains visitor-email.
     * Case-1 : Continues with usual processing in case visitor email id not provided.
     * Case-2 : Perform MX Check in case email is found within form data and sets workflow paramters accordingly.
     *
     * @param item {@link WorkItem} workflow item
     * @param session {@link WorkflowSession} workflow session
     * @param args {@Link MetaDataMap} workflow arguments
     */
    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        log.debug("Email Whitelist Check begins!!");
        ResourceResolver resourceResolver = null;
        try { 
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                Boolean emailWhitelisted = true;
                String visitorEmail = "";
                String path = workflowData.getPayload().toString();
                log.debug("EmailWhitelistCheck.execute: Checking email whitelist for path: {}",path);
                resourceResolver = session.adaptTo(ResourceResolver.class);
                Resource res = resourceResolver.getResource(WorkFlowConstants.EMAIL_WHITELIST_PATH);
                ValueMap props = ResourceUtil.getValueMap(res);
                String[] whitelistedEmails = props.get("whitelistedEmails",String[].class);
                ValueMap formData = ResourceUtil.getValueMap(resourceResolver.getResource(path));
                // Contact-Us form-data contains "visitor-email" key for visitor email in form fields
                if(formData.containsKey("visitor-email")){
                    visitorEmail = formData.get("visitor-email").toString();
                    log.debug("EmailWhitelistCheck.execute: Checking email whitelist for Contact-Us forms. visitorEmail: {}", visitorEmail);
                    emailWhitelisted = checkWhiteList(visitorEmail, whitelistedEmails, session);
                }else{
                    log.debug("EmailWhitelistCheck.execute: Checking email whitelist for online forms.");
                    // for online forms where visitor email does not have any specific form field name
                    Pattern emailPattern  = Pattern.compile(EMAIL_REGEX_PATTERN);
                    for(Map.Entry<String,Object> formField : formData.entrySet()){
                        Matcher emailMatcher = emailPattern.matcher(formField.getValue().toString());
                        if(emailMatcher.matches()){
                            visitorEmail = formField.getValue().toString();
                            log.debug("EmailWhitelistCheck.execute: Email found in online forms data!! Checking email whitelist for visitorEmail:", visitorEmail);
                            emailWhitelisted = checkWhiteList(visitorEmail,whitelistedEmails, session);
                            break;
                        }
                    }
                }
                workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, emailWhitelisted.toString());
                if(StringUtils.isNotBlank(visitorEmail)){
                    workflowData.getMetaDataMap().put(WorkFlowConstants.VISITOR_EMAIL, visitorEmail.split("@")[1]);
                }
            }
        }catch(ArrayIndexOutOfBoundsException arrayIOBAxception){
            log.error("EmailWhitelistCheck.execute: ", arrayIOBAxception);
            throw new WorkflowException(arrayIOBAxception.getMessage(), arrayIOBAxception);
        }catch (PersistenceException persistenceException){
            log.error("EmailWhitelistCheck.execute: ", persistenceException);
            throw new WorkflowException(persistenceException.getMessage(),persistenceException);
        }catch(Exception exception){
            log.error("EmailWhitelistCheck.execute: ", exception);
            throw new WorkflowException(exception.getMessage(), exception);
        }
        finally {
            if(resourceResolver!=null){
                resourceResolver.close();
            }
        }
    }

    /**
     *
     * @param context
     */
    @Activate
    protected void activate(EmailWhitelistCheck.Config properties) {
        this.mxEndpoint = properties.endPoint();
    }

    /**
     * Recieves visitor email from form data and checks it in whitelist and performs MX-lookup, in case it is not found in whitelist.
     * On successful MX Lookup it adds to whitelist and returns true.
     * On Failure of MX Lookup returns false.
     * In case of no response from MX-lookup service, returns true and do not add to whitelist.
     *
     * @param visitorEmail {@link String} visitor -mail
     * @param whitelistedEmails {@link String[]} whitelisted emails
     * @param session {@link WorkflowSession} uses session to add to whitelist
     * @return {@link Boolean} True if domain found in whitelist or in MX-lookup
     */
    private Boolean checkWhiteList(String visitorEmail, String[] whitelistedEmails, WorkflowSession session) throws PersistenceException {
        try {
            String domain = visitorEmail.split("@")[1];
            log.debug("EmailWhitelistCheck.checkWhiteList: Checking email whitelist for domain: {}",domain);
            for (String emailDomain : whitelistedEmails) {
                if (StringUtils.containsIgnoreCase(emailDomain, domain)) {
                    log.debug("EmailWhitelistCheck.checkWhiteList: Domain '{}' found in whitelist!!",domain);
                    return true;
                }
            }
            String status = emailDomainExists(domain);
            log.info("EmailWhitelistCheck.checkWhiteList: Status from emailDomainExists() for email domain '{}' is: {}", domain, status);
            if (StringUtils.isNotEmpty(status)){
                if(status == "exists") {
                    MXUtils.addToWhiteList(domain, session);
                    return true;
                } else if (StringUtils.isNotEmpty(status) && status == "error") {
                    return false;
                } else if (StringUtils.isNotEmpty(status) && status == "no response") {
                    return true;
                }
            }
        }catch (PersistenceException persistenceException){
            log.error("EmailWhitelistCheck.checkWhiteList: PersistenceException found!!", persistenceException);
            throw persistenceException;
        }
        return false;
    }

    /**
     * Uses visitor's email domain and forms path for MX-lookup.
     *
     * @param emailDomain {@link String} email domain for MX-lookup
     * @return {@link String} Returns Response from MX Serice
     */
    private String emailDomainExists(final String emailDomain) {
        log.debug("EmailWhitelistCheck.emailDomainExists: Checking MX Lookup for domain: {}",emailDomain);
        final String url = String.format("%s/MX/%s", this.mxEndpoint, emailDomain);
        final String response = getResponseFromServer(url);
        log.debug("EmailWhitelistCheck.emailDomainExists: Response received from MX Lookup server: {}",response);
        String status = "exists";
        if (response != null) {
            try {
				if (response.equals(ERROR_NOT_FOUND)) {
					log.debug("EmailWhitelistCheck.emailDomainExists : Domain {} not found in MX Lookup!! Response : []", emailDomain);
					return "error";
				}
              final JsonElement jsonElement =  GSON.fromJson(response, JsonElement.class) ;
                if(jsonElement.isJsonArray()){
                    for (final JsonElement json : jsonElement.getAsJsonArray()) {
                        final JsonObject jsonObj = json.getAsJsonObject();
                        boolean hasErrorKey = jsonObj.has("error");
                        if (hasErrorKey) {
                            log.debug("EmailWhitelistCheck.emailDomainExists : Domain {} not found in MX Lookup!! Response : \n {}" , emailDomain, response);
                            return "error";
                        }else if(!(!hasErrorKey && jsonObj.has("name"))){
                            log.debug("EmailWhitelistCheck.emailDomainExists : No 'name' property found in MX Lookup response : \n {}" , response);
                            throw new IllegalArgumentException(response);
                        }
                    }
                }else if(jsonElement.isJsonObject()){
                    if(jsonElement.getAsJsonObject().has("error")){
                        log.debug("EmailWhitelistCheck.emailDomainExists : Domain {} not found in MX Lookup!! Response : \n {}" , emailDomain, response);
                        return "error";
                    }else{
                        throw new IllegalArgumentException(response);
                    }
                }
            } catch (IllegalArgumentException illegalArgumentException){
                log.error("EmailWhitelistCheck.emailDomainExists : IllegalArgumentException occurred while checking email domain. MX endpoint response {} is not in expected format for email domain {}",illegalArgumentException,emailDomain);
            } catch (JsonSyntaxException JsonSyntaxException){
                log.error("EmailWhitelistCheck.emailDomainExists : MX endpoint response {} is not in JSON format for email domain {}" , response, emailDomain);
            }
        }else{
            log.debug("EmailWhitelistCheck.emailDomainExists : MX endpoint response is null");
            return "no response";
        }
        log.info("EmailWhitelistCheck.emailDomainExists : Domain {} in MX Lookup for Domain {}!! Response : \n" + response, status, emailDomain);
        return status;
    }


    /**
     * Takes MX-lookup endpoint path and performs mx-lookup using Http Connection.
     *
     * @param urlPath {@link URL} endpoint for mx-lookup api
     * @return {@link String} returns response from MX-lookup service
     */
    private String getResponseFromServer(final String urlPath) {
        log.debug("EmailWhitelistCheck.getResponseFromServer : Requesting MX Lookup for URL: {}" , urlPath);
        String response = null;
        try {
            final URL url = new URL(urlPath);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla");

            final int status = connection.getResponseCode();
            if ((status == HttpURLConnection.HTTP_MOVED_TEMP) || (status == HttpURLConnection.HTTP_MOVED_PERM)
                    || (status == HttpURLConnection.HTTP_SEE_OTHER)) {
                final String redirectUrl = connection.getHeaderField("Location");
                log.debug("EmailWhitelistCheck.getResponseFromServer: MX Lookup: Request Redirect to: " + redirectUrl);
                return getResponseFromServer(redirectUrl);
            } else if ((status < 200) || (status > 299)) {
                log.error("EmailWhitelistCheck.getResponseFromServer: MX Lookup Failed: Error response code: " + status);
                final InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    final String errorMessage = IOUtils.toString(errorStream);
					final JsonElement jsonResult = GSON.fromJson(errorMessage, JsonElement.class);
					if (jsonResult.isJsonObject() && jsonResult.getAsJsonObject().has("error")) {
						throw new IOException(jsonResult.getAsJsonObject().get("error").getAsString());
					} else {
						response = ERROR_NOT_FOUND;
						throw new IOException("Json response is" + errorMessage);
					}
                }
                throw new IOException(String.format("HTTP error %d %s", connection.getResponseCode(),
                        connection.getResponseMessage()));
            } else {
                response = IOUtils.toString(connection.getInputStream());
                log.info("EmailWhitelistCheck.getResponseFromServer: MX Lookup Success Response: \n{}", response);
            }
        } catch (final MalformedURLException malformedUrlExp) {
            log.error("EmailWhitelistCheck.getResponseFromServer: MX Lookup Failed. Invalid URL : " + malformedUrlExp);
        } catch (final IOException ioExp) {
            log.error("EmailWhitelistCheck.getResponseFromServer: MX Lookup Failed. Error response: " + ioExp);
        }
        return response;
    }
}
