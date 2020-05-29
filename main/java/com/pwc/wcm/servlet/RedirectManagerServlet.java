package com.pwc.wcm.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.management.InvalidAttributeValueException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC Redirect Manager Servlet",
    "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    "sling.servlet.paths=" + "/bin/redirectmanager"
})
@Designate(ocd = RedirectManagerServlet.Config.class)
public class RedirectManagerServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(RedirectManagerServlet.class);

	private static final String ALLOWED_PATTERN_OLD_URL = "^[0-9a-zA-Z_ \\-/.$]*$";
	private static final String ALLOWED_PATTERN_NEW_URL = "^[0-9a-zA-Z_ \\-/.:]*$";
	
	private String rootpath = "etc/map/http/pwc-origin-aem-staging.pwc.com";
	private String allowedOldUrlPattern = "";
	private String allowedNewUrlPattern = "";
		
	@ObjectClassDefinition(name = "PwC Redirect Manager Servlet", description = "PwC Redirect Manager Servlet")
    @interface Config {
        @AttributeDefinition(name = "Redirect Url Root Path", 
                            description = "Redirect Url Root Path.",
                            type = AttributeType.STRING)
        public String rootpath() default "etc/map/http/pwc-origin-aem-staging.pwc.com";
        
        @AttributeDefinition(name = "Old URL pattern", 
                description = "Old URL pattern.",
                type = AttributeType.STRING)
        public String old_url_pattern() default ALLOWED_PATTERN_OLD_URL;
        
        @AttributeDefinition(name = "New URL pattern", 
                description = "New URL pattern.",
                type = AttributeType.STRING)
        public String new_url_pattern() default ALLOWED_PATTERN_NEW_URL;
    }

    @Activate
    protected void activate(RedirectManagerServlet.Config properties) throws RepositoryException {
        LOGGER.trace("RedirectManagerServlet: Inside activate!");
        this.rootpath = properties.rootpath();
        if (this.rootpath.startsWith("/")) this.rootpath=this.rootpath.substring(1);
        this.allowedOldUrlPattern = properties.old_url_pattern();
        this.allowedNewUrlPattern = properties.new_url_pattern();
        LOGGER.info("RedirectManagerServlet.activate(): Redirect Manager's Config values :\n");
        LOGGER.info("Root Path" + ": " + rootpath + ", " + "Old URL Pattern" + ": " + allowedOldUrlPattern + ", "
                + "New URL Pattern" + ": " + allowedNewUrlPattern);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response) throws ServletException, IOException {

        LOGGER.debug("RedirectManagerServlet.doPost(): Action Type: " + request.getParameter("actype"));
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        switch (request.getParameter("actype").toUpperCase()){
            case "AUTOCOMPLETE":
                response.getWriter().write(doAutoComplete(request));
                break;
            case "SEARCH":
                response.getWriter().write(doSearch(request));
                break;
            case "SAVE":
                response.getWriter().write(doSave(request));
                break;
            case "DELETE":
                response.getWriter().write(doDelete(request));
                break;
        }

        response.getWriter().write("");
    }

    private String doAutoComplete(SlingHttpServletRequest request){
        //logger.info("doAutoComplete");
        String oldurl=request.getParameter("oldurl");
        String status=request.getParameter("status");
        JSONObject jso = new JSONObject();
        JSONArray jsa = new JSONArray();

        Session session = null;
        Node node = null;
        try {
            session = request.getResourceResolver().adaptTo(Session.class);
            node = session.getRootNode().getNode(rootpath.concat(oldurl));
            if (node.hasNodes()) {
                jso.put("total", node.getNodes().getSize());

                NodeIterator ni = node.getNodes();

                while ( ni.hasNext()) {
                    Node n = ni.nextNode();
                    JSONObject j = new JSONObject();
                    j.put("oldurl", node.getPath().replace("/".concat(rootpath),""));
                    jsa.put(j);
                }
                jso.put("rws", jsa);
            }else
                jso.put("total", 0);

        }catch (Exception e){
            try {
                jso.put("error", e.getMessage());
            }catch(JSONException je){}
        }
        return jsa.toString();
    }

    private String doSearch(SlingHttpServletRequest request){
        LOGGER.trace("Inside doSearch()");
        JSONObject jso = new JSONObject();
        Session session = null;

		try {
		    LOGGER.debug("RedirectManagerServlet.doSearch(): Request query parameters: " + request.getParameterMap());
            String status=request.getParameter("status");
            String oldurl=request.getParameter("oldurl").toLowerCase();
            String newurl=request.getParameter("newurl");
            if(validateUrlParamsForAllowedCharacters(oldurl, newurl)) {
                String searchUrl = "/".concat(rootpath).concat(EncodeUrl(oldurl));

                session = request.getResourceResolver().adaptTo(Session.class);
                QueryManager queryManager = session.getWorkspace().getQueryManager();

                jso.put("queryManager", "queryManager");
                String expression = "SELECT * FROM [sling:Mapping] AS s WHERE ";
                String strFolder="", strFile="";
                if (status.equalsIgnoreCase("200")){
                    expression=newurl.equals("")? expression.concat("[sling:internalRedirect]<>'' ") : expression.concat("[sling:internalRedirect] like '").concat(EncodeRedirect(newurl)).concat("%'");
                }else{
                    expression=expression.concat("[sling:status]=".concat(status));
                    if (!newurl.equals(""))
                        expression= expression.concat(" and [sling:redirect] like '").concat(EncodeRedirect(newurl)).concat("%'");
                }

                if (oldurl.endsWith("/"))
                    strFolder=expression.concat(" and ISDESCENDANTNODE([").concat(searchUrl).concat("])");
                else{
                    int lastSlash= searchUrl.lastIndexOf("/");
                    strFolder=expression.concat(" and ISDESCENDANTNODE([").concat(searchUrl).concat("])");
                    strFile=expression.concat(" and ISSAMENODE([").concat(searchUrl).concat("])");
                }

                int i=0;
                JSONArray jsa = new JSONArray();
                Node node = null;
                if (strFile.length()>0) {
                    Query qryFile = queryManager.createQuery(strFile, "JCR-SQL2");
                    QueryResult resultFile = qryFile.execute();
                    NodeIterator niFile = resultFile.getNodes();
                    while (niFile.hasNext()) {
                        jsa.put(this.ConvertJson(niFile.nextNode(), status));
                        i++;
                    }
                }

                Query qryFolder = queryManager.createQuery(strFolder, "JCR-SQL2");
                QueryResult resultFolder = qryFolder.execute();
                NodeIterator niFolder = resultFolder.getNodes();

                while (niFolder.hasNext()) {
                    jsa.put(this.ConvertJson(niFolder.nextNode(), status));
                    i++;
                    if (i>100) break;;
                }

                jso.put("total", i);
                jso.put("rws", jsa);
                // jso.put("expression",expression);
            }
        }catch (Exception e){
            LOGGER.error("RedirectManagerServlet.doSearch(): " + e.getMessage(), e);
            try {
                jso.put("error", e.getMessage());
            } catch (JSONException je) {
                LOGGER.error("RedirectManagerServlet.doSearch(): JSONException occurred! " + e.getMessage(), e);
            }
        }finally{
			if(session!=null && session.isLive()){
                session.logout();;
                session = null;
            }
		}

        String response = jso.toString();
        LOGGER.info("RedirectManagerServlet.doSearch(): Search Response: " + response);

        return response;
    }

    private JSONObject ConvertJson(Node node, String status) throws JSONException, RepositoryException, UnsupportedEncodingException {
        JSONObject j = new JSONObject();
        j.put("oldurl", DecodeUrl(node.getPath().replace("/".concat(rootpath), "")));
        if (status.equalsIgnoreCase("200")) {
            j.put("newurl", DecodeRedirect(node.getProperty("sling:internalRedirect").getString()));
            j.put("status", "200");
        }else {
            j.put("newurl", node.hasProperty("sling:redirect") ? DecodeRedirect(node.getProperty("sling:redirect").getString()) : "");
            j.put("status", node.hasProperty("sling:status") ? node.getProperty("sling:status").getString() : "");
        }
        return  j;
    }

    private String doSave(SlingHttpServletRequest request){
        LOGGER.trace("Inside doSave()");
        LOGGER.debug("RedirectManagerServlet.doSave(): Request query parameters: " + request.getParameterMap());
        String status=request.getParameter("status");
        String oldurl=request.getParameter("oldurl").toLowerCase().trim();
        String newurl=request.getParameter("newurl").trim();

        if (oldurl.endsWith("/")) oldurl=oldurl.substring(0,oldurl.length()-1);

        JSONObject jso = new JSONObject();
        Session session = null;
        Node node = null;
        try {
            if(validateUrlParamsForAllowedCharacters(oldurl, newurl)) {
                session =  request.getResourceResolver().adaptTo(Session.class);
                node= session.getRootNode().getNode(rootpath);

                // Create folder
                int idx = oldurl.lastIndexOf("/");
                try {
                    node = node.getNode(EncodeUrl(oldurl.substring(1, idx)));
                }catch (Exception e){
                    if (idx>1) {
                        for (String s : oldurl.substring(1, idx).split("/")) {
                            if (s.length() > 0) {
                                try {
                                    node = node.getNode(EncodeUrl(s));
                                } catch (Exception exx) {
                                    node = node.addNode(EncodeUrl(s), "sling:Mapping");
                                    node.setProperty("sling:match", EncodeRedirectMatch(s));
                                    session.save();
                                }
                            }
                        }
                    }
                }

                String nodeName=oldurl.substring(idx + 1,  oldurl.length());
                try {
                    node = node.getNode(EncodeUrl(nodeName));
                } catch (Exception ex) {
                    node = node.addNode(EncodeUrl(nodeName), "sling:Mapping");
                }

                node.setProperty("sling:match", EncodeRedirectMatch(nodeName));
                if (status.equalsIgnoreCase("200")){
                    node.setProperty("sling:internalRedirect", EncodeRedirect(newurl));
                    node.setProperty("sling:redirect", (Value)null);
                    node.setProperty("sling:status", (Value)null);
                }else {
                    node.setProperty("sling:redirect", EncodeRedirect(newurl));
                    node.setProperty("sling:status", status);
                    node.setProperty("sling:internalRedirect", (Value)null);	
                }

                session.save();
                jso.put("saved", true);
            }
        }catch (Exception e){
            LOGGER.error("RedirectManagerServlet.doSave(): " + e.getMessage(), e);
            try {
                jso.put("saved", false);
                jso.put("error", e.getMessage());
            } catch (JSONException je) {
                LOGGER.error("RedirectManagerServlet.doSave(): JSONException occurred! " + e.getMessage(), e);
            }
        }finally {
            if(session!=null && session.isLive()){
                session.logout();;
                session = null;
            }
		}
        String response = jso.toString();
        LOGGER.info("RedirectManagerServlet.doSave(): Save Response: " + response);
        return response;
    }

    private String doDelete(SlingHttpServletRequest request){
        LOGGER.trace("Inside doDelete()");
        LOGGER.debug("RedirectManagerServlet.doDelete(): Request query parameters: " + request.getParameterMap());
        String oldurl=request.getParameter("oldurl");
        JSONObject jso = new JSONObject();

        Session session = null;
        Node node = null;
        try {
            if(validateUrlParamsForAllowedCharacters(oldurl, "")) {
                session =  request.getResourceResolver().adaptTo(Session.class);
                node= session.getRootNode().getNode(rootpath.concat(EncodeUrl(oldurl)));
                if (node.hasNodes()){
                    node.setProperty("sling:internalRedirect", (Value)null);
                    node.setProperty("sling:redirect", (Value)null);
                    node.setProperty("sling:status", (Value)null);
                }else {
                    Node parentNode = node.getParent();
                    node.setProperty("cq:lastReplicationAction", "Deactivate");
                    while (parentNode.getNodes().getSize() == 1 && !parentNode.hasProperty("sling:internalRedirect") && !parentNode.hasProperty("sling:redirect")) {
                        node = parentNode;
                        parentNode = node.getParent();
                        node.setProperty("cq:lastReplicationAction", "Deactivate");
                    }
                }
                session.save();
                jso.put("deleted", true);
            }
        }catch (Exception e){
            LOGGER.error("RedirectManagerServlet.doDelete(): " + e.getMessage(), e);
            try {
                jso.put("deleted", false);
                jso.put("error", e.getMessage());
            } catch (JSONException je) {
                LOGGER.error("RedirectManagerServlet.doDelete(): JSONException occurred! " + e.getMessage(), e);
            }
        }finally{
			if(session!=null && session.isLive()){
                session.logout();;
                session = null;
            }
		}
        String response = jso.toString();
        LOGGER.info("RedirectManagerServlet.doDelete(): Deletion Response: " + response);
        return response;
    }
    
    private boolean validateUrlParamsForAllowedCharacters(String oldUrl, String newUrl) throws InvalidAttributeValueException {
        if (!oldUrl.matches(allowedOldUrlPattern)) {
            LOGGER.error("RedirectManagerServlet.validateUrlParamsForAllowedCharacters(): "
                    + "Old Url Validation Failed for value: " + oldUrl);
            throw new InvalidAttributeValueException("Old Url should be of the pattern: " + allowedOldUrlPattern);
        } else if (!newUrl.matches(allowedNewUrlPattern)) {
            LOGGER.error("RedirectManagerServlet.validateUrlParamsForAllowedCharacters(): "
                    + "New Url Validation Failed for value: " + newUrl);
            throw new InvalidAttributeValueException("New Url should be of the pattern: " + allowedNewUrlPattern);
        } else {
            LOGGER.error("RedirectManagerServlet.validateUrlParamsForAllowedCharacters(): "
                    + "Url Validation success for the values: oldUrl = " + oldUrl + ", newUrl = " + newUrl);
            return true;
        }
    }

    private String EncodeUrl(String s) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(s, "UTF-8").replace("%2F", "/").replace("%24", "$");
    }
    private String DecodeUrl(String s)  throws UnsupportedEncodingException{
        return java.net.URLDecoder.decode(s, "UTF-8");
    }
    private String EncodeRedirectMatch(String s){
        return "(?i)".concat(s.replace("[", "\\\\[").replace("]", "\\\\]").replace("&", "\\\\&").replace("(", "\\\\(").replace(")", "\\\\)"));
    }
    private String EncodeRedirect(String s){
        return s.replace("[", "\\\\[").replace("]", "\\\\]").replace("&", "\\\\&").replace("(", "\\\\(").replace(")", "\\\\)");
    }
    private String DecodeRedirect(String s){
        return s.replace("\\\\[", "[").replace("\\\\]", "]").replace("\\\\&", "&").replace("\\\\(", "(").replace("\\\\)", ")");
    }
}
