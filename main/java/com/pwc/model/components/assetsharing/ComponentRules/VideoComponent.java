package com.pwc.model.components.assetsharing.ComponentRules;

import com.pwc.model.components.assetsharing.ComponentRule;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;

public class VideoComponent extends ComponentRule {
    private static final String ASSET_RESOURCE_TYPE_PROPERTY = "sling:resourceType";
    private static final String MAIN_NODE_RESOURCE_TYPE_REGEX = "(/apps/)?pwc/components/content/videoplayer";
    private static final String VIDEO_OBJECT_METADATA_SUFFIX = "/jcr:content/metadata";
    private SlingHttpServletRequest request;

    public VideoComponent(SlingHttpServletRequest request) {
        this.request = request;
        setMainNodeResourceType(MAIN_NODE_RESOURCE_TYPE_REGEX);
    }

    @Override
    public Boolean isMatch(Resource resource) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        if(mainComponentResource == null) return false;
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        if (!mainNode.hasProperty(getSocialLabelProperty())) return false;
        if (!mainNode.hasProperty(getSocialItemsProperty())) return false;
        if (!mainNode.hasProperty(ASSET_RESOURCE_TYPE_PROPERTY)) return false;
        if (!mainNode.getProperty(ASSET_RESOURCE_TYPE_PROPERTY).getString().matches(MAIN_NODE_RESOURCE_TYPE_REGEX)) return false;
        if (!mainNode.hasProperty(getFileLocationProperty())) return false;
        String fileLocation = mainNode.getProperty(getFileLocationProperty()).getString();
        ResourceResolver resourceResolver = this.request.getResourceResolver();
        Resource videoObjectResource = resourceResolver.getResource(fileLocation + VIDEO_OBJECT_METADATA_SUFFIX);
        if (videoObjectResource == null) return false;
        Node videoObjectNode = videoObjectResource.adaptTo(Node.class);
        if (!videoObjectNode.hasProperty(getVideoProperty())) return false;
        return true;
    }

    @Override
    public Boolean isVideo(Resource resource) {
        return true;
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
        Resource mainComponentResource = getMainComponentResource(resource);
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        String fileLocation = mainNode.getProperty(getFileLocationProperty()).getString();
        ResourceResolver resourceResolver = this.request.getResourceResolver();
        Resource videoObjectResource = resourceResolver.getResource(fileLocation + VIDEO_OBJECT_METADATA_SUFFIX);
        Node videoObjectNode = videoObjectResource.adaptTo(Node.class);
        String videoUrl = videoObjectNode.getProperty(getVideoProperty()).getString();
        return videoUrl;
    }
}