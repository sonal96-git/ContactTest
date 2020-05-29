package com.pwc.wcm.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiang on 4/12/2017.
 */
public class PageProperty {
    private String territory;
    private String locale;
    private boolean isMicrosite;
    private String pagePath;

    public PageProperty(String path) {
        this.pagePath = path;
    }

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

    public boolean isMicrosite() {
        return isMicrosite;
    }

    public void setMicrosite(boolean microsite) {
        isMicrosite = microsite;
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getMicrositeName() {
        if (this.isMicrosite) {
            String micrositePagePattern = "^/content/(?:dam/pwc|pwc)/((\\w{2})/(\\w{2})/website/([^/]+))((/.*)|(.*))";
            Pattern pattern = Pattern.compile(micrositePagePattern);
            Matcher matcher = pattern.matcher(this.pagePath);
            if (matcher.find()) {
                return matcher.group(4);
            } else
                return null;
        } else {
            return null;
        }
    }
}
