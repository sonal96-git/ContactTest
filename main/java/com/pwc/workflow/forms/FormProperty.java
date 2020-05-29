package com.pwc.workflow.forms;

import java.util.List;

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
    Constants.SERVICE_DESCRIPTION + "= PwC Script for deciding form type",
   "process.label=" + "PwC Form Property Provider"    
})
public class FormProperty implements WorkflowProcess {
 
    /*@Property(value = "PwC Script for deciding form type")
    static final String DESCRIPTION = Constants.SERVICE_DESCRIPTION;
    @Property(value = "PwC Form Property Provider")
    static final String LABEL="process.label";*/
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    @Reference
    AdminResourceResolver adminResourceResolver;
   
	public static final Logger log  = LoggerFactory.getLogger(FormProperty.class);
  
	private static final String TYPE_JCR_PATH = "JCR_PATH";
 
    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
    	try {
    		
    		log.info("ENTERED FORM PROPERTY");
    		
    		WorkflowData workflowData = item.getWorkflowData();
    		
    		if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            String path = workflowData.getPayload().toString();
            
            if(workflowData.getMetaDataMap().get("listOfNodes")!=null )
            {
            	//log.error("called from  pwc resend mail and archive workflow");
            	List<String> listOfNodes= (List<String>)workflowData.getMetaDataMap().get("listOfNodes");
        		if(listOfNodes.size()>0)
        		path=listOfNodes.get(0);
        		 
            }
            //
            String nodeProp = args.get("PROCESS_ARGS", String.class);
            
            if(nodeProp!=null){
            ResourceResolver resourceResolver= adminResourceResolver.getAdminResourceResolver();
        	Resource res = resourceResolver.getResource(path);
        	ValueMap props = ResourceUtil.getValueMap(res);
        	String propVal=props.get(nodeProp,"");
        	
        	
        	workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, props.get(nodeProp,propVal));
            resourceResolver.close();
            }
           
            
            }
    		log.info("EXITING FORM PROPERTY");
    		
    	}catch(Exception e){
            	log.error(e.getMessage(),e);
            	throw new WorkflowException(e.getMessage(), e);
        }
     }
}



