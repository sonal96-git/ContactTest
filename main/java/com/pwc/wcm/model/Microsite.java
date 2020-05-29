package com.pwc.wcm.model;

/**
 * Created by rjiang022 on 6/12/2015.
 */
public class Microsite {

    private String path;
    private boolean isMultiLanguage;
    private String locale;
    private String territory;
    private String forwardDomain;

    public String getForwardDomain() {
        return forwardDomain;
    }

    public void setForwardDomain(String forwardDomain) {
        this.forwardDomain = forwardDomain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isMultiLanguage() {
        return isMultiLanguage;
    }

    public void setIsMultiLanguage(boolean isMultiLanguage) {
        this.isMultiLanguage = isMultiLanguage;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }
}