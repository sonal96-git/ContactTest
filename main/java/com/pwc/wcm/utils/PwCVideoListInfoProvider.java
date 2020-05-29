package com.pwc.wcm.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.ListInfoProvider;
import com.day.cq.dam.api.Asset;
 
@Component(immediate = true, service = ListInfoProvider.class)
public class PwCVideoListInfoProvider implements ListInfoProvider {
     
    private int count = 0;
    private String asset_width_property = "width";
    private String asset_height_property = "height";
 
    @SuppressWarnings("deprecation")
	@Override
	public void updateListGlobalInfo(SlingHttpServletRequest request, JSONObject info, Resource resource) throws JSONException {
        info.put("pwcvideo", count);
        count = 0; // reset for next execution
    }
 
    @Override
	public void updateListItemInfo(SlingHttpServletRequest request, JSONObject info, Resource resource) throws JSONException {
        Asset asset = resource.adaptTo(Asset.class);
        if (asset != null) {
            String dcFormat = asset.getMetadataValue("dc:format");
            if (dcFormat != null && dcFormat.equals("video/pwcvideo"))
            {
                Resource metadataResource = resource.getChild("jcr:content/metadata");
                info.put("pwcvideo", "video");
                if(metadataResource == null) {
                    info.put("width", "");
                    info.put("height", "");
                } else {
                    ValueMap values = metadataResource.adaptTo(ValueMap.class);
                    if(values == null) {
                        info.put("width", "");
                        info.put("height", "");
                    } else {
                        if (values.containsKey(asset_width_property)) {
                            info.put("width", values.get(asset_width_property, String.class));
                        } else {
                            info.put("width", "");
                        }
                        if (values.containsKey(asset_height_property)) {
                            info.put("height", values.get(asset_height_property, String.class));
                        } else {
                            info.put("height", "");
                        }
                    }
                }
            	count++;
            	return;
            }
        }
    	info.put("pwcvideo", "");
    }
 
}
