package com.pwc.wcm.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.user.Constants;
import com.pwc.user.model.UserProfile;
import com.pwc.user.services.UserInformationService;
import com.pwc.user.services.UserPagesPathConfigurationService;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.wcm.services.DestinationPagePathProviderService;
import com.pwc.wcm.utils.UrlSecurity;

@Component(immediate = true, service = { DestinationPagePathProviderService.class }, enabled = true)
public class DestinationPagePathProviderServiceImpl implements DestinationPagePathProviderService {
    
    private static final String PARENT_PAGE_PATH_QUERY_PARAM = "parentPagePath";
    private static final String PARENT_PAGE_PATH_DEFAULT = "/content/pwc/gx/en";
    private static final String CONTACT_US_NEW_PAGE_PATH = "/content/pwc/global/forms/contactUsNew";
    
    @Reference
    private UserPagesPathConfigurationService userPagesPathConfigurationService;
    
    @Reference
    private UserInformationService userInformationService;
    
    @Override
    public String getDestinationPagePath(String currentPagePath, SlingHttpServletRequest slingRequest) {
        String destinationPagePath = currentPagePath;
        if (currentPagePath.equals(CONTACT_US_NEW_PAGE_PATH) || currentPagePath.contains(Constants.USER_REG_PAGE_PREFIX)) {
            destinationPagePath = getParentPagePath(currentPagePath, slingRequest);
        }
        return destinationPagePath;
    }
    
    @Override
    public String getParentPagePath(String currentPagePath, SlingHttpServletRequest slingRequest) {
        String parentPagePath = null;
        if (userPagesPathConfigurationService.getSharedReadingListPagePath().contains(currentPagePath)) {
            String encodedEmail = slingRequest.getParameter("email");
            if (encodedEmail != null) {
                String email = UrlSecurity.decode(encodedEmail);
                email = email.replace(Constants.SHARED_READING_LIST_PRE_ENCODED_EMAIL_PREFIX, StringUtils.EMPTY);
                if (email != null) {
                    UserProfile userProfile = userInformationService.getUserProfileInformation(email);
                    if (userProfile != null) {
                        parentPagePath = UserRegistrationUtil.getParentPagePath(userProfile.getPreferredLocale());
                    }
                }
            }
        } else {
            parentPagePath = (String) slingRequest.getAttribute(PARENT_PAGE_PATH_QUERY_PARAM);
            if (parentPagePath == null) {
                parentPagePath = slingRequest.getParameter(PARENT_PAGE_PATH_QUERY_PARAM);
                if (parentPagePath == null)
                    parentPagePath = PARENT_PAGE_PATH_DEFAULT;
            }
        }
        if (parentPagePath == null)
            parentPagePath = PARENT_PAGE_PATH_DEFAULT;
        return parentPagePath;
    }
    
}
