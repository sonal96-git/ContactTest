package com.pwc.user.services;

import org.apache.sling.api.SlingHttpServletRequest;

import com.pwc.user.model.UserProfile;

/**
 * The Interface ProfileInitializationService used for the Profile pages to set the common required attributes in the request as well as
 * used for overlay component.
 */
public interface ProfileInitializationService {
    
    /**
     * Initialize profile by setting the common required attributes like {@link UserProfile} in the {@link SlingHttpServletRequest} only if
     * the user is logged-in.
     *
     * @param request {@link SlingHttpServletRequest}
     * @return returns true if the user is logged-in otherwise false
     */
    public boolean initializeProfile(final SlingHttpServletRequest request);
    
    /**
     * Gets the absolute login path.
     *
     * @param redirectUrl {@link String} redirect URL added as the parameter in login path, '/' is used as default redirectUrl if the
     *            redirectUrl is null.
     * @param parentPagePath {@link String} parent page path like '/content/pwc/gx/en' added as the parameter in login path. If
     *            parentPagePath is null, default is used
     * @return {@link String}
     */
    public String getAbsoluteLoginPath(String redirectUrl, String parentPagePath);
    
    /**
     * Gets the login and register suffix for overlay.
     *
     * @param pagePath {@link String} the page path to which the user will be redirected after login/register
     * @param resourcePath {@link String} the resource path from which the parentPagePath has to be formed
     * @return {@link String} the login and register suffix for overlay
     */
    public String getLoginAndRegisterUrlSuffixForOverlay(String pagePath, String resourcePath);
    
}
