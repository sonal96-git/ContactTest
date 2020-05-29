package com.pwc.model.components.userreport;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.cm.ConfigurationAdmin;

import com.adobe.cq.sightly.WCMUsePojo;
import com.pwc.util.ExceptionLogger;

public class ExternalUserController extends WCMUsePojo {

	private String host;
	private final String CONTENT_FOLDER = "/content/reports/pwc/external-users-report";
	private final String JCR_CONTENT_CSV ="external-users-report.csv/jcr:content";	
	private final String DELETED_JCR_CONTENT_CSV ="external-deleted-users-report.csv/jcr:content";
	private final String JCR_CONTENT_CSV_365 = "pwc-365-users-report.csv/jcr:content";

	@Override
	public void activate() throws Exception {

		ConfigurationAdmin configAdmin = getSlingScriptHelper().getService(org.osgi.service.cm.ConfigurationAdmin.class);
		host = configAdmin.getConfiguration("PwC Default Domain").getProperties().get("domain").toString();

	}

	public String getHost(){return host;}

	public String getModifiedDate(){
		return getFormattedDate(CONTENT_FOLDER,JCR_CONTENT_CSV);
	}
	
	public String getDeletedModifiedDate(){
		return getFormattedDate(CONTENT_FOLDER,DELETED_JCR_CONTENT_CSV);
	}

	public String getModifiedDate365() {
		return getFormattedDate(CONTENT_FOLDER, JCR_CONTENT_CSV_365);
	}

	private String getFormattedDate(String contentFolder,String contentCsv){
			
		ResourceResolver res = getResourceResolver();
    	Session session = res.adaptTo(Session.class);
    	GregorianCalendar modifiedDate = null;
    	String formatted = "";

    	try {

			Node node = session.getNode(contentFolder);
			if(node.hasNode(contentCsv)) {
				
				Node csvContent = node.getNode(contentCsv);
				modifiedDate = (GregorianCalendar) csvContent.getProperty("jcr:lastModified").getDate();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ssZ");
				formatted = sdf.format(modifiedDate.getTime());
			}

		} catch (Exception e) {
			ExceptionLogger.logExceptionMessage("ExternalUser:getDateModified. Error while getting csv lastModified", e);
		}

		return formatted;
    }

}
