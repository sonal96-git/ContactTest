package com.pwc.query.controllers.models.elements;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.pwc.ApplicationConstants;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.PageService;

public class FacetedElement extends ControllerElements {

	private int index;

	private String href;
	private String relativeHref;

	private String text;
	private String title;
	private String image;
	private List<String> tags;
	private String filterTags;

	private String publishDate;
	
	private VideoPlayer playerOptions;

	private String damSize;

	private String videoType;

	public FacetedElement(Content content, int index, ControllerBean controllerBean) {

		this.index = index;

        this.isPage  = content.isPage();
        this.isVideo = content.isVideo();

		this.href = content.getPath() + (this.isPage ? ".html" : "");
		this.relativeHref = content.getRelativePath() + (this.isPage ? ".html" : "");
		this.title = !StringUtils.isBlank(content.getCollectionTitle()) ? content.getCollectionTitle():(!StringUtils.isBlank(content.getTitle()) ? content.getTitle() : content.getName() ) ;
		this.image = content.getImage();
		this.itemUrl = content.getItemUrl();
		this.itemWidth = content.getItemWidth();
		String description = content.getDescription();
		this.text = controllerBean.isBrandSimplificationEnabled() && controllerBean.isCollectionV2() ?
			CommonUtils.getEllipsesString(description, ApplicationConstants.COLLECTIONV2_TEXT_LIMIT) :
			description;

		this.tags = content.getTags() != null ? Arrays.asList(content.getTags())  : new ArrayList<>();
		this.filterTags = getFilterTags(controllerBean);
		
		this.itemHeight = content.getItemHeight();
		this.publishDate = content.getDateValue() != null ? content.getDateValue() : "";
        this.itemVideoTranscriptLink = content.getItemVideoTranscriptLink();

		this.playerOptions = new VideoPlayer(content);

		this.damSize =  content.getDamSize();

		this.videoType = content.getVideoType();
	}

	public String getVideoType() {
        return videoType;
    }

	public String getDamSize() {
		return damSize;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getRelativeHref() {
		return relativeHref;
	}

	public void setRelativeHref(String relativeHref) {
		this.relativeHref = relativeHref;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}

    public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public VideoPlayer getPlayerOptions() {
		return playerOptions;
	}

	public void setPlayerOptions(VideoPlayer playerOptions) {
		this.playerOptions = playerOptions;
	}
	private String getFilterTags(ControllerBean controllerBean) {
		
		JSONObject jsonTag = null;
		JSONArray jsonTagArray = new JSONArray();
		
		List<List<String>> tagsList = controllerBean.getFilters();
		TagManager tagMan = controllerBean.getResourceResolver().adaptTo(TagManager.class);

		PageService pageService = new PageService();
        Locale locale = new Locale(pageService.getLocale(controllerBean.getRequest(), controllerBean.getCurrentPage()));

		try {
			
			
			for (List<String> list : tagsList) {
				
				for (String filterTag : list) {
					
					for (String elementTag : this.tags) {
						
						if(CommonsUtils.isMatch(elementTag,filterTag,controllerBean)) {
							
							Tag tag = tagMan.resolve(filterTag.trim());
							
							if(tag == null || CommonsUtils.isInArray(jsonTagArray,tag.getTagID())) continue;
							
			                jsonTag = new JSONObject();
			                jsonTag.put("tagsTitle", tag.getTitle(locale));
			                jsonTag.put("tagID", tag.getTagID());
							jsonTag.put("tag", filterTag);
	
							jsonTagArray.put(jsonTag);
							break;
						}
					}
				}
			}
		} catch (JSONException e) {
			ExceptionLogger.logException(e);
		}

		return jsonTagArray.toString();
	}

}
