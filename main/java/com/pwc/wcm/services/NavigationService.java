package com.pwc.wcm.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONObject;

import com.day.cq.wcm.api.Page;

/**
 * Provides information related to navigation bar, which includes  
 * 1. territory selector data
 * 2. language selector data
 * 3. social share links
 * 4. hamburger data
 * 5. breadcrumb data
 */
public interface NavigationService {
    /**
     * Provides data needed in navigation, which includes:
     * 1. territory selector data
     * 2. language selector data
     * 3. social share links
     * 4. hamburger data
     * 5. breadcrumb data
     *
     * @param page   current page
     * @param locale locale of current page
     * @return Json Object
     */
    public JSONObject getNavigationData(Page page, String locale, SlingHttpServletRequest request);

}
