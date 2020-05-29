package com.pwc.workflow.forms;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
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
import com.pwc.util.MXUtils;
import com.pwc.workflow.WorkFlowConstants;

/**
 * Workflow Process Step for adding visitor's emaildomain to whitelist after operations team validation Step in MX-Check workflow.
 *
 * @author jayati
 */
@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC workflow for adding to whitelisted Emails",
   "process.label=" + "PwC Whitelisting Emails"    
})
public class WhitelistEmail implements WorkflowProcess {

    public static final Logger log  = LoggerFactory.getLogger(WhitelistEmail.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";

    /**
     * Workflow Process Step defination for adding visitor's emaildomain to whitelist after
     * operations team validation Step in MX-Check workflow.
     *
     * @param item {@link WorkItem} workflow item
     * @param session {@link WorkflowSession} workflow session
     * @param args {@link MetaDataMap} workflow arguments
     */
    @Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                String visitorEmail = workflowData.getMetaDataMap().get(WorkFlowConstants.VISITOR_EMAIL).toString();
                if(StringUtils.isNotBlank(visitorEmail)){
                    MXUtils.addToWhiteList(visitorEmail,session);
                }
            }
        }catch (PersistenceException persistenceException){
            log.error(persistenceException.getMessage(),persistenceException);
            throw new WorkflowException(persistenceException.getMessage(), persistenceException);
        }catch(Exception exception){
            log.error(exception.getMessage(),exception);
            throw new WorkflowException(exception.getMessage(), exception);
        }
    }

}
