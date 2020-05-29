package com.pwc.user.components;

import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.pwc.user.services.ProfileInitializationService;
import com.pwc.wcm.utils.I18nPwC;

/**
 * InitProfile extends the {@link WCMUsePojo} and gets the user information and other information like parentPagePath and set this
 * information along with {@link I18nPwC} object in the request attributes so that the other components can use it. It is also responsible
 * to redirect the user to Login page if the user is not logged in.
 */
public class InitProfile extends WCMUsePojo {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InitProfile.class);
    
    private boolean isUserLoggedIn;
    
    @Override
    public void activate() throws Exception {
        // Adding headers to disable catching for account pages from Akamai and Dispatcher
        getResponse().setHeader("Dispatcher", "no-cache");
        getResponse().setHeader("Edge-control", "no-store");
        SlingHttpServletRequest slingRequest = getRequest();
        ProfileInitializationService profileInitializationService = (ProfileInitializationService) getSlingScriptHelper()
                .getService(ProfileInitializationService.class);
        this.isUserLoggedIn = profileInitializationService.initializeProfile(slingRequest);
        
        if (!isUserLoggedIn) {
            LOGGER.debug("InitProfile : activate() : redirecting to Login page since user is not logged in");
            String loginPagePath = profileInitializationService.getAbsoluteLoginPath(slingRequest.getRequestURI(),
                    slingRequest.getParameter("parentPagePath"));
            getResponse().sendRedirect(loginPagePath);
        }
    }
    
    /**
     * Returns if the current user is logged in or not.
     * 
     * @return returns true if user is logged in
     */
    public boolean getIsUserLoggedIn() {
        return this.isUserLoggedIn;
    }
    
}
