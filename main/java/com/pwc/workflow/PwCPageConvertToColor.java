package com.pwc.workflow;

import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Converts to color enabled page",
		Constants.SERVICE_VENDOR + "= PwC",
		"process.label=" + "PwC Workflow Page Convert To Colors"    
})
public class PwCPageConvertToColor implements WorkflowProcess{
	Logger logger = LoggerFactory.getLogger(PwCPageConvertToColor.class);

	@Reference
	ResourceResolverFactory resourceResolverFactory;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
		logger.info("Starting Page Convert To Color");
		final WorkflowData workflowData = workItem.getWorkflowData();
		final String path = workflowData.getPayload().toString();

		ResourceResolver resourceResolver = null; 

		try {
			resourceResolver = getResourceResolver(workflowSession.getSession());

			final Resource resource = resourceResolver.getResource(path + "/jcr:content");
			if (resource.getParent().adaptTo(Page.class) == null) {
				logger.info("Resource is not a page, exiting Page Convert To Color");
				return;	
			}

			final Node contentNode = resource.adaptTo(Node.class);

			PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
			PwCWorkFlowUtil.incrementVersion(path, pageManager, "Incrementing version before converting to color page");

			final Session jcrSession = contentNode.getSession();
			contentNode.setProperty("colorsEnabled","true");
			contentNode.setProperty("standardSpacing","true");
			jcrSession.save();

		} catch (Exception e) {
			throw new WorkflowException("Unable to complete processing the PwCPageConvertToColor Process step", e);
		} 

		logger.info("Ending Page Convert to Color");
	}

	private ResourceResolver getResourceResolver(Session session) throws LoginException {
		return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
				session));
	}

}
