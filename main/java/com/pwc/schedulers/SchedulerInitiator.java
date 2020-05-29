package com.pwc.schedulers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.TopologyEventListener;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.workflow.WorkflowService;

@Component(immediate = true, service = { TopologyEventListener.class }, enabled = true, name = "com.pwc.schedulers.SchedulerInitiator",
property = {
		Constants.SERVICE_DESCRIPTION + "= This is a PwC Scheduler which check the spam submissions" })
@Designate(ocd = SchedulerInitiator.Config.class)
public class SchedulerInitiator implements TopologyEventListener {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private Scheduler scheduler;

	@Reference
	private SlingRepository repository;

	@Reference
	private WorkflowService workflowService;
	
	public String prop_period;
	public String prop_user;
	public String prop_operationUser;
	public Boolean prop_releaseEnabled;
	public String prop_location;
	public String prop_noOfSub;
	public String prop_spamPeriod;
	private Session session;
	public long period;
	public boolean isMaster = false;

	SpamCheckScheduler job;

	@ObjectClassDefinition(name = "PwC Form Spam Check Scheduler", 
			description = "This is a PwC Scheduler which check the spam submissions")
	@interface Config {
		@AttributeDefinition(name = "Scheduler Period", 
				description = "Provide period in seconds.To stop the scheduler enter 0.",
				type = AttributeType.STRING)
		public String scheduler_period() default "1800";
		
		@AttributeDefinition(name = "Spam User", 
				description = "Provide user/group id for handling spam check",
				type = AttributeType.STRING)
		public String scheduler_spamUser() default "pwcspam";
		
		@AttributeDefinition(name = "Operation User/Group", 
				description = "Provide user/group id handling PwC operations",
				type = AttributeType.STRING)
		public String onlineForm_operation_user() default "pwc-operation-team";
		
		@AttributeDefinition(name = "Enable Release From Spam Step", 
				description = "Enable Release From Spam Step to PwC operations operations team",
				type = AttributeType.BOOLEAN)
		public boolean onlineForm_release_enabled() default false;
		
		@AttributeDefinition(name = "Number of Submissions", 
				description = "Number of submissions which declares a spam",
				type = AttributeType.STRING)
		public String scheduler_submissionCount() default "100";
		
		@AttributeDefinition(name = "Spam Check Period", 
				description = "Provide period in seconds.",
				type = AttributeType.STRING)
		public String scheduler_spamCheckPeriod() default "1800";
		
		/*@AttributeDefinition(name = "Location", 
				description = "Path to store the query",
				type = AttributeType.STRING)
		public String propLocation() default "/content/pwc/global";*/
	}

	@Override
	public void handleTopologyEvent(final TopologyEvent event) {

		if ( event.getType() == TopologyEvent.Type.TOPOLOGY_CHANGED
				|| event.getType() == TopologyEvent.Type. TOPOLOGY_INIT) {
			this.isMaster = event.getNewView().getLocalInstance().isLeader();
			job=new SpamCheckScheduler(this.isMaster);
			log.info( "isLeader confirmed "+ this .isMaster );
		}
	}

	@Activate
	protected void activate(SchedulerInitiator.Config properties) throws Exception {

		log.info("Scheduler Activated");
		String jobName="PwCspamJob";
		try {
			boolean canRunConcurrently=false;
			Map<String, Serializable> config = new HashMap<String, Serializable>();			
			prop_period = properties.scheduler_period();
			prop_user = properties.scheduler_spamUser();
			prop_operationUser = properties.onlineForm_operation_user();
			prop_releaseEnabled = properties.onlineForm_release_enabled();
			prop_noOfSub = properties.scheduler_submissionCount();
			// prop_location= properties.propLocation();
			prop_spamPeriod = properties.scheduler_spamCheckPeriod();
			period = Long.parseLong(prop_period);
			// TBD - Deprecation needs to be removed
			session = repository.loginAdministrative(null);
			Node ofNode;
			if(session.nodeExists(SchedulerConstants.USER_PATH))
				ofNode=session.getNode(SchedulerConstants.USER_PATH);
			else
				ofNode = JcrUtil.createPath(SchedulerConstants.USER_PATH, "sling:Folder", session);
			ofNode.setProperty(SchedulerConstants.SPAM_USER, prop_user);
			ofNode.setProperty(SchedulerConstants.OPERATION_USER,prop_operationUser);
			session.save();

			job=new SpamCheckScheduler ( session, workflowService, prop_period,prop_user,prop_location,prop_noOfSub,prop_spamPeriod,prop_releaseEnabled);
			if(period ==0)
			{
				this.scheduler .unschedule(jobName);
				log.info( "Scheduler is stopped");
			}
			else
			{
				//this.scheduler.addPeriodicJob(jobName,job,config,period,canRunConcurrently);
				ScheduleOptions scheduleOptions= this.scheduler.NOW(-1, period);
				scheduleOptions.canRunConcurrently(canRunConcurrently);
				scheduleOptions.name(jobName);
				scheduleOptions.onLeaderOnly(true);
				scheduleOptions.config(config);
				this.scheduler.schedule(job, scheduleOptions);
			}
		}
		catch (Exception e) {
			if(period==0)
			{
				//this.scheduler.removeJob(jobName);
				this.scheduler .unschedule(jobName);
				log.info("Scheduler is stopped");
			}
			else{
				log.error("Exception while activating CIN Scheduler",e);
			}
		}
	}//activate method

	protected void deactivate(ComponentContext componentContext) {
		log.info("Scheduler Deactivated");
		if (session != null) {
			session.logout();
			session = null;
		}
	}
}//scheduler Class
