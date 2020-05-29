package com.pwc.wcm.model;

/**
 * Created by rjiang022 on 6/12/2015.
 */
public class AEMLink {

    private boolean isHomePage;
    private String territory;
    private String locale;
    private String content;

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFullLink(){
        if(!isHomePage)
            return "/" + this.getTerritory() + "/" + this.getLocale()  + this.getContent();
        else
            return "/" + this.getTerritory() + this.getContent();
    }

    public String getLinkWithoutTerritory() {
        return "/" + this.getLocale() + this.getContent();
    }

    public String getLinkwithTerritory() {
        return "/" + this.getTerritory() + this.getContent();
    }

    public boolean isHomePage() {
        return isHomePage;
    }

    public void setIsHomePage(boolean isHomePage) {
        this.isHomePage = isHomePage;
    }
}