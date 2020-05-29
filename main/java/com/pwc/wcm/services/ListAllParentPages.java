package com.pwc.wcm.services;

import java.util.Set;

public interface ListAllParentPages {
	/**
	 * Method to get set of all parent pages having sitemap_domain property.
	 * 
	 * @return returns the set of path list.
	 */
	Set<String> getPathOfAllParentPage();

}

