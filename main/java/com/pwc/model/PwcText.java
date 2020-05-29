package com.pwc.model;

import org.apache.sling.api.resource.*;

import org.apache.sling.models.annotations.Model;
import javax.inject.*;

@Model (adaptables=Resource.class)
public class PwcText {
	
	@Inject
	private String header;
	
	@Inject
	private String content;

	public String getHeader() {
		return header;
	}

	public String getContent() {
		return content;
	}

	
}
