package com.pwc.workflow;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.security.AccessControlManager;

import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC temporary workflow group cleaner",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow Temporary group  Cleaner"    
})
public class PwCTemporaryGroupsCleanImpl implements WorkflowProcess{

	@Reference
	AdminResourceResolver adminResourceResolver;
	
	private String TYPE_JCR_PATH  =  "JCR_PATH";
	private String DAM_ASSET  =  "dam:Asset";
	private AccessControlManager am;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	 @Deactivate
    protected void deactivate(final Map<String, Object> properties) {

    }

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {
		
		ResourceResolver adminResolver = null;
		Session session = null;
 
	    WorkflowData data = workItem.getWorkflowData();
		String path = (String)data.getPayload();
		String type = data.getPayloadType();
	
		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);

			synchronized(session){
				if(path.contains("/content/dam/pwc")){
					data.getMetaDataMap().put(WorkFlowConstants.HAS_VERSION_BEEN_RESTORED,true);
				}
			Node srcContentNode = session.getNode(
					path +"/"+ WorkFlowConstants.CONTENT_ELEMENT);
			synchronized(srcContentNode){
			
		    if (type.equals(TYPE_JCR_PATH  ) && data.getPayload() != null) {
		        String payloadData = (String) data.getPayload();
		        if (session.itemExists(payloadData)) {
		            path = payloadData;
		            Node node = session.getNode(path);
		            am = AccessControlUtil.getAccessControlManager(session);
					UserManager um = AccessControlUtil.getUserManager(session);

//					javax.jcr.Value[] s =srcContentNode
//							.getProperty(WorkFlowConstants.PARTICIPANT_AUTHORS_AND_APPROVERS).getValues();
					
					/*String tempGrp = srcContentNode
							.getProperty(WorkFlowConstants.PARTICIPANT_AUTHORS_AND_APPROVERS).getString();
					
//					 s =srcContentNode
//							.getProperty(WorkFlowConstants.PARTICIPANT_APPROVERS).getValues();
					
					 String tempGrpApprovers = srcContentNode
								.getProperty(WorkFlowConstants.PARTICIPANT_APPROVERS).getString();
					 
//					 s =srcContentNode
//								.getProperty(WorkFlowConstants.PARTICIPANT_AUTHORS).getValues();
						
						 String tempGrpAuthors = srcContentNode
									.getProperty(WorkFlowConstants.PARTICIPANT_AUTHORS).getString();
						
				    if(tempGrp!=null)
					um.getAuthorizable(tempGrp).remove();
					
				    if(tempGrpApprovers!=null)
						um.getAuthorizable(tempGrpApprovers).remove();
						
				    if(tempGrpAuthors!=null)
						um.getAuthorizable(tempGrpAuthors).remove();
					
					
					srcContentNode.setProperty(
							WorkFlowConstants.PARTICIPANT_AUTHORS_AND_APPROVERS, (Value[])null);

					srcContentNode.setProperty(
							WorkFlowConstants.PARTICIPANT_APPROVERS, (Value[])null);
					
					
					srcContentNode.setProperty(
							WorkFlowConstants.PARTICIPANT_AUTHORS, (Value[])null);*/
					
					srcContentNode.setProperty(WorkFlowConstants.ASSETS_TO_ACTIVATE, (Value[])null);
					
					session.save();
		        }
		        }
		    }
		    }
		}catch(Exception e){
			logger.error("Error clearing notification group : "+(String)data.getMetaDataMap().get(WorkFlowConstants.NOTIFICATION_GROUP));
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
