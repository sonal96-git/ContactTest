package com.pwc.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(	name = "PwC MA User Report Scheduler Configuration",
						description = "This Scheduler will generate report for PwC MA Users")
public @interface PwCMAUserReportSchedularConfiguration {
	
	@AttributeDefinition(	name = "Cron expression defining when this Scheduled Service will run",
							description = "[every minute = 0 * * * * ?], [02:00am daily = 0 0 2 1/1 * ? *]", type = AttributeType.STRING)
	public String scheduler_expression() default "0 0 2 1/1 * ? *";
	
	@AttributeDefinition(	name = "Replication Agent Path",
							description = "Path of Replication agent to get Publish environment server name & crendentials",
							type = AttributeType.STRING)
	public String agent_path() default "/etc/replication/agents.author/publish";
	
	@AttributeDefinition(	name = "PwC MA External User Report Generator Servlet Path",
							description = "Path of Servlet which creates report of external user on publish environment",
							type = AttributeType.STRING)
	public String servlet_path() default "/bin/pwc/mauserreportgenerator";
}
