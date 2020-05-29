package com.pwc.model.components.download;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

public class AdditionalFileModel {
    private String url;
    private String text;
    private String externalUrl;

    public AdditionalFileModel(JSONObject jsonFileModel) throws JSONException {
        this.url = jsonFileModel.getString("url");
        this.text = jsonFileModel.getString("text");
        this.externalUrl = jsonFileModel.getString("externalUrl");
    }

    public static Boolean isValid(JSONObject jsonFileModel) throws JSONException {
        return !StringUtils.isBlank(jsonFileModel.getString("url")) || !StringUtils.isBlank(jsonFileModel.getString("externalUrl"));
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public String getExternalUrl() {
        return externalUrl;
    }
}
