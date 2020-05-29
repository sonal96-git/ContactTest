package com.pwc.wcm.model;

/**
 * @author adamtrissel
 *
 *	Class to unmarshal JSON content in the JCR VideoComponent
 *
 */
public class VideoBean {

	private String title;
	private String url;
	private String titleLink;
	private String linkText;
	private String thumbnail;
	private String extendedDescription;
	
	public VideoBean() { }
	
	public String getExtendedDescription() {
		return extendedDescription;
	}

	public void setExtendedDescription(String extendedDescription) {
		this.extendedDescription = extendedDescription;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public VideoBean(String title, String url, String titleLink) {
		super();
		this.title = title;
		this.url = url;
		this.titleLink = titleLink;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getTitleLink() {
		return titleLink;
	}
	
	public void setTitleLink(String titleLink) {
		this.titleLink = titleLink;
	}

	public String getLinkText() {
		return linkText;
	}

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

//	public String getExtendedDescription() {
//		return extendedDescription;
//	}
//
//	public void setExtendedDescription(String extendedDescription) {
//		this.extendedDescription = extendedDescription;
//	}
	
}
