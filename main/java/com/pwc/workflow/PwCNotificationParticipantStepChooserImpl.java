package com.pwc.workflow;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.ParticipantStepChooser;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.metadata.MetaDataMap;

@Component(service = ParticipantStepChooser.class, immediate = true, name = "com.pwc.workflow.PwCNotificationParticipantStepChooserImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic participant chooser depending notification registry.",    
    "process.label=" + "PwC Workflow Notification Participant Chooser"      
})
@Designate(ocd = PwCNotificationParticipantStepChooserImpl.Config.class)
public class PwCNotificationParticipantStepChooserImpl implements
		ParticipantStepChooser {
	
	private Group notificationGroupForAuthors;
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	 @ObjectClassDefinition(name = "PwC Workflow Notification Participant Chooser", description = "Implementation of PwC dynamic participant chooser depending notification registry.")
		@interface Config {
			@AttributeDefinition(name = "Process Label",
					description = "Implementation of PwC dynamic participant chooser depending notification registry",
					type = AttributeType.STRING)
			String process_label() default "PwC Workflow Notification Participant Chooser";
		}

	@Override
	public String getParticipant(WorkItem item, WorkflowSession session,
			MetaDataMap map) throws WorkflowException {

		try {
			// get the notfiction group id created previously for dynamic
			// approvals-this includes authors and approvers
			final WorkflowData data = item.getWorkflowData();

			String src = (String) data.getPayload();

			Node srcContentNode = session.getSession().getNode(
					src +"/"+ WorkFlowConstants.CONTENT_ELEMENT);
			String id ="";
			javax.jcr.Value[] s =srcContentNode
					.getProperty(WorkFlowConstants.PARTICIPANT_AUTHORS).getValues();
			
			id = s[0].getString();
			
			

			return id;

		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedRepositoryOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return "";

	}


	

}