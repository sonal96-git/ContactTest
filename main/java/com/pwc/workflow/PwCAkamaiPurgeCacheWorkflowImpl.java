package com.pwc.workflow;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.ApplicationConstants;
import com.pwc.wcm.model.AkamaiConfig;
import com.pwc.wcm.services.AkaimaiPurge;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Created by rjiang022 on 2/10/2015.
 */
@Component(immediate = true, service = { WorkflowProcess.class }, enabled = true, name = "com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl",
property = {
		Constants.SERVICE_DESCRIPTION + "= Implementation of PwC Akamai cache purge",
		Constants.SERVICE_VENDOR + "= PwC"
})
@Designate(ocd = PwCAkamaiPurgeCacheWorkflowImpl.Config.class)
public class PwCAkamaiPurgeCacheWorkflowImpl implements WorkflowProcess {
		
	private String asset_pattern = "(/content/dam/([\\w+\\-/]*)*([\\w+\\-\\.]*)[\\w\\._~:/?#\\[\\]@!$&()*+,;=%]*)";
	private String microsite_pattern = "/content/pwc/\\w{2}/\\w{2}/website((/.*)|(.*))";
	private String http_regex_Str = "((https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String TYPE_JCR_PATH = "JCR_PATH";
	private ArrayList<String> credentials = new ArrayList<String>();
	private String pathPattern = "";	
	private AkamaiConfig config;
	private List<String> assetList;
	private List<String> propertyList;
	private boolean is_mul_language;
	private List<String> externalUrlList;
	private List<String> vanityUrlList;
	private List<String> domainList;
	private String defaultDomain = "https://www.pwc.com";
	private int ccudelay = 30;
	private boolean includeDynamicComponents;
	private String domainType = "";
	private Session session = null;
	private static final String PROCESS_LABEL = "PwC Akamai Purge";

	@Reference
	private ResourceResolverFactory resourceResolverFactory;
	
	@Reference
	private SlingRepository repository;
	
	@Reference
	private Replicator replicator;
	
	@Reference
	private AkaimaiPurge akamaiPurge;
	
	@Reference
	private ConfigurationAdmin configurationAdmin;
	
	private ArrayList<String> dynamicComponentList;

	@ObjectClassDefinition(name = "PwC Akamai Purge Cache", description = "Trigger Akamai to purge the cache")
	@interface Config {
		@AttributeDefinition(name = "Process Label",
				description = "Enable Akamai Purge",
				type = AttributeType.STRING)
		String process_label() default PROCESS_LABEL;

		@AttributeDefinition(name = "Enable Akamai Purge",
				description = "Enable Akamai Purge",
				type = AttributeType.BOOLEAN)
		boolean akamai_enabled();
		
		@AttributeDefinition(name = "Purge Assets", 
				description = "Enable Purge Assets",
				type = AttributeType.BOOLEAN)
		boolean akamai_purgeAsset();
		
		@AttributeDefinition(name = "CCU Call Delay", 
				description = "CCU Call Delay (in seconds)",
				type = AttributeType.INTEGER)
		int akamai_ccudelay() default 30;
		
		@AttributeDefinition(name = "Include Dynamic Components", 
				description = "Include dynamic components in Akamai CCU call",
				type = AttributeType.BOOLEAN)
		boolean akamai_dynamicComponentFlag() default true;
		
		@AttributeDefinition(name = "Dynamic Components", 
				description = "Dynamic Components",
				type = AttributeType.STRING)
		String[] DynamicComponents();
	}


	@Override
	public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
		if (config.isEnableCachePurge()) {
			assetList = new ArrayList<String>();
			propertyList = new ArrayList<String>();
			externalUrlList = new ArrayList<String>();
			vanityUrlList = new ArrayList<String>();
			domainList = new ArrayList<String>();
			domainList.add("https://www.pwc.com");
			domainList.add("https://dpe-stg.pwc.com");
			if(defaultDomain.contains("http://")) {
				domainList.add(defaultDomain.replace("http://", "https://"));
			}else{
				domainList.add(defaultDomain);
			}


			try {
				WorkflowData workflowData = item.getWorkflowData();
				if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
					String path = workflowData.getPayload().toString();
					ResourceResolver resourceResolver  = getResourceResolver(workflowSession.getSession());

					//Resource territoryResource = resourceResolver.getResource(territoryPath);
					Iterator<Resource> territoryIt = resourceResolver.findResources("SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/content/pwc/global/referencedata/territories]) and (s.[forward-domain]<>'' OR s.[forward-domain-qa]<>'' OR s.[forward-domain-staging]<>'')","JCR-SQL2");
					//Iterator<Resource> territoryIt = allTerritories.iterator();
					String forwardDomain="";
					if (domainType != null && domainType.trim().length() > 0) {
						if (domainType.equalsIgnoreCase("staging") || domainType.equalsIgnoreCase("qa"))
							forwardDomain = "forward-domain" + "-" + domainType;
					} else
						forwardDomain = "forward-domain";
					while(territoryIt.hasNext()){
						Resource res = territoryIt.next();
						ValueMap valueMap = res.getValueMap();
						if(valueMap.containsKey(forwardDomain)){
							String eachDomain = valueMap.get(forwardDomain,"");
							if(!eachDomain.equals("")) {
								if (!domainList.contains(eachDomain)) {
									domainList.add(eachDomain.replace("http://","https://")); //add https
								}
							}
						}

					}
					Node currentPage =workflowSession.getSession().getNode(path);
					Pattern r = Pattern.compile(microsite_pattern);
					Matcher m = r.matcher(path);
					if (config.isPurgeSinglePage()) {
						//logger.info("----Start processing NON-microsite (Single page ONLY)----");
						String externalUrl = translateContentUrl(currentPage);
						URI externalURI = new URI(externalUrl);
						String host = externalURI.getHost();
						externalUrlList.add(externalUrl);

						//Adding page url with rebrand selector in purge list
						externalUrlList
						.add((externalUrl.endsWith(ApplicationConstants.HTML_EXTENSION) ? externalUrl.replace(ApplicationConstants.HTML_EXTENSION, "") : externalUrl)
								+ ApplicationConstants.REBRAND_URL_SELECTOR + ApplicationConstants.HTML_EXTENSION);

						//add all the cross-domain pwc urls and send to akamai for purging...
						Node jcrNode = currentPage.getNode("jcr:content");
						iteratePageProperties(jcrNode);
						for(String eachProperty: propertyList){
							externalUrlList.add(eachProperty);
						}
						//if the page contains dynamic components, then these need to be cleared too
						if(includeDynamicComponents) {
							for (int i = 0; i < dynamicComponentList.size(); i++) {
								try {
									Pattern esiPattern = Pattern.compile("([^.]+).([^.]+).([^.]+)");
									Matcher esiMatcher = esiPattern.matcher(dynamicComponentList.get(i));
									if (esiMatcher.find()) {
										String componentName = esiMatcher.group(1);
										String esi = esiMatcher.group(2);
										String extension = esiMatcher.group(3);
										String query = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" + path + "/jcr:content]) and [jcr:path] LIKE '%/" + componentName + "%'";
										//logger.info("QUERY = ["+ query + "]");
										Iterator<Resource> ccomponentIt = resourceResolver.findResources(query, "JCR-SQL2");
										if (ccomponentIt != null && ccomponentIt.hasNext()) {
											Resource dynamicResource = ccomponentIt.next();
											String dynamicComponentUrl = externalURI.getScheme() + "://" + host + dynamicResource.getPath() + "." + esi + "." + extension.replace("{currentPage}", path);//the replacing part is specific for leftnav special case.
											externalUrlList.add(dynamicComponentUrl);
										}
									}

								} catch (Exception ex) {
									logger.error("com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl Dynamic component configuration value error, format={component}.dynamic.html", ex);
								}
							}
						}
						if (jcrNode.hasProperty("sling:vanityPath")) {
							if (jcrNode.getProperty("sling:vanityPath").isMultiple()) {
								Value[] values = jcrNode.getProperty("sling:vanityPath").getValues();
								for (Value value : values) {
									String vanity = translateVanityUrl(currentPage.getPath() + ".html", value.getString());
									if (!externalUrlList.contains(vanity))
										externalUrlList.add(vanity);
								}
							} else {
								Value value = jcrNode.getProperty("sling:vanityPath").getValue();
								String vanity = translateVanityUrl(currentPage.getPath() + ".html", value.getString());
								if (!externalUrlList.contains(vanity))
									externalUrlList.add(vanity);
							}
						}
						if (config.isEnablePurgeAsset()) {
							Node currentPageJcr = currentPage.getNode("jcr:content");
							iterateSinglePageAsset(currentPageJcr);
							for (String eachAsset : assetList) {
								String externalAssetUrl = translateAssetUrl(eachAsset);
								externalUrlList.add(externalAssetUrl);
							}
						}

					} else {
						String externalUrl = translateContentUrl(currentPage);
						externalUrlList.add(externalUrl);
					}
					purge();
				}
			} catch (Exception ex) {
				logger.error("Error in executing Akamai Cache Purge Workflow : ", ex);
			} finally {
				if (session != null) {
					session.logout();
					session = null;
				}
			}
		}
		logger.info("Page Akamai is done");
	}

	public void purge() {
		try {
			List<String> allUrls = new ArrayList<String>();
			for (String eachUrl : externalUrlList) {
				if (eachUrl != null && eachUrl.trim().length() > 0)
					allUrls.add(eachUrl);
			}
			for (String eachVanity : vanityUrlList) {
				if (eachVanity != null && eachVanity.trim().length() > 0)
					allUrls.add(eachVanity);
			}
			if (!allUrls.isEmpty())
				akamaiPurge.purge(allUrls);
		} catch (Exception ex) {
			logger.error("Akamai Purging error ", ex);
		}
	}

	private String translateAssetUrl(String path) {
		String translatedUrl = "";
		try {
			LinkTransformerService transformer = new LinkTransformerServiceImpl(repository, defaultDomain, domainType);
			translatedUrl = transformer.transformAEMUrl(path, "");
		} catch (Exception ex) {
			logger.error("com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl.translateAssetUrl", ex);
		}
		return translatedUrl;
	}

	private String translateContentUrl(Node currentPage) {
		String translatedUrl = "";
		try {
			LinkTransformerService transformer = new LinkTransformerServiceImpl(repository, defaultDomain, domainType);
			translatedUrl = transformer.transformAEMUrl(currentPage.getPath());
		} catch (Exception ex) {
			logger.error("com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl.translateAssetUrl", ex);
		}
		Pattern pattern = Pattern.compile("^https?://[^/]+$|/$"); // if the translatedUrl is a domain name such as http://www.pwc.co.uk, then dont add .html at the end.
		Matcher sm = pattern.matcher(translatedUrl);
		if(!sm.find())
			translatedUrl = translatedUrl + ".html";
		return translatedUrl;
	}

	private String translateVanityUrl(String currentPagePath, String path) {
		String translatedUrl = "";
		try {
			LinkTransformerService transformer = new LinkTransformerServiceImpl(repository, defaultDomain, domainType);
			translatedUrl = transformer.transformVanity(path, currentPagePath);
		} catch (Exception ex) {
			logger.error("com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl.translateAssetUrl", ex);
		}
		return translatedUrl;
	}

	private void iterateSinglePageAsset(Node node) throws RepositoryException {
		NodeIterator list = node.getNodes();

		while (list.hasNext()) {
			Node childNode = list.nextNode();
			if (childNode.getPath().indexOf(pathPattern) > -1) {
				PropertyIterator pi = childNode.getProperties();
				while (pi.hasNext()) {
					try {
						javax.jcr.Property property = pi.nextProperty();
						Pattern r = Pattern.compile(asset_pattern);
						Matcher m = r.matcher(property.getString());
						if (m.find()) {
							assetList.add(m.group(0));
						}
					} catch (Exception ex) {
					}
				}
				iterateSinglePageAsset(childNode);
			}
		}
	}

	private void iteratePageProperties(Node node) throws RepositoryException {
		NodeIterator list = node.getNodes();
		while (list.hasNext()) {
			Node childNode = list.nextNode();
			PropertyIterator pi = childNode.getProperties();
			while (pi.hasNext()) {
				try {

					javax.jcr.Property property = pi.nextProperty();
					Pattern r = Pattern.compile(http_regex_Str);
					Matcher m = r.matcher(property.getString());
					while (m.find()) {
						try{
							String propertyVal = m.group(1);
							URI validURI = new URI(propertyVal);
							if(!validURI.getPath().trim().equalsIgnoreCase("")&&!validURI.getPath().trim().equals("/")) { //to avoid full domain purge such as http://www.pwc.com
								String eachHost = validURI.getScheme() + "://" + validURI.getHost();
								if(domainList.contains(eachHost) && !propertyList.contains(propertyVal)) {
									propertyList.add(propertyVal);
								}
							}
						}catch(Exception ex){

						}
					}
				} catch (Exception ex) {
				}
			}
			iteratePageProperties(childNode);
		}
	}



	@Activate
	protected void activate(PwCAkamaiPurgeCacheWorkflowImpl.Config properties) {
		try {
			config = new AkamaiConfig();
			String[] dycomp = properties.DynamicComponents();
			config.setEnableCachePurge(properties.akamai_enabled());
			config.setEnablePurgeAsset(properties.akamai_purgeAsset());
			includeDynamicComponents = properties.akamai_dynamicComponentFlag();
			config.setPurgeSinglePage(true);

			logger.info("akamai_ccudelay " + properties.akamai_ccudelay());
			this.ccudelay = properties.akamai_ccudelay();
			dynamicComponentList = new ArrayList<String>(Arrays.asList(dycomp));			
			Configuration config = configurationAdmin.getConfiguration("PwC Default Domain");
			defaultDomain = (String) config.getProperties().get("domain");
			domainType = (String) config.getProperties().get("domainType");
			logger.info(defaultDomain);
		} catch (Exception exp) {
			logger.error("Exception in com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl.activate", exp);
		}
	}

	private ResourceResolver getResourceResolver(Session session) throws org.apache.sling.api.resource.LoginException {
		return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
				session));
	}

}
