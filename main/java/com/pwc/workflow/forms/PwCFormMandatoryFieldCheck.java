package com.pwc.workflow.forms;

import java.util.Collections;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
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
import com.pwc.wcm.utils.FormConstants;
import com.pwc.workflow.WorkFlowConstants;

/**
 * Workflow Process Step to perform check on mandatory fields of "pwc/components/content/onlineForm/startLongform" component authored in the page,
 * during the page publish workflow.
 */
@Component(service = WorkflowProcess.class, immediate = true)
public class PwCFormMandatoryFieldCheck implements WorkflowProcess {
    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    public static final Logger LOGGER = LoggerFactory.getLogger(PwCFormMandatoryFieldCheck.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    private Session session;

    @Override
    public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {

                String path = workflowData.getPayload().toString();
                session = workflowSession.adaptTo(Session.class);
                int invalidForms = 0;

                ResourceResolver resourceResolver = getResourceResolver(session);

                String query = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" + path + "/" + WorkFlowConstants.CONTENT_ELEMENT
                        + "]) and [jcr:path] LIKE '%/" + "startlongform" + "%'";
                Iterator<Resource> ccomponentIt = resourceResolver.findResources(query, "JCR-SQL2");
                if (ccomponentIt != null && ccomponentIt.hasNext()) {
                    Node formStartNode = session.getNode(ccomponentIt.next().getPath());
                    if (!(hasProperty(formStartNode, FormConstants.FORM_NAME)
                            && hasProperty(formStartNode, FormConstants.TO) && hasProperty(formStartNode, FormConstants.SUBJECT) && hasProperty(
                            formStartNode, FormConstants.FORM_ID))) {
                        invalidForms++;
                    }
                }
                    workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, Boolean.toString(invalidForms == 0));
                    LOGGER.info("PwCFormMandatoryFieldCheck.execute: Form Mandatory Field Check finished");
            }

        } catch (Exception exception) {
            LOGGER.error("PwCFormMandatoryFieldCheck.execute: Exception occurred while checking the mandatory form fields {}", exception);
            throw new WorkflowException(exception.getMessage(), exception);
        }
    }
    private boolean hasProperty(Node node, String propertyName){
        boolean valid = false;
        try {
            valid = node.hasProperty(propertyName);
            if(!valid){
                LOGGER.info("PwCFormMandatoryFieldCheck.hasProperty: Form Mandatory Field Checked failed for " + node.getPath() + "  " + propertyName + " is missing");
            }
        } catch (RepositoryException repositoryException) {
            LOGGER.error("PwCFormMandatoryFieldCheck.hasProperty: Repository exception occurred while checking node property {}",repositoryException);
        }
        return valid;
    }
    private ResourceResolver getResourceResolver(Session session) throws org.apache.sling.api.resource.LoginException {
        return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                                                                                                    session));
    }
}
