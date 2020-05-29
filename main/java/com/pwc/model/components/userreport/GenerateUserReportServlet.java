package com.pwc.model.components.userreport;


import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "csv-report-generate"
})
public class GenerateUserReportServlet extends SlingAllMethodsServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5644830787380338805L;
	
	@Reference
    private ConfigurationAdmin confAdmin;
    
    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException {
    	
		try {
			
			Configuration config = confAdmin.getConfiguration("PwC Pub Host");
			String pubIP = config.getProperties().get("domainIp").toString();
			
			HttpClient httpclient =  new HttpClient();
			HttpMethod  method = new GetMethod(pubIP+"/content/pwc.user-report.json");
			
			httpclient.executeMethod(method);
			
		} catch (Exception  e) {
			ExceptionLogger.logExceptionMessage("GenerateUserReportServlet:doget. Error while generating the report ", e);
		}
		
    }
}