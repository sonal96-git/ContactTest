package com.pwc.query.controllers.models.contents;

public class PageLink {
    String pageLink;
    int page;
    int pos;

    public PageLink(String link,int page , int position){
        this.pageLink = link;
        this.page = page;
        this.pos = position;
    }
    public String getPageLink() {
        return pageLink;
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
