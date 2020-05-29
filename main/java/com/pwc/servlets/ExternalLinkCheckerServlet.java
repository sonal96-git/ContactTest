package com.pwc.servlets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;

import javax.jcr.RepositoryException;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONException;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.TidyJSONWriter;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.extensions=" + "json",
    "sling.servlet.selectors=" + "externallinkchecker"
})
public class ExternalLinkCheckerServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;
	private HttpClient httpClient;
	
	@Activate
	protected void activate(ComponentContext context)	throws RepositoryException	{
		@SuppressWarnings("rawtypes")
		Dictionary props = context.getProperties();	
		MultiThreadedHttpConnectionManager conMgr = new MultiThreadedHttpConnectionManager();
		this.httpClient = new HttpClient(conMgr);

		int connectionTimeout = PropertiesUtil.toInteger(props.get("connection.timeout"), 10000);
		if (connectionTimeout >= 5000) {
			this.httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
		}

	}

	@Override
	protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp)	throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");

		String check = req.getParameter("check");

		int status = 0;
		String comment = "";
		try {
                status = check(check);
                comment = HttpStatus.getStatusText(status);	
                if(status == 0)
                	comment = "Please check the url manually";
		}		
		catch (Exception ioe)
		{
			comment = "A general I/O error occured";
		}
		try {			
			new TidyJSONWriter(resp.getWriter()).object()
			.key("RESULT")
			.value(String.valueOf(status)).key("COMMENT").value(comment)
			.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int check(String url) 
    {
		int code = 0;          
		 try{       
		 		URL u = new URL(url); 	 
		 		URLConnection result = u.openConnection();
		 		HttpURLConnection huc = null;
		           
		           if(result instanceof HttpsURLConnection)
		           {        	   
		               huc = (HttpsURLConnection)u.openConnection();               
		               
		           }else if(result instanceof HttpURLConnection)
		           {        	           	   
		               huc = (HttpURLConnection)u.openConnection();               
		           }
		           
		           huc.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.13) Gecko/2009073021 Firefox/3.0.13 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.11");
		           huc.setRequestMethod("GET");  
		           huc.setConnectTimeout(3000);  
		           huc.connect();             
		           code = huc.getResponseCode();
   
		 	}catch(Exception ex)
		 	{
		 		ex.getStackTrace();
		 	}		 	
		 
		 	return code;
	}

}