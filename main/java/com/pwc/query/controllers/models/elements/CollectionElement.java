package com.pwc.query.controllers.models.elements;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.wcm.utils.PageService;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CollectionElement extends ControllerElements {
	
	private Content item;
	
	private String itemName;
	private String itemHref;
	private String relativeItemHref;

	private String itemImage;
	private String itemImageFaceted;
	private String itemDescription;
	
	private String[] pageTags;
	private String[] unformattedTags;

	private List<String> itemTags;
	
	private String formattedDate;
	private String groupByLabel;
	private String groupByLabelPrevious;
	
	private String itemIndex;
	private String damSize;
	private String itemTitle;

	private boolean itemIsSubTag;
	private boolean newsletterDateTitle;
	
	private String videoType;

	public CollectionElement(Content content,CollectionElement preElement, int index ,ControllerBean controllerBean) {

		ValueMap componentProperties = controllerBean.getProperties();

		boolean displayTags = componentProperties.get(CollectionProps.DISPLAY_TAGS) != null ?
				componentProperties.get(CollectionProps.DISPLAY_TAGS).toString().equals("true") : false;

		List<String> displaySubTags = componentProperties.get(CollectionProps.DISPLAY_SUB_TAGS) != null ?
				Arrays.asList((String[]) componentProperties.get(CollectionProps.DISPLAY_SUB_TAGS)): new ArrayList<String>();


		this.itemIndex = RandomStringUtils.random(4,true,true);
		this.item      = content;
		this.itemName  = !StringUtils.isBlank(content.getCollectionTitle()) ? content.getCollectionTitle():(!StringUtils.isBlank(content.getTitle()) ? content.getTitle() : content.getName() ) ;
		this.isPage  = content.isPage();
		this.isVideo = content.isVideo();
		this.itemHref  = content.getPath() + (this.isPage ? ".html" : "");
		this.relativeItemHref = content.getRelativePath() + (this.isPage ? ".html" : "");
		this.itemImage = content.getImage();
		this.itemImageFaceted = content.getImage();
		this.itemUrl    =content.getItemUrl();
		this.itemHeight =content.getItemHeight();
		this.itemWidth  =content.getItemWidth();
		this.itemDescription  = content.getDescription();
		this.itemVideoTranscriptLink = content.getItemVideoTranscriptLink();
		this.itemTitle = content.getTitle();

		this.damSize =  content.getDamSize();
		
		this.pageTags =  content.getTags() != null ? getFormattedTags(content.getTags()): new String[0];
		this.itemTags = content.getTags() != null  && displayTags ?
				getShowTags(Arrays.asList(content.getTags()),displaySubTags,controllerBean): new ArrayList<String>();
		this.unformattedTags = content.getTags() != null ? content.getTags() : new String[0];
		
		this.formattedDate = content.getDateValue() !=null ? content.getDateValue() : "";

		this.groupByLabel  = content.getDateValue() !=null ? content.getDateValue() : "";//TODO nedd to create a method
		this.groupByLabelPrevious = preElement!=null && preElement.getFormattedDate()  !=null ? preElement.getFormattedDate():"";
		

		this.itemIsSubTag = content.isItemIsSubTag();
		this.newsletterDateTitle = content.isNewsletterDateTitle();

		this.videoType = content.getVideoType();
	}

	public Content getItem() {
		return item;
	}

	public void setItem(Content item) {
		this.item = item;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemHref() {
		return itemHref;
	}

	public void setItemHref(String itemHref) {
		this.itemHref = itemHref;
	}

	public String getRelativeItemHref() {
		return relativeItemHref;
	}

	public void setRelativeItemHref(String relativeItemHref) {
		this.relativeItemHref = relativeItemHref;
	}
	
	public String getItemImage() {
		return itemImage;
	}

	public void setItemImage(String itemImage) {
		this.itemImage = itemImage;
	}

	public String getItemImageFaceted() {
		return itemImageFaceted;
	}

	public void setItemImageFaceted(String itemImageFaceted) {
		this.itemImageFaceted = itemImageFaceted;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String[] getPageTags() {
		return pageTags;
	}

	public void setPageTags(String[] pageTags) {
		this.pageTags = pageTags;
	}

	public List<String>  getItemTags() {
		return itemTags;
	}

	public void setItemTags(List<String>  itemTags) {
		this.itemTags = itemTags;
	}

	public String[] getUnformattedTags() {
		return unformattedTags;
	}

	public void setUnformattedTags(String[] unformattedTags) {
		this.unformattedTags = unformattedTags;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public String getGroupByLabel() {
		return groupByLabel;
	}

	public void setGroupByLabel(String groupByLabel) {
		this.groupByLabel = groupByLabel;
	}

	public String getGroupByLabelPrevious() {
		return groupByLabelPrevious;
	}

	public void setGroupByLabelPrevious(String groupByLabelPrevious) {
		this.groupByLabelPrevious = groupByLabelPrevious;
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
	
	public String getItemIndex() {
		return itemIndex;
	}

	public void setItemIndex(String itemIndex) {
		this.itemIndex = itemIndex;
	}

	public String getDamSize() {
		return damSize;
	}

	public void setDamSize(String damSize) {
		this.damSize = damSize;
	}

	public String getItemTitle() {
		return itemTitle;
	}

	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}

	private String[] getFormattedTags(String[] tags){
		String[] formattedTags = new String[tags.length];
		for(int i=0; i < tags.length;i++ ) {
			formattedTags[i] =  tags[i].replace(":", "-").replace("/","-");
		}

		return formattedTags;
	}

	public String getVideoType() {
        return videoType;
    }

	private List<String> getShowTags(List<String> tags,List<String>  displaySubTags,ControllerBean controllerBean) {

		List<String> tagsName =  new ArrayList<>();

		PageService pageService = new PageService();
        Locale locale = new Locale(pageService.getLocale(controllerBean.getRequest(), controllerBean.getCurrentPage()));	

        Tag tagObj;
		for(String tag: tags) {

			if (!displaySubTags.contains(tag)) continue;

			TagManager tagManager = controllerBean.getResourceResolver().adaptTo(TagManager.class);

            tagObj = tagManager.resolve(tag);
            tagsName.add(tagObj.getTitle(locale));


		}
		return tagsName;
	}




}
