package com.pwc.workflow;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolverFactory;
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

@Component(service = WorkflowProcess.class, immediate = true)
public class PwCPageMandatoryFieldCheck implements WorkflowProcess {
    
	@Reference
    private ResourceResolverFactory resolverFactory;
    public static final Logger log = LoggerFactory.getLogger(PwCPageMandatoryFieldCheck.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    private Session session;

    @Override
    public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        try {
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {

                String path = workflowData.getPayload().toString();
                session = workflowSession.adaptTo(Session.class);
                Node currentNode = session.getNode(path);
                Node sourceUrlChildJcrNode = currentNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
                boolean valid = false;

                valid = hasProperty(sourceUrlChildJcrNode, "jcr:title"); //Browser Title
                valid = hasProperty(sourceUrlChildJcrNode, "pageTitle"); //Page title
                valid = hasProperty(sourceUrlChildJcrNode, "pwc_rvp_title"); //Collection Title
                valid = hasProperty(sourceUrlChildJcrNode, "jcr:description"); //Abstract
                valid = hasProperty(sourceUrlChildJcrNode, "jcr:language"); //Locale
                valid = hasProperty(sourceUrlChildJcrNode, "meta_robots"); //Meta Robots
                valid = hasProperty(sourceUrlChildJcrNode, "hide_level"); //Meta Robots
                valid = hasProperty(sourceUrlChildJcrNode, "pwcReleaseDate"); //Meta Robots
                if (valid) {
                    workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, "false");
                } else {
                    workflowData.getMetaDataMap().put(WorkFlowConstants.STATUS_RETURN, "true");
                }
                log.info("Page Mandatory Field Check finished");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        } finally {

        }
    }
    private boolean hasProperty(Node node, String propertyName){
        boolean valid = false;
        try {
            valid = node.hasProperty(propertyName);
            if(!valid){
                log.info("PwC Page Mandatory Field Checked failed for " + node.getPath() + "  " + propertyName + " is missing");
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return valid;
    }
}
