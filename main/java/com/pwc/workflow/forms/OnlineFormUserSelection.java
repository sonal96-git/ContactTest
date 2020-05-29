package com.pwc.workflow.forms;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.pwc.schedulers.SchedulerConstants;

@Component(service = ParticipantStepChooser.class, immediate = true,
property = {    
    ParticipantStepChooser.SERVICE_PROPERTY_LABEL + "= PwC Form Participant Chooser"    
})
public class OnlineFormUserSelection implements ParticipantStepChooser {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public String getParticipant(WorkItem item, WorkflowSession session,MetaDataMap args){
		
		String user = "admin";
		try{
			String userType = args.get("PROCESS_ARGS", String.class);
			//Session jcrSession = session.adaptTo(Session.class);
			//Node userNode=jcrSession.getNode(SchedulerConstants.USER_PATH);
			if(userType!=null && userType.equals(SchedulerConstants.STRING_SPAM))
			{
			  user="pwcspam";//userNode.getProperty(SchedulerConstants.SPAM_USER).getValue().toString();
			}
			else
			{
			  user="pwc-operations-team";//userNode.getProperty(SchedulerConstants.OPERATION_USER).getValue().toString();	
			}
		    
		
		}
		catch(Exception e)
		{
		  logger.error("Error at partcipant chooser step",e);	
		}
		
		logger.info("User assigned to"+ user);
		return user;
	}

}