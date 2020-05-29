package com.pwc.model.components.assetsharing;

import com.pwc.model.components.assetsharing.ComponentRules.ImageComponent;
import com.pwc.model.components.assetsharing.ComponentRules.TextImageComponent;
import com.pwc.model.components.assetsharing.ComponentRules.VideoComponent;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;

public class ComponentCalculator {
    private ArrayList<ComponentRule> componentRules;
    private SlingHttpServletRequest request;

    public ComponentCalculator(SlingHttpServletRequest request) {
        this.request = request;
        componentRules = new ArrayList<>();
        componentRules.add(new ImageComponent(request));
        componentRules.add(new TextImageComponent(request));
        componentRules.add(new VideoComponent(request));
    }

    public Resource getMainComponentResource(Resource resource) throws Exception {
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            return rule.getMainComponentResource(resource);
        }
        return resource;
    }

    public String getFullImageUrl(Resource resource) throws Exception {
        String fullImageUrl = StringUtils.EMPTY;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            String url = rule.getImageUrl(resource, true);
            fullImageUrl = CommonUtils.getExternalUrl(this.request, url);
            //mimic the behavior of dpe.js (remove /content/pwc)
            String serverName = this.request.getServerName();
            if (!serverName.equals("dpe-preview.pwc.com") && !serverName.equals("dpe.pwc.com") && !serverName.equals("dpe-stg-preview.pwc.com") && !serverName.equals("dpe-stg.pwc.com") && !serverName.equals("localhost") && !serverName.equals("dpe-preview.pwcinternal.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-author-stgv2.pwc.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-extpub-qa2.pwc.com")&& !serverName.equals("dpe-qa2.pwc.com")&& !serverName.equals("pwc-com-dpe-qa2.pwc.com")&& !serverName.equals("dpe-qa2-preview.pwc.com")&& !serverName.equals("pwc-com-dpe-staging2.pwc.com")&& !serverName.equals("10.195.143.85")&& !serverName.equals("10.195.143.84")){
            	if (fullImageUrl.indexOf("/content/dam/pwc/") != -1){
            		fullImageUrl = fullImageUrl.replace("/content/dam/pwc/", "/");
            	}
            }
            break;
        }
        return fullImageUrl;
    }

    public String getVideoUrl(Resource resource) throws Exception {
        String videoUrl = StringUtils.EMPTY;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            videoUrl = rule.getVideoUrl(resource);
            break;
        }
        return videoUrl;
    }

    public Boolean getIsVideo(Resource resource) throws Exception {
        Boolean isVideo = false;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            isVideo = rule.isVideo(resource);
            break;
        }
        return isVideo;
    }

    public Boolean getDeepLinkingEnabled(Resource resource) throws Exception {
    	Boolean isEnabled = false;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            isEnabled = rule.getDeepLinkingEnabled(resource);
            break;
        }
        return isEnabled;
    }

    public String getAssetId(Resource resource) throws Exception {
        String assetId = StringUtils.EMPTY;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            assetId = rule.getAssetId(resource);
            break;
        }
        return assetId;
    }

    public Boolean getIsEnabled(Resource resource) throws Exception {
    	Boolean isEnabled = false;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            isEnabled = rule.getIsEnabled(resource);
            break;
        }
        return isEnabled;
    }
    
    public String getSocialLabel(Resource resource) throws Exception {
        String socialLabel = StringUtils.EMPTY;
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            socialLabel = rule.getSocialLabel(resource);
            break;
        }
        return socialLabel;
    }

    public ArrayList<SocialItem> getSocialItems(Resource resource, String assetId) throws Exception {
        ArrayList<SocialItem> socialItems = new ArrayList<>();
        for (ComponentRule rule : componentRules) {
            if(!rule.isMatch(resource)) continue;
            socialItems = rule.getSocialItems(resource, this.request, assetId);
            break;
        }
        return socialItems;
    }
}
