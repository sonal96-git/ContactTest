package com.pwc.model.components.typepadblog;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;

@Model(adaptables = Resource.class)
public class TypePadModel {
    @Inject
    public String widgetId;

    @Inject
    @Optional
    public String category;

    @Inject
    @Optional
    public String blogtitle;

    @Inject
    public String display;

    @Inject
    @Optional
    public Long maxresults;

    @Inject @Optional
    public String linktargettype;
}