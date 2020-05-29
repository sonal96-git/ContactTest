package com.pwc.model;

import java.util.Map;

public class Microsite {
    
    private String path;
    private String name;
    private String cookieConsentUrl;
	private String cookieConsentStageUrl;
    private Map<String, String> forwardDomains;
    
    public Microsite() {
    }
    
	public Microsite(String path, String name, String cookieConsentUrl, String cookieConsentStageUrl, Map<String, String> forwardDomains) {
        this.path = path;
        this.name = name;
        this.cookieConsentUrl = cookieConsentUrl;
		this.cookieConsentStageUrl = cookieConsentStageUrl;
        this.forwardDomains = forwardDomains;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCookieConsentUrl() {
        return cookieConsentUrl;
    }
    
    public void setCookieConsentUrl(String cookieConsentUrl) {
        this.cookieConsentUrl = cookieConsentUrl;
    }
    
    public Map<String, String> getForwardDomains() {
        return forwardDomains;
    }
    
    public void setForwardDomains(Map<String, String> forwardDomains) {
        this.forwardDomains = forwardDomains;
    }

	public String getCookieConsentStageUrl() {
		return cookieConsentStageUrl;
	}

	public void setCookieConsentStageUrl(String cookieConsentStageUrl) {
		this.cookieConsentStageUrl = cookieConsentStageUrl;
	}


    
}
