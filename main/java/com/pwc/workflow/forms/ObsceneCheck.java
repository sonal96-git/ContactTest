package com.pwc.workflow.forms;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
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

/**
 * Workflow Process Step defination to perform Obscene Check from Banned word list
 * in Reference Data during PwC Form Submission workflow.
 */
@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for obscene check",
   "process.label=" + "PwC Form Obscene Check"    
})
public class ObsceneCheck implements WorkflowProcess {
	
    public static final Logger log  = LoggerFactory.getLogger(ObsceneCheck.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";

    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        ResourceResolver resourceResolver = null;
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                String path = workflowData.getPayload().toString();
                resourceResolver = session.adaptTo(ResourceResolver.class);
                Boolean booleanParam= false;
                Resource res = resourceResolver.getResource(WorkFlowConstants.BANNED_WORDS_PATH);
                ValueMap props = ResourceUtil.getValueMap(res);
                String[] bannedWords = props.get("bannedWords",String[].class);
                Resource formData = resourceResolver.getResource(path);
                props = ResourceUtil.getValueMap(formData);
                String formdata = props.toString();
                for (String bannedWord: bannedWords) {
                    if(formdata.matches("(?i).*\\b"+bannedWord.trim()+"\\b.*")){
                        booleanParam = true;
                        break;
                    }
                }
                workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, booleanParam.toString());
            }
        }catch(Exception e){
            log.error(e.getMessage(),e);
            throw new WorkflowException(e.getMessage(), e);
        }
        finally {
            if(resourceResolver!=null){
                resourceResolver.close();
            }
        }
    }
}
