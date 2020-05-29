package com.pwc.model.components.assetsharing.ComponentRules;

import com.pwc.model.components.assetsharing.ComponentRule;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;

public class TextImageComponent extends ComponentRule {

    private static final String ASSET_RESOURCE_TYPE_PROPERTY = "sling:resourceType";
    private static final String ASSET_IMAGE_TYPE_PROPERTY_VALUE_REGEX = "foundation/components/image|(/apps/)?pwc/components/content/image";
    private static final String MAIN_NODE_RESOURCE_TYPE_REGEX = "(/apps/)?pwc/components/content/textimage";
    private SlingHttpServletRequest request;

    public TextImageComponent(SlingHttpServletRequest request) {
        this.request = request;
        setMainNodeResourceType(MAIN_NODE_RESOURCE_TYPE_REGEX);
    }
    @Override
    public Boolean isMatch(Resource resource) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        if(mainComponentResource == null) return false;
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        Node parentAssetNode = mainNode.getNode("image");
        if (parentAssetNode == null) return false;
        if (!mainNode.hasProperty(getSocialLabelProperty())) return false;
        if (!mainNode.hasProperty(getSocialItemsProperty())) return false;
        if (!parentAssetNode.hasProperty(ASSET_RESOURCE_TYPE_PROPERTY)) return false;
        if (!parentAssetNode.getProperty(ASSET_RESOURCE_TYPE_PROPERTY).getString().matches(ASSET_IMAGE_TYPE_PROPERTY_VALUE_REGEX)) return false;
        if (!parentAssetNode.hasProperty(getFileLocationProperty())) return false;
        return true;
    }

    @Override
    public Boolean isVideo(Resource resource) {
        return false;
    }

    @Override
    public String getImageUrl(Resource resource, boolean includeExtension) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        Node assetNode = mainNode.getNode("image");
        String assetUrl = getUrl(assetNode, this.request, includeExtension);
        return assetUrl;
    }

    @Override
    public String getVideoUrl(Resource resource) throws Exception {
        return StringUtils.EMPTY;
    }
}
