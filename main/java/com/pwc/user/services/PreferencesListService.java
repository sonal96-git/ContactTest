package com.pwc.user.services;

import java.util.Map;

import com.pwc.user.Constants;
import com.pwc.user.model.PreferenceOption;
import com.pwc.wcm.utils.I18nPwC;

/**
 * The Interface PreferencesListService used to get User Preferences.
 */
public interface PreferencesListService {
    
    /**
     * Gets the preference category to {@link PreferenceOption} {@link Map} for the given territory code. If the preferences are not present
     * for the given territory code, the preferences for {@value Constants#DEFAULT_TERRITORY} are returned. If territory Code is null, null
     * is returned.
     *
     * @param territoryCode {@link String}
     * @param i18n {@link I18nPwC} required to get the localized preference title for {@link PreferenceOption}
     * @return {@link Map}
     */
    public Map<String, PreferenceOption> getPreferencesMapByTerritory(final String territoryCode, final I18nPwC i18n);
    
    /**
     * Gets {@link PreferenceOption} for the given territory code and given category. If the preferences are not present for the given
     * territory code, the preferences for {@value Constants#DEFAULT_TERRITORY} are returned. If category or territory Code is null, null is
     * returned.
     *
     * @param category {@link String}
     * @param territoryCode {@link String}
     * @param i18n {@link I18nPwC} required to get the localized preference title for {@link PreferenceOption}
     * @return the preferences by territory and category
     */
    public PreferenceOption getPreferencesByTerritoryAndCategory(final String category, final String territoryCode, final I18nPwC i18n);
    
    /**
     * Gets the path where the preferences are stored.
     *
     * @return {@link String}
     */
    public String getUserPreferencesPath();
    
}
