package com.pwc.servlets;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "queryHelper"
})
public class QueryHelperServlet extends SlingAllMethodsServlet {

    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        Resource pageResource = request.getResource();
        Resource jcrContent = request.getResourceResolver().getResource(pageResource.getPath()+"/jcr:content");

        String property = request.getParameter("property");

        Iterator<Resource> childrenIt = jcrContent.getChildren().iterator();

        JSONArray array = new JSONArray();
        try {
            getNodesProperty(childrenIt, property, array);
        }catch (JSONException e) {
            ExceptionLogger.logException(e);
        }
        response.getWriter().write(array.toString());
    }

    private void getNodesProperty(Iterator<Resource> childrenIt,String property,JSONArray array) throws JSONException{

        while (childrenIt.hasNext()) {
            Resource child = childrenIt.next();
            if((child.getValueMap().get("sling:resourceType") != null && child.getValueMap().get("sling:resourceType").equals(property)) &&
               (child.getValueMap().get("id") != null ) ){
                array.put(getJson(child.getValueMap().get("id").toString()));
            }
            getNodesProperty(child.getChildren().iterator(), property, array);
        }

    }
    private JSONObject getJson(String id) throws JSONException {

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", id);

        return jsonObj;
    }
}
