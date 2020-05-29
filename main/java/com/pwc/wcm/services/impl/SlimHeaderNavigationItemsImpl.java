/*
 * 
 */
package com.pwc.wcm.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pwc.AdminResourceResolver;
import com.pwc.model.HamburgerMenuLink;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.services.SlimHeaderNavigationItems;
import com.pwc.wcm.utils.JsonToMapConversion;

@Component(immediate = true, service = { SlimHeaderNavigationItems.class }, enabled = true)
public class SlimHeaderNavigationItemsImpl implements SlimHeaderNavigationItems {

	public final static String PRIMARY_NAVIGATION_URL = "primaryNavigationURL";
	public final static String SECONDARY_NAVIGATION_URL = "secondaryNavigationURL";
	public final static String PRIMARY_NAVIGATION_TEXT = "navText1";
	public final static String SECONDARY_NAVIGATION_TEXT = "navText2";
	public final static String NEW_WINDOW_TARGET_PROPERTY = "_blank";
	public final static String IN_PAGE_TARGET_PROPERTY = "_self";
	public final static String TARGET_PROPERTY_NAME_PRIMARY = "openNewWindow1";
	public final static String TARGET_PROPERTY_NAME_SECONDARY = "openNewWindow2";
	public final static String DEFAULT_ARGUMENT_VALUE = "emptyArgument";
	public final static String CONSTANT_PRIMARY_NAVIGATION = "primaryNavigation";
	public final static String CONSTANT_SECONDARY_NAVIGATION = "secondaryNavigation";
	public final static String CONSTANT_NESTED_PRIMARY_NAVIGATION = "allItems";
	public final static String NAVURL1="navURL1";
	public final static String NAVURL2="navURL2";

	
	@Reference
	private JsonToMapConversion jsonToMapConversionService;
	    
	@Reference
	private LinkTransformerServiceFactory linkTransformerServiceFactory;
	
	@Reference
	private SlingRepository repository;
	
	@Reference
	private AdminResourceResolver adminResourceResolver;
	

	private List<HamburgerMenuLink> generateNavLinks(final Resource resource, final String navType,
			final Page currentPage) {
		List<HamburgerMenuLink> hamburgerMenuLink = new ArrayList<HamburgerMenuLink>();
		final ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
		try {
		final ValueMap properties = resource.getValueMap();
		final Resource pageResource = currentPage.getContentResource();
		final InheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(pageResource);
		String navigationStrategy = null;
		String[] navArray = null;
			
		navigationStrategy = pageProperties.getInherited("navigationStrategy", "none");

		if("nestedPrimary".equals(navType) && "nestedNavigation".equals(navigationStrategy)){
			navArray = returnMultiFieldArray(navigationStrategy, pageProperties, properties,
					CONSTANT_NESTED_PRIMARY_NAVIGATION);
			try {
				return nestedNavigationGeneration(navArray, currentPage, resourceResolver);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if ("primary".equals(navType)) {
			navArray = returnMultiFieldArray(navigationStrategy, pageProperties, properties,
					CONSTANT_PRIMARY_NAVIGATION);

		} else if ("secondary".equals(navType)) {
			navArray = returnMultiFieldArray(navigationStrategy, pageProperties, properties,
					CONSTANT_SECONDARY_NAVIGATION);
		}
		if ("sibling-children".equals(navigationStrategy)) {
			if(showChildrenInNavigation(currentPage.getPath(),resourceResolver)){
				hamburgerMenuLink = autoGenerateNavigation(currentPage, navType);
			}else{
				hamburgerMenuLink = autoGenerateNavigation(currentPage.getParent(), navType);
			}
		} else {
			if (navArray != null) {
				hamburgerMenuLink = manualGenerateNavigation(navArray, navType, navigationStrategy, resourceResolver);
			}
		}
		} finally {
			if (resourceResolver != null && resourceResolver.isLive())
				resourceResolver.close();
		}
		return hamburgerMenuLink;
	}
	
	private boolean showChildrenInNavigation(String path, final ResourceResolver resourceResolver) {
		ValueMap prop = resourceResolver.resolve(path + "/jcr:content").adaptTo(ValueMap.class);
		Map<String, String> map = new HashMap<String, String>();
	    map.put("path", path);
	    map.put("property", "showNavigation");
	    map.put("property.value", "yes");
		QueryBuilder builder = resourceResolver.adaptTo(QueryBuilder.class);
		Query query = builder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
	    int result = query.getResult().getHits().size();
	    if("yes".equals(prop.get("showNavigation", String.class))){
	    	result--;
	    }
		return result == 0 ? false : true ;
	
	}

	private List<HamburgerMenuLink> nestedNavigationGeneration(final String[] navArray, final Page currentPage,
			final ResourceResolver resourceResolver) throws JSONException {

		String sortPrimary = (String) currentPage.getProperties().getOrDefault("enableorderprimary","false");
		String sortSecondary = (String) currentPage.getProperties().getOrDefault("enableordersecondary","false");
		final List<HamburgerMenuLink> hamburgerMenuLinkList = new ArrayList<HamburgerMenuLink>();
		HamburgerMenuLink hamburgerMenuLink = null;
		List<HamburgerMenuLink> secondaryNested = null;
		Page navPage = null, secNavPage = null;
		JsonArray arr = new Gson().fromJson("["+navArray[0]+"]", JsonArray.class);
		for (int i = 0; i < arr.size(); i++) {

			JsonObject object = (JsonObject) arr.get(i);
			String nestedPrimaryURL = object.get("primaryGroupurl").getAsString();
			navPage = (Page) resourceResolver.resolve(nestedPrimaryURL).adaptTo(Page.class);
			if(navPage != null && navPage.getProperties().get("showNavigation", "no").equals("yes")) {
				final String value = navPage.getNavigationTitle() != null ? navPage.getNavigationTitle()
						: navPage.getTitle();

				final String primaryTarget = object.get("primaryGroupTarget").getAsBoolean() ? NEW_WINDOW_TARGET_PROPERTY : IN_PAGE_TARGET_PROPERTY;
				String secondaryItems = object.get("items").getAsString();
				JsonArray secondaryNav = new Gson().fromJson("["+secondaryItems+"]", JsonArray.class);
				secondaryNested = new ArrayList<>();
				for(int j=0;j<secondaryNav.size();j++){
					JsonObject object1 = (JsonObject) secondaryNav.get(j);
					String nestedSecondaryURL = object1.get("secondaryItemurl").getAsString();
					secNavPage = (Page) resourceResolver.resolve(nestedSecondaryURL).adaptTo(Page.class);
					if(secNavPage != null && secNavPage.getProperties().get("showNavigation", "no").equals("yes"))
					{
						String val = secNavPage.getNavigationTitle() != null ? secNavPage.getNavigationTitle()
								: secNavPage.getTitle();
						String secondarytarget = object1.get("secondaryItemTarget").getAsBoolean() ? NEW_WINDOW_TARGET_PROPERTY : IN_PAGE_TARGET_PROPERTY;
						HamburgerMenuLink hamburgerMenuLink1 = new HamburgerMenuLink(getTransformedUrl(nestedSecondaryURL), val, secondarytarget, new ArrayList<HamburgerMenuLink>());
						secondaryNested.add(hamburgerMenuLink1);
					}
				}
				if(sortSecondary.equals("true")){
					Collections.sort(secondaryNested, new Comparator<HamburgerMenuLink>() {
						@Override
						public int compare(HamburgerMenuLink o1, HamburgerMenuLink o2) {
							return o1.getText().compareTo(o2.getText());
						}
					});
				}
				hamburgerMenuLink = new HamburgerMenuLink(getTransformedUrl(nestedPrimaryURL), value, primaryTarget, secondaryNested);
				hamburgerMenuLinkList.add(hamburgerMenuLink);
		}
		}
		if(sortPrimary.equals("true")){
			Collections.sort(hamburgerMenuLinkList, new Comparator<HamburgerMenuLink>() {
				@Override
				public int compare(HamburgerMenuLink o1, HamburgerMenuLink o2) {
					return o1.getText().compareTo(o2.getText());
				}
			});
		}
		return hamburgerMenuLinkList;
	}

	private String[] returnMultiFieldArray(final String navigationStrategy, final InheritanceValueMap pageProperties,
			final ValueMap properties, final String navigationType) {
		String[] navArray = null;
		if ("manual".equals(navigationStrategy)) {
			navArray = pageProperties.getInherited(navigationType, String[].class);
		} else {
			final String[] arr = properties.get(navigationType, String[].class);
			if (arr != null) {
				navArray = pageProperties.getInherited(navigationType, arr);
			} else {
				navArray = pageProperties.getInherited(navigationType, String[].class);
			}
		}
		return navArray;
	}

	private List<HamburgerMenuLink> autoGenerateNavigation(final Page currentPage, final String navType) {
		final List<HamburgerMenuLink> hamburgerMenuLinkList = new ArrayList<HamburgerMenuLink>();
		HamburgerMenuLink hamburgerMenuLink = null;
		List<HamburgerMenuLink> children = null;
		Page navPage = null;
		Iterator<Page> iterator = null;
		if ("primary".equals(navType)) {
			Page primaryPage = currentPage.getParent();
			if (primaryPage != null) {
				iterator = primaryPage.listChildren();
			}
		} else if ("secondary".equals(navType)) {
			iterator = currentPage.listChildren();
		}
		if(iterator != null){
			while (iterator.hasNext()) {
				children = new ArrayList<HamburgerMenuLink>();
				navPage = iterator.next();
				final String showInNav = navPage.getProperties().get("showNavigation", "no");
				if ("yes".equals(showInNav)) {
					final String value = navPage.getNavigationTitle() != null ? navPage.getNavigationTitle()
							: navPage.getTitle();
					if ("primary".equals(navType)) {
						Iterator<Page> childIterator = navPage.listChildren();
						while (childIterator.hasNext()) {
							Page childNavPage = childIterator.next();
							String showChildInNav = childNavPage.getProperties().get("showNavigation", "no");
							if ("yes".equals(showChildInNav)) {
								String val = childNavPage.getNavigationTitle() != null ? childNavPage.getNavigationTitle() : childNavPage.getTitle();
								children.add(new HamburgerMenuLink(getTransformedUrl(childNavPage.getPath()), val, IN_PAGE_TARGET_PROPERTY, new ArrayList<>()));
							}
						}
					}
		                        String path = getTransformedUrl(navPage.getPath());
					hamburgerMenuLink = new HamburgerMenuLink(path, value, IN_PAGE_TARGET_PROPERTY, children);
					hamburgerMenuLinkList.add(hamburgerMenuLink);
				}

			}
		}
		return hamburgerMenuLinkList;
	}

	private List<HamburgerMenuLink> manualGenerateNavigation(final String[] navArray, final String navType,
			final String navigationStrategy, final ResourceResolver resourceResolver) {
		if ("none".equals(navigationStrategy)) {
			return generateNavigationBasedOnNavType(navArray, navType, PRIMARY_NAVIGATION_TEXT,
					SECONDARY_NAVIGATION_TEXT, resourceResolver);
		} else { // navigationStrategy="manual"
			return generateNavigationBasedOnNavType(navArray, navType, DEFAULT_ARGUMENT_VALUE, DEFAULT_ARGUMENT_VALUE, resourceResolver);
		}
	}

	private List<HamburgerMenuLink> generateNavigationBasedOnNavType(final String[] navArray, final String navType,
			final String primaryNavText, final String secondaryNavText, final ResourceResolver resourceResolver) {
		if ("primary".equals(navType)) {
			return generateNavigation(navArray, PRIMARY_NAVIGATION_URL, TARGET_PROPERTY_NAME_PRIMARY, primaryNavText,
					"primary", resourceResolver);
		} else { // navType="secondary"
			return generateNavigation(navArray, SECONDARY_NAVIGATION_URL, TARGET_PROPERTY_NAME_SECONDARY,
					secondaryNavText, "secondary", resourceResolver);
		}
	}

	private List<HamburgerMenuLink> generateNavigation(final String[] navArray, final String urlType,
			final String targetPropertyName, final String textType, final String navType, final ResourceResolver resourceResolver) {
		final List<HamburgerMenuLink> hamburgerList = new ArrayList<HamburgerMenuLink>();
		Page navPage = null;
		String targetValue = "";
		HamburgerMenuLink hamburgerMenuLink = null;
		List<HamburgerMenuLink> children = null;
		String value = null;
		String path = null;
		Map<String, String> map = null;
		for (final String navigation : navArray) {
			children = new ArrayList<HamburgerMenuLink>();
			map = jsonToMapConversionService.convertJsonToMap(navigation);
			value = map.get(textType);
			path = map.get(urlType);
			if(path == null)
			{
				if("primary".equals(navType))
					path=map.get(NAVURL1);
				else 
					path=map.get(NAVURL2);
			}
			targetValue = (Boolean.parseBoolean(map.get(targetPropertyName)) ? NEW_WINDOW_TARGET_PROPERTY
					: IN_PAGE_TARGET_PROPERTY);
			navPage = resourceResolver.resolve(path).adaptTo(Page.class);
			if (DEFAULT_ARGUMENT_VALUE.equals(textType)) {
				if (navPage != null) {
					value = navPage.getNavigationTitle() != null ? navPage.getNavigationTitle() : navPage.getTitle();
				}
			}
			if (value != null && navPage != null && navPage.getProperties().get("showNavigation", "no").equals("yes")) {
				if ("primary".equals(navType)) {
					Iterator<Page> iterator = navPage.listChildren();
					while (iterator.hasNext()) {
						Page page = iterator.next();
						if(page.getProperties().get("showNavigation", "no").equals("yes")) {
						    String val = page.getNavigationTitle();
	                                                val=val!= null ? val : page.getTitle();
	                                                String pagePath = getTransformedUrl(page.getPath());
	                                                children.add(
	                                                                new HamburgerMenuLink(pagePath, val, IN_PAGE_TARGET_PROPERTY, new ArrayList<>()));
						}			
					}
				}
				path = getTransformedUrl(path);
				hamburgerMenuLink = new HamburgerMenuLink(path, value, targetValue, children);
				hamburgerList.add(hamburgerMenuLink);
			}
		}
		return hamburgerList;
	}

	@Override
	public Map<String, List<HamburgerMenuLink>> generateNavigationLinks(final Resource resource,
			final Page currentPage) {
		Map<String, List<HamburgerMenuLink>> map = new HashMap<String, List<HamburgerMenuLink>>();
		map.put("primary", generateNavLinks(resource, "primary", currentPage));
		map.put("secondary", generateNavLinks(resource, "secondary", currentPage));
		map.put("nestedPrimary", generateNavLinks(resource, "nestedPrimary", currentPage));
		return map;
	}
	
	/**
	 * Gets the AEM transformed URL of the given path using {@link LinkTransformerService}.
	 * 
	 * @param path {@link String} the path
	 * @return {@link String} the transformed URL
	 */
        private String getTransformedUrl(String path) {
            if (path != null) {
                LinkTransformerService linkTransformerService = linkTransformerServiceFactory
                        .getLinkTransformerServiceIfTransformerEnabled(repository);
                if (linkTransformerService != null)
                    path = linkTransformerService.transformAEMUrl(path);
            }
            return path;
        }
}

