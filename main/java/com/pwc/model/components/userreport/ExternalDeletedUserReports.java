package com.pwc.model.components.userreport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.AdminResourceResolver;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "user-deleted-report"
})
public class ExternalDeletedUserReports extends SlingAllMethodsServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Resource userNode;
    
    @Reference
    private AdminResourceResolver resourceResolver;
    private ResourceResolver adminResourceResolver;
    
    private StringBuffer csv = new StringBuffer();
          
    private final String PROFILE_NODE = "profile";
    private final String DELETE_REQUEST_NODE = "delete-requests";
    private final String USER_NODE_PRIM_TYPE = "rep:User"; 
    private final String EXTERNAL_DELETED_USER_PATH = "/home/users/pwc-external-deleted-users"; 
    private final String CSV_DELETED_HEADERS = "Email,Is Proflle Deleted,Profile Delete Date, Deletion Comment,Deletion Option,Deletion Option Title,Phone,Deletion Time \n";  
    private final String CSV_DELETED_FILE = "external-deleted-users-report.csv";   
    private final String CONTENT_DELETED_FOLDER = "/content/reports/pwc/external-users-report";
    

    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException {

    	response.setContentType("text/html");
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	response.setHeader("Access-Control-Allow-Credentials", "true");
    	
    	adminResourceResolver = resourceResolver.getAdminResourceResolver();
    	
    	csv.setLength(0);
    	csv.append(CSV_DELETED_HEADERS);
    	
    	try {
    		
    		//Setting info to "csv"
			setCSVInfo(EXTERNAL_DELETED_USER_PATH);
			
	    	InputStream is = new ByteArrayInputStream(csv.toString().getBytes("UTF-8"));
	    	
	    	Session session = adminResourceResolver.adaptTo(Session.class);
        
			Node node = session.getNode(CONTENT_DELETED_FOLDER);

			UserRegistrationUtil.createCSVFileReport(node, session, is, CSV_DELETED_FILE);
			
	        session.save();            
	        session.logout();
	        
		} catch (RepositoryException | ParseException | UnsupportedEncodingException e) {
			ExceptionLogger.logExceptionMessage("UserReportCSVServlet:doGet. Error while getting csv report", e);
		} 
    }

	private void setCSVInfo(String path) throws ParseException {
    	
    	DeletedUserProfile deletedUserProfile;    
    	DeletedUserRequest deletedUserRequest;    
    	String primType;   	
    	
    	ValueMap properties;
    	
    	userNode = adminResourceResolver.getResource(path);
    	Iterable<Resource> itChildren = userNode.getChildren();
    	
    	for (Resource resource : itChildren) {
    		
    		properties = resource.getValueMap();
        	
        	primType = properties.containsKey("jcr:primaryType") ? 
        			properties.get("jcr:primaryType").toString():"";

        	if(primType.equals(USER_NODE_PRIM_TYPE)) {
        		
        		Resource profileNode = resource.getChild(PROFILE_NODE);
        		Resource requestNode = resource.getChild(DELETE_REQUEST_NODE);
        		
        		if(profileNode != null) {        			        			            		
        			deletedUserProfile = profileNode.adaptTo(DeletedUserProfile.class);
        			if(requestNode != null){
        				Iterable<Resource> requestChildren = requestNode.getChildren();
        		    	
        		    	for (Resource resourceRequest : requestChildren) {
        		    		deletedUserRequest = resourceRequest.adaptTo(DeletedUserRequest.class);
        		    		csv.append(deletedUserProfile.toString()+deletedUserRequest.toString());
        		    	}
        			}
        		}
        		
        	} else {
        		setCSVInfo(resource.getPath());
        	}
    	}
	}         
}




  
