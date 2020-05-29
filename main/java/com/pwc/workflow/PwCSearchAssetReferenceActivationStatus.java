package com.pwc.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCSearchAssetReferenceActivationStatus",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of reference search for Assets which have to be activated",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Search Service for Assets which have to be activated"    
})
public class PwCSearchAssetReferenceActivationStatus implements WorkflowProcess {

	private static final Logger log = LoggerFactory
			.getLogger(PwCSearchAssetReferenceActivationStatus.class);

	private Session session;

	private String payLoadPath;
	
	@Reference
	ResourceResolverFactory rFact;
	
	@Reference
	Replicator replicator;
	
    @Reference(service = ReferenceProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private final List<ReferenceProvider> referenceProviders = new CopyOnWriteArrayList<ReferenceProvider>();

	protected void bindReferenceProviders(ReferenceProvider referenceProvider) {

        referenceProviders.add(referenceProvider);
    }

    protected void unbindReferenceProviders(ReferenceProvider referenceProvider) {

        referenceProviders.remove(referenceProvider);
    }

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
    		
    		String[] paths = {srcPath};
    		
    		//searchForReferenceActivationStatus(paths, session,null);
    		PwCWorkFlowUtil.searchForReferenceActivationStatus(paths, session, null, getResourceResolver(session), referenceProviders, replicator);
            
        } catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
