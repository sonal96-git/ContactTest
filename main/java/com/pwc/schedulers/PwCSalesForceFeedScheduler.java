package com.pwc.schedulers;


import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.ConfigManager;
import com.pwc.util.RestClient;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * This class generates the customised report of users with preferred territory 'US'.
 */
@Component(immediate = true, service = PwCSalesForceFeedScheduler.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "= Generates the customised report of users with preferred territory 'US'"})
@Designate(ocd = SalesForceFeedSchedulerConfig.class)
public class PwCSalesForceFeedScheduler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PwCSalesForceFeedScheduler.class);

    private SalesForceFeedSchedulerConfig salesForceFeedSchedulerConfig;
    private int schedulerId;
    private String repAgentPath;
    private String servletPath;


    @Reference
    private ConfigManager configManager;

    @Reference
    private Scheduler scheduler;

    @Activate
    protected void activate(SalesForceFeedSchedulerConfig salesForceFeedSchedulerConfig) {
        LOGGER.info("SalesForceFeedScheduler:: activate()");
        schedulerId = salesForceFeedSchedulerConfig.scheduler_name().hashCode();
        removeScheduler();
        addScheduler(salesForceFeedSchedulerConfig);
    }

    @Deactivate
    protected void deactivate(SalesForceFeedSchedulerConfig salesForceFeedSchedulerConfig) {
        LOGGER.info("SalesForceFeedScheduler:: deactivate()");
        removeScheduler();
    }

    @Modified
    protected void modified(SalesForceFeedSchedulerConfig salesForceFeedSchedulerConfig) {
        LOGGER.info("SalesForceFeedScheduler:: modified()");
        schedulerId = salesForceFeedSchedulerConfig.scheduler_name().hashCode();
        removeScheduler();
        addScheduler(salesForceFeedSchedulerConfig);
    }

    private void removeScheduler() {
        scheduler.unschedule(String.valueOf(schedulerId));
    }

    /**
     * This method adds the scheduler
     *
     * @param salesForceFeedSchedulerConfig
     */
    private void addScheduler(SalesForceFeedSchedulerConfig salesForceFeedSchedulerConfig) {
        if(salesForceFeedSchedulerConfig.enabled()) {
            this.repAgentPath = salesForceFeedSchedulerConfig.agent_path();
            this.servletPath = salesForceFeedSchedulerConfig.servlet_path();
            ScheduleOptions scheduleOptions = scheduler.EXPR(salesForceFeedSchedulerConfig.scheduler_expression());
            scheduleOptions.canRunConcurrently(false);
            scheduleOptions.name(String.valueOf(schedulerId));
            scheduler.schedule(this, scheduleOptions);
            LOGGER.info("SalesForceFeedScheduler.addScheduler() :: Scheduled Sales force report generation.");
        }else{
            LOGGER.info("SalesForceFeedScheduler.addScheduler() :: SalesForceFeedScheduler is not enabled.");
        }
    }


    @Override
    public void run() {
        LOGGER.info("SalesForceFeedScheduler.run() :: SalesForceFeedScheduler has started.");
        try {
            AgentConfig activeAgentConfig = this.configManager.getConfigurations().get(repAgentPath);
            if (activeAgentConfig == null) {
                LOGGER.debug("SalesForceFeedScheduler.run() :: Agent configuration is null");
            } else {
                String user = activeAgentConfig.getTransportUser();
                if ((user != null) && (user.length() > 0)) {
                    String pass = activeAgentConfig.getTransportPassword();
                    if (pass == null) {
                        LOGGER.error("SalesForceFeedScheduler.run() :: Agent password is null");
                        pass = "";
                    }
                    LOGGER.debug("SalesForceFeedScheduler.run() :: Auth User: {}", user);
                    String publisherServerName = StringUtils.substringBefore(activeAgentConfig.getTransportURI(), "/bin");
                    final RestClient restClient = new RestClient(publisherServerName + this.servletPath);
                    restClient.setUserName(user);
                    restClient.setPassword(pass);
                    restClient.execute(RestClient.RequestMethod.POST);
                } else {
                    LOGGER.error("SalesForceFeedScheduler.run() :: user is null");
                }
            }
        } catch (IOException | AuthenticationException excep) {
            LOGGER.error("PwCMAUserReportSchedular" + excep.getMessage(), excep);
        }
    }
}