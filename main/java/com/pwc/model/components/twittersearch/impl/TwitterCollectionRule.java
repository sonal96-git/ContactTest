package com.pwc.model.components.twittersearch.impl;

import com.pwc.model.components.twittersearch.ITwitterTimelineRule;
import com.pwc.model.components.twittersearch.TwitterModel;
import org.apache.commons.lang3.StringUtils;

public class TwitterCollectionRule implements ITwitterTimelineRule {

    private String URL_BASE_FORMAT = "https://twitter.com/%s/timelines/%s";
    private String CSS_CLASS = "twitter-timeline";

    @Override
    public Boolean isMatch(TwitterModel model) {
        return model != null && model.widgetType.equals("collection");
    }

    @Override
    public String getUrl(TwitterModel model) {
        if(model == null || StringUtils.isBlank(model.twitterUsername) || StringUtils.isBlank(model.collectionId)) return StringUtils.EMPTY;
        return String.format(URL_BASE_FORMAT, model.twitterUsername, model.collectionId);
    }

    @Override
    public String getCssClass(TwitterModel model) {
        return CSS_CLASS;
    }

    @Override
    public Boolean isValid(TwitterModel model) {
        return model != null && !StringUtils.isBlank(model.collectionId) && !StringUtils.isBlank(model.twitterUsername);
    }
}
