package com.pwc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reviewed 16/07/2012
 *
 */
public class Link {

    private boolean openInNewWindow;
	private String text;
	private String url;
	private String linkAddlCSS;
	private int level;
	private List<Link> childrenLinks = new ArrayList<Link>();
	private String resolvedUrl;
	
	public String getResolvedUrl() {
		return resolvedUrl;
	}

	public void setResolvedUrl(String resolvedUrl) {
		this.resolvedUrl = resolvedUrl;
	}

	public Link() { }

	public Link(String text, String url, boolean openInNewWindow, String linkAddlCSS, int level) {
		super();
		this.openInNewWindow = openInNewWindow;
		this.text = text;
		this.url = url;
		this.linkAddlCSS = linkAddlCSS;
		this.level = level;
	}

	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url.replace(".html","");
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLinkAddlCSS() {
		return linkAddlCSS;
	}

	public void setLinkAddlCSS(String linkAddlCSS) {
		this.linkAddlCSS = linkAddlCSS;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public List<Link> getChildrenLinks() {
		return childrenLinks;
	}

	public void setChildrenLinks(List<Link> childrenLinks) {
		this.childrenLinks = childrenLinks;
	}
	
}
