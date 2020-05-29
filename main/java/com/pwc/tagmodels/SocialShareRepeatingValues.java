/*
 * 
 */
package com.pwc.tagmodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * Created with IntelliJ IDEA. User: intelligrape Date: 9/1/14 Time: 2:41 PM To
 * change this template use File | Settings | File Templates.
 */
public class SocialShareRepeatingValues extends SimpleTagSupport {
	@Override
	public void doTag() throws JspException {
		final Resource resource = (Resource) getJspContext().getAttribute("resource");
		final PageContext pageContext = (PageContext) getJspContext();
		final String path = resource.getPath() + "/options";
		final Resource resourceSocialType = resource.getResourceResolver().getResource(path);
		final LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();
		if (resourceSocialType != null) {
			final Iterator<Resource> iterator = resourceSocialType.listChildren();
			while (iterator.hasNext()) {
				final Resource childResource = iterator.next();
				String value = childResource.adaptTo(ValueMap.class).get("socialtype", String.class);
				value = value.replace("[", "");
				value = value.replace("]", "");
				value = value + " Share";
				list = Arrays.asList(value.split(","));
				if (value != null) {
					final String resourceName = childResource.getName();
					map.put(resourceName, list);
				}
			}
			pageContext.setAttribute("socialAttributes", map);
		}
	}
}

