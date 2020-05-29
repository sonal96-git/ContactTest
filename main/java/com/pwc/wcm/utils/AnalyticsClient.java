/*
 * Copyright 1997-2008 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package com.pwc.wcm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class AnalyticsClient {
    
    
    public static final Logger log = LoggerFactory.getLogger(AnalyticsClient.class);

    private static final int SLEEP_TIME = 5000;
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String ENDPOINT = "";
    private static int TIMEOUT = 60;
    private static String proxy = null;
    
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    public static void setCredentialFromConfig(final String username, final String password, final String endpoint,
            final int timeout) {
        USERNAME = username;
        PASSWORD = password;
        ENDPOINT = endpoint;
        TIMEOUT = timeout;
    }
    
    public static Map<Object, Object> o(final Object... values) {
        final Map<Object, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < (values.length - 1); i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    public static int queue(final ReportDescription reportDesc) throws IOException {
        log.debug("Entering Analytics Queue!!");
        final JsonObject response = callMethod("Report.Queue", o("reportDescription", reportDesc), JsonObject.class);
        return response.get("reportID").getAsInt();
    }
    
    //  public static ReportResponse get(int reportId) throws IOException {
    public static String get(final int reportId) throws IOException, InterruptedException {
        //              return callMethod("Report.Get", o("reportID", reportId), ReportResponse.class);
        String response1 = null;
        int count = 0;
        while (response1 == null) {
            try {
                log.debug("Fetching Response for Report ID: " + reportId);
                response1 = callMethod("Report.Get", o("reportID", reportId));
            } catch (final IOException e) {
                log.error("Analytics Error: " + e.getMessage());
                if (e.getMessage().contains("report_not_ready")) {
                    count++;
                    if ((count * SLEEP_TIME) > (TIMEOUT * 1000)) {
                        log.error("Report Not Ready. Time out reached for reportId: {}\n", reportId, e);
                        throw new IOException(
                                "This request is taking too long to process, it is being timed out by the server.");
                    }
                    log.error("Report Not Ready. Retry No. " + count);
                    Thread.sleep(SLEEP_TIME);
                    continue;
                } else {
                    log.error("Error Response recieved from Omniture Server. " + e);
                    throw e;
                }
            }
        }
        return response1;
    }

    //  public <T> T callMethod(String method, Object data, Type resultType) throws IOException {
    public static String callMethod(final String method, final Object data) throws IOException {
        final String request = GSON.toJson(data);
        final String response = callMethod(method, request);
        //              return GSON.fromJson(response, resultType);
        return response;
    }

    public static <T> T callMethod(final String method, final Object data, final Type resultType) throws IOException {
        final String request = GSON.toJson(data);
        final String response = callMethod(method, request);
        log.info("Request:" + request);
        log.info("Response:" + response);
        return GSON.fromJson(response, resultType);
    }
    
    public static String callMethod(final String method, final String data) throws IOException {
        final URL url = new URL(String.format("%s/?method=%s", ENDPOINT, method));
        final HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        authenticate(connection);
        connection.setDoOutput(true);
        
        IOUtils.write(data, connection.getOutputStream());
        return readResponse(connection);
    }

    public static void authenticate(final HttpURLConnection connection) {
        connection.addRequestProperty("X-WSSE", getHeader());
    }
    
    private static String getHeader() {
        final String nonce = UUID.randomUUID().toString();
        final String created = currentDate();
        final String passwordDigest = getBase64Digest(nonce, created, PASSWORD);
        final StringBuilder builder = new StringBuilder("UsernameToken ");
        addField(builder, "Username", USERNAME).append(", ");
        addField(builder, "PasswordDigest", passwordDigest).append(", ");
        addField(builder, "Nonce", Base64.encodeBase64String(nonce.getBytes())).append(", ");
        addField(builder, "Created", created);
        return builder.toString();
    }
    
    private static StringBuilder addField(final StringBuilder builder, final String fieldName,
            final String fieldValue) {
        builder.append(fieldName).append("=\"").append(fieldValue).append('"');
        return builder;
    }
    
    private static String currentDate() {
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    private static String getBase64Digest(final String... strings) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            for (final String s : strings) {
                //                              DigestUtils.updateDigest(digest, s);
                digest.update(StringUtils.getBytesUtf8(s));
            }
            return Base64.encodeBase64String(digest.digest());
        } catch (final NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException: ", e);
        }

        return "";
    }
    
    public static String readResponse(final HttpURLConnection connection) throws IOException {
        final int status = connection.getResponseCode();
        if ((status < 200) || (status > 299)) {
            log.error("Error response code received from Omniture Server: " + status);
            final InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                final String errorMessage = IOUtils.toString(errorStream);
                final JsonObject jsonResult = GSON.fromJson(errorMessage, JsonObject.class);
                log.error("Error response received from Omniture Server: " + jsonResult);
                log.error("Error Stream received from Omniture server: " + errorMessage);
                throw new IOException(jsonResult.get("error").toString());
            }
            throw new IOException(
                    String.format("HTTP error %d %s", connection.getResponseCode(), connection.getResponseMessage()));
        } else {
            return IOUtils.toString(connection.getInputStream());
        }

    }
}
