package com.pwc.model.components.highlights;



import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;
import org.json.JSONObject;

import com.adobe.cq.sightly.WCMUsePojo;
import com.pwc.util.ExceptionLogger;
import com.pwc.util.URLHandlerUtility;

public class HighLightsItems extends WCMUsePojo{
		
	private final String NAME_KEY = "name";
	private final String ID_KEY = "id";
	private final String FILE_REFERENCE_IMAGE_KEY = "fileReference";

	
	private Resource resource;
	private ResourceResolver resolver;
	private HighLightsItemsModel model;	
	
	@Override
	public void activate() {	
		this.resource = getResource();
		this.resolver = getResourceResolver();		
		if(ResourceUtil.isNonExistingResource(this.resource)) 
    		return;
    	else
		    this.model = this.resource.adaptTo(HighLightsItemsModel.class);   	
		UpdateSpeaker();
	}
	
	private void UpdateSpeaker() {		
		try {
			this.model.title = this.model.title == null ? getTitleItem() : this.model.title;
			this.model.link_path = this.model.link_path != null ? URLHandlerUtility.handleURLForInternalLinks(this.model.link_path, this.resolver) : "";		
			this.model.fileReference = getImage();
		}
		catch(Exception e){
			ExceptionLogger.logExceptionMessage("HighLightsItems UpdateSpeaker error :",e);	
		}
	}	
	
	private String getImage() {
		String resourcePath = this.resource.getPath()+"/image";
		Resource resourceImage = resolver.getResource(resourcePath);
		if(resourceImage != null) {
		ValueMap properties = resourceImage.getValueMap();
		String imagePath = properties.get(FILE_REFERENCE_IMAGE_KEY) != null ? properties.get(FILE_REFERENCE_IMAGE_KEY).toString() : "";
		return imagePath;
		}
		else
			return "";
	}
	
	private String getTitleItem() {		
		Resource resourceParent = this.resource.getParent();
		if(ResourceUtil.isNonExistingResource(resourceParent)) return "";	
		HighLightsModel model = resourceParent.adaptTo(HighLightsModel.class); 		
		String[] itemsList = model.getHighlight_items() != null ? model.getHighlight_items() : null;         
		String itemId = this.resource.getName();
		if(itemsList != null) {
			for(String item : itemsList) {
				try{
					JSONObject jsonItem = new JSONObject(item);
					if(jsonItem.optString(ID_KEY).equals(itemId))
					return jsonItem.optString(NAME_KEY);
				}catch(Exception e){
					ExceptionLogger.logExceptionMessage("HighLightsItems getTitleItem error :",e);	
				}
			}
		}  
        return itemId;    
	}
	
	public Boolean getIsEnabled() {
        return this.model != null && (!StringUtils.isBlank(this.model.title) && !StringUtils.isBlank(this.model.summary) && !StringUtils.isBlank(this.model.fileReference));
    }
	
	public Boolean getIsSimple() {
		Resource resourceParent = this.resource.getParent();
		if(ResourceUtil.isNonExistingResource(resourceParent)) return null;		
		HighLightsModel model = resourceParent.adaptTo(HighLightsModel.class); 	
		return model.style.equals("simple");
	}

	public HighLightsItemsModel getModel() {
		return model;
	}
	
	public String getTarget() {
		return this.model.getTarget() ? "_blank" : "_self"; 		
	}
	
}
