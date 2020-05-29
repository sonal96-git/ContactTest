package com.pwc.model.components.horizontallinklist;

import com.pwc.util.URLHandlerUtility;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

public class HorizontalLink {

    private final String NAME_KEY = "linkName";
    private final String URL_KEY = "linkUrl";
    private final String LINK_TARGET_KEY = "linkTarget";

    private String name;

    private String url;

    private String linkTarget;

    public HorizontalLink(JSONObject link, ResourceResolver resourceResolver) throws JSONException {
        this.name = link.optString(NAME_KEY);
        this.url = URLHandlerUtility.handleURLForInternalLinks(link.optString(URL_KEY), resourceResolver);
        this.linkTarget = link.optBoolean(LINK_TARGET_KEY) ? "_blank" : "_self";
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getLinkTarget() {
        return linkTarget;
    }
}
