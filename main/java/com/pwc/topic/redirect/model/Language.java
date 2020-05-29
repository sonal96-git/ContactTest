package com.pwc.topic.redirect.model;

/**
 * Model representing Language resource under {@link Territory} in Topic Sites Reference Data.
 */
public class Language {
    private String locale;
    private String path;
    private String name;
    private String topicHomePageUrl;
    private String topicHomePagePath;
    
    public Language() {
    }
    
    public Language(String locale, String path, String name, String topicHomePageUrl, String topicHomePagePath) {
        this.locale = locale;
        this.path = path;
        this.name = name;
        this.topicHomePageUrl = topicHomePageUrl;
        this.topicHomePagePath = topicHomePagePath;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
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
    
    public String getTopicHomePageUrl() {
        return topicHomePageUrl;
    }
    
    public void setTopicHomePageUrl(String topicHomePageUrl) {
        this.topicHomePageUrl = topicHomePageUrl;
    }
    
    public String getTopicHomePagePath() {
        return topicHomePagePath;
    }
    
    public void setTopicHomePagePath(String topicHomePagePath) {
        this.topicHomePagePath = topicHomePagePath;
    }
    
    @Override
    public String toString() {
        return " Locale: " + locale + ", Path: " + path + ", Name: " + name + ", Topic HomepageUrl: " + topicHomePageUrl;
    }
}
