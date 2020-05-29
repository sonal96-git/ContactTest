package com.pwc.model.components.assetsharing.ComponentRules;

import com.pwc.model.components.assetsharing.ComponentRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;

public class ImageComponent extends ComponentRule {
    private static final String ASSET_PROPERTY = "fileReference";
    private static final String ASSET_RESOURCE_TYPE_PROPERTY = "sling:resourceType";
    private static final String ASSET_RESOURCE_TYPE_PROPERTY_VALUE_REGEX = "(/apps/)?pwc/components/content/image";
    private static final String MAIN_NODE_RESOURCE_TYPE_REGEX = "(/apps/)?pwc/components/content/image";
    private SlingHttpServletRequest request;

    public ImageComponent(SlingHttpServletRequest request) {
        this.request = request;
        setMainNodeResourceType(MAIN_NODE_RESOURCE_TYPE_REGEX);
    }
    @Override
    public Boolean isMatch(Resource resource) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        if(mainComponentResource == null) return false;
        Node node = mainComponentResource.adaptTo(Node.class);
        if(node == null) return false;
        if(!node.hasProperty(getSocialLabelProperty())) return false;
        if(!node.hasProperty(getSocialItemsProperty())) return false;
        if(!node.hasProperty(ASSET_RESOURCE_TYPE_PROPERTY)) return false;
        if(!node.getProperty(ASSET_RESOURCE_TYPE_PROPERTY).getString().matches(ASSET_RESOURCE_TYPE_PROPERTY_VALUE_REGEX)) return false;
        if(!node.hasProperty(ASSET_PROPERTY)) return false;
        return true;
    }

    @Override
    public Boolean isVideo(Resource resource) {
        return false;
    }

    @Override
    public String getImageUrl(Resource resource, boolean includeExtension) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        Node node = mainComponentResource.adaptTo(Node.class);
        String assetUrl = getUrl(node, this.request, includeExtension);
        return assetUrl;
    }

    @Override
    public String getVideoUrl(Resource resource) throws Exception {
        return StringUtils.EMPTY;
    }
}
