package com.pwc.model;
import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.*;

import javax.inject.*;

@Model(adaptables=Resource.class)
public class LinkListField {
	
	@Inject 
	private String text;

	public String getUrl() {
		return url;
	}
	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}
	public int getLevel() {
		return level;
	}
	public String getText() {
		return text;
	}
	@Inject 
	private String url;
	@Inject 
	private boolean openInNewWindow;
	@Inject 
	private int level;
	
}
