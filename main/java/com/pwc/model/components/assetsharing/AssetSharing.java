package com.pwc.model.components.assetsharing;

import com.pwc.util.ExceptionLogger;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import java.util.ArrayList;

@Model(adaptables = SlingHttpServletRequest.class)
public class AssetSharing {
    //ATTRIBUTES - GENERAL
    private SlingHttpServletRequest request;
    private Resource resource;

    //ATTRIBUTES - DIALOG PROPERTIES
    private Boolean deepLinkingEnabled;
    private Boolean isEnabled;
    private Boolean isVideo;
    private String socialLabel;
    private String assetId;
    private ArrayList<SocialItem> socialItems;

    public AssetSharing(SlingHttpServletRequest request) {
        
    	this.request = request;
        this.resource = this.request.getResource();
        if(this.resource == null) return;
        ComponentCalculator componentCalculator = new ComponentCalculator(this.request);
        this.deepLinkingEnabled = false;
        this.assetId = StringUtils.EMPTY;
        this.isEnabled = false;
        this.socialLabel = StringUtils.EMPTY;
        this.socialItems = new ArrayList<>();
        try {
        	this.deepLinkingEnabled = componentCalculator.getDeepLinkingEnabled(this.resource);

        	if (this.deepLinkingEnabled)
        	{
        		this.assetId = componentCalculator.getAssetId(this.resource);
        	}

        	this.isEnabled = componentCalculator.getIsEnabled(this.resource);
			this.socialLabel = componentCalculator.getSocialLabel(this.resource);
			this.socialItems = componentCalculator.getSocialItems(this.resource, assetId);
            this.isVideo = componentCalculator.getIsVideo(this.resource);
		} catch (Exception e) {
			ExceptionLogger.logException(e);
		}
    }

    public Boolean getDeepLinkingEnabled() {
    	return deepLinkingEnabled;
    }
    public String getAssetId() {
    	return assetId;
    }
    public Boolean getIsEnabled() {
    	return isEnabled;
    }
    public String getSocialLabel() {
        return socialLabel;
    }
    public ArrayList<SocialItem> getSocialItems() {
        return socialItems;
    }
    public Boolean getIsVideo() { return isVideo; }
}

