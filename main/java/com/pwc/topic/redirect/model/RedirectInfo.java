package com.pwc.topic.redirect.model;

/**
 * Model used for storing/retrieving all the information related to Topic Site Redirection.
 */
public class RedirectInfo {
    private String serverRedirectionUrl;
    private String clientRedirectionUrl;
    private String srcTerritoryCode;
    private String destTerritoryCode;
    private String srcTerritoryTitle;
    private String destTerritoryTitle;
    private String topicTitle;
    private String notificationMsg;
    private String popupMsg;
    private boolean enablePopupMsg;
    private boolean redirectionRequired;
    
    public String getServerRedirectionUrl() {
        return serverRedirectionUrl;
    }
    
    public void setServerRedirectionUrl(final String serverRedirectionUrl) {
        this.serverRedirectionUrl = serverRedirectionUrl;
    }
    
    public String getClientRedirectionUrl() {
        return clientRedirectionUrl;
    }
    
    public void setClientRedirectionUrl(final String clientRedirectionUrl) {
        this.clientRedirectionUrl = clientRedirectionUrl;
    }
    
    public String getSrcTerritoryCode() {
        return srcTerritoryCode;
    }

    public void setSrcTerritoryCode(String srcTerritoryCode) {
        this.srcTerritoryCode = srcTerritoryCode;
    }

    public String getDestTerritoryCode() {
        return destTerritoryCode;
    }

    public void setDestTerritoryCode(String destTerritoryCode) {
        this.destTerritoryCode = destTerritoryCode;
    }

    public boolean isEnablePopupMsg() {
        return enablePopupMsg;
    }
    
    public void setEnablePopupMsg(final boolean enablePopupMsg) {
        this.enablePopupMsg = enablePopupMsg;
    }
    
    public boolean isRedirectionRequired() {
        return redirectionRequired;
    }
    
    public void setRedirectionRequired(final boolean redirectionRequired) {
        this.redirectionRequired = redirectionRequired;
    }
    
    public String getSrcTerritoryTitle() {
        return srcTerritoryTitle;
    }
    
    public void setSrcTerritoryTitle(final String srcTerritoryTitle) {
        this.srcTerritoryTitle = srcTerritoryTitle;
    }
    
    public String getDestTerritoryTitle() {
        return destTerritoryTitle;
    }
    
    public void setDestTerritoryTitle(final String destTerritoryTitle) {
        this.destTerritoryTitle = destTerritoryTitle;
    }
    
    public String getTopicTitle() {
        return topicTitle;
    }
    
    public void setTopicTitle(final String topicTitle) {
        this.topicTitle = topicTitle;
    }
    
    public String getNotificationMsg() {
        return notificationMsg;
    }
    
    public void setNotificationMsg(final String notificationMsg) {
        this.notificationMsg = notificationMsg;
    }
    
    public String getPopupMsg() {
        return popupMsg;
    }
    
    public void setPopupMsg(final String popupMsg) {
        this.popupMsg = popupMsg;
    }
    
    @Override
    public String toString() {
        return "RedirectInfo: serverRedirectionUrl= " + serverRedirectionUrl + ", clientRedirectionUrl= " + clientRedirectionUrl
                + ", srcTerritoryTitle=" + srcTerritoryTitle + ", destTerritoryTitle=" + destTerritoryTitle + ", enablePopupMsg="
                + enablePopupMsg + ", redirectionRequired= " + redirectionRequired;
    }
}
