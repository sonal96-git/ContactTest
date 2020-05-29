package com.pwc.model.components.inlinequote;

import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

import javax.inject.Inject;

@Model(adaptables = Resource.class)
public class InlineQuote {
    @Inject @Optional
    private String quoteText;

    @Inject @Optional
    private String quoteAuthor;

    @Inject @Optional
    private String quoteSource;

    @Inject @Optional
    private Boolean addMargin;

	public String getQuoteText() {
		return quoteText;
	}

	public String getQuoteAuthor() {
		return quoteAuthor;
	}

	public String getQuoteSource() {
		return quoteSource;
	}

	public Boolean getAddMargin() {
		return addMargin;
	}
	
	public Boolean getIsFooterEnabled() {
		return this != null && (!StringUtils.isBlank(this.quoteAuthor) || !StringUtils.isBlank(this.quoteSource));
	}
}