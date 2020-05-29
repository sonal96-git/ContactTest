package com.pwc.user.services.impl;

import java.util.Locale;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.user.Constants;
import com.pwc.user.model.User;
import com.pwc.user.model.UserProfile;
import com.pwc.user.services.ProfileInitializationService;
import com.pwc.user.services.UserInformationService;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.LocaleUtils;
import com.pwc.wcm.utils.UrlSecurity;

@Component(immediate = true, service = ProfileInitializationService.class)
public class ProfileInitializationServiceImpl implements ProfileInitializationService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileInitializationService.class);
    private static final String USER_ATTRIBUTE = "user";
    private static final String LOCALE_ATTRIBUTE = "locale";
    private static final String I18N_ATTRIBUTE = "i18n";
    private static final String REFERER_ATTRIBUTE = "referrer";
    private static final String PARENT_PAGE_PATH_ATTRIBUTE = "parentPagePath";
    private static final String LOGIN_PATH = "/content/pwc/userReg/login.html?redirectUrl=";
    private static final String DEFAULT_PARENT_PAGE_PATH = "/content/pwc/gx/en";
    
    @Reference
    private UserInformationService userInformationService;
    
    @Reference
    private LinkTransformerServiceFactory linkTransformerServiceFactory;
    
    @Reference
    SlingRepository repository;
    
    @Override
    public boolean initializeProfile(final SlingHttpServletRequest request) {
        boolean isUserLoggedIn = false;
        if (request != null) {
            User user = userInformationService.getUser(request);
            request.setAttribute(USER_ATTRIBUTE, user);
            isUserLoggedIn = user.isUserLoggedIn();
            UserProfile userProfile = user.getUserProfile();
            String referer = request.getHeader("Referer");
            if (referer == null || (referer != null && referer.contains(Constants.USER_REG_PAGE_PREFIX))) {
                referer = request.getParameter("referer");
                if (referer != null)
                    referer = UrlSecurity.decode(referer);
            }
            if (user.isUserLoggedIn() && userProfile != null) {
                
                String locale = userProfile.getPreferredLocale();
                request.setAttribute(LOCALE_ATTRIBUTE, locale);
                
                Locale pageLang = new Locale(locale);
                I18nPwC i18n = new I18nPwC(request, request.getResourceBundle(pageLang));
                request.setAttribute(I18N_ATTRIBUTE, i18n);
                
                request.setAttribute(REFERER_ATTRIBUTE, referer);
                
                String parentPagePath = UserRegistrationUtil.getParentPagePath(userProfile.getTerritory(), LocaleUtils.getLanguageFromLocale(locale));
                request.setAttribute(PARENT_PAGE_PATH_ATTRIBUTE, parentPagePath);
            }
        }
        return isUserLoggedIn;
    }
    
    @Override
    public String getAbsoluteLoginPath(String redirectUrl, String parentPagePath) {
        String absoluteLoginPagePath = LOGIN_PATH;
        if (parentPagePath == null)
            parentPagePath = DEFAULT_PARENT_PAGE_PATH;
        if (redirectUrl == null)
            redirectUrl = "/";
        LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerServiceIfTransformerEnabled(repository);
        if(linkTransformerService != null)
            absoluteLoginPagePath = linkTransformerService.transformAEMUrl(absoluteLoginPagePath);
        absoluteLoginPagePath = absoluteLoginPagePath + UrlSecurity.encode(redirectUrl) + "&parentPagePath="
                + parentPagePath;
        LOGGER.debug("ProfileInitializationService : getAbsoluteLoginPath() : Absolute Page path returned is {} ", absoluteLoginPagePath);
        return absoluteLoginPagePath;
    }
    
    @Override
    public String getLoginAndRegisterUrlSuffixForOverlay(String pagePath, String resourcePath) {
        String loginAndRegisterSuffixForOverlay = ".html";
        LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerServiceIfTransformerEnabled(repository);
        if(linkTransformerService != null)
            pagePath = linkTransformerService.transformAEMUrl(pagePath);
        pagePath = UrlSecurity.encode(pagePath);
        loginAndRegisterSuffixForOverlay = ".html?redirectUrl=" + pagePath + "&referrer=" + pagePath + "&parentPagePath="
                + UserRegistrationUtil.getParentPagePath(CommonUtils.getCurrentPageTerritory(resourcePath), CommonUtils.getCurrentPageLanguage(resourcePath));
        return loginAndRegisterSuffixForOverlay;
    }
    
}
