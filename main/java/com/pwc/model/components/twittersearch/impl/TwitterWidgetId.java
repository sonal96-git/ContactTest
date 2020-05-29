package com.pwc.model.components.twittersearch.impl;

import com.pwc.model.components.twittersearch.ITwitterTimelineRule;
import com.pwc.model.components.twittersearch.TwitterModel;
import org.apache.commons.lang3.StringUtils;

public class TwitterWidgetId implements ITwitterTimelineRule {
	
    private String URL_BASE_FORMAT = "https://twitter.com/search";
    private String CSS_CLASS = "twitter-timeline";

    @Override
    public Boolean isMatch(TwitterModel model) {
        return model != null && model.widgetType.equals("widgetid");
    }

    @Override
    public String getUrl(TwitterModel model) {
        if(model == null || StringUtils.isBlank(model.widgetId)) return StringUtils.EMPTY;
        return String.format(URL_BASE_FORMAT);
    }

    @Override
    public String getCssClass(TwitterModel model) {
        return CSS_CLASS;
    }

    @Override
    public Boolean isValid(TwitterModel model) {
        return model != null && !StringUtils.isBlank(model.widgetId);
    }
}
