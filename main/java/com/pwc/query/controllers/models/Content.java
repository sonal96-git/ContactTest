package com.pwc.query.controllers.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.foundation.Image;
import com.pwc.query.enums.AssetProps;
import com.pwc.query.enums.PageProps;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import javax.jcr.Value;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Content implements Comparable<Content>{

    private String name;
    private String path;
    private String relativePath;
	private String title;
    private String image;
    private String itemType;
    private String dateValue;
    private String orderByData;
    private String description;
    private String collectionTitle;
    private String itemVideoTranscriptLink;

    private String itemUrl;
    private String itemHeight;
    private String itemWidth;
    private String damSize;
    private String videoType;

    private String[] tags;

    private boolean isPage;
    private boolean isVideo;
    private boolean itemIsSubTag;
    private boolean newsletterDateTitle;

    private Date unformattedDate;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getOrderByData() {
        return orderByData;
    }

    public void setOrderByData(String orderByData) {
        this.orderByData = orderByData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getDateValue() {
        return dateValue;
    }


    public void setDateValue(String dateValue) {
        this.dateValue = dateValue;
    }

    public String getDamSize() {
        return damSize;
    }

    public void setDamSize(String damSize) {
        this.damSize = damSize;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    public String getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(String itemHeight) {
        this.itemHeight = itemHeight;
    }

    public String getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(String itemWidth) {
        this.itemWidth = itemWidth;
    }

    public boolean isPage() {
        return isPage;
    }

    public void setPage(boolean isPage) {
        this.isPage = isPage;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public boolean isItemIsSubTag() {
        return itemIsSubTag;
    }

    public void setItemIsSubTag(boolean itemIsSubTag) {
        this.itemIsSubTag = itemIsSubTag;
    }

    public boolean isNewsletterDateTitle() {
        return newsletterDateTitle;
    }

    public void setNewsletterDateTitle(boolean newsletterDateTitle) {
        this.newsletterDateTitle = newsletterDateTitle;
    }

    public String getItemVideoTranscriptLink() {
        return itemVideoTranscriptLink;
    }

    public void setItemVideoTranscriptLink(String itemVideoTranscriptLink) {
        this.itemVideoTranscriptLink = itemVideoTranscriptLink;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setPageAttr(Resource resource, ControllerBean controllerBean, List<String> fallbackImages) throws Exception  {

        Page page = controllerBean.getPageManager().getContainingPage(resource.getPath());
        ValueMap pageProp = page.getProperties();

        I18nPwC i18nPwC = controllerBean.getI18nPwC();

        this.name = resource.getName();
        this.relativePath = resource.getPath();
        
        if(!controllerBean.isEnabledTransformer()) {
            this.path = resource.getPath();
        }else {
            this.path = controllerBean.getDefaultDomainConf() == null ?
                    CommonUtils.getExternalUrl(controllerBean.getRequest(),resource.getPath()):
                    getExternalUrl(resource.getPath(),controllerBean);
        }

        this.image = page.getContentResource(PageProps.IMAGE.toString()) != null ? new Image(page.getContentResource(PageProps.IMAGE.toString())).getFileReference() : StringUtils.EMPTY;
        if(StringUtils.isEmpty(this.image) && fallbackImages.size()>0){
             Random randomizer = new Random();
             this.image = fallbackImages.get(randomizer.nextInt(fallbackImages.size()));
        }
        this.unformattedDate = pageProp.get(PageProps.PWC_RELEASE_DATE) != null ? ((GregorianCalendar) pageProp.get(PageProps.PWC_RELEASE_DATE)).getTime() : null;

        this.dateValue = pageProp.get(PageProps.PWC_RELEASE_DATE) != null ? formattedDate(((GregorianCalendar) pageProp.get(PageProps.PWC_RELEASE_DATE)).getTime()) : null;

        this.title = pageProp.get(PageProps.TITLE) != null ? (String) pageProp.get(PageProps.TITLE) : "";
        this.description = pageProp.get(PageProps.DESCRIPTION) != null ? (String) pageProp.get(PageProps.DESCRIPTION) : "";
        this.itemType = pageProp.get(PageProps.PRIMARY_TYPE) != null ? (String) pageProp.get(PageProps.PRIMARY_TYPE) : "";
        this.collectionTitle = pageProp.get(PageProps.PWC_RVP_TITLE) != null ? (String) pageProp.get(PageProps.PWC_RVP_TITLE) : "";
        this.tags = pageProp.get(PageProps.TAGS) != null ? ( pageProp.get(PageProps.TAGS) instanceof String[] ?
        		(String[]) pageProp.get(PageProps.TAGS) : new String[] {pageProp.get(PageProps.TAGS).toString() } ) : null;

        this.isPage = true;
        this.isVideo = false;
        this.itemIsSubTag = false;
        this.newsletterDateTitle = false;

        this.damSize = "";
        this.itemUrl = "";
        this.itemHeight = "";
        this.itemWidth = "";
        this.itemVideoTranscriptLink ="";

    }

    public void setAssetAttr(Resource resource, Node node, ControllerBean controllerBean) throws Exception {

        I18nPwC i18nPwC = controllerBean.getI18nPwC();

        this.itemType = "dam:AssetContent";

        this.name = resource.getName();
        if(!controllerBean.isEnabledTransformer()) {
            this.path = resource.getPath();
        }else {
            this.path = controllerBean.getDefaultDomainConf() == null ?
                    CommonUtils.getExternalUrl(controllerBean.getRequest(),resource.getPath()):
                    getExternalUrl(resource.getPath(),controllerBean);
        }
        this.image = resource.getPath();

        this.unformattedDate = node.hasProperty(AssetProps.PWC_RELEASE_DATE.toString()) ?
                node.getProperty(AssetProps.PWC_RELEASE_DATE.toString()).getValue().getDate().getTime() : null;

        this.dateValue = node.hasProperty(AssetProps.PWC_RELEASE_DATE.toString()) ?
                formattedDate(node.getProperty(AssetProps.PWC_RELEASE_DATE.toString()).getValue().getDate().getTime()): null;

        this.title = node.hasProperty(AssetProps.TITLE.toString()) ? (node.getProperty(AssetProps.TITLE.toString()).isMultiple() ?
                node.getProperty(AssetProps.TITLE.toString()).getValues()[0].toString() : node.getProperty(AssetProps.TITLE.toString()).getValue().toString()):this.name;

        this.description = node.hasProperty(AssetProps.DESCRIPTION.toString()) ? (node.getProperty(AssetProps.DESCRIPTION.toString()).isMultiple() ?
                node.getProperty(AssetProps.DESCRIPTION.toString()).getValues()[0].toString() : node.getProperty(AssetProps.DESCRIPTION.toString()).getValue().toString()):"";

        this.tags = node.hasProperty(AssetProps.TAGS.toString()) ? getStringArray(node.getProperty(AssetProps.TAGS.toString()).getValues()) : null;

        this.isPage = false;
        this.itemIsSubTag = false;
        this.newsletterDateTitle = false;
        this.isVideo = node.getProperty(AssetProps.FORMAT.toString()).getValue().toString().equals(AssetProps.FORMAT_VIDEO.toString());

        this.videoType = node.hasProperty(AssetProps.VIDEO_TYPE.toString()) ? node.getProperty(AssetProps.VIDEO_TYPE.toString()).getValue().toString() : "";

        String size = node.hasProperty(AssetProps.DAM_SIZE.toString()) ? (node.getProperty(AssetProps.DAM_SIZE.toString()).isMultiple() ?
                node.getProperty(AssetProps.DAM_SIZE.toString()).getValues()[0].toString() : node.getProperty(AssetProps.DAM_SIZE.toString()).getValue().toString()) : "";

        long damSizeKB = StringUtils.isNotBlank(size) ? Math.round(Long.valueOf(size)/ 1024) : 0 ;
        this.damSize = damSizeKB > 1024 ? Math.round(damSizeKB/1024 )+" MB": damSizeKB+" KB";

        this.itemVideoTranscriptLink =node.hasProperty(AssetProps.TRANSCRIP_LINK.toString()) ? (node.getProperty(AssetProps.TRANSCRIP_LINK.toString()).isMultiple() ?
                node.getProperty(AssetProps.TRANSCRIP_LINK.toString()).getValues()[0].toString() : node.getProperty(AssetProps.TRANSCRIP_LINK.toString()).getValue().toString()) : "";

        this.itemUrl = this.isVideo ? (node.hasProperty(AssetProps.VIDEO_URL.toString()) ? (node.getProperty(AssetProps.VIDEO_URL.toString()).isMultiple() ?
                node.getProperty(AssetProps.VIDEO_URL.toString()).getValues()[0].toString() : node.getProperty(AssetProps.VIDEO_URL.toString()).getValue().toString()) : "") : "";

        this.itemHeight = this.isVideo ? (node.hasProperty(AssetProps.VIDEO_HEIGHT.toString()) ? (node.getProperty(AssetProps.VIDEO_HEIGHT.toString()).isMultiple() ?
                node.getProperty(AssetProps.VIDEO_HEIGHT.toString()).getValues()[0].toString() : node.getProperty(AssetProps.VIDEO_HEIGHT.toString()).getValue().toString()) : "") : "";

        this.itemWidth = this.isVideo ? (node.hasProperty(AssetProps.VIDEO_WIDTH.toString()) ? (node.getProperty(AssetProps.VIDEO_WIDTH.toString()).isMultiple() ?
                node.getProperty(AssetProps.VIDEO_WIDTH.toString()).getValues()[0].toString() : node.getProperty(AssetProps.VIDEO_WIDTH.toString()).getValue().toString()) : "") : "";

    }

    private String getExternalUrl(String path, ControllerBean controllerBean ) throws Exception{

        String defaultDomain = (String) controllerBean.getDefaultDomainConf().getProperties().get("domain");
        String domainType = (String) controllerBean.getDefaultDomainConf().getProperties().get("domainType");
        LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(controllerBean.getRepository(), defaultDomain, domainType);

        return linkTransformerService.transformAEMUrl(path);
    }
    private String[] getStringArray(Value[] values) {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].toString();
        }
        return result;
    }

    @Override
    public int compareTo(Content content) {
        return Comparators.DATE.compare(this,content);
    }


    public static class Comparators {

        public static Comparator<Content> TITLE = new Comparator<Content>() {
            @Override
            public int compare(Content c1, Content c2) {
                return c1.title.compareTo(c2.title);
            }
        };

        public static Comparator<Content> DATE = new Comparator<Content>() {
            @Override
            public int compare(Content c1, Content c2) {

                int order = 1;
                DateFormat toDate = new SimpleDateFormat("dd/MM/yyyy");

                if ((c1.getDateValue() == null || c1.getDateValue().isEmpty()) &&
                    (c2.getDateValue() == null || c2.getDateValue().isEmpty())){ return 0; }

                if ((c1.getDateValue() == null || c1.getDateValue().isEmpty())){ return 1; }

                if ((c2.getDateValue() == null || c2.getDateValue().isEmpty())){ return -1; }

                try {
                    order = toDate.parse(c2.getDateValue()).compareTo(toDate.parse(c1.getDateValue()));
                    order = order * -1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return order;
            }
        };
        public static Comparator<Content> DATE_DESC = new Comparator<Content>() {
            @Override
            public int compare(Content c1, Content c2) {

                int order = 1;
                DateFormat toDate = new SimpleDateFormat("dd/MM/yyyy");

                if ((c1.getDateValue() == null || c1.getDateValue().isEmpty()) &&
                        (c2.getDateValue() == null || c2.getDateValue().isEmpty())){ return 0; }

                if ((c1.getDateValue() == null || c1.getDateValue().isEmpty())){ return 1; }

                if ((c2.getDateValue() == null || c2.getDateValue().isEmpty())){ return -1; }

                try {
                    order = toDate.parse(c2.getDateValue()).compareTo(toDate.parse(c1.getDateValue()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return order;
            }
        };
    }

    private String formattedDate(Date date) {

        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        String formatted = format1.format(date);
        return formatted;
    }
}
