package com.pwc.model.components.twittersearch.impl;

import com.pwc.model.components.twittersearch.ITwitterTimelineRule;
import com.pwc.model.components.twittersearch.TwitterModel;
import org.apache.commons.lang3.StringUtils;

public class TwitterProfileRule implements ITwitterTimelineRule {

    private String URL_BASE_FORMAT = "https://twitter.com/%s";
    private String CSS_CLASS = "twitter-timeline";

    @Override
    public Boolean isMatch(TwitterModel model) {
        return model != null && model.widgetType.equals("profile");
    }

    @Override
    public String getUrl(TwitterModel model) {
        if(model == null || StringUtils.isBlank(model.twitterUsername)) return StringUtils.EMPTY;
        return String.format(URL_BASE_FORMAT, model.twitterUsername);
    }

    @Override
    public String getCssClass(TwitterModel model) {
        return CSS_CLASS;
    }

    @Override
    public Boolean isValid(TwitterModel model) {
        return model != null && !StringUtils.isBlank(model.twitterUsername);
    }
}
