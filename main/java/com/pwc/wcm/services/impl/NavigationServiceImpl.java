package com.pwc.wcm.services.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.pwc.AdminResourceResolver;
import com.pwc.user.Constants;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.services.LanguageService;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.services.NavigationService;
import com.pwc.wcm.services.SlimHeaderNavigationItems;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.JsonToMapConversion;

@Component(immediate = true, service = { NavigationService.class }, enabled = true)
public class NavigationServiceImpl implements NavigationService {
    

	private static final Logger LOGGER = LoggerFactory.getLogger(NavigationService.class);
    private static final String LANGUAGE_SELECTOR_DATA = "languageSelectorData";
    private static final String TERRITORY_SELECTOR_DATA = "territorySelectorData";
    private static final String BREADCRUMB_DATA = "breadcrumbData";
    private static final String HAMBURGER_DATA = "hamburgerData";
    private static final String SOCIAL_SHARE_DATA = "socialShareData";
    private static final String PAGE_PROPERTY_KEY_INHERITED = "inherited";
    private static final String PAGE_PROPERTY_KEY_BREADCRUMB_ITEM = "breadcrumbItems";
    private static final String PAGE_PROPERTY_KEY_NAV_TITLE = "navTitle";
    private static final String PAGE_PROPERTY_KEY_JCR_TITLE = "jcr:title";
    private static final String REFERAL_TEMPLATE_PATH = "/apps/pwc/templates/longform-contact-profile";
    private static final String PAGE_HEADER_REFERER = "referer";
    private static final String PROPERTY_KEY_ABSOLUTE_PARENT = "absParent";
    private static final String PROPERTY_KEY_RELATIVE_PARENT = "relParent";
    private static final String LINK_URL_JSON_KEY = "linkURL";
    private static final String LINK_TEXT_JSON_KEY = "linkText";
    private static final String BREADCRUMB_JSON_KEY = "breadcrumbs";
    private static final String SECTION_TITLE_JSON_KEY = "sectionTitle";
    private static final String LEVEL_ONE_LINKS_JSON_KEY = "levelOneLinks";
    private static final String BREADCRUMB_VALUE_DEFAULT = "default";
    private static final String HAMBURGER_LINKS_JSON_KEY = "hamburgerLinks";
    private static final String SECTION_PAGE_PATH_JSON_KEY = "sectionPagePath";
    private static final String NAVIGATION_STRATEGY_JSON_KEY = "navigationStrategy";
    private static final String SOCIAL_SHARE_CONFIGURATION_JSON_KEY = "socialShareConfiguration";
    private static final String TITLE_JSON_KEY = "title";
    private static final String LINK_JSON_KEY = "link";
    private static final String SOCIAL_URL_JSON_KEY = "socialURL";
    private static final String SOCIAL_CHANNEL_JSON_KEY = "socialchannel";
    private static final String PROPERTY_VALUE_NONE = "none";
    
    @Reference
    private LanguageService languageService;
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @Reference
    private SlimHeaderNavigationItems buildNavigationItemsService;
    
    @Reference
    private JsonToMapConversion jsonToMapConversionService;
    
    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    @Reference
    private LinkTransformerServiceFactory linkTransformerServiceFactory;
    
    @Reference
    private SlingRepository repository;
     
    private PageManager pageManager = null;
        
    @Override
    public JSONObject getNavigationData(Page page, String locale, SlingHttpServletRequest request) {
        LOGGER.info("NavigationService : Entered getNavigationData");
        JSONObject navigationData = new JSONObject();
        ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
        try {
            if (page != null) {
            	String pagePath = page.getPath();
				boolean isStrategyPage = CommonUtils.isStrategyDomain(pagePath);             
                navigationData.put(LANGUAGE_SELECTOR_DATA, languageService.getLanguageSelectorData(page, locale, request));
				navigationData.put(TERRITORY_SELECTOR_DATA,
						isStrategyPage ? countryTerritoryMapperService.getStatergyTerritorySelectorData()
								: countryTerritoryMapperService.getTerritorySelectorData());
                navigationData.put(BREADCRUMB_DATA, getBreadcrumbData(page, request, resourceResolver));
                navigationData.put(HAMBURGER_DATA, getHamburgerData(page, request, resourceResolver));
                navigationData.put(SOCIAL_SHARE_DATA, getSocialShareData(page, request, resourceResolver));
            }
            
        } catch (JSONException jsonException) {
            LOGGER.error("NavigationService : getNavigationData() : JSONException Occurred  {}", jsonException);
        }
        finally {
			if (resourceResolver != null && resourceResolver.isLive())
				resourceResolver.close();
		}
        return navigationData;
    }
    
    /**
     * provides the breadcrumb data in Json format
     *
     * @param page
     * @param request
     * @param resourceResolver
     * @return JSONObject
     */
    private JSONObject getBreadcrumbData(Page page, SlingHttpServletRequest request, ResourceResolver resourceResolver) {
        LOGGER.info("NavigationService : Entered getBreadcrumbData");
        JSONObject breadcrumbsData = new JSONObject();
        JSONArray breadcrumbs = new JSONArray();
        InheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(page.getContentResource());
        boolean inherited = pageProperties.get(PAGE_PROPERTY_KEY_INHERITED, false);
        String[] breadcrumb;
        if (inherited) {
            breadcrumb = pageProperties.get(PAGE_PROPERTY_KEY_BREADCRUMB_ITEM, String[].class);
        } else {
            String[] parentBreadcrumb = pageProperties.getInherited(PAGE_PROPERTY_KEY_BREADCRUMB_ITEM, String[].class);
            if (parentBreadcrumb != null && parentBreadcrumb[0].equalsIgnoreCase(BREADCRUMB_VALUE_DEFAULT)) {
                breadcrumb = pageProperties.get(PAGE_PROPERTY_KEY_BREADCRUMB_ITEM, String[].class);
            } else {
                breadcrumb = parentBreadcrumb;
            }
        }
        
        if (breadcrumb != null && !breadcrumb[0].equalsIgnoreCase(BREADCRUMB_VALUE_DEFAULT)) {
            for (String link : breadcrumb) {
                PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                Page breadcrumbPage = pageManager.getPage(link.split("\\.")[0]);
                if (breadcrumbPage != null) {
                    ValueMap properties = breadcrumbPage.getProperties();
                    String title = properties.get(PAGE_PROPERTY_KEY_NAV_TITLE, (String) properties.get(PAGE_PROPERTY_KEY_JCR_TITLE));
                    breadcrumbs.put(getBreadcrumbJsonObj(title, link, page.getPath()));
                }
                
            }
        } else {
            Page trail, referralPage = null;
            int currentLevel2 = 4;
            String referralTemplate = pageProperties.get(NameConstants.NN_TEMPLATE, "");
            if (REFERAL_TEMPLATE_PATH.equals(referralTemplate)) {
                String referralUrl = request.getHeader(PAGE_HEADER_REFERER);
                String resourcePath = null;
                try {
                    resourcePath = "/content/pwc" + new URI("" + referralUrl).getPath().replaceAll("\\..*", "");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Resource referralResource = resourceResolver.resolve(resourcePath);
                referralPage = referralResource.adaptTo(Page.class);
                if (referralPage != null) {
                    currentLevel2 = referralPage.getDepth() > 3 ? referralPage.getDepth() : 4;
                }
            } else {
                currentLevel2 = page.getDepth();
            }
            ValueMap valueMap = request.getResource().getValueMap();
            long level2 = valueMap.get(PROPERTY_KEY_ABSOLUTE_PARENT, 2L);
            long endLevel2 = valueMap.get(PROPERTY_KEY_RELATIVE_PARENT, 1L);
            request.setAttribute(PROPERTY_KEY_ABSOLUTE_PARENT, level2);
            request.setAttribute(PROPERTY_KEY_RELATIVE_PARENT, endLevel2);
            
            // get starting point of trail
            while (level2 < currentLevel2 - endLevel2) {
                if (REFERAL_TEMPLATE_PATH.equals(referralTemplate) && referralPage != null)
                    trail = referralPage.getAbsoluteParent((int) level2);
                else
                    trail = page.getAbsoluteParent((int) level2);
                if (trail == null) {
                    break;
                }
                String title = trail.getNavigationTitle();
                if (StringUtils.isBlank(title)) {
                    title = trail.getTitle();
                }
                if (StringUtils.isBlank(title)) {
                    title = trail.getName();
                }
                String link = trail.getPath() + ".html";
                breadcrumbs.put(getBreadcrumbJsonObj(title, link, page.getPath()));
                level2++;
            }
        }
        try {
            breadcrumbsData.put(BREADCRUMB_JSON_KEY, breadcrumbs);
        } catch (JSONException jsonException) {
            LOGGER.error("NavigationService : getBreadcrumbData() : JSONException occurred  {}", jsonException);
        }
        LOGGER.debug("NavigationService : getBreadcrumbData() breadcrumbsData:" + breadcrumbsData);
        return breadcrumbsData;
    }
    
    /**
     * Method prepares Json Object of Breadcrumb when provided with title and href link
     *
     * @param title
     * @param link
     * @return JSONObject
     */
    private JSONObject getBreadcrumbJsonObj(String title, String link, String currentPagePath) {
        JSONObject breadcrumb = new JSONObject();
        try {
            link = getTransformedUrl(link);
            breadcrumb.put(LINK_TEXT_JSON_KEY, title);
            breadcrumb.put(LINK_URL_JSON_KEY, link);
        } catch (JSONException jsonException) {
            LOGGER.error("NavigationService : getBreadcrumbJsonObj() : JSONException Occurred  {}", jsonException);
        }
        return breadcrumb;
    }
    
    /**
     * Method returns Heamburger Data as Json Object
     *
     * @param page
     * @param request
     * @param resourceResolver
     * @return JSONObject
     */
    private JSONObject getHamburgerData(Page page, SlingHttpServletRequest request, ResourceResolver resourceResolver) {
        LOGGER.info("NavigationService : Entered getHamburgerData");
        JSONObject hamburgerData = new JSONObject();
        
        InheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(page.getContentResource());
        pageManager = resourceResolver.adaptTo(PageManager.class);
        String sectionPagePath = PROPERTY_VALUE_NONE.equals(pageProperties.get(SECTION_TITLE_JSON_KEY, PROPERTY_VALUE_NONE))
                ? pageProperties.getInherited(SECTION_TITLE_JSON_KEY, "")
                : pageProperties.get(SECTION_TITLE_JSON_KEY, "");
        String sectionTitle = "";
        if (StringUtils.isNotBlank(sectionPagePath)) {
            Page sectionPage = pageManager.getPage(sectionPagePath);
            if (sectionPage != null)
                sectionTitle = sectionPage.getNavigationTitle() != null ? sectionPage.getNavigationTitle() : sectionPage.getTitle();
            sectionPagePath = getTransformedUrl(sectionPagePath);
        }
        try {
            String[] levelOneLinks = pageProperties.getInherited(LEVEL_ONE_LINKS_JSON_KEY, String[].class);
            JSONArray levelOneJsonArray = new JSONArray();
            if (levelOneLinks != null && levelOneLinks.length > 0) {
                for (String levelOneLink : levelOneLinks) {
                    
                    JSONObject jsonObject = new JSONObject(levelOneLink);
                    
                    // get page by link and fetch the title from the link
                    String link = jsonObject.get(LINK_JSON_KEY).toString();
                    if (StringUtils.isNotBlank(link)) {
                        Resource pageResource = resourceResolver.getResource(link.split("\\.")[0]);
                        if (pageResource != null) {
                            Page hamburgerPage = pageResource.adaptTo(Page.class);
                            if (hamburgerPage != null) {
                                ValueMap properties = hamburgerPage.getProperties();
                                String title = properties.get(PAGE_PROPERTY_KEY_NAV_TITLE, (String) properties.get(PAGE_PROPERTY_KEY_JCR_TITLE));

                                jsonObject.put(TITLE_JSON_KEY, title);
                                levelOneJsonArray.put(jsonObject);
                            }
                        }
                        link = getTransformedUrl(link);
                        jsonObject.put(LINK_JSON_KEY, link);
                    }

                }
            }
            
            hamburgerData.put(HAMBURGER_LINKS_JSON_KEY, new JSONObject(
                    new Gson().toJson(buildNavigationItemsService.generateNavigationLinks(request.getResource(), page), Map.class)));
            hamburgerData.put(LEVEL_ONE_LINKS_JSON_KEY, levelOneJsonArray);
            hamburgerData.put(SECTION_PAGE_PATH_JSON_KEY, sectionPagePath);
            hamburgerData.put(SECTION_TITLE_JSON_KEY, sectionTitle);
            hamburgerData.put(NAVIGATION_STRATEGY_JSON_KEY, pageProperties.getInherited(NAVIGATION_STRATEGY_JSON_KEY, PROPERTY_VALUE_NONE));
        } catch (JSONException jsonException) {
            LOGGER.error("NavigationService : getHamburgerData() : JSONException Occurred  {}", jsonException);
        }
        return hamburgerData;
    }
    
    /**
     * Method returns Social Share Data in Json Format
     *
     * @param page
     * @param request
     * @return JSONObject
     */
    private JSONObject getSocialShareData(Page page, SlingHttpServletRequest request, ResourceResolver resourceResolver) {
        LOGGER.info("NavigationService : Entered getSocialShareData");
        JSONObject socialShareData = new JSONObject();
        Resource resource = resourceResolver.getResource(request.getResource().getPath());
        ModifiableValueMap modifiableValueMap= resource.adaptTo(ModifiableValueMap.class);
        String[] socialShares = modifiableValueMap.get(SOCIAL_SHARE_CONFIGURATION_JSON_KEY, String[].class);
        try {
            JSONArray socialShareJsonArray = new JSONArray();
            if (null != socialShares && socialShares.length > 0) {
                List<String> socialShareList = new ArrayList(Arrays.asList(socialShares));
                if (socialShareList.contains(Constants.GOOGLE_PLUS_VALUE) && resourceResolver.isLive()) {
                    socialShareList.remove(Constants.GOOGLE_PLUS_VALUE);
                    socialShares = socialShareList.toArray(new String[0]);
                    modifiableValueMap.put(SOCIAL_SHARE_CONFIGURATION_JSON_KEY, socialShares);
                    resourceResolver.commit();
                    LOGGER.info("NavigationService.getSocialShareData : Removed Google plus value from the node {}", resource.getPath());
                }
                for (String socialShare : socialShares) {
                    JSONObject jsonObject = new JSONObject(socialShare);
                    socialShareJsonArray.put(jsonObject.get(SOCIAL_CHANNEL_JSON_KEY));
                }
            }
            socialShareData.put(SOCIAL_SHARE_CONFIGURATION_JSON_KEY, socialShareJsonArray);
            socialShareData.put(SOCIAL_URL_JSON_KEY, page.getPath());
        } catch (JSONException jsonException) {
            LOGGER.error("NavigationService : getSocialShareData() : JSONException Occurred  {}", jsonException);
        } catch (PersistenceException persistenceException) {
            LOGGER.error("NavigationService.getSocialShareData : PersistenceException occurs {} while updating the node {}", persistenceException, resource.getPath());
        }
        return socialShareData;
    }
    
    /**
     * Gets the AEM transformed URL of the given path using {@link LinkTransformerService}.
     *
     * @param path {@link String} the path
     * @return {@link String} the transformed URL
     */
    private String getTransformedUrl(String path) {
        if (path != null) {
            LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerServiceIfTransformerEnabled(repository);
            if(linkTransformerService != null)
                path = linkTransformerService.transformAEMUrl(path);
        }
        return path;
    }
    
}
