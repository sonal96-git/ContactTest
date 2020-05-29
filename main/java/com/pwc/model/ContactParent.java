package com.pwc.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rjiang022 on 6/9/2015.
 */
@Model(adaptables = Resource.class)
public class ContactParent {
    @Inject
    @Named("cq:lastReplicationAction")
    @Optional
    private String lastReplicationAction;

    public String getLastReplicationAction() {
        return lastReplicationAction;
    }
}
