package com.pwc.query.controllers.models.elements;

import com.pwc.query.controllers.models.Content;
import com.pwc.query.enums.PlayerOption;
import org.apache.commons.lang.StringUtils;

public class VideoPlayer {
	
	private String link;
	private String image;
	private String flashplayer;
	private String width;
	private String height;
	private String aspectratio;
	private String title;
	private String description;
	private boolean autostart;
	
	
	public VideoPlayer(Content content) {
	
		this.link = content.getItemUrl();
		this.image = content.getImage();
		this.flashplayer = PlayerOption.FLASH_PLAYER.toString();
		this.width = PlayerOption.WIDTH.toString();
		this.height = PlayerOption.HEIGHT.toString();
		this.aspectratio = PlayerOption.ASPECTRATIO.toString();
		this.title = !StringUtils.isBlank(content.getCollectionTitle()) ? content.getCollectionTitle():(!StringUtils.isBlank(content.getTitle()) ? content.getTitle() : content.getName() ) ;
		this.description = content.getDescription();
		this.autostart = false;
	}
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getFlashplayer() {
		return flashplayer;
	}
	public void setFlashplayer(String flashplayer) {
		this.flashplayer = flashplayer;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getAspectratio() {
		return aspectratio;
	}
	public void setAspectratio(String aspectratio) {
		this.aspectratio = aspectratio;
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
	public boolean isAutostart() {
		return autostart;
	}
	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}
	
	

}
