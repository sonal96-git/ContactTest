package com.pwc.model.components.highlights;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.jcr.Node;
import javax.servlet.Servlet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/speakers",
})
public class HighLightsServlet extends SlingAllMethodsServlet  {


    private static final long serialVersionUID = 1L;
    private final String LINK_LIST_KEY = "highlight_items";
    private final String NAME_KEY = "name";
	private final String ID_KEY = "id";	
	
	private HighLightsModel highLightsModel;	

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
    	String action = request.getParameter("action") != null ? request.getParameter("action") : "";
    	String parentPath = request.getParameter("parenPath") != null ? request.getParameter("parenPath") : "";   
     	switch (action) {
		case "1":   // action to update name of Items from item of parent dialog		
			UpdateHighLightsItemsToHighLights(request , parentPath);
			break;		
		case "2":	// delete node and update list of parent dialog item.	
			UpdateWhenDeleteItems(request ,parentPath, response);
			break;		
		default:
			break;
		} 
    }
    
	private String DecodeString(String text) { 
		String result = "";
		try{
			result = URLEncoder.encode(text, "ISO-8859-1" );
			result = URLDecoder.decode(result, "UTF-8" );
		}catch(Exception e){
			ExceptionLogger.logExceptionMessage("HighLightsServlet DecodeString error :",e);	
		}    
		return result;
     }
    
     private void UpdateWhenDeleteItems(SlingHttpServletRequest request, String parentPath,SlingHttpServletResponse response) {   
		ResourceResolver resolver = request.getResourceResolver();
		Resource resource = resolver.getResource(parentPath);
		if(resource == null) return;
		this.highLightsModel = resource.adaptTo(HighLightsModel.class);         
		String[] itemsList = this.highLightsModel.getHighlight_items() != null ? this.highLightsModel.getHighlight_items() : null;
		if(itemsList == null) return;         
		String nodeName = request.getParameter("nodeName") != null ? request.getParameter("nodeName") : "";  
		Node node = resource.adaptTo(Node.class);             
		try{    
		 	String[] values = new String[itemsList.length];	
		 	for(int i = 0; i < itemsList.length; i++) {
		        JSONObject jsonLink = new JSONObject(itemsList[i]);	                 
		        if(jsonLink.optString(ID_KEY).equals(nodeName) && i == 0) {	                	
		        	values = Arrays.copyOfRange(itemsList, i+1, itemsList.length);
		        	break;
		        }
		        if(jsonLink.optString(ID_KEY).equals(nodeName)) {	 
		    	  values = (String[])ArrayUtils.addAll((Arrays.copyOfRange(itemsList, 0, i)),Arrays.copyOfRange(itemsList, i+1, itemsList.length));	 
		          break;
		        } 
		 	}      
			node.setProperty(LINK_LIST_KEY, values);
			node.getSession().save();
         }catch (Exception e) {
        	 ExceptionLogger.logExceptionMessage("HighLightsServlet UpdateWhenDeleteItems error :",e);	
         }            
     }      
    
        
     private void UpdateHighLightsItemsToHighLights(SlingHttpServletRequest request, String parentPath) {    	 
    	if(parentPath.equals(""))return;    	
        String nodeName = request.getParameter("nodeName") != null ? request.getParameter("nodeName") : "";
        String title = request.getParameter("title") != null ? request.getParameter("title") : "";
        String titleResult = DecodeString(title.trim());    
        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = resolver.getResource(parentPath);
        Node node = resource.adaptTo(Node.class);      
        if(node == null) return;		
		this.highLightsModel = resource.adaptTo(HighLightsModel.class);			
        String[] itemsList = this.highLightsModel.getHighlight_items() != null ? this.highLightsModel.getHighlight_items() : null;            
        if(itemsList == null) return;
        try{    
        	String[] values = new String[itemsList.length];	
        	for(int i = 0; i < itemsList.length; i++) {
                JSONObject jsonLink = new JSONObject(itemsList[i]);
                if(jsonLink.optString(ID_KEY).equals(nodeName) && !titleResult.equals("")) {	                	
                	jsonLink.put(NAME_KEY, titleResult);	                	
                }	       
                values[i] = jsonLink.toString();  
            }	   
			node.setProperty(LINK_LIST_KEY, values);
			node.getSession().save();
        }catch (Exception e) {
        	ExceptionLogger.logExceptionMessage("HighLightsServlet UpdateHighLightsItemsToHighLights error :",e);	
        }        
    }
    
}
