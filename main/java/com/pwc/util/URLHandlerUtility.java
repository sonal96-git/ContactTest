package com.pwc.util;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.wcm.api.Page;

/**
 * Utility To handle Internal URLs.
 */
public class URLHandlerUtility {
    
    /**
     * Checks if the given URL is an internal URL or not.
     *
     * @param url {@link String}
     * @param resolver {@link ResourceResolver}
     * @return {@link boolean}
     */
    public static boolean isInternalURL(final String url, final ResourceResolver resolver) {
	final Resource resource = (resolver != null) ? resolver.getResource(url) : null;
	return (resource != null && resource.adaptTo(Page.class) != null);
    }
    
    /**
     * Adds ".html" to the given URL if its an internal Page.
     *
     * @param url {@link String}
     * @param resolver {@link ResourceResolver}
     * @return {@link String}
     */
    public static String handleURLForInternalLinks(final String url, final ResourceResolver resolver) {
	return (isInternalURL(url, resolver)) ? url + ".html" : url;
    }
}
