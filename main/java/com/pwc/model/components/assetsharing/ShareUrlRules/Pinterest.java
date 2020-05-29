package com.pwc.model.components.assetsharing.ShareUrlRules;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.model.components.assetsharing.ShareRule;
import com.pwc.model.components.assetsharing.SocialItem;
import com.pwc.model.components.assetsharing.ComponentCalculator;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public class Pinterest extends ShareRule {
    private static final String STRING_MATCH_RULE = "pinterest";
    private static final String SHARE_URL_SUFIX = "/pinterest.html";
    private static final int ASSET_WIDTH = 735;
    private static final int ASSET_HEIGHT = 1102;
    public Pinterest() {
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
        return null;
    }

    @Override
    public String getShareUrl(SlingHttpServletRequest request, ComponentCalculator calculator, Boolean returnEncodedUrl) throws Exception {
        Resource comp = calculator.getMainComponentResource(request.getResource());
        ResourceResolver resolver = request.getResourceResolver();
        PageManager manager = resolver.adaptTo(PageManager.class);
        Page page = manager.getContainingPage(comp);
        String mainPagePath = page.getPath();
        if(mainPagePath.lastIndexOf(".html") == -1)
            mainPagePath += ".html";
        mainPagePath = CommonUtils.getExternalUrl(request, mainPagePath);
        mainPagePath = returnEncodedUrl ? CommonUtils.urlEncode(mainPagePath) : mainPagePath;
        return mainPagePath;
    }
}
