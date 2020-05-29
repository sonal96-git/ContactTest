package com.pwc.wcm.model;

import org.apache.commons.lang.StringUtils;

import com.day.cq.wcm.foundation.Image;

import java.util.List;


public class MarqueeModel {


    private String westColor;
    private String westBackgroundUrl;
    private String westHeader;
    private String westHeaderUrl;
    private String westSubHeader;
    private String westSubHeaderUrl;

    private String northColor;
    private String northBackgroundUrl;
    private String northHeader;
    private String northHeaderUrl;
    private String northSubHeader;
    private String northSubHeaderUrl;
    
    private String eastColor;
    private String eastBackgroundUrl;
    private String eastHeader;
    private String eastHeaderUrl;
    private String eastSubHeader;
    private String eastSubHeaderUrl;
    private String eastHtmlOverride;
    
    private String southColor;
    private String southBackgroundUrl;
    private String southHeader;
    private String southHeaderUrl;
    private String southSubHeader;
    private String southSubHeaderUrl;

    private Image westBackground;
    private Image northBackground;
    private Image eastBackground;
    private Image southBackground;

	private String marqueeHeading;
	private String marqueeDescription;
	private String bannerImage;
	private Link leftCTA;
	private List<Link> actions;
    private Image backgroundImage;
    private Image mobileImage;
	
    private String primaryText;
    private String caption;
    private Link primaryCtaLink;
    private List<Link> secondaryCtaLink;
    private String cssWrapper;

    private String videoDivPrefix;
    private String videoType;
    private String youtubeVideoID;
    private String limelightPlayerID;
	private String limelightVideoID;
	private String ooyalaPlayerID;
	private String ooyalaVideoID;


    public boolean isShow() {
        return !StringUtils.isBlank(this.primaryText);// && StringUtils.isBlank(backgroundImage));
    }
    
	public String getMarqueeHeading() {
		return marqueeHeading;
	}

	public void setMarqueeHeading(String marqueeHeading) {
		this.marqueeHeading = marqueeHeading;
	}

	public String getMarqueeDescription() {
		return marqueeDescription;
	}

	public void setMarqueeDescription(String marqueeDescription) {
		this.marqueeDescription = marqueeDescription;
	}

	public String getBannerImage() {
		return bannerImage;
	}

	public void setBannerImage(String bannerImage) {
		this.bannerImage = bannerImage;
	}

	public Link getLeftCTA() {
		return leftCTA;
	}

	public void setLeftCTA(Link leftCTA) {
		this.leftCTA = leftCTA;
	}

	public List<Link> getActions() {
		return actions;
	}

	public void setActions(List<Link> actions) {
		this.actions = actions;
	}

    public String getPrimaryText() {
        return primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public Link getPrimaryCtaLink() {
        return primaryCtaLink;
    }

    public void setPrimaryCtaLink(Link primaryCtaLink) {
        this.primaryCtaLink = primaryCtaLink;
    }

    public List<Link> getSecondaryCtaLink() {
        return secondaryCtaLink;
    }

    public void setSecondaryCtaLink(List<Link> secondaryCtaLink) {
        this.secondaryCtaLink = secondaryCtaLink;
    }

    public void setCssWrapper(String cssWrapper) {
        this.cssWrapper = cssWrapper;
    }

    public String getCssWrapper() {
        return cssWrapper;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

	public Image getMobileImage() {
		return mobileImage;
	}

	public void setMobileImage(Image mobileImage) {
		this.mobileImage = mobileImage;
	}

    public String getVideoType() {
		return videoType;
	}

	public void setVideoType(String videoType) {
		this.videoType = videoType;
	}

	public String getYoutubeVideoID() {
		return youtubeVideoID;
	}

	public void setYoutubeVideoID(String youtubeVideoID) {
		this.youtubeVideoID = youtubeVideoID;
	}

    public String getLimelightPlayerID() {
		return limelightPlayerID;
	}

	public void setLimelightPlayerID(String limelightPlayerID) {
		this.limelightPlayerID = limelightPlayerID;
	}

	public String getLimelightVideoID() {
		return limelightVideoID;
	}

	public void setLimelightVideoID(String limelightVideoID) {
		this.limelightVideoID = limelightVideoID;
	}

    public String getOoyalaPlayerID() {
		return ooyalaPlayerID;
	}

	public void setOoyalaPlayerID(String ooyalaPlayerID) {
		this.ooyalaPlayerID = ooyalaPlayerID;
	}

	public String getOoyalaVideoID() {
		return ooyalaVideoID;
	}

	public void setOoyalaVideoID(String ooyalaVideoID) {
		this.ooyalaVideoID = ooyalaVideoID;
	}

    public String getVideoDivPrefix() {
        return videoDivPrefix;
    }

    public void setVideoDivPrefix(String videoDivPrefix) {
        this.videoDivPrefix = videoDivPrefix;
    }

    public String getWestColor() {
        return westColor;
    }

    public void setWestColor(String westColor) {
        this.westColor = westColor;
    }

    public Image getWestBackground() {
        return westBackground;
    }

    public void setWestBackground(Image westBackground) {
        this.westBackground = westBackground;
    }

    public String getWestHeader() {
        return westHeader;
    }

    public void setWestHeader(String westHeader) {
        this.westHeader = westHeader;
    }

    public String getWestHeaderUrl() {
        return westHeaderUrl;
    }

    public void setWestHeaderUrl(String westHeaderUrl) {
        this.westHeaderUrl = westHeaderUrl;
    }

    public String getWestSubHeader() {
        return westSubHeader;
    }

    public void setWestSubHeader(String westSubHeader) {
        this.westSubHeader = westSubHeader;
    }

    public String getWestSubHeaderUrl() {
        return westSubHeaderUrl;
    }

    public void setWestSubHeaderUrl(String westSubHeaderUrl) {
        this.westSubHeaderUrl = westSubHeaderUrl;
    }

    public String getNorthColor() {
        return northColor;
    }

    public void setNorthColor(String northColor) {
        this.northColor = northColor;
    }

    public Image getNorthBackground() {
        return northBackground;
    }

    public void setNorthBackground(Image northBackground) {
        this.northBackground = northBackground;
    }

    public String getNorthHeader() {
        return northHeader;
    }

    public void setNorthHeader(String northHeader) {
        this.northHeader = northHeader;
    }

    public String getNorthHeaderUrl() {
        return northHeaderUrl;
    }

    public void setNorthHeaderUrl(String northHeaderUrl) {
        this.northHeaderUrl = northHeaderUrl;
    }

    public String getNorthSubHeader() {
        return northSubHeader;
    }

    public void setNorthSubHeader(String northSubHeader) {
        this.northSubHeader = northSubHeader;
    }

    public String getNorthSubHeaderUrl() {
        return northSubHeaderUrl;
    }

    public void setNorthSubHeaderUrl(String northSubHeaderUrl) {
        this.northSubHeaderUrl = northSubHeaderUrl;
    }

    public String getEastColor() {
        return eastColor;
    }

    public void setEastColor(String eastColor) {
        this.eastColor = eastColor;
    }

    public Image getEastBackground() {
        return eastBackground;
    }

    public void setEastBackground(Image eastBackground) {
        this.eastBackground = eastBackground;
    }

    public String getEastHeader() {
        return eastHeader;
    }

    public void setEastHeader(String eastHeader) {
        this.eastHeader = eastHeader;
    }

    public String getEastHeaderUrl() {
        return eastHeaderUrl;
    }

    public void setEastHeaderUrl(String eastHeaderUrl) {
        this.eastHeaderUrl = eastHeaderUrl;
    }

    public String getEastSubHeader() {
        return eastSubHeader;
    }

    public void setEastSubHeader(String eastSubHeader) {
        this.eastSubHeader = eastSubHeader;
    }

    public String getEastSubHeaderUrl() {
        return eastSubHeaderUrl;
    }

    public void setEastSubHeaderUrl(String eastSubHeaderUrl) {
        this.eastSubHeaderUrl = eastSubHeaderUrl;
    }

    public String getSouthColor() {
        return southColor;
    }

    public void setSouthColor(String southColor) {
        this.southColor = southColor;
    }

    public Image getSouthBackground() {
        return southBackground;
    }

    public void setSouthBackground(Image southBackground) {
        this.southBackground = southBackground;
    }

    public String getSouthHeader() {
        return southHeader;
    }

    public void setSouthHeader(String southHeader) {
        this.southHeader = southHeader;
    }

    public String getSouthHeaderUrl() {
        return southHeaderUrl;
    }

    public void setSouthHeaderUrl(String southHeaderUrl) {
        this.southHeaderUrl = southHeaderUrl;
    }

    public String getSouthSubHeader() {
        return southSubHeader;
    }

    public void setSouthSubHeader(String southSubHeader) {
        this.southSubHeader = southSubHeader;
    }

    public String getSouthSubHeaderUrl() {
        return southSubHeaderUrl;
    }

    public void setSouthSubHeaderUrl(String southSubHeaderUrl) {
        this.southSubHeaderUrl = southSubHeaderUrl;
    }

    public String getWestBackgroundUrl() {
    	if (!StringUtils.isBlank(westBackgroundUrl)) {
    		if (westBackgroundUrl.startsWith("#")) {
    			return "background:" + westBackgroundUrl + ";";
    		} else {
    			return "background: url(" + westBackgroundUrl + ") no-repeat;";
    		}
    	}
        return westBackgroundUrl;
    }

    public void setWestBackgroundUrl(String westBackgroundUrl) {
        this.westBackgroundUrl = westBackgroundUrl;
    }

    public String getNorthBackgroundUrl() {
        return northBackgroundUrl;
    }

    public void setNorthBackgroundUrl(String northBackgroundUrl) {
        this.northBackgroundUrl = northBackgroundUrl;
    }

    public String getEastBackgroundUrl() {
    	if (!StringUtils.isBlank(eastBackgroundUrl)) {
    		if (eastBackgroundUrl.startsWith("#")) {
    			return "background:" + eastBackgroundUrl + ";";
    		} else {
    			return "background: url(" + eastBackgroundUrl + ") no-repeat;";
    		}
    	}
        return eastBackgroundUrl;
    }

    public void setEastBackgroundUrl(String eastBackgroundUrl) {
        this.eastBackgroundUrl = eastBackgroundUrl;
    }

    public String getSouthBackgroundUrl() {
        return southBackgroundUrl;
    }

    public void setSouthBackgroundUrl(String southBackgroundUrl) {
        this.southBackgroundUrl = southBackgroundUrl;
    }

    public String getEastHtmlOverride() {
        return eastHtmlOverride;
    }

    public void setEastHtmlOverride(String eastHtmlOverride) {
        this.eastHtmlOverride = eastHtmlOverride;
    }
}
