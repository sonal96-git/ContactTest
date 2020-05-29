/*
 * RestClient
 */
package com.pwc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class is a HTTP based RestClient which request to servers using method GET & POST.
 */
public class RestClient {
	
	public enum RequestMethod {
		GET, POST
	}
	
	private static final int TWO = 2;
	
	private String url;
	
	private String response;
	
	private String userName;
	
	private String password;
	
	private String message;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
	
	/**
	 * Convert stream to string.
	 * 
	 * @param is the is
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static String convertStreamToString(final InputStream is) throws IOException {
		return IOUtils.toString(is, StandardCharsets.UTF_8.name());
	}
	
	private static HttpRequestRetryHandler retryHandler() {
		return (exception, executionCount, context) -> {
			LOGGER.info("Try retry request: {} ", executionCount);
			
			if (executionCount >= TWO) {
				LOGGER.error("Do not retry if over max retry count");
				return false;
			}
			if (exception instanceof InterruptedIOException) {
				LOGGER.error("Inside retryHandler method for InterruptedIOException occured", exception);
				return false;
			}
			if (exception instanceof UnknownHostException) {
				LOGGER.error("Inside retryHandler method for UnknownHostException occured", exception);
				return false;
			}
			if (exception instanceof SSLException) {
				LOGGER.error("Inside retryHandler method for SSLException occured", exception);
				return false;
			}
			
			LOGGER.info("Retrying the request::{}", executionCount);
			return true;
		};
	}
	
	/**
	 * Instantiates a new rest client.
	 * 
	 * @param url the url
	 */
	public RestClient(final String url) {
		LOGGER.info("Initializing RestClient Service constructor & URL is {}", url);
		this.url = url;
	}
	
	/**
	 * Execute.
	 * 
	 * @param method the method
	 * @return the response bean
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 */
	public void execute(final RequestMethod method) throws IOException, AuthenticationException {
		switch (method) {
			case GET: {
				final HttpGet request = restGetMethod();
				executeRequest(request, url);
			}
			case POST: {
				final HttpPost request = postMethod();
				executeRequest(request, url);
			}
		}
		
	}
	
	/**
	 * Execute request.
	 * 
	 * @param request the request
	 * @param url the url
	 * @return the response bean
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void executeRequest(final HttpUriRequest request, final String url) throws IOException {
		LOGGER.info("Start of RestClient :: executeRequest method.");
		LOGGER.info("Url is :: {}", url);
		final HttpClient client = HttpClientBuilder.create().setRetryHandler(retryHandler()).build();
		final HttpResponse httpResponse = client.execute(request);
		final int responseCode = httpResponse.getStatusLine().getStatusCode();
		message = httpResponse.getStatusLine().getReasonPhrase();
		final HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			InputStream instream = null;
			try {
				instream = entity.getContent();
				response = convertStreamToString(instream);
			} finally {
				if (instream != null) {
					instream.close();
				}
			}
		}
		LOGGER.info("Response code is  :: {}", responseCode);
		LOGGER.info("Response is ::{}", response);
		LOGGER.info("End of RestClient :: executeRequest method.");
	}
	
	private HttpPost postMethod() throws AuthenticationException {
		final HttpPost request = new HttpPost(url);
		setAuthentication(request);
		return request;
	}
	
	private HttpGet restGetMethod() throws UnsupportedEncodingException, AuthenticationException {
		final HttpGet request = new HttpGet(url);
		setAuthentication(request);
		return request;
	}
	
	/**
	 * Sets the authentication.
	 * 
	 * @param request the new authentication
	 * @throws AuthenticationException the authentication exception
	 */
	private void setAuthentication(final HttpRequest request) throws AuthenticationException {
		if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
			final UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, password);
			request.addHeader(new BasicScheme().authenticate(creds, request, null));
		}
	}
	
	/**
	 * Sets the password.
	 * 
	 * @param password the new password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}
	
	/**
	 * Sets the user name.
	 * 
	 * @param userName the new user name
	 */
	public void setUserName(final String userName) {
		this.userName = userName;
	}
	
}
