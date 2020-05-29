package com.pwc.model.components.assetsharing;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.pwc.wcm.utils.CommonUtils;

public class SocialItem {

    private ShareCalculator urlCalculator;

    private static final String JSON_SOCIAL_CHANNEL_PROPERTY = "socialchannel";
    private static final String JSON_SHARE_TITLE_PROPERTY = "sharetitle";
    private static final String JSON_SHARE_DESCRIPTION_PROPERTY = "sharedescription";
    private static final String JSON_SHARE_TWITTER_HANDLE_PROPERTY = "twitterhandle";
    private static final String JSON_SHARE_TWEET_TEXT_PROPERTY = "tweettext";
    private static final String TWITTER_HANDLE_PREFIX = "&via=";
    private static final String TWEET_TEXT_PREFIX = "&text=";

    private String socialChannel;
    private String shareTitle;
    private String shareDescription;
    private String shareUrl;
    private String shortUrl;
    private String imageUrl;
    private String videoUrl;
    private String imageUrlSufix;
    private String twitterHandle;
    private String tweetText;
    private String assetId;

    public SocialItem(ShareCalculator calculator, String assetId) {
        this.socialChannel = StringUtils.EMPTY;
        this.shareTitle = StringUtils.EMPTY;
        this.shareDescription = StringUtils.EMPTY;
        this.twitterHandle = StringUtils.EMPTY;
        this.urlCalculator = calculator;
        this.assetId = assetId;
    }

    public void fromJSON(JSONObject item) throws Exception {
        if(item.has(JSON_SOCIAL_CHANNEL_PROPERTY))
            this.socialChannel = item.getString(JSON_SOCIAL_CHANNEL_PROPERTY);
        if(item.has(JSON_SHARE_TITLE_PROPERTY))
            this.shareTitle = item.getString(JSON_SHARE_TITLE_PROPERTY);
        if(item.has(JSON_SHARE_DESCRIPTION_PROPERTY))
            this.shareDescription = item.getString(JSON_SHARE_DESCRIPTION_PROPERTY);
        if(item.has(JSON_SHARE_TWITTER_HANDLE_PROPERTY)) {
            String handle = item.getString(JSON_SHARE_TWITTER_HANDLE_PROPERTY);
            if(!handle.isEmpty()) {
                //INFO: Remove first character if is @
                handle = String.valueOf(handle.charAt(0)).equals("@") ? handle.substring(1) : handle;
                this.twitterHandle = handle;
            }
        }
        if(item.has(JSON_SHARE_TWEET_TEXT_PROPERTY)) {
            this.tweetText = item.getString(JSON_SHARE_TWEET_TEXT_PROPERTY);
        }
        this.shareUrl = urlCalculator.getShareUrl(this);
        
        String bitlyAccess = urlCalculator.getBitlyAccess();
        
        this.shortUrl = assetId.equals(StringUtils.EMPTY) ?
        					CommonUtils.getShortUrl(bitlyAccess, this.shareUrl)
        				:
        					CommonUtils.getShortUrl(bitlyAccess, this.shareUrl + "%23" + assetId);

        this.imageUrlSufix = urlCalculator.getImageUrlResizeSuffix(this.socialChannel);
    }

    public String getSocialChannel(){
        return this.socialChannel;
    }

    public String getShareUrl() {
        return this.shareUrl;
    }
    
    public String getShortUrl() {
        return this.shortUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public String getShareTitleEscaped() {
      return CommonUtils.urlEncode(this.shareTitle);
    }

    public String getShareDescription() {
    	String result = !shareDescription.equals("") ? shareDescription : " ";
    	return CommonUtils.urlEncode(result);       
    }

    public String getShareDescriptionEscaped() {
        return CommonUtils.urlEncode(this.shareDescription);
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public String getTwitterHandleQuery() {
        return StringUtils.isBlank(this.twitterHandle) ? StringUtils.EMPTY : TWITTER_HANDLE_PREFIX + CommonUtils.urlEncode(this.twitterHandle);
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public String getImageUrlSufix() {
        return imageUrlSufix;
    }

    public void setImageUrlSufix(String imageUrlSufix) {
        this.imageUrlSufix = imageUrlSufix;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTweetText() { return tweetText; }

    public String getTweetTextQuery() {
        return StringUtils.isBlank(this.tweetText) ? StringUtils.EMPTY : TWEET_TEXT_PREFIX + CommonUtils.urlEncode(this.tweetText);
    }
}
