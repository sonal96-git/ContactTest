package com.pwc.wcm.services.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridRequestSigner;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.pwc.wcm.services.AkaimaiPurge;
import com.pwc.wcm.services.PwCAkamaiCredentials;

/**
 * Created by Rui on 2016/11/28.
 */
@Component(immediate = true, service = { AkaimaiPurge.class }, enabled = true)
public class AkamaiPurgeImpl implements AkaimaiPurge {
	
	@Reference
	private PwCAkamaiCredentials akamaiCredentials;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void purge(List<String> urls) {
		try {
			ClientCredential clientCredential = akamaiCredentials.getAkamaiClientCredentials();
			logger.info("-----Start purging pages for Urls----");
			JSONObject obj = new JSONObject();
			obj.put("objects", urls);
			if (clientCredential != null && urls != null && !urls.isEmpty())
				callAkamaiService(obj, clientCredential);
			logger.info("Finish purging deleted pages");
		} catch (IOException | RequestSigningException | JSONException exp) {
			logger.error("Exception in  AkamaiPurgeImpl.purge for Urls {}", exp);
		}
	}
	
	@Override
	public void purge(JSONObject obj) {
		ClientCredential clientCredential = akamaiCredentials.getAkamaiClientCredentials();
		logger.debug("-----Start purging pages for JSON Object----");
		try {
			if (clientCredential != null && obj != null)
				callAkamaiService(obj, clientCredential);
			else
				logger.debug("Skipping Akamai Purge Due to Empty Params, ClientCredential {} :: Urls {}", clientCredential, obj);
			logger.debug("Finish purging deleted pages");
		} catch (IOException | RequestSigningException exp) {
			logger.error("Exception in AkamaiPurgeImpl.purge for JSON Object {}", exp);
		}
		
	}
	
	private void callAkamaiService(JSONObject obj, ClientCredential credential) throws IOException, RequestSigningException {
		logger.info("Start Akamai Purging for Urls :\n {} ", obj);
		HttpTransport httpTransport = new ApacheHttpTransport();
		HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
		URI uri = URI.create(akamaiCredentials.getAkamaiUri());
		String requestBody = obj.toString();
		HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(uri),
				ByteArrayContent.fromString("application/json", requestBody));
		GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credential);
		requestSigner.sign(request);
		HttpResponse response = request.execute();
		String responseMessage = StringUtils.EMPTY;
		try (Scanner scanner = new Scanner(response.getContent(), StandardCharsets.UTF_8.name())) {
			responseMessage = scanner.useDelimiter("\\A").next();
		}
		logger.info("Purging Akamai successfully, response message is {} ", responseMessage);
	}
	
}
