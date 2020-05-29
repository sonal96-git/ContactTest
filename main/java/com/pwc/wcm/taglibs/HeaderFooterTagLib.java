package com.pwc.wcm.taglibs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.pwc.wcm.model.Link;
import com.pwc.wcm.utils.CommonUtils;

@SuppressWarnings("serial")
public class HeaderFooterTagLib extends BaseTagLib {
	private static final String HEADER_TYPE_CONSTANT = "header";
	private static final String FOOTER_TYPE_CONSTANT = "footer";
	private String type = HEADER_TYPE_CONSTANT;

	@Override
	protected int startTag() {
        ValueMap properties = ResourceUtil.getValueMap(resource);
	    
	    boolean isFooter = FOOTER_TYPE_CONSTANT.equals(properties.get("type", HEADER_TYPE_CONSTANT));

	    String[] topLevelLinks = properties.get("topLevelNavConfig", String[].class);
	    if(topLevelLinks != null && topLevelLinks.length > 0){
            List<Link> topLevelLinksList = new ArrayList<Link>(topLevelLinks.length);
            for (String topLevelLink : topLevelLinks) {
            	try {
            		// if component is rendered as a footer, we want to default to ignore children being brought
            		// back for links. For header default would be to NOT ignoreChildren links
            		topLevelLinksList.add(buildTopNavLinkHierarchy(request, topLevelLink, isFooter));
            	} catch(Exception e) {
            		log.error("HeaderFooterTagLib: Error creating Navigation links.", e);
            	}
            }
            pageContext.setAttribute("topLevelLinksList", topLevelLinksList);
        }
	    
	    // footer is broken down into 2 groups of nav elements
	    if (isFooter) {
		    String[] bottomNavConfig = properties.get("bottomLevelNavConfig", String[].class);
		    if(bottomNavConfig != null && bottomNavConfig.length > 0){
	            List<Link> bottomLinksList = new ArrayList<Link>(bottomNavConfig.length);
	            for (String topLevelLink : bottomNavConfig) {
	            	try {
	            		// if component is rendered as a footer, we want to default to ignore children being brought
	            		// back for links. For header default would be to NOT ignoreChildren links
	            		bottomLinksList.add(buildTopNavLinkHierarchy(request, topLevelLink, isFooter));
	            	} catch(Exception e) {
	            		log.error("HeaderFooterTagLib: Error creating footer bottom navigation links.", e);
	            	}
	            }
	            pageContext.setAttribute("bottomLinksList", bottomLinksList);
	        }
            pageContext.setAttribute("copyright", properties.get("copyright", ""));
	    }
        
        return EVAL_BODY_INCLUDE;
	}
	
	private Link buildTopNavLinkHierarchy(SlingHttpServletRequest request, String link, boolean defaultIgnoreChildren) throws JSONException {
        if (StringUtils.isNotBlank(link)) {
        	Link returnLink = new Link();
            JSONObject linkJson = new JSONObject(link);

            returnLink.setText(linkJson.optString("text", "").trim());
            String urlPath = linkJson.optString("url", "");
            
        	Page currentPage = (Page) pageContext.getAttribute("currentPage");
        	if (currentPage.getPath().startsWith(urlPath)) {
                pageContext.setAttribute("selectedTopLevelLink", urlPath);
        	}

            boolean ignoreChildren = linkJson.optBoolean("ignoreChildren", defaultIgnoreChildren);
            if(StringUtils.isNotBlank(urlPath)) {
                Resource searchDestinationResource = request.getResourceResolver().getResource(urlPath);
                if(searchDestinationResource != null) {
                    Page page = searchDestinationResource.adaptTo(Page.class);
                    if (StringUtils.isBlank(returnLink.getText())) {
                    	returnLink.setText(CommonUtils.getNavigationDisplayTitle(page));
                    }
                    if (page != null) {
                    	returnLink.setUrl(urlPath + ".html");
                        if (!ignoreChildren) {
                            Filter<Page> pageFilter = new PageFilter();
                            Iterator<Page> childrenPages = page.listChildren(pageFilter);
                            while (childrenPages != null && childrenPages.hasNext()) {
                            	Page childPage = childrenPages.next();
                            	String linkText = CommonUtils.getNavigationDisplayTitle(childPage) ;
                            	returnLink.getChildrenLinks().add(new Link(linkText, childPage.getPath() + ".html", false, "", 0));
                            }
                        }
                    }
                }
            }

            return returnLink;
        }
        
        return null;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
