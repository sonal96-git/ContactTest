package com.pwc.model.components.assetsharing;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.model.components.assetsharing.impl.ShareCalculatorImpl;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.sling.models.annotations.Model;

import java.util.ArrayList;


@Model(adaptables = SlingHttpServletRequest.class)
public class ShareContent {
    //ATTRIBUTES - GENERAL
    private Resource resource;
    private SlingHttpServletRequest request;

    private SocialContent content;
    private String mainSharedUrl;

    private String fullImageUrl;
    private String videoUrl;

    private Boolean isVideo;

    private String facebookAppId;

    private String assetId;

    private Boolean isDeepLinkingEnabled;

    private ArrayList<ContentItem> items;

    public ShareContent(SlingHttpServletRequest request) {
        this.request = request;
        this.resource = this.request.getResource();
        this.items = new ArrayList<>();
        fillContent();
    }

    private void fillContent() {
        ComponentCalculator calculator = new ComponentCalculator(this.request);

        try {
            this.fullImageUrl = calculator.getFullImageUrl(this.resource);
            this.videoUrl = calculator.getVideoUrl(this.resource);
            this.isVideo = calculator.getIsVideo(this.resource);
            //INFO: I'm always accessing from the assetsharing node, so i have to get the main component node
	        Resource parentResource = calculator.getMainComponentResource(this.resource);
	        if(parentResource != null) {
	            this.content = parentResource.adaptTo(SocialContent.class);
                this.facebookAppId = this.content.facebookAppId;
                this.assetId = this.content.assetId;
                this.isDeepLinkingEnabled = this.content.enableDeepLinking != null && this.content.enableDeepLinking.equals("true") && StringUtils.isNotBlank(this.assetId);
                ResourceResolver resolver = this.request.getResourceResolver();
                PageManager manager = resolver.adaptTo(PageManager.class);
                Page page = manager.getContainingPage(parentResource);
                String mainPagePath = page.getPath();
                if(mainPagePath.lastIndexOf(".html") == -1)
                    mainPagePath += ".html";
                mainPagePath = CommonUtils.getExternalUrl(this.request, mainPagePath);
                
                //mimic the behavior of dpe.js (remove /content/pwc)
                String serverName = this.request.getServerName();
                 if (!serverName.equals("dpe-preview.pwc.com") && !serverName.equals("dpe.pwc.com") && !serverName.equals("dpe-stg-preview.pwc.com") && !serverName.equals("dpe-stg.pwc.com") && !serverName.equals("localhost") && !serverName.equals("dpe-preview.pwcinternal.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-author-stgv2.pwc.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-extpub-qa2.pwc.com")&& !serverName.equals("dpe-qa2.pwc.com")&& !serverName.equals("pwc-com-dpe-qa2.pwc.com")&& !serverName.equals("dpe-qa2-preview.pwc.com")&& !serverName.equals("pwc-com-dpe-staging2.pwc.com")&& !serverName.equals("10.195.143.85")&& !serverName.equals("10.195.143.84")){
                	if (mainPagePath.indexOf("/content/pwc/") != -1){
                		mainPagePath = mainPagePath.replace("/content/pwc/", "/");
                	}
                }
                this.setMainSharedUrl(mainPagePath);
	            if(this.content != null && this.content.socialItems.length > 0) {
	                fillItems();
	            }
	        }
		} catch (Exception e) {
			ExceptionLogger.logException(e);
		}
    }

    private void fillItems() throws Exception {
        ShareCalculatorImpl shareCalculator = new ShareCalculatorImpl(this.request);
        JSONArray jsonArray = new JSONArray();
        for(String p: this.content.socialItems) {
            JSONObject item = new JSONObject(p);
            item.put("imageurl", this.fullImageUrl);
            item.put("videourl", this.videoUrl);
            jsonArray.put(item);
        }
        for (int i=0;i< jsonArray.length(); i++) {
            ContentItem socialItem = new ContentItem();
            socialItem.fromJSON(jsonArray.getJSONObject(i));
            if(!this.videoUrl.equals(StringUtils.EMPTY) && videoUrl != null) {
                String videoId = CommonUtils.getYoutubeId(videoUrl);
                if(videoId != null) {
                    this.videoUrl = shareCalculator.parseYoutubeUrl(socialItem.getSocialchannel(), videoId);
                    socialItem.setVideourl(this.videoUrl);
                }
            }
            socialItem.setPwcimageSizeSufix(shareCalculator.getImageUrlResizeSuffix(socialItem.getSocialchannel()));
            socialItem.setDefaultHeight(shareCalculator.getDefaultHeight(socialItem.getSocialchannel()));
            socialItem.setDefaultWidth(shareCalculator.getDefaultWidth(socialItem.getSocialchannel()));
            socialItem.setShareUrl(shareCalculator.getShareUrl(socialItem.getSocialchannel(), false));
            this.items.add(socialItem);
        }
    }

    public ArrayList<ContentItem> getItems() {
        return this.items;
    }

    public void setItems(ArrayList<ContentItem> items) {
        this.items = items;
    }

    public String getMainSharedUrl() {
        return mainSharedUrl;
    }

    public void setMainSharedUrl(String mainSharedUrl) {
        this.mainSharedUrl = mainSharedUrl;
    }

    public Boolean getIsVideo() { return isVideo; }

    public void setIsVideo(Boolean video) { isVideo = video; }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public String getAssetId() {
        return assetId;
    }

    public Boolean getIsDeepLinkingEnabled() {
        return isDeepLinkingEnabled;
    }
}
