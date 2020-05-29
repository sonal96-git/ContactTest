package com.pwc.workflow;

import java.io.IOException;
import java.util.Collections;

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akamai.edgegrid.signer.ClientCredential;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.wcm.services.AkaimaiPurge;
import com.pwc.wcm.services.PwCAkamaiCredentials;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC Author Akamai asset cache purge",
    Constants.SERVICE_VENDOR + "= PwC",
   "process.label=" + "PwC Author Akamai Asset Purge"    
})
public class PwCAuthorAssetAkamaiPurgeWorkflowImpl implements WorkflowProcess {
	private final Logger logger = LoggerFactory.getLogger(PwCAuthorAssetAkamaiPurgeWorkflowImpl.class);
	private static final String TYPE_JCR_PATH = "JCR_PATH";
	private static final String PWC_AUTHOR_CACHE_CLEAR_CONFIG_PID = "com.pwc.workflow.PwCAuthorDispatcherCacheClearWorkflow";
	private static final String PWC_AUTHOR_AKAMAI_CONFIG_PID = "com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl";
	private static final String AKAMAI_ENABLED_CONFIG_NAME = "akamai.enabled";
	private static final String AUTHOR_DOMAIN_CONFIG_NAME = "akamai.author.domain";
	private static final String AKAMAI_REQUEST_OBJECTS_KEY = "objects";
	private static final String DEBUG_LOG_MSG_PREFIX = "PwC Author Akamai Cache Purge: ";
	private static final String AKAMAI_CACHE_PURGE_FAILURE_MSG = "PwC Author Akamai Cache Purge failed. ";
	private static final String CONFIG_NOT_FOUND_ERROR = "'PwC Author Cache Purge' config not found! Akamai cache purging won't be done for DAM assets.";
	private boolean akamaiEnabled = false;
	private String domain = "";
	private ClientCredential clientCredential = null;
	
	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	@Reference
	private ConfigurationAdmin configurationAdmin;
	@Reference
	private AkaimaiPurge akamaiPurge;
	@Reference
	PwCAkamaiCredentials akamaiCredentials;
	
	@Activate
	protected void activate(final ComponentContext context) {
		try {
			Configuration cacheConfig = configurationAdmin.getConfiguration(PWC_AUTHOR_CACHE_CLEAR_CONFIG_PID);
			Configuration akamaiConfig = configurationAdmin.getConfiguration(PWC_AUTHOR_AKAMAI_CONFIG_PID);
			if (cacheConfig.getProperties() != null && akamaiConfig.getProperties() != null) {
				akamaiEnabled = PropertiesUtil.toBoolean(akamaiConfig.getProperties().get(AKAMAI_ENABLED_CONFIG_NAME), true);
				domain = PropertiesUtil.toString(cacheConfig.getProperties().get(AUTHOR_DOMAIN_CONFIG_NAME), "");
			}
		} catch (IOException ioExcep) {
			logger.warn(CONFIG_NOT_FOUND_ERROR, ioExcep);
		}
		logger.info("PwC Author Asset Akamai Purging " + getConfiguredValues());
	}
	
	/*
	 * Purges Akamai cache for the URLs of all the renditions of the asset on which this workflow step is launched.
	 */
	@Override
	public void execute(final WorkItem item, final WorkflowSession workflowSession, final MetaDataMap metaDataMap)
			throws WorkflowException {
		logger.info("Starting Author Akamai Cache Purging!");
		logger.debug(DEBUG_LOG_MSG_PREFIX + getConfiguredValues());
		clientCredential = akamaiCredentials.getAkamaiClientCredentials();
		if (akamaiEnabled && StringUtils.isNotBlank(domain) && clientCredential != null) {
			final WorkflowData workflowData = item.getWorkflowData();
			if (TYPE_JCR_PATH.equals(workflowData.getPayloadType())) {
				final String assetPath = workflowData.getPayload().toString();
				final JSONArray renditionUrls = getRenditionUrls(workflowSession, assetPath);
				try {
					final JSONObject obj = new JSONObject();
					if (renditionUrls.length() > 0) {
						obj.put(AKAMAI_REQUEST_OBJECTS_KEY, renditionUrls);
						akamaiPurge.purge(obj);
					}
				} catch (final JSONException jsonExcep) {
					logger.error(AKAMAI_CACHE_PURGE_FAILURE_MSG + "Error Occurred while creating JSON Object for URLs: " + renditionUrls,
							jsonExcep);
				}
			} else {
				logger.info(
						"PwCAuthorAssetAkamaiPurgeWorkflowImpl.execute: Author Akamai Cache Purging couldn't be executed, as payload type {} in not equal to {}",
						workflowData.getPayloadType(), TYPE_JCR_PATH);
			}
		} else {
			logger.warn(
					"PwCAuthorAssetAkamaiPurgeWorkflowImpl.execute: Author Akamai Cache Purging couldn't be executed. Either akamai is not enabled or some akamai configuations are missing. {}",
					getConfiguredValues());
		}
	}
	
	/**
	 * Returns the configured values for Akamai Purging.
	 * 
	 * @return {@link String}
	 */
	private String getConfiguredValues() {
		return "Configured Values:" + "\nAkamai Enabled: " + akamaiEnabled + "\nAkamai Author Domain: " + domain + "\nClientCredentials"
				+ clientCredential;
	}
	
	/**
	 * Returns a {@link JSONArray} containing paths of all the resources under the renditions folder of the asset
	 * passed.
	 * 
	 * @param workflowSession {@link WorkflowSession}
	 * @param originalRenditionPath {@link String} Path to the 'original' rendition whose siblings are to be returned
	 * @return {@JSONArray} Contains paths of all renditions including 'original'
	 */
	private JSONArray getRenditionUrls(final WorkflowSession workflowSession, final String originalRenditionPath) {
		final JSONArray renditionUrls = new JSONArray();
		try {
			final ResourceResolver resourceResolver = getResourceResolver(workflowSession.getSession());
			final Resource assetResource = resourceResolver.resolve(originalRenditionPath);
			if (null == assetResource) {
				logger.info("PwCAuthorAssetAkamaiPurgeWorkflowImpl.getRenditionUrls: Resource not found at path {}", originalRenditionPath);
			} else {
				Asset damAsset = DamUtil.resolveToAsset(assetResource);
				if (null == damAsset) {
					logger.info("PwCAuthorAssetAkamaiPurgeWorkflowImpl.getRenditionUrls: Dam Asset not found at path {}",
							originalRenditionPath);
				} else {
					renditionUrls.put(toAbsoluteUrl(damAsset.getPath()));
					for (final Rendition rendition : damAsset.getRenditions()) {
						renditionUrls.put(toAbsoluteUrl(rendition.getPath()));
					}
				}
			}
		} catch (final LoginException loginExcep) {
			logger.error(
					"PwCAuthorAssetAkamaiPurgeWorkflowImpl.getRenditionUrls: Login failed. Skipping Akamai Cache Purge for the renditions of "
							+ originalRenditionPath,
					loginExcep);
		}
		logger.info("PwCAuthorAssetAkamaiPurgeWorkflowImpl.getRenditionUrls: Found {} Renditions : {} ", renditionUrls.length(),
				renditionUrls.toString());
		return renditionUrls;
	}
	
	/**
	 * Returns the transformed absolute URL corresponding to the passed asset path.
	 * 
	 * @param assetPath {@link String}
	 * @return {@link String}
	 */
	private String toAbsoluteUrl(final String assetPath) {
		return domain + assetPath;
	}
	
	/**
	 * Returns the {@link ResourceResolver} of the passed session.
	 * 
	 * @param session {@link Session}
	 * @return {@link ResourceResolver}
	 * @throws LoginException
	 */
	private ResourceResolver getResourceResolver(final Session session) throws LoginException {
		return resourceResolverFactory
				.getResourceResolver(Collections.<String, Object> singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session));
	}
}
