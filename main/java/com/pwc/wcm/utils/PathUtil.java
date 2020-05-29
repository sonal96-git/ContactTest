package com.pwc.wcm.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pwc.wcm.model.PageProperty;

/**
 * Created by jiang on 4/12/2017.
 */
public class PathUtil {
    private static String micrositePagePattern = "^/content/(?:dam/pwc|pwc)/((\\w{2})/(\\w{2})/website/([^/]+))((/.*)|(.*))";
    private static String regularSitePagePattern = "^/content/(?:dam/pwc|pwc)/((\\w{2})/(\\w{2})/(.*))";
    public static PageProperty getPageProperty(String ref_data_path){
        PageProperty pageProperty = new PageProperty(ref_data_path);
        Pattern pattern = Pattern.compile(micrositePagePattern);
        Matcher matcher = pattern.matcher(ref_data_path);
        if(matcher.find()){
            String territory = matcher.group(2);
            pageProperty.setTerritory(territory);
            String locale = matcher.group(3);
            pageProperty.setLocale(locale);
            pageProperty.setMicrosite(true);
        }
        else{
            Pattern reg_pattern = Pattern.compile(regularSitePagePattern);
            Matcher reg_matcher = reg_pattern.matcher(ref_data_path);
            if(reg_matcher.find()){
                String territory = reg_matcher.group(2);
                pageProperty.setTerritory(territory);
                String locale = reg_matcher.group(3);
                pageProperty.setLocale(locale);
                pageProperty.setMicrosite(false);
            }
        }
        return  pageProperty;

    }
}
