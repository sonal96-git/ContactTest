package com.pwc.model.components.assetsharing.impl;

import com.pwc.model.components.assetsharing.ComponentCalculator;
import com.pwc.model.components.assetsharing.ShareCalculator;
import com.pwc.model.components.assetsharing.ShareRule;
import com.pwc.model.components.assetsharing.ShareUrlRules.Facebook;
import com.pwc.model.components.assetsharing.ShareUrlRules.Linkedin;
import com.pwc.model.components.assetsharing.ShareUrlRules.Pinterest;
import com.pwc.model.components.assetsharing.ShareUrlRules.Twitter;
import com.pwc.model.components.assetsharing.ShareUrlRules.Weibo;
import com.pwc.model.components.assetsharing.SocialItem;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.ArrayList;

public class ShareCalculatorImpl implements ShareCalculator {
    private ArrayList<ShareRule> shareRules;
    private SlingHttpServletRequest request;

    public ShareCalculatorImpl(SlingHttpServletRequest request) {
        this.request = request;
        this.shareRules = new ArrayList<>();
        this.shareRules.add(new Facebook());
        this.shareRules.add(new Twitter());
        this.shareRules.add(new Linkedin());
        this.shareRules.add(new Pinterest());
        this.shareRules.add(new Weibo());
    }

    public String getShareUrl(SocialItem item) throws Exception {
        String shareUrl = StringUtils.EMPTY;
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(item)) continue;
            shareUrl = rule.getShareUrl(this.request, new ComponentCalculator(this.request), true);
            break;
        }
        return shareUrl;
    }
    
    public String getBitlyAccess() throws Exception {
        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        SlingScriptHelper scriptHelper = bindings.getSling();
        ConfigurationAdmin configAdmin = scriptHelper.getService(org.osgi.service.cm.ConfigurationAdmin.class);
        
        Configuration config = configAdmin.getConfiguration("PwC Social");
		String bitlyAccess = config.getProperties().get("bitly_access").toString();
        
        return bitlyAccess;
    }

    public String parseYoutubeUrl(String socialChanel, String youtubeId) {
        String parsedYoutubeUrl = StringUtils.EMPTY;
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(socialChanel)) continue;
            parsedYoutubeUrl = rule.parseYoutubeUrl(youtubeId);
            break;
        }
        return parsedYoutubeUrl;
    }

    public String getShareUrl(String socialLabel, boolean returnEncodedUrl) throws Exception {
        String shareUrl = StringUtils.EMPTY;
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(socialLabel)) continue;
            shareUrl = rule.getShareUrl(this.request, new ComponentCalculator(this.request), returnEncodedUrl);
            break;
        }
        return shareUrl;
    }

    public String getImageUrlResizeSuffix(String socialLabel) {
        String assetUrlSuffix = ".pwcimage.%s.%s.jpg";
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(socialLabel)) continue;
            assetUrlSuffix = String.format(assetUrlSuffix, rule.getAssetWidth(), rule.getAssetHeight());
            break;
        }
        return assetUrlSuffix;
    }

    public String getDefaultWidth(String socialLabel) {
        String width = StringUtils.EMPTY;
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(socialLabel)) continue;
            width = String.valueOf(rule.getAssetWidth());
            break;
        }
        return width;
    }

    public String getDefaultHeight(String socialLabel) {
        String height = StringUtils.EMPTY;
        for (ShareRule rule : shareRules) {
            if (!rule.isMatch(socialLabel)) continue;
            height = String.valueOf(rule.getAssetHeight());
            break;
        }
        return height;
    }
}
