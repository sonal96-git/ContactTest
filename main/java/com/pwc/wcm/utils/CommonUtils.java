/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pwc.wcm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.day.cq.commons.ImageHelper;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.foundation.Image;
import com.day.image.Layer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pwc.AdminResourceResolver;
import com.pwc.ApplicationConstants;
import com.pwc.model.BitlyConfig;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/*
 * Reviewed 04 Sep 2012
 * @Modified 17 Mar 2020
 *
 */
public class CommonUtils {
	
	private static final Log LOGGER = LogFactory.getLog(CommonUtils.class);

	private static final String REGEX_PwC_PAGE_TERRITORY_HIERARCHY = "/content/pwc/([\\w]+)/([\\w]+).*";
	private static final String REGEX_PwC_MICROSITE_HIERARCHY = "/content/pwc/[\\w]+/[\\w]+/website/([^/]+).*";
    private static final String MICRO_SITE_HOME_PAGE_PATTERN = "^/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})/website/(([^/]*.html$)|([^/]+)($|/$))";
    private static final String PARENT_PAGE_PATH_PREFIX = "/content/pwc/";
	private static final String STRATEGY_PAGE_REGEX = "^\\/content\\/pwc\\/\\d*\\/.*$";
	
  	public BitlyConfig getConfig() throws IOException {
  		BitlyConfig bitlyConfig = new BitlyConfig();
  		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
  	    BundleContext bundleContext = bundle.getBundleContext();
  	    ServiceReference<ConfigurationAdmin> serviceReference = bundleContext.getServiceReference(ConfigurationAdmin.class);
		if (serviceReference != null) {
  	    ConfigurationAdmin configAdmin = bundleContext.getService(serviceReference);
  	    Configuration config = configAdmin.getConfiguration("PwC Social");
		bitlyConfig.setBitlyApiUrl(config.getProperties().get("bitly_api_url").toString());
		bitlyConfig.setGroupId(config.getProperties().get("group_guid").toString());
		}
		return bitlyConfig;
	}
	 
  
    public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static String getYoutubeId(String youtubeUrl) {
		Pattern pattern = Pattern.compile(
				"(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(youtubeUrl);
		while (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String getShortUrl(String bitlyAccess, String longUrl) throws IOException, JSONException {
		BitlyConfig bitlyConfig = new BitlyConfig();
		HttpURLConnection conn = null;

		try {
			final String decodedLongUrl = URLDecoder.decode(longUrl, "UTF-8");
			CommonUtils cu = new CommonUtils();
			bitlyConfig = cu.getConfig();
			URL url = new URL(bitlyConfig.getBitlyApiUrl());
			LOGGER.debug("Request URL: {} " + url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + bitlyAccess);
			JSONObject bodyJson = new JSONObject();
			bodyJson.put("long_url", decodedLongUrl);
			bodyJson.put("group_guid", bitlyConfig.getGroupId());
			String reqBody = bodyJson.toString();
			JsonObject jsonObject = new JsonParser().parse(reqBody).getAsJsonObject();
			OutputStream os = conn.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(jsonObject.toString());
			osw.flush();
			osw.close();

			StringBuilder sb = new StringBuilder();
			int httpResult = conn.getResponseCode();
			if (httpResult == HttpURLConnection.HTTP_OK || httpResult == HttpURLConnection.HTTP_CREATED) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				LOGGER.debug("Response from Bitly Server : {} " + sb.toString());
				JSONObject json = new JSONObject(sb.toString());
				LOGGER.debug("Short Bitly Url : {} " + json.getString("link"));
				if(json.has("link")) {       
                        return json.getString("link");                   
                    }
			} else {
				LOGGER.debug(conn.getResponseMessage());
			}
			return longUrl;
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	public static String getExternalUrl(SlingHttpServletRequest request, String path) throws Exception{

    	SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
    	SlingScriptHelper scriptHelper = bindings.getSling();
    	AdminResourceResolver adminResourceResolver = scriptHelper.getService(AdminResourceResolver.class);
    	ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
    	Session session = resourceResolver.adaptTo(Session.class);
    	ConfigurationAdmin configAdmin = scriptHelper.getService(org.osgi.service.cm.ConfigurationAdmin.class);
    	Configuration defaultDomainConf = configAdmin.getConfiguration("PwC Default Domain");
        String defaultDomain = (String) defaultDomainConf.getProperties().get("domain");
        String domainType = (String) defaultDomainConf.getProperties().get("domainType");
        LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(session, defaultDomain, domainType);
        return linkTransformerService.transformAEMUrl(path);

    }
	
	public static String getTransformedUrl(HttpServletRequest request, String path) {

    	SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
    	SlingScriptHelper scriptHelper = bindings.getSling();
    	AdminResourceResolver adminResourceResolver = scriptHelper.getService(AdminResourceResolver.class);
    	ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
    	Session session = resourceResolver.adaptTo(Session.class);
    	LinkTransformerServiceFactory linkTransformerServiceFactory = scriptHelper.getService(LinkTransformerServiceFactory.class);
        LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerService(session);
		String transformedUrl = linkTransformerService.transformAEMUrl(path);
		if (resourceResolver != null && resourceResolver.isLive()) {
			resourceResolver.close();
		}
		return transformedUrl;
	}

	 public static String getExternalUrl(AdminResourceResolver adminResourceResolver,ConfigurationAdmin configAdmin, String path) throws Exception{
	     	ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
	     	Session session = resourceResolver.adaptTo(Session.class);
	     	Configuration defaultDomainConf = configAdmin.getConfiguration("PwC Default Domain");
	         String defaultDomain = (String) defaultDomainConf.getProperties().get("domain");
	         String domainType = (String) defaultDomainConf.getProperties().get("domainType");
	         LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(session, defaultDomain, domainType);
	        return linkTransformerService.transformAEMUrl(path);
	      }

	public static String getImagePath(Resource resource) {
		Image image = new Image(resource);
		image.setSelector(".img");
		if (image.hasContent()) {
			return externalLink(image.getSrc(), resource.getResourceResolver());
		}
		return StringUtils.EMPTY;
	}

	public static int getImageHeight(String path, ResourceResolver resourceResolver) {
		Resource resource = resourceResolver.resolve(path);
		Asset asset = resource.adaptTo(Asset.class);
		Rendition original = asset.getOriginal();
		Layer layer = ImageHelper.createLayer(original);
		return layer.getHeight();
	}

	private static String externalLink(String path, ResourceResolver resourceResolver) {
		String[] urlMapppings = null;
		String absolutePath = path;
		ValueMap properties = ConfigUtils.getProperties(resourceResolver);

		if (properties != null) {
			urlMapppings = properties.get(ConfigUtils.SITE_MAP_URL_MAPPING_S_PROP, String[].class);
		}
		if (urlMapppings != null) {
			for (String mapping : urlMapppings) {
				String[] values = mapping.split("-");
				if (values.length > 1 && absolutePath.startsWith(values[0])) {
					absolutePath = values[1] + absolutePath.substring(values[0].length());
				}
			}
		}
		return absolutePath;
	}

	public static String getImagePath(Resource resource, String backgroundImage) {
		Image image = new Image(resource);
		image.setSelector(".img");
		if (image.hasContent()) {
			return externalLink(image.getSrc(), resource.getResourceResolver());
		}
		return backgroundImage;
	}

	private static Page getFirstNode(Page page) {
		if (page.listChildren() != null) {
			Iterator<Page> iterator = page.listChildren();
			while (iterator.hasNext()) {
				Page pagei = iterator.next();
				if (pagei.getProperties().get("hideInNav") == null
						|| (pagei.getProperties().get("hideInNav")) == "false") {
					return pagei;
				}
			}
		}
		return null;
	}

	public static String convertPathInternalLink(SlingHttpServletRequest request, String path) {
		if (StringUtils.isEmpty(path)) {
			return "";
		}

		Resource searchDestinationResource = request.getResourceResolver().getResource(path);
		if (searchDestinationResource != null && !StringUtils.isEmpty(path)) {
			Page page = searchDestinationResource.adaptTo(Page.class);
			if (page != null) {
				return path + ".html";
			}
		}
		return path;
	}

	public static String getHomePage(Page currentPage) {
		Page currentLanguageNode = currentPage.getParent(currentPage.getDepth() - 3);
		if (currentLanguageNode == null) {
			return "/";
		}
		Page firstNode = getFirstNode(currentLanguageNode);
		if (firstNode != null) {
			return firstNode.getPath() + ".html";
		}
		return "/";
	}

	public static String urlEncode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}

	public static String removeHiphensNCapitalize(String desStr) {
		String result = "";
		String[] words = desStr.split("\\-");
		for (int i = 0; i < words.length; i++) {
			result += StringUtils.capitalize(words[i]);
		}
		return result;
	}

	public static String removeExpNNCapitalize(String desStr, String exp) {
		String result = "";
		String[] words = desStr.split(exp);
		for (int i = 0; i < words.length; i++) {
			result += StringUtils.capitalize(words[i]);
		}
		return removeHiphensNCapitalize(result);
	}

	public static String getCustomStackTrace(Throwable aThrowable) {
		final StringBuilder result = new StringBuilder("BOO-BOO: ");
		result.append(aThrowable.toString());
		final String NEW_LINE = System.getProperty("line.separator");
		result.append(NEW_LINE);

		for (StackTraceElement element : aThrowable.getStackTrace()) {
			result.append(element);
			result.append(NEW_LINE);
		}
		return result.toString();
	}

	public static String getResourcePathFromURL(String url) {
		int uriPosition = url.indexOf("/", 10);
		return url.substring(uriPosition);
	}

	public static String getNavigationDisplayTitle(Page page) {
		return (StringUtils.isNotBlank(page.getNavigationTitle())) ? page.getNavigationTitle() : page.getTitle();
	}

	public static String convertUrl(SlingHttpServletRequest request, String path) {
		if (path != null && path.trim().length() > 0) {
			URI uri = null;
			try {
				uri = new URI(path.trim());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (uri != null && uri.isAbsolute()) {
				return path;
			} else {
				return convertPathInternalLink(request, path.trim());
			}
		} else {
			return "";
		}
	}

	/* Created to PR-2792 only one tag bug when tag is an String instead an array */
	public static String getKeywords(HttpServletRequest request, String tagId, Page page) {
		SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;
		ResourceResolver resourceResolver = slingHttpServletRequest.getResourceResolver();
		Locale locale = page.getLanguage(false);
		TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
		Tag tag = tagManager.resolve(tagId);
		if (tag != null) {
			StringBuilder tagResult = new StringBuilder();
			tagResult.append(tag.getTitle(locale));
			return tagResult.toString();
		}
		return "";
	}

	/**
	 * Returns comma separated list of tag's titles assigned to page, each title
	 * value of tag contains both unlocalized and localized title separated by '||'
	 * e.g. 'Hello||Bonjour'. If no localized title is present for tag, empty string
	 * is used and title value is returned as 'Hello||'
	 * 
	 * @param slingRequest
	 *            {@link SlingHttpServletRequest} sling request of the page
	 * @param page
	 *            {@link Page} object of the page
	 * @return comma separated tag titles String or empty String if no tag is
	 *         assigned to page
	 */
	public static String getPwCKeywords(SlingHttpServletRequest slingRequest, Page currentPage) {
		StringBuffer keywords = new StringBuffer();
		String localeString = new PageService().getLocale(slingRequest, currentPage);
		if (currentPage != null) {
			Locale locale = new Locale(localeString);
			Tag[] tags = currentPage.getTags();
			for (int i = 0; i < tags.length; i++) {
				if (keywords.length() > 0) {
					keywords.append(", ");
				}
				keywords.append(tags[i].getTitle() + "||");
				String localizedTitle = tags[i].getLocalizedTitle(locale);
				if (localizedTitle != null)
					keywords.append(localizedTitle);
			}
		}
		return keywords.toString();
	}
	
	/**
	 * Returns the Territory code of the given page path.
	 * 
	 * @param path {@link String}
	 * @return {@link String}
	 */
	public static String getCurrentPageTerritory(final String path) {
		return path != null && path.matches(REGEX_PwC_PAGE_TERRITORY_HIERARCHY)
				? path.replaceFirst(REGEX_PwC_PAGE_TERRITORY_HIERARCHY, "$1")
				: null;
	}
	
	/**
	 * Returns the language code of the given page path.
	 * 
	 * @param path {@link String}
	 * @return {@link String}
	 */
	public static String getCurrentPageLanguage(final String path) {
		return path != null && path.matches(REGEX_PwC_PAGE_TERRITORY_HIERARCHY)
				? path.replaceFirst(REGEX_PwC_PAGE_TERRITORY_HIERARCHY, "$2")
				: null;
	}
	
	/**
	 * Returns the Language page path of the given path.
	 * 
	 * @param path {@link String}
	 * @return {@link String}
	 */
	public static String getParentLanguagePagePath(String childPagePath) {
		String locale = getPathLocale(childPagePath);
		return locale == null ? null
				: PARENT_PAGE_PATH_PREFIX + LocaleUtils.getTerritoryFromLocale(locale) + "/"
						+ LocaleUtils.getLanguageFromLocale(locale);
	}
    
        /**
         * Returns the locale of the given path.
         * 
         * @param path {@link String}
         * @return {@link String}
         */
        public static String getPathLocale(final String path) {
            return LocaleUtils.getLocale(getCurrentPageTerritory(path), getCurrentPageLanguage(path));
        }
        
        /**
         * Returns the locale of the current page.
         * 
         * @param path {@request SlingHttpServletRequest, @currentPage Page}
         * @return {@locale String}
         */
        public static String getLocale(SlingHttpServletRequest request, Page currentPage) {
        	String locale = null;
        	if (currentPage != null) {
        		String pagePath = currentPage.getPath();
				if (pagePath.contains("/content/pwc/userReg") || pagePath.contains("/content/pwc/global/forms/")) {
					locale = getPathLocale(pagePath);
				} else {
					locale = new PageService().getLocale(request, currentPage);
				}
			}
			return locale;
        }
        /**
         * Returns the name of the Microsite that the given path belongs to.
         * 
         * @param path {@link String}
         * @return {@link String}
         */
        public static String getMicrositeNameFromPagePath(String path) {
            return path != null && path.matches(REGEX_PwC_MICROSITE_HIERARCHY) 
                    ? path.replaceFirst(REGEX_PwC_MICROSITE_HIERARCHY, "$1")
                    : null;
        }

		public static String getTransformedUrl(String path, LinkTransformerServiceFactory linkTransformerServiceFactory, SlingRepository repository ) {
			if (path != null) {
				LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerService(repository);
				if(linkTransformerService != null)
					path = linkTransformerService.transformAEMUrl(path);
			}
			return path;
		}

		/**
		 * Returns if the given path if of a microsite.
		 *
		 * @param path {@link String} path of the page.
		 * @return {@link Boolean}
		 */
		public static Boolean isMicroSite(final String path) {
			boolean isMicro = false;
			Pattern r = Pattern.compile(MICRO_SITE_HOME_PAGE_PATTERN);
			Matcher m = r.matcher(path);
			isMicro = m.find();
			if (!isMicro) {
				Pattern r2 = Pattern.compile("/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})/website/(.*)");
				Matcher m2 = r2.matcher(path);
				isMicro = m2.find();
			}
			return isMicro;
		}

		/**
		 * Schedule the Generic and Delta siteMap generation.
		 * Called on activation of Generic SiteMap Scheduler and Delta SiteMap Scduler
		 *
		 * @param scheduler {@link Scheduler} A scheduler to schedule time/cron based jobs.
		 * @param schedulingExpression {@link String} Cron expression for a job.
		 * @param canRunConcurrently {@link Boolean} Whether this job can run even if previous scheduled runs are still running.
		 * @param jobName {@link String} Name of job to execute.
		 * @param config {@link Map<String, Serializable>} This configuration is only passed to the job the job implements Job.
		 * @param siteMapSchedulerThread {@link Runnable} The job to execute
		 */
		public static void scheduleSiteMapGeneration(final Scheduler scheduler,final String schedulingExpression,final boolean canRunConcurrently,final String jobName,final Map<String, Serializable> config,final Runnable siteMapSchedulerThread) {
			ScheduleOptions options = scheduler.EXPR(schedulingExpression);
			options.canRunConcurrently(canRunConcurrently);
			options.name(jobName);
			options.config(config);
			scheduler.schedule(siteMapSchedulerThread, options);
		}

		/**
		 * Returns the image path for a particular page.
		 *
		 * @param imageResource {@link Resource}
		 * @return {@link String}
		 */
		public static String getPageImagePath(Resource imageResource) {
			ValueMap imageResourceValueMap = imageResource.getValueMap();
			return imageResourceValueMap.containsKey(FieldConstant.FILE_REFERENCE) ?
				imageResourceValueMap.get(FieldConstant.FILE_REFERENCE).toString() :
				null != imageResource.getChild(FieldConstant.FILE) ? imageResource.getChild(FieldConstant.FILE).getPath() : "";
		}

	/**
	 * Returns the substring with ellipses, if fullString exceeded the charLimit. If the last word gets cut in subString, it returns the subString with last complete word, appending the ellipses.
	 *
	 * @param fullString {@link String} Object of full String
	 * @param charLimit  {@link Integer} Integer object of charLimit after which ellipses is to be applied.
	 * @return {@link String}
	 */
	public static String getEllipsesString(String fullString, Integer charLimit) {
		if (null != fullString && null != charLimit && fullString.length() > charLimit) {
			String subString = fullString.substring(0, charLimit + 1);
			return (subString.endsWith(" ") ?
				subString.substring(0, subString.length() - 1) :
				subString.substring(0, subString.lastIndexOf(" ") == -1 ? charLimit : subString.lastIndexOf(" ")))
				+ ApplicationConstants.SUFFIX_TEXT;
		}
		return fullString;
	}
	
	/**
	 * Write response for servlet with content type 'application/html'.
	 * 
	 * @param response {@link SlingHttpServletResponse} servlet response object
	 * @throws IOException {@link IOException} This exception occurs when an IO operation has failed for some reason.
	 */
	public static void writeResponse(final SlingHttpServletResponse response, final String msg) throws IOException {
		response.setContentType("application/html; charset=UTF-8");
		final PrintWriter writer = response.getWriter();
		writer.print(msg);
		writer.flush();
	}
	
	/**
	 * Checks if pagepath contains digits as territory code i.e. URL is from a strategyand territory. 
	 *
	 * @param path {@link String}
	 * @return isStrategyEndURL {@link booelean}
	 */
	public static boolean isStrategyAndURL(String path) {
		Pattern sandPattern = Pattern.compile("^/content/(?:dam/pwc|pwc)/(\\d{2})/*");		
        Matcher sandMatcher = sandPattern.matcher(path);	
		return sandMatcher.find();
	}
	/**
	 * Match pagePath with Strategy domain regex pattern check if territory code in path is numeric or not.
	 * 
	 * @param pagePath {@link String} Path of page
	 * @return true{@link Boolean} true if territory code is numeric
	 */
	public static Boolean isStrategyDomain(String pagePath) {
		return pagePath.matches(STRATEGY_PAGE_REGEX);
	}

	
	/**
	 * Match parentPagePath from request with Strategy domain regex pattern check if territory code in path is numeric
	 * or not.
	 * 
	 * @param request {@link SlingHttpServletRequest} Path of page
	 * @return true{@link Boolean} true if territory code is numeric
	 */
	public static boolean isStrategyDomain(SlingHttpServletRequest request) {
		String pagePath = request.getParameter("parentPagePath");
		boolean isStrategyDomain = false;
		if (StringUtils.isNotBlank(pagePath) && isStrategyDomain(pagePath)) {
			isStrategyDomain = true;
		}
		return isStrategyDomain;
	}
	public static String columnControlClass(Resource child){
		if(child == null || !child.getResourceType().equals("pwc/components/content/columncontrol/parsys/colctrl"))
			return StringUtils.EMPTY;
		String numberOfColumns = child.getValueMap().get("layout",String.class);
		String cols = StringUtils.isBlank(numberOfColumns) ? "0" : numberOfColumns.substring(0,numberOfColumns.indexOf(";"));
		if(!cols.equals("2")){
			return StringUtils.EMPTY;
		}
		String stackOrderMobile = child.getValueMap().get("stackOrderMobile", String.class);
		String columnClass = StringUtils.isBlank(stackOrderMobile) || "column1".equals(stackOrderMobile) ? "" : "col-flipping--enable";
		String verticalCenter = child.getValueMap().get("verticallyCenterColumn",StringUtils.EMPTY);
		if(verticalCenter.equals("column1"))
		{
			columnClass += " " + "vert-center--enable--col1";
		}
		else if(verticalCenter.equals("column2"))
		{
			columnClass += " " + "vert-center--enable--col2";
		}
		return columnClass;
	}

		public static Boolean includeContextHub(Resource resource){
		InheritanceValueMap valueMap = new HierarchyNodeInheritanceValueMap(resource);
		String[] cloudServiceConfigs = (String[]) valueMap.getInherited("cq:cloudserviceconfigs",String[].class);
		if(cloudServiceConfigs == null)
			return false;
		for(int i=0;i<cloudServiceConfigs.length;i++){
			if(cloudServiceConfigs[i].indexOf("testandtarget") > 0){
				return true;
			}
		}
		return false;
	}
	
	public static String getPageUrl(final SlingHttpServletRequest slingRequest, String currentPagePath) {
		String queryParam = slingRequest.getQueryString();
		String queryParamString = StringUtils.isNotEmpty(queryParam) ? new StringBuilder("?").append(queryParam).toString()
				: StringUtils.EMPTY;
		String[] selectors = slingRequest.getRequestPathInfo().getSelectors();
		String selectorParam = ArrayUtils.isNotEmpty(selectors) ? new StringBuilder(".").append(String.join(".", selectors)).toString()
				: StringUtils.EMPTY;
		return new StringBuilder(currentPagePath).append(selectorParam).append(".html").append(queryParamString).toString();
	}
}
