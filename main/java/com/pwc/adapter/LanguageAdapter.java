package com.pwc.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.Language;
import com.pwc.model.Microsite;
import com.pwc.wcm.utils.LocaleUtils;

/**
 * Adapter to map the properties of a {@link Resource} to a {@link Language}.
 */
public class LanguageAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageAdapter.class);
    private static final String PROP_LANGUAGE_TITLE = "languageTitle";
    private static final String PROP_COOKIE_CONSENT_URL = "cookieConsentUrl";
	private static final String PROP_COOKIE_CONSENT_STAGE_URL = "cookieConsentStageUrl";
    private static final String REF_DATA_MICROSITE_ROOT_NODE_NAME = "website";
    
    /**
     * Returns a new {@link Language} object after mapping the required properties of a Resource.
     *
     * @param languageRes {@link Resource} The properties of this resource will be added to the Language
     * @return {@link Language}
     */
    public Language adaptResourceToLanguage(final Resource languageRes) {
        Language language = null;
        if (languageRes != null) {
            final ValueMap properties = languageRes.getValueMap();
            final String code = languageRes.getName();
            final String path = languageRes.getPath();
            final String locale = getLocaleForLanguageResource(languageRes);
            final String langTitle = properties.get(PROP_LANGUAGE_TITLE, code);
            final String cookieConsentUrl = properties.get(PROP_COOKIE_CONSENT_URL, "");
			final String cookieConsentStageUrl = properties.get(PROP_COOKIE_CONSENT_STAGE_URL, "");
			language = new Language(langTitle, code, path, cookieConsentUrl, cookieConsentStageUrl, locale,
                    getMicrositeMap(languageRes.getChild(REF_DATA_MICROSITE_ROOT_NODE_NAME)));
            LOGGER.debug("Adapting resource at path {} to Language: {}", path, language.toString());
        }
        return language;
    }
    
    /**
     * Returns Locale by appending the names of provided Language resource and its parent Territory resource.
     * 
     * @param languageRes {@link String}
     * @return {@link String}
     */
    private String getLocaleForLanguageResource(Resource languageRes) {
        String languageCode = languageRes.getName();
        String territoryCode = languageRes.getParent().getName();
        return LocaleUtils.getLocale(territoryCode, languageCode);
    }
    
    /**
     * Returns a map of the name of the Microsites and the {@link Microsite} Info object.
     * 
     * @param websiteRes {@link Resource}
     * @return {@link Map}
     */
    private Map<String, Microsite> getMicrositeMap(final Resource websiteRes) {
        Map<String, Microsite> micrositeMap = null;
        if (websiteRes != null) {
            micrositeMap = new HashMap<String, Microsite>();
            for (final Resource micrositeRes : websiteRes.getChildren()) {
                micrositeMap.put(micrositeRes.getName(), micrositeRes.adaptTo(Microsite.class));
            }
        }
        return micrositeMap;
    }
}
