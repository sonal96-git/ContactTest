package com.pwc.wcm.taglibs;

import java.text.SimpleDateFormat;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.scripting.jsp.util.TagUtil;

import com.day.cq.wcm.foundation.Image;
import com.pwc.wcm.services.model.ListCacheItem;

@SuppressWarnings("serial")
public class ListPageItemTagLib extends BaseTagLib {

    private static final String DEFAULT_DATE_FORMAT     = "MMMM dd, yyyy";
    private static final String PUBLISH_DATE_FORMAT     = "MM/dd/yy";

    private static final String DATE_FORMAT_ATTRIBUTE   = "dateFormat";
    private static final String LIST_ITEM_ATTRIBUTE     = "listitem";
    private static final String IMAGE_ATTRIBUTE         = "image";
    private static final String PATH_ATTRIBUTE          = "path";
    private static final String RESOURCE_TYPE_ATTRIBUTE = "resourceType";
    private static final String TITLE_ATTRIBUTE         = "title";
    private static final String DESCRIPTION_ATTRIBUTE   = "pageDescription";
    private static final String PUBLISH_DATE_ATTRIBUTE  = "publishDate";
    private static final String EXTERNAL_URL_ATTRIBUTE  = "externalUrl";

    @Override
    protected int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
        Resource resource = request.getResource();

        ListCacheItem item = (ListCacheItem)request.getAttribute(LIST_ITEM_ATTRIBUTE);

        Resource imageResource = request.getResourceResolver().getResource(item.getImageContentResourcePath());
        if(imageResource != null) {
            request.setAttribute(IMAGE_ATTRIBUTE, new Image(imageResource));
        }
        setPageAttribute(PATH_ATTRIBUTE, item.getPath());
        setPageAttribute(RESOURCE_TYPE_ATTRIBUTE, resource.getResourceType());
        setPageAttribute(TITLE_ATTRIBUTE, item.getTitle());
        setPageAttribute(DESCRIPTION_ATTRIBUTE, item.getDescription());

        String sDate = item.getPublishDate();
        if(sDate != null && ! sDate.isEmpty()) {
            try{
                String dateFormat = getComponentContextStringAttribute(DATE_FORMAT_ATTRIBUTE, getComponentContext()) != null ? getComponentContextStringAttribute(DATE_FORMAT_ATTRIBUTE, getComponentContext()) : DEFAULT_DATE_FORMAT;
                setPageAttribute(PUBLISH_DATE_ATTRIBUTE, new SimpleDateFormat(dateFormat).format(new SimpleDateFormat(PUBLISH_DATE_FORMAT).parse(sDate)));
            } catch(Exception e) {
                // do nothing?
            }
        }
        if(!item.getExternalUrl().isEmpty()) {
            setPageAttribute(EXTERNAL_URL_ATTRIBUTE, item.getExternalUrl());
        }
        return EVAL_BODY_INCLUDE;
    }
}
