package com.pwc.util;

import com.adobe.granite.workflow.WorkflowSession;
import com.pwc.workflow.WorkFlowConstants;
import com.pwc.workflow.forms.EmailWhitelistCheck;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utils class for MX Validation Step in Form Submission Workflow.
 *
 * @author jayati
 */
public class MXUtils {

    public static final Logger LOGGER  = LoggerFactory.getLogger(MXUtils.class);
    public static final String WHITELISTED_EMAILS_NODE = "whitelistedEmails";

    /**
     * Adds visitor emmail-domain to whitelist in reference-data node in CRX after verifying
     * from MX-Lookup or operations team validation of correct email domain.
     * @param visitorEmailDomain {@link String}describes visitor Email Domain
     * @param session {@Link WorkflowSession} holds session of current workflow instance
     */
    public static void addToWhiteList(String visitorEmailDomain, WorkflowSession session) throws PersistenceException, NullPointerException{
       LOGGER.info("Attempt to add '{}' email domain to whitelist begins!!", visitorEmailDomain);
       ResourceResolver resourceResolver = null;
            try {
                resourceResolver = session.adaptTo(ResourceResolver.class);
                Resource res = resourceResolver.getResource(WorkFlowConstants.EMAIL_WHITELIST_PATH);
                LOGGER.debug("Found resource at whitelist path:{}", res.getPath());
                ModifiableValueMap whiteListMap = res.adaptTo(ModifiableValueMap.class);
                String[] whitelistedEmails = whiteListMap.get(WHITELISTED_EMAILS_NODE, String[].class);
                List<String> whitelist = new ArrayList<>(Arrays.asList(whitelistedEmails));
                whitelist.add(visitorEmailDomain);
                LOGGER.debug("Prepared the email domain whitelist!!");
                whiteListMap.put(WHITELISTED_EMAILS_NODE, whitelist.toArray());
                resourceResolver.commit();
                LOGGER.debug("Succesfully added '{}' email domain to whitelist!!", visitorEmailDomain);
            }catch (PersistenceException persistenceException){
                LOGGER.error("PersistenceException found while trying to add '{}' email domain to whitelist!! \n {}", visitorEmailDomain, persistenceException);
                throw persistenceException;
            }catch (NullPointerException npe){
                LOGGER.error("NullPointerException found while trying to add '{}' email domain to whitelist!! \n {}", visitorEmailDomain, npe);
                throw npe;
            }
            finally {
                if(resourceResolver!=null){
                    resourceResolver.close();
                }
            }
    }
}
