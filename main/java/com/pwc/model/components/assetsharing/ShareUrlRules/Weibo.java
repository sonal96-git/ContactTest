package com.pwc.model.components.assetsharing.ShareUrlRules;

import com.pwc.model.components.assetsharing.ShareRule;
import com.pwc.model.components.assetsharing.SocialItem;

public class Weibo extends ShareRule {
    private static final String STRING_MATCH_RULE = "weibo";
    private static final String SHARE_URL_SUFIX = "/weibo.html";
    private static final String YOUTUBE_URL_PREFIX = "https://www.youtube.com/embed/";
    private static final int ASSET_WIDTH = 1200;
    private static final int ASSET_HEIGHT = 630;
    public Weibo() {
        this.setAssetHeight(ASSET_HEIGHT);
        this.setAssetWidth(ASSET_WIDTH);
        this.setShareUrlSufix(SHARE_URL_SUFIX);
    }
    @Override
    public Boolean isMatch(SocialItem item) {
        return item.getSocialChannel().equals(STRING_MATCH_RULE);
    }

    @Override
    public Boolean isMatch(String socialLabel) {
        return socialLabel.equals(STRING_MATCH_RULE);
    }

    @Override
    public String parseYoutubeUrl(String youtubeId) {
        return YOUTUBE_URL_PREFIX + youtubeId;
    }
}