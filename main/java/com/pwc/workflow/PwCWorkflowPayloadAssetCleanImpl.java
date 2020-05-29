package com.pwc.workflow;

import javax.jcr.Node;
import javax.jcr.Session;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC payload asset cleaner",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow Payload Asset Cleaner"    
})
public class PwCWorkflowPayloadAssetCleanImpl implements WorkflowProcess{

	
	private String TYPE_JCR_PATH  =  "JCR_PATH";
	private String DAM_ASSET  =  "dam:Asset";

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {
		
		final Session session = workflowSession.getSession(); 
		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();
		try {
		    if (type.equals(TYPE_JCR_PATH  ) && data.getPayload() != null) {
		        String payloadData = (String) data.getPayload();
		        if (session.itemExists(payloadData)) {
		            path = payloadData;
		            Node node = session.getNode(path);
		            if( DAM_ASSET.equals(node.getPrimaryNodeType().getName())){
		        	session.removeItem(path);
		        	session.save();
		        	   
		        	   
		           }
		        }
		    }
		}catch(Exception e){
			
		}
		
	}

}
