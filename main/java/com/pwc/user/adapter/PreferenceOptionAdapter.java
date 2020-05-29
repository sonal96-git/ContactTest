package com.pwc.user.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.pwc.user.model.PreferenceOption;
import com.pwc.wcm.utils.I18nPwC;

/**
 * Provides various methods to get the preferences list in the form of {@link PreferenceOption} or {@link Map} of preference category to
 * {@link PreferenceOption} from the preferences page.
 */
public class PreferenceOptionAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceOptionAdapter.class);
    private static final String I18N_KEY_PROPERTY = "i18nKey";
    private static final String SECOND_TIER_DESIGN_PROPERTY = "secondDesign";
    private static final String SECOND_TIER_DESIGN_DEFAULT_VALUE = "checkbox";
    
    /**
     * It adapts the user's preference page to {@link PreferenceOption}.
     * 
     * @param page preference {@link Page}
     * @param i18n {@link I18nPwC} object required to set the preference title in the particular locale translated string. If the i18n is
     *            null, the i18n key is set as title for the {@link PreferenceOption}.
     * @return {@link PreferenceOption}. If the page is null, it returns null.
     */
    public PreferenceOption adaptPageToPreferenceOption(final Page page, final I18nPwC i18n) {
        PreferenceOption preferenceOption = null;
        if (page != null) {
            ValueMap properties = page.getProperties();
            preferenceOption = new PreferenceOption();
            String i18nTitle = properties.get(I18N_KEY_PROPERTY, page.getTitle());
            if (i18n != null)
                i18nTitle = i18n.getPwC(i18nTitle);
            preferenceOption.setI18nTitle(i18nTitle);
            preferenceOption.setName(page.getName());
            preferenceOption.setPath(page.getPath());
            preferenceOption.setTitle(page.getTitle());
            preferenceOption.setCategory(page.getName().toLowerCase());
            preferenceOption.setSecondTierDesign(properties.get(SECOND_TIER_DESIGN_PROPERTY, SECOND_TIER_DESIGN_DEFAULT_VALUE));
            Iterator<Page> pageIterator = page.listChildren();
            preferenceOption.setHasChildren(pageIterator.hasNext());
            List<PreferenceOption> preferenceOptionList = new ArrayList<PreferenceOption>();
            while (pageIterator.hasNext()) {
                PreferenceOption childPreferenceOption = adaptPageToPreferenceOption(pageIterator.next(), i18n);
                preferenceOptionList.add(childPreferenceOption);
            }
            preferenceOption.setChildrenOption(preferenceOptionList);
        }
        LOGGER.debug("PreferenceOptionAdapter adaptPageToPreferenceOption() : Returning PreferenceOption : {} ", preferenceOption);
        return preferenceOption;
    }
    
    /**
     * It adapts the user's preference page to preference category to {@link PreferenceOption} {@link Map}.
     * 
     * @param page preference {@link Page}. It expects the parent preference page under which the the preference list pages are maintained.
     * @param i18n {@link I18nPwC} object required to set the preference title of the {@link PreferenceOption} in the locale translated
     *            string. If the i18n is null, the i18n key is set as title for the {@link PreferenceOption}.
     * @return preference category to {@link PreferenceOption} {@link Map}, returns null if the page is null.
     */
    public Map<String, PreferenceOption> adaptPageToPreferenceOptionMap(final Page page, final I18nPwC i18n) {
        Map<String, PreferenceOption> preferencesMap = null;
        if (page != null) {
            preferencesMap = new LinkedHashMap<String, PreferenceOption>();
            Iterator<Page> pageIterator = page.listChildren();
            while (pageIterator.hasNext()) {
                PreferenceOption childPreferenceOption = adaptPageToPreferenceOption(pageIterator.next(), i18n);
                preferencesMap.put(childPreferenceOption.getCategory(), childPreferenceOption);
            }
        }
        LOGGER.debug("PreferenceOptionAdapter adaptPageToPreferenceOptionMap() : Returning PreferenceOptionMap : {} ", preferencesMap);
        return preferencesMap;
    }
    
    /**
     * It adapts the user's preference page to {@link PreferenceOption} for the particular category given.
     * 
     * @param page preference {@link Page}. It expects the parent preference page under which the the preference list pages are maintained.
     * @param i18n {@link I18nPwC} object required to set the preference title of the {@link PreferenceOption} in the locale translated
     *            string. If the i18n is null, the i18n key is set as title for the {@link PreferenceOption}.
     * @param category the category for which the {@link PreferenceOption} is returned
     * @return {@link PreferenceOption}. If the page or category is null or the category given does not match any category under page, it
     *         returns null.
     */
    public PreferenceOption adaptPageToPreferenceOptionByCategory(final Page page, final I18nPwC i18n, final String category) {
        PreferenceOption preferenceOption = null;
        if (page != null && category != null) {
            Iterator<Page> pageIterator = page.listChildren();
            while (pageIterator.hasNext()) {
                Page childPage = pageIterator.next();
                if (childPage.getName().toLowerCase().equals(category.toLowerCase()))
                    preferenceOption = adaptPageToPreferenceOption(childPage, i18n);
            }
        }
        LOGGER.debug("PreferenceOptionAdapter adaptPageToPreferenceOptionByCategory() : Returning PreferenceOption : {} for category {} ",
                preferenceOption, category);
        return preferenceOption;
    }
    
}
