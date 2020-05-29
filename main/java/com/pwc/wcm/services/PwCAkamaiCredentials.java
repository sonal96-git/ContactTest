package com.pwc.wcm.services;

import com.akamai.edgegrid.signer.ClientCredential;

/**
 * Service to provide Akamai Configuration like ClientCredential & Akamai URI.
 */
public interface PwCAkamaiCredentials {
	/**
	 * Get the a newly-created immutable object of type ClientCredential.
	 *
	 * @return {@link ClientCredential} The ClientCredential object containing information related to PwC Akamai
	 *         Account.
	 */
	ClientCredential getAkamaiClientCredentials();
	
	/**
	 * Get the PwC Akamai Account URI.
	 *
	 * @return {@link String} URI of PwC Akamai Account.
	 */
	String getAkamaiUri();
	
}
