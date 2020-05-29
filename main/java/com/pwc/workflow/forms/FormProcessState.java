package com.pwc.workflow.forms;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
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
import com.pwc.wcm.utils.FormConstants;

/**
 * Workflow process Step to set value for "formtoprocess" value in form-data.
 */
@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for setting the formtoprocess status",
   "process.label=" + "PwC Form Process Status"    
})
public class FormProcessState implements WorkflowProcess {

    public static final Logger log  = LoggerFactory.getLogger(FormProcessState.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";

    /**
     * Workflow process Step to set value for "formtoprocess" value in form-data using value provided in arguments.
     *
     * @param item {@link WorkItem} workflow item
     * @param session {@Link WorkflowSession} workflow session
     * @param args {@link MetaDataMap} workflow arguments
     */
    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        ResourceResolver resourceResolver = null;
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                String path = workflowData.getPayload().toString();
                String formState = args.get("PROCESS_ARGS", String.class);
                if(formState!=null){
                    resourceResolver = session.adaptTo(ResourceResolver.class);
                    Resource resource = resourceResolver.getResource(path);
                    ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
                    map.put(FormConstants.FORM_TO_PROCESS, formState);
                    resourceResolver.commit();
                }
                log.info("EXISTING FROM  FORM STATUS AFTER SETTING STATUS ="+formState );
            }

        }catch(PersistenceException persistenceException){
            log.error(persistenceException.getMessage(),persistenceException);
            throw new WorkflowException(persistenceException.getMessage(), persistenceException);
        }catch(Exception exception){
            log.error(exception.getMessage(),exception);
            throw new WorkflowException(exception.getMessage(), exception);
        }
        finally {
            if(resourceResolver!=null)
                resourceResolver.close();
        }
    }
}
