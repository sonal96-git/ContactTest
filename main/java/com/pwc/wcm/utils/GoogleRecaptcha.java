package com.pwc.wcm.utils;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(immediate = true, service = { GoogleRecaptcha.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC Google Recaptcha Configuration" })
@Designate(ocd = GoogleRecaptcha.Config.class)
public class GoogleRecaptcha {

	public static final String APPEND_TEXT_DEFAULT = "";
		
	private static String collectionLimit;
	private static String appendText;
	
	@ObjectClassDefinition(name = "PwC Google Recaptcha Configuration", description = "PwC Google Recaptcha Configuration")
    @interface Config {
        @AttributeDefinition(name = "Private Key", 
                            description = "Secret key",
                            type = AttributeType.STRING)
        public String collectionLimit() default StringUtils.EMPTY;
        
        @AttributeDefinition(name = "Public Key", 
                description = "Public Key",
                type = AttributeType.STRING)
        public String appendText() default StringUtils.EMPTY;
    }
	
	@Activate
	protected void activate(final GoogleRecaptcha.Config config) {
		appendText = config.collectionLimit();
		collectionLimit =  config.appendText();
	}
	
	public String getPrivateKey() {
		return appendText;
	}
	
	public String getPublicKey() {
		return collectionLimit;
	}

}
