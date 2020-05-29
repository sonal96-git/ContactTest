package com.pwc.model;

import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.*;
import javax.inject.*;

@Model(adaptables = Resource.class)
public class Webtile {
	@Inject
	@Optional
	private String url;

	@Inject
	@Optional
	private String title;

	@Inject
	@Optional
	@Default(values = "[Insert Heading Here]")
	private String description;
	
	@Inject
	@Optional
	private String colorscheme;
	@Inject
	@Optional
	private String callToAction;
	
	
	public String getCallToAction() {
		return callToAction;
	}

	public String getColorscheme() {
		return colorscheme;
	}

	public String getUrl() {
		return url;
	}



	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}


}
