package com.pwc.workflow.forms;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for releasing the form from the spam",
   "process.label=" + "PwC Release Form"    
})
public class ReleaseSpamForm implements WorkflowProcess {
	  
	@Reference
    private ResourceResolverFactory resolverFactory;
    
	@Reference
	AdminResourceResolver adminResourceResolver;
    
    public static final Logger log  = LoggerFactory.getLogger(ReleaseSpamForm.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
 
    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    	
    	try {
    			WorkflowData workflowData = item.getWorkflowData();
    			if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
    				if(workflowData!=null && workflowData.getPayload()!=null){
    					String path = workflowData.getPayload().toString();
    					ResourceResolver resourceResolver=adminResourceResolver.getAdminResourceResolver();
    					Resource res = resourceResolver.getResource(path);
    					Node node = res.adaptTo(Node.class);
    					
    					node.getParent().getParent().setProperty("spam",(String)null);
    					
    					node.getSession().save();
    					
    				}
            }
    	}catch(Exception e){
            	log.error(e.getMessage(),e);
            	
        }
            
    }
}
