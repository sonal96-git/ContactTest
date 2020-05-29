package com.pwc.query.controllers.models.elements;


public class ControllerElements {

    protected String itemUrl;
    protected String itemWidth;
    protected String itemHeight;

    protected boolean isPage;
    protected boolean isVideo;

    protected String itemVideoTranscriptLink;

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

    public String getItemVideoTranscriptLink() {
        return itemVideoTranscriptLink;
    }

    public void setItemVideoTranscriptLink(String itemVideoTranscriptLink) {
        this.itemVideoTranscriptLink = itemVideoTranscriptLink;
    }

}
