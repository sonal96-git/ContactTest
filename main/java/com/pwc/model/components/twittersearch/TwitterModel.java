package com.pwc.model.components.twittersearch;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;

@Model(adaptables = Resource.class)
public class TwitterModel {
    @Inject
    @Optional
    public String widgetId;

    @Inject
    @Optional
    public String twittertitle;

    @Inject
    public String widgetType;

    @Inject
    @Optional
    public String twitterUsername;

    @Inject
    @Optional
    public String collectionId;

    @Inject
    @Optional
    public String tweetUrl;

    @Inject
    @Optional
    public String listName;

    @Inject
    @Optional
    public String limit;

    @Inject
    @Optional
    public String height;

    @Inject
    @Optional
    public String[] chrome;

    @Inject
    @Optional String advancedConfiguration;
}
