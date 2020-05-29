package com.pwc.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageManager;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC version restore for rejection of publish process.",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow version restore for rejection in publish process."    
})
public class PwCWorkflowVersionRestoreForRejection implements WorkflowProcess{
	 
	 @Reference
     ResourceResolverFactory rFact;

	 @Reference
	 AdminResourceResolver adminResourceResolver;

	 @Reference
	 protected SlingRepository repository;
	
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

			Node sourceUrlNode = null;
    		String srcPath = workItem.getWorkflowData().getPayload().toString();
    		
    		synchronized(session){
    		
    		//Session adminSession = repository.loginAdministrative(null);
    		 
    		//marker for reject path
    		data.getMetaDataMap().put(WorkFlowConstants.HAS_VERSION_BEEN_RESTORED,true);
     		
    		//adminSession = session;
    		
    		sourceUrlNode = session.getNode(srcPath+"/"+WorkFlowConstants.CONTENT_ELEMENT);
    		
    		String labelToRestore = null;
    		
    		if(sourceUrlNode.getProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION_ID)!=null)
    			labelToRestore = sourceUrlNode.getProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION_ID).getString();
    		
    		
    		PageManager pageManager = getResourceResolver(session).adaptTo(PageManager.class);
        	
			boolean autoSynd = ((Value)data.getMetaDataMap().get(WorkFlowConstants.SYNDICATION_DONE_MANUALLY)).getString().trim().equals("false");
    		if(!labelToRestore.equals("null")&& autoSynd){
    			PwCWorkFlowUtil.restoreVersion(srcPath, labelToRestore, pageManager, "Restoring page version while rejecting the publish workflow for "+srcPath,session);
    		}
    		
    		session.save();
    		
    		}
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.logout();
			}
			if (adminResolver != null) { 
				adminResolver.close(); 
			}
		}
		
	}

    private ResourceResolver getResourceResolver(Session session) {
        try {
            Map<String, Object> authInfo = new HashMap<String, Object>();
            authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
            return rFact.getResourceResolver(authInfo);
        } catch (Exception e) {
            logger.error("Failed to get ResourceResolver.", e);
        }
        return null;
    }

}
