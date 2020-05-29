package com.pwc.wcm.services.impl;

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.pwc.wcm.services.LanguageService;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.services.LocaleService;
import com.pwc.wcm.utils.JsonToMapConversion;

@Component(immediate = true, service = { LanguageService.class }, enabled = true)
public class LanguageServiceImpl implements LanguageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageService.class);
    private static final String LINK_URL_JSON_KEY = "linkURL";
    private static final String LINk_TEXT_JSON_KEY = "linkText";
    private static final String IS_CURRENT_LANGUAGE_JSON_KEY = "isCurrentLanguage";
    private static final String ALTERNATE_LANGUAGE_CODE_MAP_KEY = "alternateLanguageCode";
    private static final String ALTERNATE_LANGUAGE_URL_MAP_KEY = "alternateLanguageURL";
    private static final String SELECTABLE_ALTERNATE_LANGUAGE_PAGE_PROPERTY_KEY = "selectableAlternateLanguage";
    private static final String CURRENT_LANGUAGE_TITLE_KEY = "currentLanguageTitle";
    private static final String ALTERNATE_LANGUAGES_KEY = "alternateLanguages";
    private static final String ALTERNATE_LANGUAGE_SLIM_HEADER_PROPERTY_KEY = "multilingualSiteSettings";

    @Reference
    private LocaleService localeService;
    
    @Reference
    private SlingRepository repository;
    
    @Reference
    private LinkTransformerServiceFactory linkTransformerServiceFactory;

    @Reference
    private JsonToMapConversion jsonToMapConversionService;

    /**
     * Method provides {@link JSONArray} of the alternate languages available
     * including the current page language passed as 'currentLanguageTitle'
     * it will returns empty array if page doesn't have alternate languages configured.
     *
     * @param page                 {@link Page}
     * @param currentLanguageTitle {@link String}
     * @param request
     * @return alternateLanguageList {@link JSONArray}
     */
    private JSONArray getAlternateLanguages(Page page, String currentLanguageTitle, SlingHttpServletRequest request) {
        JSONArray jsonArray = new JSONArray();
        // get alternate language property available for language selector on current page
        if (page != null && currentLanguageTitle != null) {
            String[] alternateLanguages = page.getProperties().get(SELECTABLE_ALTERNATE_LANGUAGE_PAGE_PROPERTY_KEY, String[].class);
            if (alternateLanguages != null && alternateLanguages.length > 0) {
                try {
                    jsonArray.put(getJsonLanguageObject("#", currentLanguageTitle, true));
                    for (String alternateLanguage : alternateLanguages) {
                        Map<String, String> languageMap = jsonToMapConversionService.convertJsonToMap(alternateLanguage);
                        String languageCode = languageMap.get(ALTERNATE_LANGUAGE_CODE_MAP_KEY);
                        String languageTitle = localeService.getLanguageOriginalFromLanguageCode(languageCode);
                        String languageHref = getTransformedUrl(languageMap.get(ALTERNATE_LANGUAGE_URL_MAP_KEY)); 
                        jsonArray.put(getJsonLanguageObject(languageHref, languageTitle, false));
                    }
                } catch (JSONException jsonException) {
                    LOGGER.error("LanguageService : getAlternateLanguages() : JSONException Occurred  {}", jsonException);
                }
            } else {
                //if alternateLanguages not available on Page, fetch it from slimHeaderComponent
                ValueMap valueMap = request.getResource().getValueMap();
                alternateLanguages = valueMap.get(ALTERNATE_LANGUAGE_SLIM_HEADER_PROPERTY_KEY, String[].class);
                if (alternateLanguages != null && alternateLanguages.length > 0) {
                    try {
                        for (String alternateLanguage : alternateLanguages) {
                            JSONObject alternateLanguageJSON = new JSONObject(alternateLanguage);
                            String link = getTransformedUrl(alternateLanguageJSON.getString(LINK_URL_JSON_KEY));
                            alternateLanguageJSON.put(LINK_URL_JSON_KEY, link);
                            jsonArray.put(alternateLanguageJSON);
                        }
                    } catch (JSONException jsonException) {
                        LOGGER.error("LanguageService : getAlternateLanguages() : JSONException Occurred  {}", jsonException);
                    }
                }
            }
            LOGGER.debug("LanguageService : getAlternateLanguages() : Returning Alternative Language Map {} for Path  {}", jsonArray,
                    page.getPath());
        }
        return jsonArray;
    }

    /**
     * method prepares a Json Object for alternate language
     *
     * @param languageHref
     * @param languageTitle
     * @param isCurrentLanguage
     * @return Json Object
     * @throws JSONException
     */
    private JSONObject getJsonLanguageObject(String languageHref, String languageTitle, boolean isCurrentLanguage) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(LINK_URL_JSON_KEY, languageHref);
        jsonObject.put(LINk_TEXT_JSON_KEY, languageTitle);
        jsonObject.put(IS_CURRENT_LANGUAGE_JSON_KEY, isCurrentLanguage);
        return jsonObject;
    }

    @Override
    public JSONObject getLanguageSelectorData(Page page, String locale, SlingHttpServletRequest request) {
        LOGGER.info("LanguageService : Entered getLanguageSelectorData");
        JSONObject languageSelectorData = new JSONObject();
        try {
            if (page != null) {
                String currentLanguageTitle = localeService.getLanguageOriginalFromLocale(locale);
                languageSelectorData = new JSONObject();
                languageSelectorData.put(CURRENT_LANGUAGE_TITLE_KEY, currentLanguageTitle);
                languageSelectorData.put(ALTERNATE_LANGUAGES_KEY, getAlternateLanguages(page, currentLanguageTitle, request));
            } else {
                languageSelectorData.put(CURRENT_LANGUAGE_TITLE_KEY, "");
                languageSelectorData.put(ALTERNATE_LANGUAGES_KEY, new JSONArray());
            }

        } catch (JSONException jsonException) {
            LOGGER.error("LanguageService : getLanguageSelectorData() : JSONException Occurred  {}", jsonException);
        }
        return languageSelectorData;
    }
    
    /**
     * Gets the AEM transformed URL of the given path using {@link LinkTransformerService}.
     *
     * @param path {@link String} the path
     * @return {@link String} the transformed URL
     */
    private String getTransformedUrl(String path) {
        if (path != null) {
            LinkTransformerService linkTransformerService = linkTransformerServiceFactory.getLinkTransformerServiceIfTransformerEnabled(repository);
            if(linkTransformerService != null)
                path = linkTransformerService.transformAEMUrl(path);
        }
        return path;
    }

}
