package com.pwc.model.components.assetsharing;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentItem {

    private static final String TWITTER_HANDLE_KEY = "twitterhandle";
    private static final String TWITTER_TWEET_TEXT_KEY = "tweettext";
    private static final String SOCIAL_CHANNEL_KEY = "socialchannel";
    private static final String SHARE_DESCRIPTION_KEY = "sharedescription";
    private static final String SHARE_TITLE_KEY = "sharetitle";
    private static final String IMAGE_URL_KEY = "imageurl";
    private static final String VIDEO_URL_KEY = "videourl";

    private String socialchannel;

    private String sharetitle;

    private String sharedescription;

    private String twitterhandle;

    private String tweettext;

    private String imageurl;

    private String videourl;

    private String pwcimageSizeSufix;

    private String defaultWidth;

    private String defaultHeight;

    private String shareUrl;

    public void fromJSON(JSONObject item) throws Exception {
        this.socialchannel = item.has(SOCIAL_CHANNEL_KEY) ? item.getString(SOCIAL_CHANNEL_KEY) : StringUtils.EMPTY;
        this.sharedescription = item.has(SHARE_DESCRIPTION_KEY) ? item.getString(SHARE_DESCRIPTION_KEY) : StringUtils.EMPTY;
        this.sharetitle = item.has(SHARE_TITLE_KEY) ? item.getString(SHARE_TITLE_KEY) : StringUtils.EMPTY;
        this.twitterhandle = item.has(TWITTER_HANDLE_KEY) ? item.getString(TWITTER_HANDLE_KEY) : StringUtils.EMPTY;
        this.tweettext = item.has(TWITTER_TWEET_TEXT_KEY) ? item.getString(TWITTER_TWEET_TEXT_KEY) : StringUtils.EMPTY;
        this.imageurl = item.has(IMAGE_URL_KEY) ? item.getString(IMAGE_URL_KEY) : StringUtils.EMPTY;
        this.videourl = item.has(VIDEO_URL_KEY) ? item.getString(VIDEO_URL_KEY) : StringUtils.EMPTY;
    }

    public String getTwitterhandle() {
        return twitterhandle;
    }

    public void setTwitterhandle(String twitterhandle) {
        this.twitterhandle = twitterhandle;
    }

    public String getSharedescription() { return sharedescription; }

    public void setSharedescription(String sharedescription) {
        this.sharedescription = sharedescription;
    }

    public String getSharetitle() {
        return sharetitle;
    }

    public void setSharetitle(String sharetitle) {
        this.sharetitle = sharetitle;
    }

    public String getSocialchannel() {
        return socialchannel;
    }

    public void setSocialchannel(String socialchannel) {
        this.socialchannel = socialchannel;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPwcimageSizeSufix() {
        return pwcimageSizeSufix;
    }

    public void setPwcimageSizeSufix(String pwcimageSizeSufix) {
        this.pwcimageSizeSufix = pwcimageSizeSufix;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public String getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(String defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public String getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(String defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getTweettext() { return tweettext; }
}
