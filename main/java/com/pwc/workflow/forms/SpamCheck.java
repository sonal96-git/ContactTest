package com.pwc.workflow.forms;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
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
import com.pwc.workflow.WorkFlowConstants;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for checking the spam",
   "process.label=" + "PwC Spam Check"    
})
public class SpamCheck implements WorkflowProcess {
	   
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    AdminResourceResolver adminResourceResolver;
    
    public static final Logger log  = LoggerFactory.getLogger(SpamCheck.class);
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
    					ValueMap props = ResourceUtil. getValueMap(res);
                        workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, props.get("spam","false"));
    				}
            }
    	}catch(Exception exception){
            	log.error(exception.getMessage(),exception);
        }
    }
}
