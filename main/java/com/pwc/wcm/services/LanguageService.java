package com.pwc.wcm.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONObject;

import com.day.cq.wcm.api.Page;


/**
 * Provides information related to alternate languages
 */
public interface LanguageService {
    /**
     * Method Provides language Selector data as Json Object
     *
     * @param page
     * @param locale
     * @param request
     * @return JSONObject
     */
    JSONObject getLanguageSelectorData(Page page, String locale, SlingHttpServletRequest request);

}
