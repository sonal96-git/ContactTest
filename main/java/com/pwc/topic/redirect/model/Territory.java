package com.pwc.topic.redirect.model;

import java.util.Map;

/**
 * Model representing Territory resource under {@link Topic} in Topic Sites Reference Data.
 */
public class Territory {
    private String code;
    private String path;
    private String defaultLocale;
    private Map<String, Language> localeToLanguageMappings;
    
    public Territory() {
    }
    
    public Territory(String code, String path, String defaultLocale, Map<String, Language> localeToLanguageMappings) {
        this.code = code;
        this.path = path;
        this.defaultLocale = defaultLocale;
        this.localeToLanguageMappings = localeToLanguageMappings;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getDefaultLocale() {
        return defaultLocale;
    }
    
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
    
    public Map<String, Language> getLocaleToLanguageMappings() {
        return localeToLanguageMappings;
    }
    
    public void setLocaleToLanguageMappings(Map<String, Language> localeToLanguageMappings) {
        this.localeToLanguageMappings = localeToLanguageMappings;
    }
    
    @Override
    public String toString() {
        return "Territory Code: " + code + ", Path: " + path + ", Default Locale: " + defaultLocale + ", Languages: "
                + localeToLanguageMappings;
    }
}
