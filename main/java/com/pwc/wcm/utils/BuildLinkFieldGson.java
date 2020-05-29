package com.pwc.wcm.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pwc.wcm.model.Link;

/**
 * Reviewed 16/07/2012
 *
 */
public class BuildLinkFieldGson {

	public static Link buildLink(String linkStr) throws JSONException {
		Link link = null;
		if (!StringUtils.isBlank(linkStr)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			link = gson.fromJson(linkStr, Link.class);
		}
		return link;
	}
}
