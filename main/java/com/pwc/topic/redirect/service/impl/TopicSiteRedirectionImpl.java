package com.pwc.topic.redirect.service.impl;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.Territory;
import com.pwc.topic.redirect.model.RedirectInfo;
import com.pwc.topic.redirect.service.TopicSiteRedirection;
import com.pwc.topic.redirect.service.TopicTerritoryMapper;
import com.pwc.user.model.User;
import com.pwc.user.model.UserProfile;
import com.pwc.user.services.UserInformationService;
import com.pwc.util.AkamaiUtils;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.LocaleUtils;

@Component(immediate = true, service = { TopicSiteRedirection.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Provides information for redirection of Topic Sites" })
public class TopicSiteRedirectionImpl implements TopicSiteRedirection {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicSiteRedirection.class);
    private static final String GLOBAL_TERRITORY_CODE = "gx";
    private static final String GLOBAL_TOPIC_TERRITORY_CODE = "gx";
    private static final String LOG_MSG_PREFIX = "Topic Site Redirect: ";
    private static final String NOTIFICATION_MSG_I18N_KEY = "TopicSite_NotificationMsg";
    private static final String POPUP_MSG_I18N_KEY = "TopicSite_PopupMsg";
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @Reference
    private TopicTerritoryMapper topicTerritoryMapper;
    
    @Reference
    private UserInformationService userInformationService;
    
    @Override
    public RedirectInfo getTopicSiteRedirectionInfo(final SlingHttpServletRequest request, final String currentPagePath, final String topicType,
            final String topicHomePagePath, final String topicSiteTitle) {
        final RedirectInfo redirectInfo = new RedirectInfo();
        final String currentPageTerritory = CommonUtils.getCurrentPageTerritory(currentPagePath);
        final String currentPageLanguage = CommonUtils.getCurrentPageLanguage(currentPagePath);
        if (StringUtils.isNotBlank(topicType) && StringUtils.isNotBlank(topicHomePagePath) && currentPageTerritory != null
                && isCurrentPagePartOfTopicSites(currentPagePath)) {
            LOGGER.debug(LOG_MSG_PREFIX + "Redirect processing begins for the page: {} and topic: {}, having topic homepage: " //
                    + topicHomePagePath, currentPagePath, topicType);
            final User currentUser = userInformationService.getUser(request);
            final boolean isUserLoggedIn = currentUser != null && currentUser.isUserLoggedIn();
            final DestinationTerritory destTerritory = isUserLoggedIn
                    ? getDestinationTerritoryInfoForCurrentUser(currentUser.getUserProfile(), currentPageTerritory, topicType)
                    : getDestinationTerritoryInfoForAnnonymousUser(getCurrentUserLocation(request), currentPageTerritory, topicType);
            boolean alreadyInDestTerritory = destTerritory != null && currentPageTerritory.equals(destTerritory.code);
            if (alreadyInDestTerritory) {
                redirectInfo.setDestTerritoryCode(currentPageTerritory);
                LOGGER.info(LOG_MSG_PREFIX+"User is already in correct Territory! No redirection required!!");
            }
            if (destTerritory != null && StringUtils.isNotBlank(destTerritory.homepageUrl) && !alreadyInDestTerritory)
                return updateRedirectInfo(request, currentPagePath, topicHomePagePath, topicSiteTitle, currentPageTerritory, currentPageLanguage,
                        destTerritory.homepageUrl, destTerritory.code, redirectInfo);
        }
        return redirectInfo;
    }
    
    /**
     * Prepares a {@link RedirectInfo} response object.
     * 
     * @param request {@link SlingHttpServletRequest}
     * @param currentPagePath {@link String}
     * @param topicHomePagePath {@link String}
     * @param topicSiteTitle {@link String}
     * @param currentPageTerritory {@link String}
     * @param currentPageLanguage {@link String}
     * @param homepageUrl {@link String}
     * @param destTerritoryCode {@link String}
     * @param redirectInfo {@link RedirectInfo}
     * @return {@link RedirectInfo}
     */
    private RedirectInfo updateRedirectInfo(final SlingHttpServletRequest request, final String currentPagePath, final String topicHomePagePath,
            final String topicSiteTitle, final String currentPageTerritory, final String currentPageLanguage, final String homepageUrl,
            final String destTerritoryCode, final RedirectInfo redirectInfo) {
        redirectInfo.setRedirectionRequired(true);
        LOGGER.debug(LOG_MSG_PREFIX + "Destination Territory Code: {}", destTerritoryCode);
        if (currentPagePath.equals(topicHomePagePath) && currentPageTerritory.equals(GLOBAL_TERRITORY_CODE)) {
            LOGGER.debug(
                    LOG_MSG_PREFIX + "Server-side Topic redirection detected! Page will be redirected to the '{}' territory for the URL: {}",
                    destTerritoryCode, homepageUrl);
            redirectInfo.setServerRedirectionUrl(homepageUrl);
        } else {
            redirectInfo.setSrcTerritoryCode(currentPageTerritory);
            redirectInfo.setDestTerritoryCode(destTerritoryCode);
            redirectInfo.setClientRedirectionUrl(homepageUrl);
            redirectInfo.setSrcTerritoryTitle(getTerritoryNameFromTerritoryCode(currentPageTerritory));
            redirectInfo.setDestTerritoryTitle(getTerritoryNameFromTerritoryCode(destTerritoryCode));
            redirectInfo.setEnablePopupMsg(currentPagePath.equals(topicHomePagePath));
            setRedirectionTranslatedMessages(request, currentPageTerritory, currentPageLanguage, topicSiteTitle, redirectInfo);
            LOGGER.debug(LOG_MSG_PREFIX + "Client-side Topic redirection found!\n {}", redirectInfo.toString());
        }
        return redirectInfo;
    }
    
    /**
     * Adds the translated redirection messages in the response.
     * 
     * @param request {@link SlingHttpServletRequest}
     * @param currentPageTerritory {@link String}
     * @param currentPageLanguage {@link String}
     * @param topicSiteTitle {@link String}
     * @param redirectInfo {@link RedirectInfo}
     */
    private void setRedirectionTranslatedMessages(final SlingHttpServletRequest request, final String currentPageTerritory,
            final String currentPageLanguage, final String topicSiteTitle, final RedirectInfo redirectInfo) {
        final Locale locale = new Locale(LocaleUtils.getLocale(currentPageTerritory, currentPageLanguage));
        final I18nPwC i18n = new I18nPwC(request, request.getResourceBundle(locale));
        redirectInfo.setSrcTerritoryTitle(getTranslatedText(i18n, redirectInfo.getSrcTerritoryTitle()));
        redirectInfo.setDestTerritoryTitle(getTranslatedText(i18n, redirectInfo.getDestTerritoryTitle()));
        redirectInfo.setTopicTitle(getTranslatedText(i18n, topicSiteTitle));
        redirectInfo.setNotificationMsg(getTranslatedText(i18n, NOTIFICATION_MSG_I18N_KEY));
        redirectInfo.setPopupMsg(getTranslatedText(i18n, POPUP_MSG_I18N_KEY));
    }
    
    /**
     * Returns the translated message for the given key if the key isn't blank.
     * 
     * @param i18n {@link I18nPwC} object for the required locale
     * @param key {@link String} for which the translation is required
     * @return {@link String} Translated message if key isn't empty, else empty string
     */
    private String getTranslatedText(final I18nPwC i18n, final String key) {
        return StringUtils.isBlank(key) ? "" : i18n.getPwC(key);
    }
    
    /**
     * Returns true if given territories don't match.
     * 
     * @param currentPageTerritory {@link String}
     * @param userTerritory {@link String}
     * @return boolean
     */
    private boolean isRedirectionRequired(final String currentPageTerritory, final String userTerritory) {
        return !userTerritory.startsWith(currentPageTerritory);
    }
    
    /**
     * Returns the {@link DestinationTerritory} to which the redirection will occur, corresponding to the user preferred locale, if present,
     * else the default locale for the territory.
     * 
     * @param userProfile {@link UserProfile}
     * @param currentPageTerritory {@link String}
     * @param topicType {@link String}
     * @return {@link DestinationTerritory} if user preferred territory found and is different from current territory, else null
     */
    private DestinationTerritory getDestinationTerritoryInfoForCurrentUser(final UserProfile userProfile, final String currentPageTerritory,
            final String topicType) {
        String userTerritoryCode = userProfile.getTerritory();
        if (StringUtils.isBlank(userTerritoryCode)) { // For old profiles when country was equal to territory
            userTerritoryCode = userProfile.getCountry();
        }
        
        if (isRedirectionRequired(currentPageTerritory, userTerritoryCode)) {
            final String userLocale = userProfile.getPreferredLocale();
            LOGGER.debug(LOG_MSG_PREFIX + "Logged in User found of territory: {} and locale: {}", userTerritoryCode, userLocale);
            return StringUtils.isBlank(userLocale) ? getDestinationTerritoryInfoForTopicAndTerritory(topicType, userTerritoryCode)
                    : getDestinationTerritoryInfoForTopicTerritoryAndLocale(topicType, userTerritoryCode, userLocale);
        } else {
            return StringUtils.isBlank(userTerritoryCode) ? null : new DestinationTerritory(currentPageTerritory, null);
        }
    }
    
    /**
     * Returns the {@link DestinationTerritory} to which the redirection will occur, corresponding to the user's location.
     * 
     * @param currentCountry {@link String} User's current country
     * @param currentPageTerritory {@link String} Territory code for the requested page
     * @param topicType {@link String}
     * @return {@link DestinationTerritory} if user location found and is different from current territory, else null
     */
    private DestinationTerritory getDestinationTerritoryInfoForAnnonymousUser(final String currentCountry, final String currentPageTerritory,
            final String topicType) {
        if (StringUtils.isBlank(currentCountry)) {
            LOGGER.warn(LOG_MSG_PREFIX + "Failed to fetch User's location! Skipping topic site redirection.");
        } else {
            final Territory userTerritory = countryTerritoryMapperService.getTerritoryByCountry(currentCountry);
            if (userTerritory == null) {
                LOGGER.warn(LOG_MSG_PREFIX + "No Territory Mapping found for the user's current country code: " + currentCountry
                        + "! Skipping topic site redirection.");
            } else {
                final String userTerritoryCode = userTerritory.getTerritoryCode();
                if (isRedirectionRequired(currentPageTerritory, userTerritoryCode)) {
                    LOGGER.debug(LOG_MSG_PREFIX + "Annonymous User found of territory: {}", userTerritoryCode);
                    return getDestinationTerritoryInfoForTopicAndTerritory(topicType, userTerritoryCode);
                } else {
                    return StringUtils.isBlank(userTerritoryCode) ? null : new DestinationTerritory(currentPageTerritory, null);
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the {@link DestinationTerritory} information for the given topic and territory.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @return {@link DestinationTerritory}
     */
    private DestinationTerritory getDestinationTerritoryInfoForTopicAndTerritory(final String topicType, final String territoryCode) {
        String defaultHomepageUrl = topicTerritoryMapper.getDefaultHomepageUrlForTopicAndTerritory(topicType, territoryCode);
        if (StringUtils.isBlank(defaultHomepageUrl)) {
            LOGGER.debug(LOG_MSG_PREFIX + "Default Homepage URL not found! Fetching the Homepage URL for global territory: {}",
                    GLOBAL_TOPIC_TERRITORY_CODE);
            defaultHomepageUrl = topicTerritoryMapper.getDefaultHomepageUrlForTopicAndTerritory(topicType, GLOBAL_TOPIC_TERRITORY_CODE);
            return new DestinationTerritory(GLOBAL_TOPIC_TERRITORY_CODE, defaultHomepageUrl);
        }
        return new DestinationTerritory(territoryCode, defaultHomepageUrl);
    }
    
    /**
     * Returns the {@link DestinationTerritory} information for the given topic, territory and locale.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @param locale {@link String}
     * @return {@link DestinationTerritory}
     */
    private DestinationTerritory getDestinationTerritoryInfoForTopicTerritoryAndLocale(final String topicType, final String territoryCode,
            final String locale) {
        final String homePageUrl = topicTerritoryMapper.getTopicHomepageUrlForTerritoryAndLocale(topicType, territoryCode, locale);
        if (StringUtils.isBlank(homePageUrl)) {
            LOGGER.debug(LOG_MSG_PREFIX + "Homepage URL not found for the topic: {}, territory: {}, locale: " + locale, topicType,
                    territoryCode);
            LOGGER.debug(LOG_MSG_PREFIX + "Fetching Homepage URL for default locale.");
            return getDestinationTerritoryInfoForTopicAndTerritory(topicType, territoryCode);
        }
        return new DestinationTerritory(territoryCode, homePageUrl);
    }
    
    /**
     * Fetches the user location and returns its corresponding PwC territory.
     * 
     * @param request {@link SlingHttpServletRequest}
     * @return String
     */
    private String getCurrentUserLocation(final SlingHttpServletRequest request) {
        final String countryCode = AkamaiUtils.getLocationFromQueryParam(request);
        return StringUtils.isBlank(countryCode) ? AkamaiUtils.getLocationFromAkamaiRequestHeader(request) : countryCode;
    }
    
    /**
     * Returns the territory name corresponding to the passed territory code.
     * 
     * @param territoryCode {@link String}
     * @return {@link String}
     */
    private String getTerritoryNameFromTerritoryCode(final String territoryCode) {
        final Territory territory = countryTerritoryMapperService.getTerritoryByTerritoryCode(territoryCode);
        final String territoryName = territory.getTerritoryI18nKey();
        return StringUtils.isBlank(territoryName) ? territoryCode.toUpperCase() : territoryName;
    }
    
    /**
     * Returns true if current page is part of any topic site.
     * 
     * @param pagePath {@link String}
     * @return boolean
     */
    private boolean isCurrentPagePartOfTopicSites(String pagePath) {
        return topicTerritoryMapper.getTopicSiteOfPage(pagePath) != null;
    }
    
    /**
     * Private model to share destination territory related data among methods.
     */
    private class DestinationTerritory {
        String code;
        String homepageUrl;
        
        public DestinationTerritory(final String code, final String homepageUrl) {
            this.code = code;
            this.homepageUrl = homepageUrl;
        }
    }
}
