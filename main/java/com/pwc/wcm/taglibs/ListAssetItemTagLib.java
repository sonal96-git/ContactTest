package com.pwc.wcm.taglibs;

import java.text.SimpleDateFormat;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.scripting.jsp.util.TagUtil;

import com.pwc.wcm.services.model.ListCacheItem;

@SuppressWarnings("serial")
public class ListAssetItemTagLib extends BaseTagLib {

    private static final String DEFAULT_DATE_FORMAT     = "MMMM dd, yyyy";
    private static final String PUBLISH_DATE_FORMAT     = "MM/dd/yy";

    private static final String DATE_FORMAT_ATTRIBUTE   = "dateFormat";
    private static final String LIST_ITEM_ATTRIBUTE     = "listitem";
    private static final String PATH_ATTRIBUTE          = "path";
    private static final String RESOURCE_TYPE_ATTRIBUTE = "resourceType";
    private static final String TITLE_ATTRIBUTE         = "title";
    private static final String PUBLISH_DATE_ATTRIBUTE  = "publishDate";
    private static final String ASSET_TYPE_ATTRIBUTE    = "assetType";

    private static final String VIDEO_DAM_PATH 			= "/content/dam/pwc/videos";

    @Override
    protected int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
        Resource resource = request.getResource();

        ListCacheItem item = (ListCacheItem)request.getAttribute(LIST_ITEM_ATTRIBUTE);

        if(item != null) {

            if(item.getDamExternalUrl() != null && !item.getDamExternalUrl().isEmpty()) {
                setPageAttribute(PATH_ATTRIBUTE, item.getDamExternalUrl());
            } else if(item.getPath().startsWith(VIDEO_DAM_PATH)) {
            	setPageAttribute(PATH_ATTRIBUTE, generateThankYouPageLinkForVideo(item.getUuid(), item.getName()) );
            } else {
                setPageAttribute(PATH_ATTRIBUTE, item.getPath());
            }
            setPageAttribute(RESOURCE_TYPE_ATTRIBUTE, resource.getResourceType());
            setPageAttribute(TITLE_ATTRIBUTE, item.getTitle());

            String sDate = item.getPublishDate();
            try {
                String dateFormat = getComponentContextStringAttribute(DATE_FORMAT_ATTRIBUTE, getComponentContext()) != null ? getComponentContextStringAttribute(DATE_FORMAT_ATTRIBUTE, getComponentContext()) : DEFAULT_DATE_FORMAT;
                if(sDate != null && !sDate.isEmpty()) {
                    setPageAttribute(PUBLISH_DATE_ATTRIBUTE, new SimpleDateFormat(dateFormat).format(new SimpleDateFormat(PUBLISH_DATE_FORMAT).parse(sDate)));
                } else {
                    setPageAttribute(PUBLISH_DATE_ATTRIBUTE, new SimpleDateFormat(dateFormat).parse(item.getCreateDate()));
                }
            } catch(Exception e) {
                // do nothing?
            }

            setPageAttribute(ASSET_TYPE_ATTRIBUTE, item.getAssetType());
        }
        return EVAL_BODY_INCLUDE;
    }

    private String generateThankYouPageLinkForVideo(String uuid, String name) {
        StringBuilder path = new StringBuilder("/thank-you.");
        if(uuid != null && !uuid.isEmpty()) {
            path.append(uuid);
        } else {
            path.append(name);
        }
        path.append(".html");
        return path.toString();
    }
}
