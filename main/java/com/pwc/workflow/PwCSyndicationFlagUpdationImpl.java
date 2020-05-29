package com.pwc.workflow;

/**
 * @author vimenon
 *
 */
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCSyndicationFlagUpdationImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC syndication flag updater",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow Syndication Flag Updation service"    
})
public class PwCSyndicationFlagUpdationImpl implements WorkflowProcess{

	@Reference
	ResourceResolverFactory rFact;
	@Reference
	private AdminResourceResolver adminResourceResolver;
	
	private Session session;
	
	private String SRC_URL = "";
	private String[] contentPars;
	//read srcUrl And syndicationflag
	private static final Logger log = LoggerFactory.getLogger(PwCSyndicationFlagUpdationImpl.class);

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {
		
		ResourceResolver adminResolver = null;
		Session session = null;
		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();
		String srcPath=null ,targetPath= (String)data.getPayload(),templateName = null,syndicationFlag= WorkFlowConstants.SYNDICATION_OPEN;
		Node sourceUrlNode;
		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);

			sourceUrlNode = session.getNode(targetPath);
			Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			srcPath = sourceUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT).getString();
			javax.jcr.Property syndicationFlagProperty = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT).getProperty(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT);
			String flag = (String)data.getMetaDataMap().get(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT);
			
			if(WorkFlowConstants.SYNDICATION_OPEN.equals(flag)){
				syndicationFlagProperty.setValue(WorkFlowConstants.SYNDICATION_OPEN);
				sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT).setProperty(WorkFlowConstants.SYNC_ELEMENT,"true");
				
			}else{
				syndicationFlagProperty.setValue(WorkFlowConstants.SYNDICATION_RESTRICTED);
				sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT).setProperty(WorkFlowConstants.SYNC_ELEMENT,"true");
				
			}
			session.save();
		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow flag updation service : ", e);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow flag updation service : ", e);
		} finally {

			if (session != null) {
				session.logout();
			}
			if (adminResolver != null) { 
				adminResolver.close(); 
			}
		}
		
	}
   
}
