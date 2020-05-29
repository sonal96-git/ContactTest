package com.pwc.model.components.awards;



import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Reference;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.dam.api.Asset;
import com.pwc.collections.OsgiCollectionsLogger;


public class AwardsItems extends WCMUsePojo {	 

	@Reference
	private OsgiCollectionsLogger logger;	
	private Resource resource;
	private ResourceResolver resolver;
	private AwardsItemsModel model;
	private AwardsModel awardsModel;
	
	@Override
	public void activate() throws Exception {			
		this.resource = getResource();
		this.resolver = this.resource.getResourceResolver();		
		if(ResourceUtil.isNonExistingResource(this.resource)) 
    		return;
    	else
		    this.model = this.resource.adaptTo(AwardsItemsModel.class);  		
		this.model.fileReference = getFileReference();			
	}	
	
	public String getCssClassRendition() {  				
		AwardsUtils awardsUtils = new AwardsUtils();
		return awardsUtils.getCssClassRendition(this.resource);
    }	
	 
	public boolean getDisplayImagesOnly() {
		Resource parentResource = this.resource.getParent();
		this.awardsModel = parentResource.adaptTo(AwardsModel.class);  
		return this.awardsModel.displayImagesOnly;
	}
	
	public String getFileReference() {
		AwardsUtils awardsUtils = new AwardsUtils();
		return awardsUtils.getImage(this.resource, this.resolver);
	}
	
	public Boolean getIsEnabled() {
        return this.model != null && (!StringUtils.isBlank(this.model.title) && !StringUtils.isBlank(this.model.summary) && !StringUtils.isBlank(this.model.fileReference));
    }	 	

	public AwardsItemsModel getModel() {
		return model;
	}
	
	public String getTarget() {
		return this.model.getTarget() ? "_blank" : "_self"; 		
	}
	
	public String getAltText() {
		AwardsUtils awardsUtils = new AwardsUtils();
		String imagePath = awardsUtils.getImage(this.resource, this.resolver);
		Resource imageResource = resolver.getResource(imagePath);
		if(imageResource == null) return "";
		Asset asset = imageResource.adaptTo(Asset.class);
		if(asset == null) return "";
		String assetTitle = asset.getMetadataValue("dc:title");
		return assetTitle;     
 	}
	
}
