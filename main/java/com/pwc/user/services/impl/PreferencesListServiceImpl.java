package com.pwc.user.services.impl;

import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.AdminResourceResolver;
import com.pwc.user.Constants;
import com.pwc.user.adapter.PreferenceOptionAdapter;
import com.pwc.user.model.PreferenceOption;
import com.pwc.user.services.PreferencesListService;
import com.pwc.wcm.utils.I18nPwC;

@Component(immediate = true, service = { PreferencesListService.class }, enabled = true,
    property = {
    		"service.description" + "= Provides Methods to access the user perferences filtered by category and territory" })
@Designate(ocd = PreferencesListServiceImpl.Config.class )
public class PreferencesListServiceImpl implements PreferencesListService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesListService.class);
    public static final String PREFERENCES_LIST_BASE_PATH_PROPERTY = "preferences.list.path";
    public static final String PREFERENCES_LIST_BASE_PATH_VALUE = "/content/pwc/global/user-preferences";
    
    private String preferencesListBasePath;
    
    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    @ObjectClassDefinition(name = "PwC User Preferences List Service", 
    		description = "Provides Methods to access the user perferences filtered by category and territory")
    @interface Config {
        @AttributeDefinition(name = "Preferences List Base Path", 
                            description = "Content path under which all territories preferences lists are present",
                            type = AttributeType.STRING)
        public String preferences_list_path() default PREFERENCES_LIST_BASE_PATH_VALUE;
    }
    
    @Activate
    @Modified
    protected final void activate(final PreferencesListServiceImpl.Config properties) throws Exception {
        preferencesListBasePath = properties.preferences_list_path();
        LOGGER.debug("PreferencesListService : activate() : Preferences List Base Path {} ", preferencesListBasePath);
    }
    
    @Override
    public Map<String, PreferenceOption> getPreferencesMapByTerritory(final String territoryCode, final I18nPwC i18n) {
        Map<String, PreferenceOption> preferencesMap = null;
        ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
        Page preferenceTerritoryPage = getPreferencesPageByTerritory(territoryCode, resourceResolver);
        if (preferenceTerritoryPage != null)
            preferencesMap = new PreferenceOptionAdapter().adaptPageToPreferenceOptionMap(preferenceTerritoryPage, i18n);
        resourceResolver.close();
        LOGGER.debug("PreferencesListService : getPreferencesMapByTerritory() : Preferences Map {} returned for territory {} ",
                preferencesMap, territoryCode);
        return preferencesMap;
    }
    
    @Override
    public PreferenceOption getPreferencesByTerritoryAndCategory(final String category, final String territoryCode, final I18nPwC i18n) {
        PreferenceOption preferencesOption = null;
        ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
        Page preferenceTerritoryPage = getPreferencesPageByTerritory(territoryCode, resourceResolver);
        if (preferenceTerritoryPage != null)
            preferencesOption = new PreferenceOptionAdapter().adaptPageToPreferenceOptionByCategory(preferenceTerritoryPage, i18n,
                    category);
        resourceResolver.close();
        LOGGER.debug(
                "PreferencesListService : getPreferencesByTerritoryAndCategory() : Preference Option {} returned for territory & category {} ",
                preferencesOption, territoryCode + " & " + category);
        return preferencesOption;
    }
    
    /**
     * Gets the preferences {@link Page} for the given territory. If the preference page is not present, Preference {@link Page} for
     * {@value Constants#DEFAULT_TERRITORY} is returned.
     *
     * @param territoryCode {@link String}
     * @param resourceResolver {@link ResourceResolver} must have rights to read the preferences page
     * @return {@link Page}
     */
    private Page getPreferencesPageByTerritory(final String territoryCode, final ResourceResolver resourceResolver) {
        Page preferenceTerritoryPage = null;
        if (territoryCode != null) {
            String pagePath = preferencesListBasePath + "/" + territoryCode.toLowerCase().trim();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            preferenceTerritoryPage = pageManager.getPage(pagePath);
            if (preferenceTerritoryPage == null) {
                pagePath = preferencesListBasePath + "/" + Constants.DEFAULT_TERRITORY;
                preferenceTerritoryPage = pageManager.getPage(pagePath);
            }
        }
        return preferenceTerritoryPage;
    }
    
    @Override
    public String getUserPreferencesPath() {
        return preferencesListBasePath;
    }
    
}
