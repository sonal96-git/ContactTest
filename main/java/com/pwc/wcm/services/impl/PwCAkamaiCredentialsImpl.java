package com.pwc.wcm.services.impl;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akamai.edgegrid.signer.ClientCredential;
import com.pwc.wcm.services.PwCAkamaiCredentials;

/**
 * Service to provide Akamai Configuration like ClientCredential & Akamai URI.
 */
@Component(immediate = true, service = { PwCAkamaiCredentials.class }, enabled = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Akamai Crendetials Configuration Service" })
@Designate(ocd = PwCAkamaiCredentialsImpl.Config.class)
public class PwCAkamaiCredentialsImpl implements PwCAkamaiCredentials {

	private Logger logger = LoggerFactory.getLogger(PwCAkamaiCredentials.class);

	private String akamaiAccessToken = StringUtils.EMPTY;
	private String akamaiClientToken = StringUtils.EMPTY;
	private String akamaiClientSecret = StringUtils.EMPTY;
	private String akamaiHost = StringUtils.EMPTY;
	private String akamaiUri = StringUtils.EMPTY;
	private ClientCredential clientCredential = null;

	@ObjectClassDefinition(name = "PwC Akamai Crendetials Configuration Service", description = "")
	@interface Config {
		@AttributeDefinition(name = "PwC Akamai Access Token Key", 
				description = "PwC Akamai Access Token Key",
				type = AttributeType.STRING)
		String akamai_accessToken() default StringUtils.EMPTY;

		@AttributeDefinition(name = "PwC Akamai Client Token Key", 
				description = "PwC Akamai Client Token Key",
				type = AttributeType.STRING)
		String akamai_clientToken() default StringUtils.EMPTY;

		@AttributeDefinition(name = "PwC Akamai Client Secret Key", 
				description = "PwC Akamai Client Secret Key",
				type = AttributeType.STRING)
		String akamai_clientSecret() default StringUtils.EMPTY;

		@AttributeDefinition(name = "PwC Akamai Host", 
				description = "PwC Akamai Host",
				type = AttributeType.STRING)
		String akamai_host() default StringUtils.EMPTY;

		@AttributeDefinition(name = "PwC Akamai Uri", 
				description = "PwC Akamai Uri",
				type = AttributeType.STRING)
		String akamai_uri() default "https://api.ccu.akamai.com/ccu/v3/delete/url/production";
	}

	@Activate
	public void activate(final PwCAkamaiCredentialsImpl.Config properties) {
		this.akamaiAccessToken = properties.akamai_accessToken();
		this.akamaiClientToken = properties.akamai_clientToken();
		this.akamaiClientSecret = properties.akamai_clientSecret();
		this.akamaiHost = properties.akamai_host();
		this.akamaiUri = properties.akamai_uri();
		this.clientCredential = initAkamaiClientCredentials();
	}

	@Override
	public ClientCredential getAkamaiClientCredentials() {
		return clientCredential;
	}

	private ClientCredential initAkamaiClientCredentials() {
		if (checkCredentialValidity(akamaiAccessToken, akamaiClientToken, akamaiClientSecret, akamaiHost)) {
			clientCredential = ClientCredential.builder().accessToken(akamaiAccessToken).clientToken(akamaiClientToken)
					.clientSecret(akamaiClientSecret).host(akamaiHost).build();
		}
		return clientCredential;
	}

	@Override
	public String getAkamaiUri() {
		return akamaiUri;
	}

	private boolean checkCredentialValidity(String... credentials) {
		for (String string : credentials) {
			if (StringUtils.isBlank(string)) {
				logger.warn("PwCAkamaiCredentialsImpl.checkCredentialValidity Skipping Akamai caching as one or more of the credential params are missing!! ");
				return false;
			}
		}
		return true;
	}

}
