package com.pwc.workflow;

/**
 * @author vimenon
 *
 */

import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.day.cq.workflow.model.WorkflowModel;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCNotificationWorkflowImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC notification workflow for users",
    Constants.SERVICE_VENDOR + "= Adobe",
   "process.label=" + "PwC Workflow Notification  service"    
})
public class PwCNotificationWorkflowImpl implements WorkflowProcess {

	private static final Logger log = LoggerFactory
			.getLogger(PwCNotificationWorkflowImpl.class);

	private Session session;

	private Object payLoadPath;

	private String model;

	private UserManager um;

	private TreeSet<String> userList;

	@Override
	public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap meta)
			throws WorkflowException {
		
		
			final WorkflowData data = workItem.getWorkflowData();
			payLoadPath = data.getPayload();
			
		if(userList == null){
			userList = new TreeSet<String>();
		}
		model = "/conf/global/settings/workflow/models/pwc-notification-workflow/jcr:content/model";

        try {
        	session = wfSession.getSession();
    		Node sourceUrlNode;
    		String srcPath = workItem.getWorkflowData().getPayload().toString();
    		sourceUrlNode = session.getNode(srcPath);
    		Node sourceUrlChildJcrNode = sourceUrlNode
    				.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			if (sourceUrlChildJcrNode != null) {
				sourceUrlChildJcrNode.setProperty("cq:lastReplicatedBy", workItem.getWorkflow().getInitiator());
				session.save();
				populateUsersList(sourceUrlChildJcrNode, wfSession, workItem);
			}
			WorkflowModel wfModel = wfSession.getModel(model);
            WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", payLoadPath);
            
            for(String user:userList){
            	
            	wfData.getMetaDataMap().put(WorkFlowConstants.PARTICIPANT, user);
            	wfSession.startWorkflow(wfModel, wfData);
            }
            
        } catch (WorkflowException ex) {
           
           log.error(" Unable to send Notification to Inbox",ex);
          
		} catch (PathNotFoundException pnfExcep) {
			log.error("PathNotFoundException in PwCNotificationWorkflowImpl.execute() method :: {}", pnfExcep);
		} catch (RepositoryException repoExcep) {
			log.error("RepositoryException in PwCNotificationWorkflowImpl.execute() method :: {}", repoExcep);
		}
	}

	private int populateUsersList(Node sourceUrlChildJcrNode,
			WorkflowSession workflowSession, WorkItem item)
			throws RepositoryException, ValueFormatException,
			PathNotFoundException, WorkflowException {
		int numberOfUsers = 0;
		if (sourceUrlChildJcrNode
				.hasProperty(WorkFlowConstants.USERNAME_REGISTRY)) {
			for (Value v : sourceUrlChildJcrNode.getProperty(
					WorkFlowConstants.USERNAME_REGISTRY).getValues()) {
				um = AccessControlUtil.getUserManager(session);

				Authorizable auth = um.getAuthorizable(v.getString());
				numberOfUsers++;

				String id = auth.getID();

				userList.add(id);

			}

		}
		
		return numberOfUsers;
	}

}
