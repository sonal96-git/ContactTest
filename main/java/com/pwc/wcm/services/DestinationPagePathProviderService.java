package com.pwc.wcm.services;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Provide methods for the destination path for the given currentPagePath. It handles specials cases like for contactUsNew and UserReg
 * pages.
 */
public interface DestinationPagePathProviderService {
    
    /**
     * Returns the destination path for the given currentPagePath. For some paths like contactUsNew page, parentPagePath parameter is
     * actually used to get the territory and language. If the currentPagePath is not equals to some of the defined paths like contactUsNew,
     * the currentPagePath is returned otherwise parentPagePath parameter value from request is used to return destination path.
     * 
     * @param currentPagePath {@link String}
     * @param slingRequest {@link SlingHttpServletRequest}
     * @return {@link String}
     */
    public String getDestinationPagePath(String CurrentPagePath, SlingHttpServletRequest slingRequest);
    
    /**
     * Returns the parentPagePath used for UserReg or ContactUsNew pages. It considers the special cases of Logged in user pages.
     * 
     * @param slingRequest {@link SlingHttpServletRequest}
     * @return {@link String}
     */
    public String getParentPagePath(String currentPagePath, SlingHttpServletRequest slingRequest);
}
