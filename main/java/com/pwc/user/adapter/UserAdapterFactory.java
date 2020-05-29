package com.pwc.user.adapter;

import javax.servlet.http.Cookie;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.user.Constants;
import com.pwc.user.model.User;
import com.pwc.user.model.UserProfile;
import com.pwc.user.services.PreferencesListService;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.UrlSecurity;

/**
 * Adapter to map request to {@link User}.
 */
@Component(immediate = true, service = { AdapterFactory.class },
property = {AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.SlingHttpServletRequest",
        AdapterFactory.ADAPTER_CLASSES + "=com.pwc.user.model.User"
})
public class UserAdapterFactory implements AdapterFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAdapterFactory.class);

    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @Reference
    private PreferencesListService preferencesListService;
    
    @Override
    public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> typeClass) {
        if (adaptable instanceof SlingHttpServletRequest && typeClass.equals(User.class))
            return typeClass.cast(adaptRequestToUser((SlingHttpServletRequest) adaptable));
        return null;
    }
    
    /**
     * It adapts the {@link SlingHttpServletRequest} to the {@link User} Object.
     * 
     * @param slingRequest
     * @return {@link User}
     */
    private Object adaptRequestToUser(final SlingHttpServletRequest slingRequest) {
        User user = new User(false, null, false);
        Cookie pwcCookie = UserRegistrationUtil.getPwCCookie(slingRequest, Constants.COOKIE_AUTH_PWC_ID);
        Cookie userIdCookie = UserRegistrationUtil.getPwCCookie(slingRequest, Constants.COOKIE_AUTH_USER_ID);
        if (pwcCookie != null) {
            String email = UrlSecurity.decode(pwcCookie.getValue());
            if (email != null) {
                ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
                UserProfileAdapter userProfileAdapter =new UserProfileAdapter();
                final UserProfile userProfile = null == userIdCookie ? userProfileAdapter.adaptEmailtoUserProfile(email, resourceResolver,
                        countryTerritoryMapperService, preferencesListService) : userProfileAdapter.adaptUserIDtoUserProfile(UrlSecurity.decode(userIdCookie.getValue()), resourceResolver,
                        countryTerritoryMapperService, preferencesListService);
                if (userProfile != null) {
                    user.setUserLoggedIn(true);
                    user.setUserProfile(userProfile);
                    user.setInternalUserLogIn(userProfile.isInternalUser());
                }
                resourceResolver.close();
            }
        }
        LOGGER.debug("UserAdapterFactory : adaptRequestToUser() : User is {} ", user);
        return user;
    }
    
}
