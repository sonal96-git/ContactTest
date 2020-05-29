package com.pwc.topic.redirect.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.topic.redirect.model.Language;
import com.pwc.topic.redirect.model.Territory;

/**
 * Adapter to map the properties of a {@link Resource} to a {@link Territory}.
 */
public class TerritoryAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TerritoryAdapter.class);
    private static final String PROP_DEFAULT_LOCALE = "defaultLocale";
    
    /**
     * Returns a new {@link Territory} object after mapping the required properties of a Resource.
     *
     * @param territoryRes {@link Resource} The properties of this resource will be added to the Territory
     * @return {@link Territory}
     */
    public Territory adaptResourceToTerritoryTerritory(final Resource territoryRes) {
        Territory territory = null;
        if (territoryRes != null) {
            final ValueMap properties = territoryRes.getValueMap();
            final String code = territoryRes.getName();
            final String path = territoryRes.getPath();
            final String defaultLocale = properties.get(PROP_DEFAULT_LOCALE, "");
            final Map<String, Language> localeToLanguageMappings = getLocaleToLanguageMappingsForTerritory(territoryRes);
            territory = new Territory(code, path, defaultLocale, localeToLanguageMappings);
            LOGGER.debug("Adapting resource at path {} to Territory: {}", path, territory.toString());
        }
        return territory;
    }
    
    /**
     * Returns a mapping of Locale to {@link Language} corresponding to the provided Territory resource.
     *
     * @param territoryRes {@link Resource}
     * @return {@link Map} Map of Language codes to corresponding properties
     */
    private Map<String, Language> getLocaleToLanguageMappingsForTerritory(final Resource territoryRes) {
        final Map<String, Language> languages = new HashMap<String, Language>();
        for (final Resource langRes : territoryRes.getChildren()) {
            final Language languageProperties = langRes.adaptTo(Language.class);
            languages.put(languageProperties.getLocale(), languageProperties);
        }
        return languages;
    }
}
