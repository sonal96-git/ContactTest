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
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCSyndicationLauncherForRegisteredUrls",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC syndication flag updater",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow launcher for Syndication of Multiple registerd Urls on source page"    
})
public class PwCSyndicationLauncherForRegisteredUrls implements WorkflowProcess {

	private Session session = null;
	private ResourceResolver adminResolver = null;
	private String SRC_URL = "";
	private String[] contentPars;
	@Reference
	private AdminResourceResolver adminResourceResolver;
	//read srcUrl And syndicationflag
	private static final Logger log = LoggerFactory.getLogger(PwCSyndicationLauncherForRegisteredUrls.class);

	@Reference
	ResourceResolverFactory rFact;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {
		
		log.info(" Starting execute");
		
		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();
		Value[] registeredUrls=null;
		String srcPath= (String)data.getPayload(),templateName = null,syndicationFlag= WorkFlowConstants.SYNDICATION_OPEN;
		Node sourceUrlNode;
		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);

			sourceUrlNode = session.getNode(srcPath);
			Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			if(sourceUrlChildJcrNode.hasProperty(WorkFlowConstants.URL_REGISTRY_PROP)){
				registeredUrls = sourceUrlChildJcrNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP).getValues();
	
				startSyndicationWorkFLows(workflowSession,srcPath,registeredUrls);
			}
			
			session.save();
		} catch (PathNotFoundException ex) {
			
			 log.error(" Launcher for multiple register url's failed",ex);
		} catch (RepositoryException ex) {
			
			 log.error(" Launcher for multiple register url's failed",ex);
		} finally {
			if (session != null) {
				session.logout();
			}
			if (adminResolver != null) { 
				adminResolver.close(); 
			} 
		}

	}


	private void startSyndicationWorkFLows(WorkflowSession wfSession, String srcPath ,Value[] registeredUrls) throws ValueFormatException, IllegalStateException, RepositoryException {
		
		log.debug(" Starting startSyndicationWorkFLows");
		if (registeredUrls == null)
			return;
		log.debug("Target URLs count is " + registeredUrls.length);
		
		for(Value targetUrl : registeredUrls){
		
			if(targetUrl.getString().trim().isEmpty()){
				log.debug(" Target URL is blank");
				continue;
			}
		
			String model = WorkFlowConstants.SYNDICATION_WORKFLOW_MODEL;

			try {
				Node sourceUrlNode;
				String payLoadPath = targetUrl.getString();
				log.info("Checking page " + payLoadPath);
				boolean proceed = checkValidation(payLoadPath);
				if (proceed) {
					sourceUrlNode = session.getNode(srcPath);
		    			Node sourceUrlChildJcrNode = sourceUrlNode
		    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
		    		
		    		Node targetUrlNode = session.getNode(payLoadPath);
		    		Node targetUrlChildJcrNode = targetUrlNode
		    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
		    		
		    		
		    		
		            if(targetUrlChildJcrNode.hasProperty(WorkFlowConstants.SYNC_ELEMENT)) {
		            	 javax.jcr.Property stayInSync = targetUrlChildJcrNode.getProperty(WorkFlowConstants.SYNC_ELEMENT);
		            	 if(targetUrlChildJcrNode.hasProperty(WorkFlowConstants.SRC_ELEMENT)){
		            		
		            		 javax.jcr.Property srcUrl = targetUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT);
		            		 log.info(" ############# checking if the src url exists in target page ######");
		            		 if(srcPath.equals(srcUrl.getString())){
		            			 log.info(" ############# confirmed that src is still linked to target #######");
				            		 if (WorkFlowConstants.STAY_IN_SYNC_CHECK_VALUE.equals(stayInSync.getString()) || Boolean.TRUE.toString().equals(stayInSync.getString())) {
					                    WorkflowModel wfModel = wfSession.getModel(model);
					                    WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", payLoadPath);
					                    //wfData.getMetaDataMap().put(WorkFlowConstants.SRC_ELEMENT, srcPath);
					
					                    //put flag for non mmanual syndication
					                    wfData.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY, "false");
					
					                    wfSession.startWorkflow(wfModel, wfData);
					                }
		            		 }else{
		            			 log.info(" ############# confirmed that src is link id broken target #######");
		            			 
		            			 unregisterTargetFromSrc(sourceUrlNode,targetUrlNode);
					            	
		            		 }
			            }
		    		}
				}
            
			} catch (WorkflowException ex) {
				log.error(" Launcher for multiple register url's failed",ex);
			} catch (PathNotFoundException ex) {
				// TODO Auto-generated catch block
				log.error(" Launcher for multiple register url's failed",ex);
			} catch (RepositoryException ex) {
				// TODO Auto-generated catch block
				log.error(" Launcher for multiple register url's failed",ex);
			}
			
		}
		
	}

	
	private void unregisterTargetFromSrc(Node sourceUrlNode, Node targetUrlNode) throws PathNotFoundException, RepositoryException {
		

		  String src = sourceUrlNode.getPath();
		    synchronized(session){
		    	Node targetUrlChildJcrNode = targetUrlNode
	    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
	    		
				
			   
		    
		    Node srcNode = session.getNode(src+"/"+WorkFlowConstants.CONTENT_ELEMENT);
		    Value[] target = srcNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP).getValues();
			   
		    String pwcAllDestination="";
			   for(Value child : target){
				   if(child.getString().isEmpty()){
					   continue;
				   }
				   if(!(child.getString()+"/"+WorkFlowConstants.CONTENT_ELEMENT).equals(targetUrlChildJcrNode.getPath())){
					   pwcAllDestination = pwcAllDestination+","+child.getString();
				   }
			   }
			  javax.jcr.Property pwcAllDestinationUrlProp = srcNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP);
			  if(pwcAllDestinationUrlProp!=null){
			  if(!pwcAllDestination.trim().isEmpty()){
				  pwcAllDestinationUrlProp.setValue(pwcAllDestination.split(","));
			   }else{
				   pwcAllDestinationUrlProp.remove();
			   }
			  }
		    }
				
	}






	boolean checkValidation(String srcPath) throws ValueFormatException, PathNotFoundException, RepositoryException{
		
		log.debug("checkValidation for " + srcPath);
		//ResourceResolver resResolve = getResourceResolver(session);
		PageManager pm = adminResolver.adaptTo(PageManager.class);
		Page p = null;
		try {
			p = pm.getPage(srcPath);
		} catch (Exception e) {
			log.error("Excepetion for " + srcPath);
			return false;
		}
		
		if (p == null) {
			log.error("Page is null for " + srcPath);
			return false;
		}
		
		ReplicationStatus status = p.adaptTo(ReplicationStatus.class);
       
		
		if(status.isActivated()){
			log.debug("getting the node for " + srcPath+"/"+WorkFlowConstants.CONTENT_ELEMENT);
			if (session == null)
				log.debug("session is null ");
			else
				log.debug("session is NOT null ");
			Node stayInSync = session.getNode(srcPath+"/"+WorkFlowConstants.CONTENT_ELEMENT);

			if(stayInSync!=null && stayInSync.hasProperty(WorkFlowConstants.SYNC_ELEMENT))
            if (WorkFlowConstants.STAY_IN_SYNC_CHECK_VALUE.equals(stayInSync.getProperty(WorkFlowConstants.SYNC_ELEMENT).getString()) || Boolean.TRUE.toString().equals(stayInSync.getProperty(WorkFlowConstants.SYNC_ELEMENT).getString())) {
            	log.info("checkValidation for " + srcPath + " is true");
            	return true;
             
            }
		}
		log.info("checkValidation for " + srcPath + " is false");
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