package com.pwc.schedulers;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.ConfigManager;
import com.day.cq.search.QueryBuilder;
import com.pwc.util.RestClient;

/**
 * This class triggers PwCUserReportGenerateServlet on publish environment using replication agent configuration to
 * generate PWC MA user report.
 */
@Component(immediate = true, service = Runnable.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = PwCMAUserReportSchedularConfiguration.class)
public class PwCMAUserReportSchedular implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(PwCMAUserReportSchedular.class);
	
	@Reference
	private ConfigManager configManager;
	
	@Reference
	private QueryBuilder queryBuilder;
	
	@Reference
	private Scheduler scheduler;
	
	private String servletPath;
	private String repAgentPath;
	private int schedulerId;
	
	@Activate
	protected void activate(PwCMAUserReportSchedularConfiguration config) {
		LOGGER.info("PwCMAUserReportSchedular:: activate()");
		addScheduler(config);
	}
	
	@Deactivate
	protected void deactivate(PwCMAUserReportSchedularConfiguration config) {
		LOGGER.info("PwCMAUserReportSchedular:: deactivate()");
		removeScheduler();
	}
	
	@Modified
	protected void modified(PwCMAUserReportSchedularConfiguration config) {
		LOGGER.info("PwCMAUserReportSchedular:: modified()");
		removeScheduler();
		schedulerId = config.agent_path().hashCode();
		addScheduler(config);
	}
	
	private void removeScheduler() {
		scheduler.unschedule(String.valueOf(schedulerId));
	}
	
	/**
	 * This method adds the scheduler
	 *
	 * @param config
	 */
	private void addScheduler(PwCMAUserReportSchedularConfiguration config) {
		this.repAgentPath = config.agent_path();
		this.servletPath = config.servlet_path();
		ScheduleOptions scheduleOptions = scheduler.EXPR(config.scheduler_expression());
			scheduleOptions.canRunConcurrently(false);
			scheduler.schedule(this, scheduleOptions);
		LOGGER.info("PwCMAUserReportSchedular added");
	}
	
	public void run() {
		LOGGER.info("PwCMAUserReportSchedular is running");
		try {
			AgentConfig activeAgentConfig = this.configManager.getConfigurations().get(repAgentPath);
			if (activeAgentConfig == null) {
				LOGGER.error("PwCMAUserReportSchedular.run() :: Agent configuration is null");
			} else {
				String user = activeAgentConfig.getTransportUser();
				
				if ((user != null) && (user.length() > 0)) {
					String pass = activeAgentConfig.getTransportPassword();
					if (pass == null) {
						LOGGER.error("PwCMAUserReportSchedular.run() :: Agent password is null");
						pass = "";
					}
					LOGGER.debug("PwCMAUserReportSchedular.run() :: Auth User: {}", user);
					String publisherServerName = StringUtils.substringBefore(activeAgentConfig.getTransportURI(), "/bin");
					final RestClient restClient = new RestClient(publisherServerName + this.servletPath);
					restClient.setUserName(user);
					restClient.setPassword(pass);
					restClient.execute(RestClient.RequestMethod.POST);
				} else {
					LOGGER.error("PwCMAUserReportSchedular.run() :: user is null");
				}
			}
		} catch (IOException | AuthenticationException excep) {
			LOGGER.error("PwCMAUserReportSchedular" + excep.getMessage(), excep);
		}
	}
}
