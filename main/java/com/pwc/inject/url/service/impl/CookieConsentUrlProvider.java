package com.pwc.inject.url.service.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.inject.url.enums.TerritorySpecificUrlType;
import com.pwc.inject.url.service.TerritorySpecificUrlProvider;
import com.pwc.inject.url.service.TerritorySpecificUrlProviderFactory;
import com.pwc.model.Language;
import com.pwc.model.Microsite;
import com.pwc.model.Territory;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.CommonUtils;

@Component(immediate = true, service = { TerritorySpecificUrlProvider.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC Cookie Consent Script URL Provider Service",
        TerritorySpecificUrlProviderFactory.TERRITORY_SPECIFIC_URL_PROVIDER_PROPERTY + "=" + TerritorySpecificUrlType.Constants.COOKIE_CONSENT_VALUE
})
public class CookieConsentUrlProvider implements TerritorySpecificUrlProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieConsentUrlProvider.class);
    
	private Set<String> runModes = null;
	
    @Reference
    CountryTerritoryMapperService countryTerritoryMapper;
    
	@Reference
	SlingSettingsService slingSettingsService;
	
    /**
     * Returns the Cookie Consent Script's URL as per the following mentioned conditions:<br />
     * <ul>
     * <li>null if Global Territory</li>
     * <li>Value of 'cookieConsentUrl' property in reference data of current locale</li>
     * <li>Value of 'cookieConsentUrl' property in reference data of current Microsite belonging to current locale</li>
     * </ul>.
     * @param pagePath {@link String}
     * @return {@link String}
     */
    @Override
    public String getTerritorySpecificUrl(String pagePath) {
        LOGGER.info("CookieConsentServiceImpl.getCookieConsentScriptUrl: Cookie Consent URL retrieval begins for path: {}", pagePath);
        String territoryCode = CommonUtils.getCurrentPageTerritory(pagePath);
        String scriptUrl = null;
        if (isCookieConsentRequired(pagePath, territoryCode)) {
            Territory territory = countryTerritoryMapper.getTerritoryByTerritoryCode(territoryCode);
            if (territory == null) {
                LOGGER.debug("CookieConsentServiceImpl.getCookieConsentScriptUrl: Skipping Cookie Consent script handling "
                        + "as no territory mapping found in Ref Data for the territory code: {}", territoryCode);
            } else {
                String locale = CommonUtils.getPathLocale(pagePath);
                Language language = territory.getLocaleToLanguageMap().get(locale);
                return getCookieConsentUrlForPageAndLocale(pagePath, language, locale);
            }
        }
        return scriptUrl;
    }
    
    /**
     * Returns a boolean value after checking if the Cookie Consent Script is required to be included for a pagePath and territoryCode.
     * 
     * @param pagePath {@link String}
     * @param territoryCode {@link String}
     * @return {@link Boolean}
     */
    private Boolean isCookieConsentRequired(String pagePath, String territoryCode) {
        if (StringUtils.isBlank(pagePath) || StringUtils.isBlank(territoryCode)) {
            LOGGER.debug(
                    "CookieConsentServiceImpl.isCookieConsentRequired: Skipping Cookie Consent script handling as territory info not found for path: {}",
                    pagePath);
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Returns the Cookie Consent Script's URL stored on the given Microsite's node present in reference data under given locale.
     * 
     * @param pagePath {@link String}
     * @param language {@link Language}
     * @param locale {@link String}
     * @param micrositeName {@link String}
     * @return {@link String}
     */
	private String getMicrositeCookieConsentUrl(String pagePath, Language language, String locale, String micrositeName) {
		
        String scriptUrl = null;
        Map<String, Microsite> microsites = language.getMicrositeMap();
        if (microsites == null) {
            LOGGER.debug(
                    "CookieConsentServiceImpl.getCookieConsentScriptUrl: Skipping Cookie Consent script handling for as no Microsite nodes found in Reference data for locale '{}'",
                    locale);
        } else {
            Microsite microsite = microsites.get(micrositeName);
            if (microsite == null) {
                LOGGER.debug(
                        "CookieConsentServiceImpl.getCookieConsentScriptUrl: Skipping Cookie Consent script handling as the microsite named '{}' not found in Reference data for locale '{}'",
                        micrositeName, locale);
            } else {
				if (runModes != null && runModes.contains("publish") && runModes.contains("prod")) {
					scriptUrl = microsite.getCookieConsentUrl();
					LOGGER.info(
							"CookieConsentServiceImpl.getCookieConsentUrl: Cookie consent Stage URL for Microsite path '{}' is : {}",
							pagePath, scriptUrl);
				} else {
					scriptUrl = microsite.getCookieConsentStageUrl();
					
					LOGGER.info("CookieConsentServiceImpl.getCookieConsentStageUrl: Cookie consent URL for Microsite path '{}' is : {}",
                        pagePath, scriptUrl);
				}
            }
        }
        return scriptUrl;
    }
    
    /**
     * Returns the Cookie Consent Script's URL for the given pagePath and locale info.
     * 
     * @param pagePath {@link String}
     * @param language {@link Language}
     * @param locale {@link String}
     * @return {@link String}
     */
    private String getCookieConsentUrlForPageAndLocale(String pagePath, Language language, String locale) {
        String scriptUrl = null;
		runModes = slingSettingsService.getRunModes();
        if (language == null) {
            LOGGER.debug(
                    "CookieConsentServiceImpl.getCookieConsentScriptUrl: Skipping Cookie Consent script handling as the locale '{}' not found in reference data.",
                    locale);
        } else {
            String micrositeName = CommonUtils.getMicrositeNameFromPagePath(pagePath);
			if (StringUtils.isNotBlank(micrositeName)) {
				return getMicrositeCookieConsentUrl(pagePath, language, locale, micrositeName);
			} else if (runModes != null && runModes.contains("publish") && runModes.contains("prod")) {
				scriptUrl = language.getCookieConsentUrl();
				LOGGER.info("CookieConsentServiceImpl.getCookieConsentStageUrl: Cookie consent URL for path '{}' is : {}", pagePath,
						scriptUrl);
			}
			else {
				scriptUrl = language.getCookieConsentStageUrl();
				LOGGER.info("CookieConsentServiceImpl.getCookieConsentScriptUrl: Cookie consent Stage URL for path '{}' is : {}", pagePath,
                        scriptUrl);
			}
        }
        return scriptUrl;
    }
    
}
