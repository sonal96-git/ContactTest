package com.pwc.wcm.utils;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rjiang on 2017-04-11.
 */
public class HttpUtils {
    public static final Logger log = LoggerFactory.getLogger(AnalyticsClient.class);
    public static JSONObject getGETResponse(String urlInput){
        JSONObject jsonObject  = null;
        try {

            URL url = new URL(urlInput);
            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
            httpsCon.setDoOutput(true);
            httpsCon.setDoInput(true);
            httpsCon.setUseCaches(false);
            httpsCon.setRequestMethod("GET");
            httpsCon.setRequestProperty("Content-Type",
                    "application/json; charset=utf-8");
            Charset charset = Charset.forName("UTF-8");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    httpsCon.getInputStream()));
            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            jsonObject = new JSONObject(sb.toString());
        }
        catch(Exception ex){
            log.error("Exception thrown from getGETResponse(): ",
                    ex);
        }
        return jsonObject;
    }
}
