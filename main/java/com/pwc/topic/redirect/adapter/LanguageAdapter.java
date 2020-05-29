package com.pwc.topic.redirect.adapter;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.topic.redirect.model.Language;
import com.pwc.wcm.utils.LocaleUtils;

/**
 * Adapter to map the properties of a {@link Resource} to a {@link Language}.
 */
public class LanguageAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageAdapter.class);
    private static final String PROP_TOPIC_HOME_PAGE_URL = "homepageURL";
    private static final String PROP_TOPIC_HOME_PAGE_PATH = "homepagePath";
    
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
            final String name = languageRes.getName();
            final String path = languageRes.getPath();
            final String locale = getLocaleForLanguageResource(languageRes);
            final String topicHomePageUrl = properties.get(PROP_TOPIC_HOME_PAGE_URL, "");
            final String topicHomePagePath = properties.get(PROP_TOPIC_HOME_PAGE_PATH, "");
            language = new Language(locale, path, name, topicHomePageUrl, topicHomePagePath);
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
}
