package com.pwc.model;

import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
//some test
import javax.inject.*;

@Model(adaptables = Resource.class)
public class Html {
	@Inject @Optional
	private String htmlContent;

	public String getHtmlContent() {
		return htmlContent;
	}
	

}
