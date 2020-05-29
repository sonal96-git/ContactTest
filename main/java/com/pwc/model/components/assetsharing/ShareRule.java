package com.pwc.model.components.assetsharing;

import com.pwc.wcm.utils.CommonUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

public abstract class ShareRule {
    private String shareUrlSufix;
    private int assetWidth;
    private int assetHeight;
    public abstract Boolean isMatch(SocialItem item);
    public abstract Boolean isMatch(String socialLabel);

    public abstract String parseYoutubeUrl(String youtubeId);

    public String getShareUrl(SlingHttpServletRequest request, ComponentCalculator calculator, Boolean returnEncodedUrl) throws Exception {
        Resource comp = calculator.getMainComponentResource(request.getResource());
        String sharePath = comp.getPath();
        sharePath += getShareUrlSufix();
        String externalUrl = CommonUtils.getExternalUrl(request, sharePath);
        
        //mimic the behavior of dpe.js (remove /content/pwc)
        String serverName = request.getServerName();
         if (!serverName.equals("dpe-preview.pwc.com") && !serverName.equals("dpe.pwc.com") && !serverName.equals("dpe-stg-preview.pwc.com") && !serverName.equals("dpe-stg.pwc.com") && !serverName.equals("localhost") && !serverName.equals("dpe-preview.pwcinternal.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-author-stgv2.pwc.com")&& !serverName.equals("pwc-az-origin-author-qa2.pwc.com")&& !serverName.equals("pwc-az-origin-extpub-qa2.pwc.com")&& !serverName.equals("dpe-qa2.pwc.com")&& !serverName.equals("pwc-com-dpe-qa2.pwc.com")&& !serverName.equals("dpe-qa2-preview.pwc.com")&& !serverName.equals("pwc-com-dpe-staging2.pwc.com")&& !serverName.equals("10.195.143.85")&& !serverName.equals("10.195.143.84")){
        	if (externalUrl.indexOf("/content/pwc/") != -1){
        		externalUrl = externalUrl.replace("/content/pwc/", "/");
        	}
        }
        
        sharePath = returnEncodedUrl ? CommonUtils.urlEncode(externalUrl) : externalUrl;
        return sharePath;
    }

    public int getAssetWidth() {
        return assetWidth;
    }

    public void setAssetWidth(int assetWidth) {
        this.assetWidth = assetWidth;
    }

    public int getAssetHeight() { return assetHeight; }

    public void setAssetHeight(int assetHeight) {
        this.assetHeight = assetHeight;
    }

    public String getShareUrlSufix() {
        return shareUrlSufix;
    }

    public void setShareUrlSufix(String shareUrlSufix) {
        this.shareUrlSufix = shareUrlSufix;
    }
}
