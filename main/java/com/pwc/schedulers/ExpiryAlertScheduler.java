package com.pwc.schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataSource;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.commons.io.output.ByteArrayOutputStream;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.day.cq.workflow.WorkflowService;
import com.pwc.AdminResourceResolver;

/**
 * Created by rjiang on 2016-10-24.
 *
 */
/*@Component(
        name = "Expired Content Report Scheduler",
        label = "PwC Expired Content Report Scheduler",
        metatype = true,
        immediate = true)
@Properties({
        @Property(
                label = "Enabled",
                description = "Enable/Disable the Scheduled Service",
                name = "service.enabled",
                boolValue = true
        ),
        @Property(
                label = "Allow concurrent executions",
                description = "Allow concurrent executions of this Scheduled Service",
                name = "scheduler.concurrent",
                boolValue = false
        ),
        @Property(
                value = "0/25 0/1 * 1/1 * ? *",
                label = "cron expression",
                name = "scheduler.expression",
                description = "cron expression to start the content asset expiry check. For example 0/15 0/1 * 1/1/ * ? *"),
        @Property(
                label = "Compress Csv file",
                description = "Compress CSV File",
                name = "compress",
                boolValue = false
        ),
        @Property(
                label = "Process Pages",
                description = "Process Pages",
                name = "processPages",
                boolValue = true
        ),
        @Property(
                label = "Process Assets",
                description = "Process Assets",
                name = "processAssets",
                boolValue = false
        ),
        @Property(
                value = "/content/pwc",
                label = "Page Path",
                name = "path",
                description = "Page Path"),
        @Property(
                value = "/content/dam/pwc",
                label = "Asset Path",
                name = "assetpath",
                description = "Asset Path"),
        @Property(
                intValue = 2,
                label = "Year",
                name = "pastYear",
                description = "Query the contant that has been expired more than N years"),
        @Property(
                value = "some@mail1.com",
                label = "Recipient",
                name = "recipient",
                description = "Email Recipient")
})
@Service*/


@Component(immediate = true, service = { ExpiryAlertScheduler.class }, enabled = true, name = "Expired Content Report Scheduler",
property = {
		Constants.SERVICE_DESCRIPTION + "= " })
@Designate(ocd = ExpiryAlertScheduler.Config.class)
public class ExpiryAlertScheduler implements Runnable {

	@Reference
	private WorkflowService workflowService;

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	@Reference
	private MessageGatewayService messageGatewayService;

	@Reference
	private AdminResourceResolver resourceResolver;

	final String CQ_LASTREPLICATED = "cq:lastReplicated";
	final String CQ_LASTREPLICATEDBY = "cq:lastReplicatedBy";

	private Boolean zipContent;
	private String path;
	private String assetPath;
	private String recipient;
	private String expression;
	private Boolean concurrent;
	private Boolean processPages;
	private Boolean processAssets;
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private boolean serviceEnabled = false;
	ResourceResolver adminResourceResolver = null;
	Session session = null;
	StringBuffer line;
	int pastYear;

	@ObjectClassDefinition(name = "PwC Expired Content Report Scheduler", description = "")
	@interface Config {
		@AttributeDefinition(name = "Enabled", 
				description = "Enable/Disable the Scheduled Service",
				type = AttributeType.BOOLEAN)
		public boolean service_enabled() default true;
		
		@AttributeDefinition(name = "Allow concurrent executions", 
				description = "Allow concurrent executions of this Scheduled Service",
				type = AttributeType.BOOLEAN)
		public boolean scheduler_concurrent() default false;
		
		@AttributeDefinition(name = "cron expression", 
				description = "cron expression to start the content asset expiry check. For example 0/15 0/1 * 1/1/ * ? *",
				type = AttributeType.STRING)
		public String scheduler_expression() default "0/25 0/1 * 1/1 * ? *";
		
		@AttributeDefinition(name = "Compress Csv file", 
				description = "Compress Csv file",
				type = AttributeType.BOOLEAN)
		public boolean compress() default false;
		
		@AttributeDefinition(name = "Process Pages", 
				description = "Process Pages",
				type = AttributeType.BOOLEAN)
		public boolean processPages() default true;
		
		@AttributeDefinition(name = "Process Assets", 
				description = "Process Assets",
				type = AttributeType.BOOLEAN)
		public boolean processAssets() default false;
		
		@AttributeDefinition(name = "Page Path", 
				description = "Page Path",
				type = AttributeType.STRING)
		public String path() default "/content/pwc";
		
		@AttributeDefinition(name = "Asset Path", 
				description = "Asset Path",
				type = AttributeType.STRING)
		public String assetpath() default "/content/dam/pwc";
		
		@AttributeDefinition(name = "Past Year", 
				description = "Query the contant that has been expired more than N years",
				type = AttributeType.INTEGER)
		public int pastYear() default 2;
		
		@AttributeDefinition(name = "Recipient", 
				description = "Email Recipient",
				type = AttributeType.STRING)
		public String recipient() default "some@mail1.com";
	}

	@Override
	public void run() {
		if(serviceEnabled){
			try {

				adminResourceResolver = resourceResolver.getAdminResourceResolver();
				session = adminResourceResolver.adaptTo(Session.class);
				logger.info("-------Start Expiry Content--------");
				logger.info("Cron Expression := " + expression);
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR) - pastYear;
				int month = cal.get(Calendar.MONTH)+1;
				int date = cal.get(Calendar.DATE);
				String time = year + "-" + month + "-" + date + "T00:00:00.000Z";
				String header = "path,lastReplicated,lastReplicatedBy\n";
				line = new StringBuffer();
				line.append(header);
				if(processPages)
					findExpiredContent(path,time);
				if(processAssets)
					findExpiredContent(assetPath,time);
				byte[] data = line.toString().getBytes();
				if(!zipContent) {
					String contentType = "application/octet-stream";
					String fileName = "expiredPages.csv";
					sendEmail(data, contentType, fileName);
				}else{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ZipOutputStream zipOut = new ZipOutputStream(baos);
					ZipEntry zipEntry = new ZipEntry("Report-expired-content-" + year + "-" + month + "-" + date + ".csv");
					zipEntry.setSize(data.length);
					zipEntry.setTime(System.currentTimeMillis());
					zipOut.putNextEntry(zipEntry);
					zipOut.write(data);
					zipOut.closeEntry();
					zipOut.close();
					String contentType="application/zip";
					String fileName = "ExpiredContentReport-" + year + "-"+month + "-"+date +".zip";
					sendEmail(baos.toByteArray(), contentType, fileName);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(adminResourceResolver!=null)
					adminResourceResolver.close();
			}
		}
	}
	private void findExpiredContent(String aemPath, String time) throws Exception{
		String query_String = "SELECT p.* FROM [nt:base] AS p WHERE ISDESCENDANTNODE([" + aemPath + "]) AND p.[cq:lastReplicated]<CAST('" + time + "' AS DATE)";
		logger.info(query_String);
		Iterator<Resource> nodeIter = adminResourceResolver.findResources(query_String, Query.JCR_SQL2);
		while (nodeIter.hasNext()) {
			Node node = nodeIter.next().adaptTo(Node.class);
			String pagePath = node.getPath().replace("/jcr:content", "");
			if (isValidPagePath(pagePath)) {
				//logger.info(pagePath);
				line.append(pagePath);
				line.append(",");
				String lastReplicatedDate = "";
				String lastReplicatedBy = "";
				if(node.hasProperty(CQ_LASTREPLICATED))
					lastReplicatedDate = node.getProperty(CQ_LASTREPLICATED).getString();
				if(node.hasProperty(CQ_LASTREPLICATEDBY))
					lastReplicatedBy = node.getProperty(CQ_LASTREPLICATEDBY).getString();
				line.append(lastReplicatedDate);
				line.append(",");
				line.append(lastReplicatedBy);
				line.append("\n");
			}
		}
	}

	private boolean isValidPagePath(String path){
		Pattern pattern = Pattern.compile("/content/pwc/\\w{2}/\\w{2}/.*|/content/dam/pwc/\\w{2}/\\w{2}/.*");
		Matcher matcher = pattern.matcher(path);
		return matcher.find();

	}

	private void sendEmail(byte[] data, String contentType, String fileName) throws Exception {
		logger.info("---Sending Expired Content Report to "  + recipient + " ----");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1;
		int date = cal.get(Calendar.DATE);
		MessageGateway<Email> messageGateway;
		InputStream reportStream = new ByteArrayInputStream(data);
		DataSource datasource = new ByteArrayDataSource(reportStream, contentType);
		EmailAttachment attachment = new EmailAttachment();
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Report-" + year + "-" + month + "-" + date);
		attachment.setName("Report-" + year + "-" + month + "-" + date);

		messageGateway = messageGatewayService.getGateway(Email.class);
		MultiPartEmail email = new MultiPartEmail();
		email.addTo(recipient);
		email.setSubject("Expired Content Report-" + year + "-" + month + "-" + date);
		email.setMsg("Attachment");
		email.attach(datasource,fileName,"Report Description");
		messageGateway.send(email);
	}

	protected void activate(Config properties) {
		// final Dictionary<?, ?> properties = ctx.getProperties();
		serviceEnabled = properties.service_enabled();
		concurrent = properties.scheduler_concurrent();
		expression=properties.scheduler_expression();
		zipContent = properties.compress();
		processPages = properties.processPages();
		processAssets = properties.processAssets();
		path = properties.path();
		assetPath = properties.assetpath();
		recipient = properties.recipient();
		pastYear = properties.pastYear();
		logger.info("Compress File := " + zipContent.toString());
		logger.info(this.getClass().toString() + " expression := " + expression );
		if(serviceEnabled)
			logger.info(this.getClass().toString() + " service is enabled");
		else
			logger.info(this.getClass().toString() + " service is disabled");
	}
}
