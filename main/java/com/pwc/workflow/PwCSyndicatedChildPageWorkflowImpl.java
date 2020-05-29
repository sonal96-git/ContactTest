package com.pwc.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
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

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCSyndicatedChildPageWorkflowImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of Link breaker for PwC  Syndicated Child pages",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Link breaker workflow service for Syndicated Child pages"    
})
public class PwCSyndicatedChildPageWorkflowImpl implements WorkflowProcess {

	private static final Logger log = LoggerFactory
			.getLogger(PwCSyndicatedChildPageWorkflowImpl.class);

	private Session session;

	private Object payLoadPath;
	
	@Reference
	ResourceResolverFactory rFact;

	@Override
	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap meta)
			throws WorkflowException {
		
		
			final WorkflowData data = workItem.getWorkflowData();
			String path = null;
			String type = data.getPayloadType();
			payLoadPath = data.getPayload();
			
	
        try {
        	session = wfSession.getSession();
    		Node sourceUrlNode;
    		String srcPath = workItem.getWorkflowData().getPayload().toString();
    		sourceUrlNode = session.getNode(srcPath);
    		Node sourceUrlChildJcrNode = sourceUrlNode
    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
    		
    		
    		boolean isSyndicated = sourceUrlChildJcrNode.hasProperty(WorkFlowConstants.SRC_ELEMENT);
    		if(isSyndicated){
    			
    			String srcUrl = sourceUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT).getString();
        		
	    		if( !srcUrl.isEmpty() && !srcUrl.trim().equals("")){
	    			
	    			
	    			severLinkToParent(sourceUrlChildJcrNode);
	    		}
    		}
            
        } catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
	}

	   private void severLinkToParent(Node sourceUrlChildJcrNode) throws AccessDeniedException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, RepositoryException {
		
		   String src = null;
		    synchronized(session){
				   if(sourceUrlChildJcrNode.hasProperty(WorkFlowConstants.SRC_ELEMENT)){
					   
					  src =  sourceUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT).getString();
				   
					   sourceUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT).remove();
					   sourceUrlChildJcrNode.getProperty(WorkFlowConstants.SYNC_ELEMENT).remove();
				   }
			   }
		    
		    Node srcNode = session.getNode(src+"/"+WorkFlowConstants.CONTENT_ELEMENT);
		    Value[] target = srcNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP).getValues();
			   
		    String pwcAllDestination="";
			   for(Value child : target){
				   if(child.getString().isEmpty()){
					   continue;
				   }
				   if(!(child.getString()+"/"+WorkFlowConstants.CONTENT_ELEMENT).equals(sourceUrlChildJcrNode.getPath())){
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
