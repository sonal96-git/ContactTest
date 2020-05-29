package com.pwc.model.components.assetsharing;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import com.pwc.model.components.assetsharing.impl.ShareCalculatorImpl;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.CommonUtils;

public abstract class ComponentRule {
    private static final String FILE_LOCATION_PROPERTY = "fileReference";
    private static final String VIDEO_URL_PROPERTY = "videourl";
    private static final String DEEP_LINKING_ENABLED_PROPERTY = "enableDeepLinking";
    private static final String IS_ENABLED_PROPERTY = "enableSocialShareConfiguration";
    private static final String SOCIAL_LABEL_PROPERTY = "socialLabel";
    private static final String ASSET_ID_PROPERTY = "assetId";
    private static final String SOCIAL_ITEMS_PROPERTY = "socialItems";
    private String mainNodeResourceType;
    private String imageUrl;
    public abstract Boolean isMatch(Resource resource) throws Exception;
    public abstract Boolean isVideo(Resource resource);
    public abstract String getImageUrl(Resource resource, boolean includeExtension) throws Exception;
    public abstract String getVideoUrl(Resource resource) throws Exception;

    public Resource getMainComponentResource(Resource resource) throws Exception {
        Resource mainComponentResource = resource;
        String resourceTypeToFind = getMainNodeResourceType();
        Resource parentResource = mainComponentResource.getParent();
        if(parentResource == null) return null;
        if(parentResource.getResourceType().matches(resourceTypeToFind))
            return parentResource;
        Resource parentOfParent = parentResource.getParent();
        if(parentOfParent.getResourceType().matches(resourceTypeToFind))
            return parentOfParent;
        return null;
    }

    public ArrayList<SocialItem> fillOutputItems(Resource resource, Node node, Property property, SlingHttpServletRequest request, String assetId) throws Exception {
        ArrayList<SocialItem> outputItems = new ArrayList<>();
        ShareCalculatorImpl calculator = new ShareCalculatorImpl(request);
        String videoUrl = this.getVideoUrl(resource);
        if(!property.isMultiple()) {
            SocialItem singleItem = new SocialItem(calculator, assetId);
            singleItem.setImageUrl(CommonUtils.getExternalUrl(request, this.getImageUrl(resource, true)));
            singleItem.fromJSON(new JSONObject(property.getString()));
            videoUrl = getYoutubeVideoUrl(calculator, videoUrl, singleItem);
            singleItem.setVideoUrl(videoUrl);
            outputItems.add(singleItem);
            return outputItems;
        }
        //INFO: Multiple values multifield
        Value[] values = node.getProperty(getSocialItemsProperty()).getValues();
        JSONArray items = new JSONArray();
        for (Value v: values){
            items.put(new JSONObject(v.getString()));
        }
        for (int i = 0; i < items.length(); i++) {
            SocialItem item = new SocialItem(new ShareCalculatorImpl(request), assetId);
            item.setImageUrl(CommonUtils.getExternalUrl(request, this.getImageUrl(resource, true)));
            item.fromJSON((JSONObject) items.get(i));
            videoUrl = getYoutubeVideoUrl(calculator, videoUrl, item);
            item.setVideoUrl(videoUrl);
            outputItems.add(item);
        }
        return outputItems;
    }

    private String getYoutubeVideoUrl(ShareCalculatorImpl calculator, String videoUrl, SocialItem item) {
        if(!videoUrl.equals(StringUtils.EMPTY) && videoUrl != null) {
            String videoId = CommonUtils.getYoutubeId(videoUrl);
            if(videoId != null) {
                videoUrl = calculator.parseYoutubeUrl(item.getSocialChannel(), videoId);
            }
        }
        return videoUrl;
    }

    public String getUrl(Node node, SlingHttpServletRequest request, boolean includeExtension) throws Exception {
        this.imageUrl = node.getProperty(getFileLocationProperty()).getString();
        if(includeExtension){
            this.imageUrl = CommonUtils.convertUrl(request, imageUrl);
            return this.imageUrl;
        }
        String fileExtension = "." + FilenameUtils.getExtension(imageUrl);
        imageUrl = CommonUtils.convertUrl(request, imageUrl.replace(fileExtension, StringUtils.EMPTY));
        return imageUrl;
    }

    public Boolean getDeepLinkingEnabled(Resource resource) throws Exception
    {
    	try {
            Resource mainComponentResource 	= getMainComponentResource(resource);
            Node mainNode 					= mainComponentResource.adaptTo(Node.class);
            Property property 				= mainNode.getProperty(getDeepLinkingEnabledProperty());
            Value value 					= property.getValue();

            if(value == null)
            {
            	return false;
            }

            return true;
    	}
		catch (Exception e)
    	{
			ExceptionLogger.logException(e);
			return false;
    	}
    }

    public String getAssetId(Resource resource) throws Exception
    {
    	try {
	        Resource mainComponentResource 	= getMainComponentResource(resource);
	        Node mainNode 					= mainComponentResource.adaptTo(Node.class);
	        Property property 				= mainNode.getProperty(getAssetIdProperty());
	        Value value 					= property.getValue();

	        if(value == null)
	        	return StringUtils.EMPTY;

	        return value.toString();
    	}
		catch (Exception e)
    	{
			ExceptionLogger.logException(e);
			return StringUtils.EMPTY;
    	}
    }

    public Boolean getIsEnabled(Resource resource) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        Property property = mainNode.getProperty(getIsEnabledProperty());
        Value value = property.getValue();
        if(value == null) return false;
        return true;
    }

    public String getSocialLabel(Resource resource) throws Exception {
        Resource mainComponentResource = getMainComponentResource(resource);
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        Property property = mainNode.getProperty(getSocialLabelProperty());
        Value value = property.getValue();
        if(value == null) return StringUtils.EMPTY;
        return value.toString();
    }

    public ArrayList<SocialItem> getSocialItems(Resource resource, SlingHttpServletRequest request, String assetId) throws Exception {
        ArrayList<SocialItem> outputItems = new ArrayList<>();
        Resource mainComponentResource = getMainComponentResource(resource);
        Node mainNode = mainComponentResource.adaptTo(Node.class);
        Property property = mainNode.getProperty(getSocialItemsProperty());
        if(property == null) return outputItems;
        outputItems = fillOutputItems(resource, mainNode, property, request, assetId);
        return outputItems;
    }

    public String getSocialLabelProperty() {
        return SOCIAL_LABEL_PROPERTY;
    }

    public String getDeepLinkingEnabledProperty() {
    	return DEEP_LINKING_ENABLED_PROPERTY;
    }

    public String getAssetIdProperty() {
    	return ASSET_ID_PROPERTY;
    }

    public String getIsEnabledProperty() {
    	return IS_ENABLED_PROPERTY;
    }
    
    public String getSocialItemsProperty() {
        return SOCIAL_ITEMS_PROPERTY;
    }

    public String getFileLocationProperty() {
        return FILE_LOCATION_PROPERTY;
    }

    public String getMainNodeResourceType() {
        return mainNodeResourceType;
    }

    public void setMainNodeResourceType(String mainNodeResourceType) { this.mainNodeResourceType = mainNodeResourceType; }

    public String getVideoProperty() { return VIDEO_URL_PROPERTY; }
}
