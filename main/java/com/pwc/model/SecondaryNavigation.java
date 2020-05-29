/*
 * 
 */
package com.pwc.model;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.pwc.wcm.utils.JsonToMapConversion;

public class SecondaryNavigation {

	Resource resource;
	JsonToMapConversion jsonToMapConversion;

	public SecondaryNavigation(final Resource resource, final JsonToMapConversion jsonToMapConversion) {
		this.resource = resource;
		this.jsonToMapConversion = jsonToMapConversion;

	}

	public List<Map<String, String>> getListOfMap() {
		final ValueMap valueMap = resource.getValueMap();
		List<Map<String, String>> jsonList;
		if ("simple".equals(valueMap.get("stylerendition"))) {
			jsonList = jsonToMapConversion.getListFromJson(valueMap.get("simpleStyleRendition", String[].class));
		} else if ("complex".equals(valueMap.get("stylerendition"))) {
			jsonList = jsonToMapConversion.getListFromJson(valueMap.get("complexStyleRendition", String[].class));
		} else if ("imperatives".equals(valueMap.get("stylerendition"))) {
			jsonList = jsonToMapConversion.getListFromJson(valueMap.get("imperativesStyleRendition", String[].class));
		} else {
			jsonList = jsonToMapConversion.getListFromJson(valueMap.get("tabsStyleRendition", String[].class));
		}
		return jsonList;
	}

}

