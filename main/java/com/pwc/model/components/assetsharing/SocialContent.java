package com.pwc.model.components.assetsharing;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;

@Model(adaptables = Resource.class)
public class SocialContent  {
    @Inject
    public String[] socialItems;

    @Inject
    public String socialLabel;

    @Inject
    public String facebookAppId;

    @Inject @Optional
    public String assetId;

    @Inject @Optional
    public String enableDeepLinking;
}
