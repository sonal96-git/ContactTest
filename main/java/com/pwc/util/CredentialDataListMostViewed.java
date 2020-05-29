package com.pwc.util;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true,  service = { CredentialDataListMostViewed.class }, enabled = true,
name = "com.pwc.wcm.utils.AnalyticsClient",
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC List Most Viewed Component Configuration" })
@Designate(ocd = CredentialDataListMostViewed.Config.class )
public class CredentialDataListMostViewed {

	public static final String USER_NAME_DEFAULT = "";
	public static final String PASSWORD_DEFAULT = "";
	public static final String ENDPOINT_DEFAULT = "";
	public static final String TIMEOUT_DEFAULT = "60";
	public static final String CACHETIME_DEFAULT = "1";
	
	private static String username;
	
	private static String password;

	private static String endpoint;

	private static String timeout;

	private static String cachetime;

	public static final Logger log = LoggerFactory.getLogger(CredentialDataListMostViewed.class);
	
	@ObjectClassDefinition(name = "PwC List Most Viewed Component Configuration ", description = "PwC List Most Viewed Component Configuration")
    @interface Config {
        @AttributeDefinition(name = "Adobe Analytics User Name", 
                            description = "Adobe Analytics User Name",
                            type = AttributeType.STRING)
        public String username() default USER_NAME_DEFAULT;
        
        @AttributeDefinition(name = "Adobe Analytics Password", 
                description = "Adobe Analytics Password",
                type = AttributeType.STRING)
        public String password() default PASSWORD_DEFAULT;
        
        @AttributeDefinition(name = "Adobe Analytics ENDPOINT", 
                description = "Adobe Analytics ENDPOINT",
                type = AttributeType.STRING)
        public String endpoint() default ENDPOINT_DEFAULT;
        
        @AttributeDefinition(name = "Adobe Analytics API timeout in seconds", 
                description = "Adobe Analytics API timeout in seconds",
                type = AttributeType.STRING)
        public String timeout() default TIMEOUT_DEFAULT;
        
        @AttributeDefinition(name = "Adobe Analytics API Report cache time in hours", 
                description = "Adobe Analytics API Report cache time in hours",
                type = AttributeType.STRING)
        public String cachetime() default CACHETIME_DEFAULT;
    }
	
	@Activate
	protected void activate(final CredentialDataListMostViewed.Config config) {

		username = config.username();
		password = config.password();
		endpoint = config.endpoint();
		timeout = config.timeout();
		cachetime = config.cachetime();
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

	public String getEndpoint() {
		return endpoint;
	}
	
	public String getTimeout() {
		return timeout;
	}

	public String getCachetime() {
		return cachetime;
	}
}