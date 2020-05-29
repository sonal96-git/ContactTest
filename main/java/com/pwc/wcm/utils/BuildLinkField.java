package com.pwc.wcm.utils;

import com.pwc.wcm.model.Link;
import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Reviewed 16/07/2012
 *
 */
public class BuildLinkField {

	private static final String JSON_TEXT_KEY                = "text";
	private static final String JSON_URL_KEY                 = "url";
    private static final String JSON_OPEN_IN_NEW_WINDOW      = "openInNewWindow";
    private static final String JSON_ADDL_CSS_KEY      = "linkAddlCSS";

	/**
	 * method to parse the json format {url: '/content/sample', text: 'sample'}
	 * 
	 * @param link
	 * @return Link
	 * @throws JSONException
	 */
	public static Link buildLink(SlingHttpServletRequest request, String link) throws JSONException {
            if (!link.isEmpty()) {
                JSONObject linkJson = new JSONObject(link);

                String textStr = linkJson.optString(JSON_TEXT_KEY, "").trim();
                String linkAddlCSS = linkJson.optString(JSON_ADDL_CSS_KEY, "").trim();
                String urlStr = CommonUtils.convertPathInternalLink(request, linkJson.optString(JSON_URL_KEY, ""));
                boolean openInNewWindow = false;
                
                if (!linkJson.optString(JSON_OPEN_IN_NEW_WINDOW).isEmpty()){
                    openInNewWindow = linkJson.getBoolean(JSON_OPEN_IN_NEW_WINDOW);
                }

                return new Link(textStr, urlStr, openInNewWindow, linkAddlCSS, 0);
            }
            
            return null;
	}
}
