package com.pwc.model.components.assetsharing.ShareUrlRules;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.model.components.assetsharing.ComponentCalculator;
import com.pwc.model.components.assetsharing.ShareRule;
import com.pwc.model.components.assetsharing.SocialItem;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public class Linkedin extends ShareRule {
    private static final String STRING_MATCH_RULE = "linkedin";
    private static final String SHARE_URL_SUFIX = "/linkedin.html";
    private static final String YOUTUBE_URL_PREFIX = "https://www.youtube.com/embed/";
    private static final int ASSET_WIDTH = 800;
    private static final int ASSET_HEIGHT = 800;
    public Linkedin() {
        this.setAssetHeight(ASSET_HEIGHT);
        this.setAssetWidth(ASSET_WIDTH);
        this.setShareUrlSufix(SHARE_URL_SUFIX);
    }
    @Override
    public Boolean isMatch(SocialItem item) {
        return item.getSocialChannel().equals(STRING_MATCH_RULE);
    }

    @Override
    public Boolean isMatch(String socialLabel) { return socialLabel.equals(STRING_MATCH_RULE); }

    @Override
    public String getShareUrl(SlingHttpServletRequest request, ComponentCalculator calculator, Boolean returnEncodedUrl) throws Exception {
        Resource resource = request.getResource();
        if(resource == null) return null;
        if(calculator.getIsVideo(resource)) {

            String videoUrl = calculator.getVideoUrl(resource);
            String videoId = CommonUtils.getYoutubeId(videoUrl);
            if(videoId != null) {
                return this.parseYoutubeUrl(videoId);
            }
            return videoUrl;
        }
        return super.getShareUrl(request, calculator, returnEncodedUrl);
    }

    @Override
    public String parseYoutubeUrl(String youtubeId) {
        return YOUTUBE_URL_PREFIX + youtubeId;
    }
}
