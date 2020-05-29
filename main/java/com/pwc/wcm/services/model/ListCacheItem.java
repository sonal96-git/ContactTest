package com.pwc.wcm.services.model;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;

/**
 * Created with IntelliJ IDEA.
 * User: ken.mitsumoto
 * Date: 2/13/14
 * Time: 12:48 PM
 */
public class ListCacheItem {

    public static final Logger log = LoggerFactory.getLogger(ListCacheItem  .class);

    public static final String ORDER_BY_DATE          = "orderByDate";

    public static final String ORDER_BY_TITLE         = "orderByTitle";

    public static final String IMAGE_CONTENT_RESOURCE = "image";

    public static final String PUBLISH_DATE_PROPERTY  = "publishDate";

    public static final String EXTERNAL_URL_PROPERTY  = "externalUrl";

    public static final String JCR_TITLE_PROPERTY     = "jcr:title";

    public static final String CQ_LAST_MODIFIED_KEY   = "cq:lastModified";

    public static final String CQ_LAST_PUBLISHED_KEY  = "cq:lastModified";

    public static final String CQ_PAGE_TYPE           = "cq:Page";

    public static final String DAM_ASSET_TYPE         = "dam:Asset";

    public static final String NT_UNSTRUCTURED_TYPE   = "nt:unstructured";

    public static final String DAM_CONTENT_PATH_PREFI = "/content/dam/";

    public static final String DAM_TITLE_KEY          = "dc:title";

    public static final String DAM_MODIFY_DATE_KEY    = "xmp:ModifyDate";

    public static final String DAM_CREATE_DATE_KEY    = "xmp:CreateDate";

    public static final String DAM_PUBLISH_DATE_KEY   = "dc:publishDate";

    public static final String DAM_EXTERNAL_URL_KEY   = "dc:externalUrl";

    public static final String ARTICLE_TYPES_NAME     = "article-types";


    private String path;
    private String title;
    private String description;
    private String createDate;
    private String publishDate;
    private String imageContentResourcePath;
    private String externalUrl;
    private String assetType;
    private String name;
    private String resourceType;
    private String damExternalUrl;
    private String videoDamPath;
    private String uuid;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateDate() { return createDate; }

    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getImageContentResourcePath() {
        return imageContentResourcePath;
    }

    public void setImageContentResourcePath(String imageContentResourcePath) {
        this.imageContentResourcePath = imageContentResourcePath;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getDamExternalUrl() {
        return damExternalUrl;
    }

    public void setDamExternalUrl(String damExternalUrl) {
        this.damExternalUrl = damExternalUrl;
    }

    public String getVideoDamPath() {
        return videoDamPath;
    }

    public void setVideoDamPath(String videoDamPath) {
        this.videoDamPath = videoDamPath;
    }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public ListCacheItem clone() {
        ListCacheItem clone = new ListCacheItem();
        clone.setPath(path);
        clone.setName(name);
        clone.setTitle(title);
        clone.setDescription(description);
        clone.setCreateDate(createDate);
        clone.setPublishDate(publishDate);
        clone.setImageContentResourcePath(imageContentResourcePath);
        clone.setExternalUrl(externalUrl);
        clone.setAssetType(assetType);
        clone.setResourceType(resourceType);
        clone.setDamExternalUrl(damExternalUrl);
        clone.setVideoDamPath(videoDamPath);
        clone.setUuid(uuid);
        return clone;
    }

    public static <T extends Adaptable> ListCacheItem toListCacheItem(T t, TagManager tagManager){
        ListCacheItem listCacheItem = new ListCacheItem();
        if(t.adaptTo(Resource.class) != null && t.adaptTo(Resource.class).isResourceType(CQ_PAGE_TYPE)) {
            Page p = t.adaptTo(Page.class);
            listCacheItem.setPath(p.getPath());
            listCacheItem.setTitle(p.getTitle());
            listCacheItem.setPublishDate(p.getProperties().get(PUBLISH_DATE_PROPERTY, "").toString());
            if(p.getContentResource(IMAGE_CONTENT_RESOURCE) != null) {
                listCacheItem.setImageContentResourcePath(p.getContentResource(IMAGE_CONTENT_RESOURCE).getPath());
            }
            listCacheItem.setExternalUrl(p.getProperties().get(EXTERNAL_URL_PROPERTY, "").toString());
            listCacheItem.setDescription(p.getDescription());
            listCacheItem.setResourceType(p.adaptTo(Resource.class).getResourceType());

        } else if((t.adaptTo(Resource.class) != null && t.adaptTo(Resource.class).isResourceType(DAM_ASSET_TYPE)) ||
                ((Resource)t).getResourceType().equals(DAM_ASSET_TYPE)) {
            // list should only contain Assets.
            // metadata is only returned by TagManager search which isn't relevant here.

            Asset a = t.adaptTo(Asset.class);
            if(a == null) {
                a = (Asset)t;
            }

            listCacheItem.setPath(a.getPath());
            listCacheItem.setName(a.getName());
            listCacheItem.setCreateDate(a.getMetadataValue(DAM_CREATE_DATE_KEY));
            listCacheItem.setPublishDate(a.getMetadataValue(DAM_PUBLISH_DATE_KEY).toString());
            listCacheItem.setDamExternalUrl(a.getMetadataValue(DAM_EXTERNAL_URL_KEY).toString());
            listCacheItem.setUuid(a.getMetadataValue("dc:uuid"));
            listCacheItem.setResourceType(a.adaptTo(Resource.class).getResourceType());

            if(a.getMetadata(DAM_TITLE_KEY) instanceof Object[]) {
                listCacheItem.setTitle(((Object[]) a.getMetadata(DAM_TITLE_KEY))[0].toString());
            } else {
                listCacheItem.setTitle(a.getMetadataValue(DAM_TITLE_KEY));
            }

            Tag[] metadataTags = tagManager.getTags(a.adaptTo(Resource.class).getChild("jcr:content").getChild("metadata"));
            Tag articleTypeTag = null;
            for(int i=0; i<metadataTags.length; i++) {
                Tag tag = metadataTags[i];
                if(tag.getParent() != null && tag.getParent().getName().equalsIgnoreCase(ARTICLE_TYPES_NAME)){
                    articleTypeTag = tag;
                    break;
                }
            }
            if(articleTypeTag != null) {
                listCacheItem.setAssetType(articleTypeTag.getName());
            }
        }
        return listCacheItem;
    }

    public static class ListCacheItemComparator implements Comparator<ListCacheItem> {
        private String order;
        private boolean descending;

        public ListCacheItemComparator(String order) {
            this(order, false);
        }

        public ListCacheItemComparator(String order, boolean descending) {
            this.order = order;
            this.descending = descending;
        }

        public int compare(ListCacheItem left, ListCacheItem right) {
            int comp = 0;
            if(order.equals(ORDER_BY_TITLE)) {
                comp = left.getTitle().toLowerCase().compareTo(right.getTitle().toLowerCase());

            } else {
                comp = parseDateString(left.getPublishDate()).compareTo(parseDateString(right.getPublishDate()));
            }
            return (comp != 0 && descending) ? -comp : comp;
        }
    }

    private static Date parseDateString(String dateAsString){
        try {
            return new SimpleDateFormat("MM/dd/yy").parse(dateAsString);
        } catch(Exception e) {
            return new Date(Long.MIN_VALUE);
        }
    }



}
