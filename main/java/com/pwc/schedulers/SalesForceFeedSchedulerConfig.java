package com.pwc.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Sales Force Feed Scheduler Configuration")
@interface SalesForceFeedSchedulerConfig {

    @AttributeDefinition(
            name = "Sales Force Feed Scheduler",
            description = "Generates the customised report of users with preferred territory 'US'",
            type = AttributeType.STRING) String scheduler_name() default "Sales Force Feed Scheduler";

    @AttributeDefinition(
            name = "Enabled",
            description = "True, if scheduler service is enabled",
            type = AttributeType.BOOLEAN) boolean enabled() default false;

    @AttributeDefinition(name = "cron expression",
            description = "cron expression to start the content asset expiry check. For example 0/15 0/1 * 1/1/ * ? *",
            type = AttributeType.STRING)
    String scheduler_expression() default "";

    @AttributeDefinition(name = "Replication Agent Path",
            description = "Path of Replication agent to get Publish environment server name & crendentials",
            type = AttributeType.STRING) String agent_path() default "/etc/replication/agents.author/publish";

    @AttributeDefinition(name = "PwC MA External User Report Generator Servlet Path",
            description = "Path of Servlet which creates report of external user on publish environment",
            type = AttributeType.STRING) String servlet_path() default "/bin/dpe/generatesfmcreport";

}