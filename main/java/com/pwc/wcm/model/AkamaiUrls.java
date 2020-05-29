package com.pwc.wcm.model;

/**
 * Created by rjiang022 on 3/19/2015.
 */
public class AkamaiUrls {
    private String url;
    private int userIndex;
    public AkamaiUrls(String url, int userIndex){
        this.url = url;
        this.userIndex = userIndex;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }
}
