package com.pwc.model;

import java.util.Map;

public class Language {
    private String langTitle;
    private String code;
    private String path;
    private String cookieConsentUrl;
	private String cookieConsentStageUrl;
    private String locale;
    private Map<String, Microsite> micrositeMap;
    
    public Language() {
    }
    
    public Language(String langTitle, String code, String path, String cookieConsentUrl,String cookieConsentStageUrl, String locale, Map<String, Microsite> micrositeMap) {
        super();
        this.langTitle = langTitle;
        this.code = code;
        this.path = path;
        this.cookieConsentUrl = cookieConsentUrl;
        this.locale = locale;
        this.micrositeMap = micrositeMap;
		this.cookieConsentStageUrl = cookieConsentStageUrl;
    }
    
    public String getLangTitle() {
        return langTitle;
    }
    
    public void setLangTitle(String title) {
        this.langTitle = title;
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
    
    public String getCookieConsentUrl() {
        return cookieConsentUrl;
    }
    
    public void setCookieConsentUrl(String cookieConsentUrl) {
        this.cookieConsentUrl = cookieConsentUrl;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public Map<String, Microsite> getMicrositeMap() {
        return micrositeMap;
    }
    
    public void setMicrositeMap(Map<String, Microsite> micrositeMap) {
        this.micrositeMap = micrositeMap;
    }
    
	public String getCookieConsentStageUrl() {
		return cookieConsentStageUrl;
	}
	
	public void setCookieConsentStageUrl(String cookieConsentStageUrl) {
		this.cookieConsentStageUrl = cookieConsentStageUrl;
	}
	
    @Override
    public String toString() {
        return " Locale: " + locale + ", Path: " + path + ", Code: " + code + ", Language title: " + langTitle + ", Cookie Consent URL: "
				+ cookieConsentUrl + ",Cookie Consent Staging URL:" + cookieConsentStageUrl;
    }
	
}
