package com.pwc.wcm.utils;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by jye044 on 2/12/2015.
 */
public class SiteCatUtils {

    public static String getDataLayer(Page page, boolean reg) {

        String delimiter = "|";
        JSONObject data = new JSONObject();
        try {

            Tag[] tags = page.getTags();

            String addTags = "";
            String los = "";
            String subLos = "";
            String industry = "";
            String contentType = "";
            String folderDirectory = page.getPath().substring(9);
            String businessChallenge = "";
            String businessTopic = "";
            for (int i = 0; i < tags.length; i++) {
                String tg = tags[i].getPath().substring(10);
                if (tg.length() > 13 && tg.startsWith("pwc/services/")) {
                    String s = tg.substring(13);
                    if (s.indexOf("/") > 0) {
                        los = los.concat(delimiter).concat(s.substring(0, s.indexOf("/")));
                        subLos = subLos.concat(delimiter).concat(s.substring(s.indexOf("/") + 1));
                    } else {
                        los = los.concat(delimiter).concat(s);
                    }
                    addTags = addTags.concat(delimiter).concat(tg.substring(4));
                } else if (tg.length() > 15 && tg.startsWith("pwc/industries/")) {
                    industry = industry.concat(delimiter).concat(tg.substring(15));
                } else if (tg.length() > 18 && tg.substring(6, 18).equalsIgnoreCase("/industries/")) {
                    industry = industry.concat(delimiter).concat(tg.substring(18));
                } else if (tg.length() > 17 && tg.startsWith("pwc-content-type/")) {
                    contentType = contentType.concat(delimiter).concat(tg.substring(17));
                } else if (tg.length() > 11 && tg.startsWith("pwc/issues/")) {
                    businessChallenge = businessChallenge.concat(delimiter).concat(tg.substring(11));
                } else if (tg.length() > 14 && tg.substring(6, 14).equalsIgnoreCase("/issues/")) {
                    businessChallenge = businessChallenge.concat(delimiter).concat(tg.substring(14));
                } else if (tg.length() > 11 && tg.startsWith("pwc/topics/")) {
                    businessTopic = businessTopic.concat(delimiter).concat(tg.substring(11));
                } else if (tg.length() > 14 && tg.substring(6, 14).equalsIgnoreCase("/topics/")) {
                    businessTopic = businessTopic.concat(delimiter).concat(tg.substring(14));
                } else if (tg.length() > 4 && tg.startsWith("pwc/")) {
                    addTags = addTags.concat(delimiter).concat(tg.substring(4));
                } else if (tg.length() > 7 && tg.startsWith("pwc") && tg.indexOf("/") == 7) {
                    addTags = addTags.concat(delimiter).concat(tg.substring(7));
                } else {
                    addTags = addTags.concat(delimiter).concat(tg);
                }
            }


            data.put("accountRegistration", reg);
            data.put("additionalTags", addTags.length() > 1 ? addTags.substring(1) : addTags);

            JSONObject siteValues = new JSONObject();
            siteValues.put("los", los.length() > 1 ? los.substring(1) : los);
            siteValues.put("subLos", subLos.length() > 1 ? subLos.substring(1) : subLos);
            siteValues.put("industry", industry.length() > 1 ? industry.substring(1) : industry);
            siteValues.put("folderDirectory", folderDirectory.substring(0, folderDirectory.lastIndexOf("/")));
            siteValues.put("contentType", contentType.length() > 1 ? contentType.substring(1) : contentType);

            data.put("SiteValues", siteValues);

            JSONObject business = new JSONObject();
            business.put("businessChallenge", businessChallenge.length() > 1 ? businessChallenge.substring(1) : businessChallenge);
            business.put("businessTopic", businessTopic.length() > 1 ? businessTopic.substring(1) : businessTopic);

            data.put("business", business);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.toString();
    }
}
