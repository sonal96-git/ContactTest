package com.pwc.workflow;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * This Process step adds two properties (showNavigation and hide_level) on expired pages to hide from Collection-V2 and Navigation Component's rendition.
 */

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCHideDeactivatedPagesWorkflowImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation to hide deactivated pages from navigation and collection components",
    Constants.SERVICE_VENDOR + "= PwC",
   "process.label=" + "PwC Hide Deactivated Pages service"    
})
public class PwCHideDeactivatedPagesWorkflowImpl implements WorkflowProcess {

    private static final Logger log = LoggerFactory
            .getLogger(PwCHideDeactivatedPagesWorkflowImpl.class);

    private Session session;

    private String model;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        try {
            session = workflowSession.getSession();
            Node sourceUrlNode;
            String srcPath = workItem.getWorkflowData().getPayload().toString();
            sourceUrlNode = session.getNode(srcPath);
            Node sourceUrlChildJcrNode = sourceUrlNode
                    .getNode(WorkFlowConstants.CONTENT_ELEMENT);
            if (sourceUrlChildJcrNode != null) {
                log.debug(" Adding 2 properties in page path {} ",sourceUrlNode);
                sourceUrlChildJcrNode.setProperty("showNavigation", "no");
                sourceUrlChildJcrNode.setProperty("hide_level",String.valueOf(3));
                session.save();
            }

        } catch (PathNotFoundException pnfExcep) {
            log.error("PathNotFoundException in PwCNotificationWorkflowImpl.execute() method :: {}", pnfExcep);
        } catch (RepositoryException repoExcep) {
            log.error("RepositoryException in PwCNotificationWorkflowImpl.execute() method :: {}", repoExcep);
        }
    }
}
