package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Inherited Tags Provider Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/inheritedTags",
})
public class InheritedTagsProviderServlet extends SlingSafeMethodsServlet {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InheritedTagsProviderServlet.class);
    
    private static final String TAG_ID_JSON_KEY = "tagID";
    private static final String TITLE_PATH_JSON_KEY = "titlePath";
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ResourceResolver resourceResolver = request.getResourceResolver();
        String pagePath = request.getParameter("pagePath");
        String pageLocale = request.getParameter("pageLocale");
        final Boolean isCreatePage = Boolean.valueOf(request.getParameter("isCreatePage"));
        JSONArray inheritedTagsArray = getInheritedTags(pagePath, pageLocale, resourceResolver, isCreatePage);
        String inheritedTagString = inheritedTagsArray == null ? "[]" : inheritedTagsArray.toString();
        LOGGER.debug("InheritedTagsProviderServlet doGet() : Writing inherited tags for page {} in response {}", pagePath,
                inheritedTagString);
        response.getWriter().write(inheritedTagString);
    }
    
    /**
     * Get {@link JSONArray} of the inherited cq:tags of the page.
     * 
     * @param pagePath {@link String} for which the inherited cq:tags are returned
     * @param pageLocale {@link String} locale of the page at given pagePath
     * @param resourceResolver {@link ResourceResolver}
     * @param isCreatePage {@link Boolean} true, if request comes from create page console.
     * @return {@link JSONArray}
     */
    private JSONArray getInheritedTags(final String pagePath, final String pageLocale, final ResourceResolver resourceResolver, final Boolean isCreatePage) {
        JSONArray inheritedTagsArray = null;
        if (pagePath != null && resourceResolver != null) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getPage(pagePath);
            if (page != null) {
                Tag[] inheritedTags = getInheritedTags(page, isCreatePage);
                if (inheritedTags != null && inheritedTags.length > 0) {
                    Locale locale = pageLocale == null ? null : new Locale(pageLocale);
                    inheritedTagsArray = new JSONArray();
                    for (Tag tag : inheritedTags) {
                        inheritedTagsArray.put(getTagJSONObject(tag, locale));
                    }
                }
            }
        }
        return inheritedTagsArray;
    }
    
    /**
     * Return the {@link JSONObject} of the given {@link Tag}. E.g of Tag object { {@value #TAG_ID_JSON_KEY} : "properties:style/color",
     * {@value #TITLE_PATH_JSON_KEY} : "Asset Properties : Style / Color" }
     * 
     * @param tag {@link Tag} for which the {@link JSONObject} is created
     * @param locale {@link Locale} locale of the {@value #TITLE_PATH_JSON_KEY}
     * @return {@link JSONObject}
     */
    private JSONObject getTagJSONObject(final Tag tag, final Locale locale) {
        JSONObject tagJSONObject = new JSONObject();
        try {
            String localizedTitlePath = locale == null ? tag.getTitlePath() : tag.getTitlePath(locale);
            tagJSONObject.put(TAG_ID_JSON_KEY, tag.getTagID());
            tagJSONObject.put(TITLE_PATH_JSON_KEY, localizedTitlePath);
        } catch (JSONException jsonException) {
            LOGGER.error(
                    "InheritedTagsProviderServlet : getTagJSONObject() : JSONException occured while creating json object for Tag with path {} : {}",
                    tag.getPath(), jsonException);
        }
        return tagJSONObject;
    }
    
    /**
     * Return the inherited 'cq:tags' of the page, if the tags are not present on immediate parent of a page. it's parent page is recursively searched for cq:tags.
     * 
     * @param page {@link Page} path of page for which inherited tags are required, for create Page console it's the path of immediate parent page.
     * @param isCreatePage {@link Boolean} true, if request comes from create page console.
     * @return Array of {@link Tag} array of inherited tags for the page.
     */
    private Tag[] getInheritedTags(final Page page, final Boolean isCreatePage) {
        Tag[] pageTags = null;
        if (page != null && page.getParent() != null && page.getDepth() > 3) {
            pageTags = isCreatePage ? page.getTags() : page.getParent().getTags();
            if (pageTags.length == 0)
                pageTags = getInheritedTags(page.getParent(), false);
        }
        return pageTags;
    }
}
