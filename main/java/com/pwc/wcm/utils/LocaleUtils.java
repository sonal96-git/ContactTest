package com.pwc.wcm.utils;

public final class LocaleUtils {
    
    private static final String LOCALE_SEPARATOR = "_";
    private static final String GB_TERRITORY_CODE = "gb";
    private static final String UK_TERRITORY_CODE = "uk";
    
    public static final String getLocale(String territoryCode, String languageCode) {
        String locale = null;
        if (territoryCode != null && languageCode != null) {
            if(territoryCode.toLowerCase().equals(UK_TERRITORY_CODE))
                territoryCode = GB_TERRITORY_CODE;
            locale = languageCode + LOCALE_SEPARATOR + territoryCode;
            locale = locale.toLowerCase();
        }
        return locale;
    }
    
    public static final String getLanguageFromLocale(String locale) {
        String languageCode = null;
        if (locale != null && locale.contains(LOCALE_SEPARATOR)) {
            languageCode = locale.substring(0, locale.indexOf(LOCALE_SEPARATOR));
        }
        return languageCode;
    }
    
    public static final String getTerritoryFromLocale(String locale) {
        String territoryCode = null;
        if (locale != null && locale.contains(LOCALE_SEPARATOR)) {
            territoryCode = locale.substring(locale.indexOf(LOCALE_SEPARATOR) + 1, locale.length());
            if(territoryCode.toLowerCase().equals(GB_TERRITORY_CODE))
                territoryCode = UK_TERRITORY_CODE;
        }
        return territoryCode;
    }
    
}
