/*
 * 
 */
package com.pwc.wcm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.services.impl.ParentPagesList;

@Component(immediate = true, service = JsonToMapConversion.class)
public class JsonToMapConversion {
	private static Logger LOGGER = LoggerFactory.getLogger(ParentPagesList.class);

	public Map<String, String> convertJsonToMap(final String json) {
		final HashMap<String, String> map = new HashMap<String, String>();
		JSONObject jObject;
		try {
			jObject = new JSONObject(json);
			final Iterator<?> keys = jObject.keys();

			while (keys.hasNext()) {
				final String key = (String) keys.next();
				final String value = jObject.get(key).toString();
				map.put(key, value);

			}
		} catch (final JSONException e) {
			LOGGER.error("Error in parsing Json", e);
		}
		return map;
	}

	public List<Map<String, String>> getListFromJson(final String[] json) {
		final List<Map<String, String>> jsonList = new ArrayList<Map<String, String>>();
		if(json != null)
		{
			for (String s : json) {
				s = s.replaceAll("\\\\\"", "'");
				jsonList.add(convertJsonToMap(s));
			}
		}
		return jsonList;
	}
}

