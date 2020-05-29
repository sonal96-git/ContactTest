package com.pwc.model.components.twittersearch;

import com.day.cq.wcm.api.Page;
import com.pwc.colors.models.ColorsBean;
import com.pwc.colors.utils.ColorsHelper;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwitterEmbedHtmlCalculator {

    private static String OEMBED_TWITTER_URL = "https://publish.twitter.com/oembed";

    public static String getSingleTweet(TwitterModel model, Page currentPage, SlingHttpServletRequest request) throws Exception {
        HttpURLConnection conn = null;
        String htmlResponse = StringUtils.EMPTY;
        if(model == null) return htmlResponse;
        if(StringUtils.isBlank(model.tweetUrl)) return htmlResponse;
        try {
            ColorsBean colorsBean = new ColorsBean(currentPage,request);
            ColorsHelper colorHelper = new ColorsHelper();
            colorHelper.setColorsBean(colorsBean);
            String fullUrl = OEMBED_TWITTER_URL + "?url=" + CommonUtils.urlEncode(model.tweetUrl);

            URL url = new URL(fullUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                return htmlResponse;
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String jsonText = com.pwc.wcm.utils.CommonUtils.readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            if(json.has("html")) {
                htmlResponse = json.getString("html");
            }
            return htmlResponse;
        }
        catch(Exception ex) {
            return StringUtils.EMPTY;
        }
        finally {
            if(conn != null)
                conn.disconnect();
        }
    }
}
