package com.pwc.workflow;

/**
 * @author vimenon
 *
 */

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.day.cq.workflow.model.WorkflowModel;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCValidationForExpiryWorkflowImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of validation for PwC Expiry workflow for users",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Validation for Expiry Workflow  service"    
})
public class PwCValidationForExpiryWorkflowImpl implements WorkflowProcess {

	private static final Logger log = LoggerFactory
			.getLogger(PwCValidationForExpiryWorkflowImpl.class);

	private Session session;

	private String payLoadPath;
	
	@Reference
	ResourceResolverFactory rFact;
	
	@Override
	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap meta)
			throws WorkflowException {
		
		
			final WorkflowData data = workItem.getWorkflowData();
			String path = null;
			String type = data.getPayloadType();
			payLoadPath = (String)data.getPayload();
			
	
        try {
        	session = wfSession.getSession();
    		Node sourceUrlNode;
    		String srcPath = workItem.getWorkflowData().getPayload().toString();
    		sourceUrlNode = session.getNode(srcPath);
    		Node sourceUrlChildJcrNode = sourceUrlNode
    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
    		
    		Value[] childPages = sourceUrlChildJcrNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP).getValues();
    		
    		if(childPages!=null){
    			log.debug("Child Page Count " + childPages.length);
    			String childPath = ""; 
    			for(Value p:childPages){
    				childPath = p.getString();
    				if (childPath != null && !childPath.isEmpty() ) {
    					log.debug("Loop for Child Page " + childPath);
    					processChildPage(wfSession,data, sourceUrlChildJcrNode, childPath);
    					log.debug("End of Loop for Child Page " + childPath);
    				} else {
    					log.debug("Child Page is empty");
    				}
    			}
    			
    		}
    		
            
        } catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	private void processChildPage(WorkflowSession wfSession, final WorkflowData data, Node sourceUrlNode,
			String srcPath) throws ValueFormatException, RepositoryException,
			PathNotFoundException {
		
		log.debug("Processing child page  " + srcPath);
		ResourceResolver resResolve = getResourceResolver(session);
		PageManager pm = resResolve.adaptTo(PageManager.class);
		Page p = null;
		Node childUrlNode = null;
		try {
			childUrlNode = session.getNode(srcPath+"/"+WorkFlowConstants.CONTENT_ELEMENT);
		} catch (Exception e) {
			log.error("Excpetion child page not found  " + srcPath);
		}
		if (childUrlNode == null) {
			log.info("Child page not found  " + srcPath);
			return;
		}
		if(!srcPath.isEmpty()){
		 p = pm.getPage(srcPath);
		}else{
			log.info("Child page is empty  " + srcPath);
			return;
		}
		
		ReplicationStatus status = p.adaptTo(ReplicationStatus.class);
		log.info("replication status "+ status.isActivated());
		
		log.info("child page  "+ childUrlNode.getPath());
		if(status.isActivated()){
			 String stayInSync = childUrlNode.getProperty(WorkFlowConstants.SYNC_ELEMENT).getString();

			 log.info("stayInSync "+stayInSync);
				
			 
		     if (WorkFlowConstants.STAY_IN_SYNC_CHECK_VALUE.equals(stayInSync) || Boolean.TRUE.toString().equals(stayInSync)) {
		     
		    	 
		    	 log.info("in expiry workflow ");
		    	 data.getMetaDataMap().put(WorkFlowConstants.PAGE_EXPIRATION_CHECK,"true");
		    	 startExpiryWorkFLow(wfSession, srcPath);
		     
		     }
		}
	}

	private void startExpiryWorkFLow(WorkflowSession wfSession,String targetUrl) {
		
		String model = WorkFlowConstants.EXPIRY_WORKFLOW_MODEL;

		try {
			
			String payLoadPath = targetUrl;
			WorkflowModel wfModel = wfSession.getModel(model);
			WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH",
					payLoadPath);

			wfSession.startWorkflow(wfModel, wfData);

		} catch (WorkflowException ex) {

			log.error(" Launcher for multiple register url's failed", ex);
		} 

	}
	
	boolean checkValidation(String srcPath) throws ValueFormatException, PathNotFoundException, RepositoryException{
		
		ResourceResolver resResolve = getResourceResolver(session);
		PageManager pm = resResolve.adaptTo(PageManager.class);
		Page p = pm.getPage(srcPath);
		ReplicationStatus status = p.adaptTo(ReplicationStatus.class);
       
		
		if(status.isActivated()){
			 String stayInSync = session.getNode(srcPath).getProperty(WorkFlowConstants.SYNC_ELEMENT).getString();

             if (WorkFlowConstants.STAY_IN_SYNC_CHECK_VALUE.equals(stayInSync) || Boolean.TRUE.toString().equals(stayInSync)) {
             
            	return true;
             
             }
		}
		return false;
	}
	   private ResourceResolver getResourceResolver(Session session) {
	        try {
	            Map<String, Object> authInfo = new HashMap<String, Object>();
	            authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
	            return rFact.getResourceResolver(authInfo);
	        } catch (Exception e) {
	            log.error("Failed to get ResourceResolver.", e);
	        }
	        return null;
	    }

}
