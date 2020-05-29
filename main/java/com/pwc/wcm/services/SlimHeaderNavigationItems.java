package com.pwc.wcm.services;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;
import com.pwc.model.HamburgerMenuLink;

public interface SlimHeaderNavigationItems {
	/**
	 * Returns a Map of navigation items for 'currentPage' for both primary and
	 * secondary navigation type , where 'resource' is used to fetch the values
	 * from the old component to ensure backward compatibility.
	 * 
	 * @param resource
	 * @param currentPage
	 * @return
	 */
	public Map<String, List<HamburgerMenuLink>> generateNavigationLinks(final Resource resource,
			final Page currentPage);
}