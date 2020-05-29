package com.pwc.workflow;

import javax.jcr.RepositoryException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCAuthorDispatcherCacheClearWorkflow",
property = {
		Constants.SERVICE_DESCRIPTION + "= Implementation of PwC Author Dispatcher Cache Clear",
		Constants.SERVICE_VENDOR + "= PwC"
})
@Designate(ocd = PwCAuthorDispatcherCacheClearWorkflow.Config.class)
public class PwCAuthorDispatcherCacheClearWorkflow implements WorkflowProcess {
	
	private static final String TYPE_JCR_PATH = "JCR_PATH";
	private String dispatcherUrl;
	private Boolean enableDispatcher;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String DEFAULT_DISPATCHER_URL = "http://localhost";
	private static final String DEFAULT_AKAMAI_DOMAIN = "http://localhost:4502";
	private static final String PROCESS_LABEL = "PwC Author Purge Cache";

	@ObjectClassDefinition(name = "PwC Author Dispatcher", description = "PwC Author Dispatcher Cache Clear")
	@interface Config {
		@AttributeDefinition(name = "Process Label",
				description = "PwC Author Dispatcher",
				type = AttributeType.STRING)
		String process_label() default PROCESS_LABEL;

		@AttributeDefinition(name = "Enable Author Dispatcher Cache Purge", 
				description = "Enable Author Dispatcher Cache Purge",
				type = AttributeType.BOOLEAN)
		public boolean dispatcher_enable() default true;
		
		@AttributeDefinition(name = "Dispatcher Host URL", 
				description = "Dispatcher Host URL",
				type = AttributeType.STRING)
		public String dispatcher_url() default DEFAULT_DISPATCHER_URL;
		
		@AttributeDefinition(name = "Author Instance Domain for Akamai Purging", 
				description = "Updation requires bundle refresh for the new values to take effect",
				type = AttributeType.STRING)
		public String akamai_author_domain() default DEFAULT_AKAMAI_DOMAIN;
	}

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
		if (enableDispatcher) {
			try {
				WorkflowData workflowData = workItem.getWorkflowData();
				if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
					String path = workflowData.getPayload().toString().replace("/jcr:content/renditions/original", "");
					logger.info("Flush path := " + path);
					String uri = "/dispatcherUrl/invalidate.cache";
					HttpClient client = new HttpClient();
					PostMethod post = new PostMethod(dispatcherUrl + uri);
					post.setRequestHeader("CQ-Action", "Activate");
					post.setRequestHeader("CQ-Handle", path);
					post.setRequestHeader("Content-length", "0");
					post.setRequestHeader("Content-Type", "application/octet-stream");
					client.executeMethod(post);
					post.releaseConnection();
					logger.info("result: " + post.getResponseBodyAsString());

				}
			} catch (Exception ex) {
				logger.error("com.pwc.workflow.PwCAuthorDispatcherCacheClearWorkflow", ex);
			}
		}
	}

	@Activate
	protected void activate(PwCAuthorDispatcherCacheClearWorkflow.Config properties) throws RepositoryException {
		dispatcherUrl = properties.dispatcher_url();
		enableDispatcher = properties.dispatcher_enable();
	}
}
