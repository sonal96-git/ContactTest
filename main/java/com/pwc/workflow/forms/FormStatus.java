package com.pwc.workflow.forms;

import java.util.List;

import javax.jcr.Session;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.pwc.workflow.WorkFlowConstants;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for setting the form status",
   "process.label=" + "PwC Form Status"    
})
public class FormStatus implements WorkflowProcess {
	
    private Session jcrSession;
    public static final Logger log  = LoggerFactory.getLogger(FormStatus.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";

    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        log.info("ENTERED FORM STATUS");
        ResourceResolver resourceResolver = null;
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                String path = workflowData.getPayload().toString();
                if(workflowData.getMetaDataMap().get("listOfNodes")!=null )
                {
                    //log.error("called from  pwc resend mail and archive workflow");
                    List<String> listOfNodes= (List<String>)workflowData.getMetaDataMap().get("listOfNodes");
                    path=listOfNodes.get(0);
                }
                String status = args.get("PROCESS_ARGS", String.class);
                if(status!=null){
                    resourceResolver = session.adaptTo(ResourceResolver.class);
                    Resource resource = resourceResolver.getResource(path);
                    ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
                    map.put(WorkFlowConstants.FORM_STATUS, status);
                    resourceResolver.commit();
                    log.info("EXISTING FROM  FORM STATUS AFTER SETTING STATUS ="+status );
                }
            }
        }catch(Exception exception){
            log.error(exception.getMessage(),exception);
            throw new WorkflowException(exception.getMessage(), exception);
        }finally {
            if(resourceResolver!=null)
                resourceResolver.close();
        }
    }
}
