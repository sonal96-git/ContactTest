package com.pwc.workflow;

/**
 * @author vimenon
 *
 */
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
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

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCSubWorkflowChooserImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC sub workflow chooser",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow sub-workflow Chooser"    
})
public class PwCSubWorkflowChooserImpl implements WorkflowProcess{

    @Reference
    AdminResourceResolver adminResourceResolver;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {

		ResourceResolver adminResolver = null;
		Session session = null;
		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();
		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);
			
		    if (type.equals(WorkFlowConstants.TYPE_JCR_PATH  ) && data.getPayload() != null) {
		        String payloadData = (String) data.getPayload();
		        if (session.itemExists(payloadData)) {
		            path = payloadData;
		            Node node = session.getNode(path);
		            
		           if( WorkFlowConstants.DAM_ASSET.equals(node.getPrimaryNodeType().getName())){
		        	  
		        	   data.getMetaDataMap().put("rejectApprove", "rejected");
		           }else{
		        	   data.getMetaDataMap().put("rejectApprove", "approved");
		           }
		        }
		    }
		}catch(Exception e){
			logger.error("Sub Workflow Chooser Error",e);
			
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
