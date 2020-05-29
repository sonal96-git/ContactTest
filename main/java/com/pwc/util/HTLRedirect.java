package com.pwc.util;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletResponse;
import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;

public class HTLRedirect extends WCMUsePojo{	
	
    private final String LOCATION = "redirectTarget";
	
	@Override 
	public void activate() throws Exception {
						
		String location = getCurrentPage().getProperties().get(LOCATION,"").toString();
		Page currentPage = getCurrentPage();		
		SlingHttpServletResponse response = getResponse();
		
	   		    
	    if (!location.isEmpty()) {
	        
	        if (currentPage != null && !location.equals(currentPage.getPath())) {
	            
	        	String redirectPath = "";
	            final int protocolIndex = location.indexOf(":/");
	            final int queryIndex = location.indexOf("/content/");		            
	            
	            if ( protocolIndex > -1 && (queryIndex == -1 || queryIndex > protocolIndex) ) {
	                redirectPath = location;
	            } else {
	                redirectPath = location + ".html";
	            }
	            response.sendRedirect(redirectPath);
	        } else {
	        	response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        }
	        return;
	    }	   
		
	}

}
