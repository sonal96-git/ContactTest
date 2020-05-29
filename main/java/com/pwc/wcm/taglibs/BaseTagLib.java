/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pwc.wcm.taglibs;

import com.day.cq.wcm.api.components.ComponentContext;

import org.apache.sling.api.SlingHttpServletRequest;

import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.foundation.Image;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.scripting.jsp.util.TagUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;
import com.pwc.wcm.utils.CommonUtils;

import javax.servlet.jsp.tagext.BodyTagSupport;

import java.util.HashSet;
import java.util.Set;

/**
 * A base for general component taglib. This class catches the exceptions and
 * make sure they are not shown in the authoring UI.
 *
 * @author lap.tran
 *
 */
@SuppressWarnings("serial")
public class BaseTagLib extends BodyTagSupport {

    protected static final Logger log = LoggerFactory.getLogger(BaseTagLib.class);
    private Set<String> variables;
    protected SlingHttpServletRequest request;
    protected Resource resource;

    /**
     * This API uses for starting a tag
     *
     * @return int
     */
    protected int startTag() {
        return EVAL_BODY_INCLUDE;
    }

    /**
     * This API uses for ending a tag
     *
     * @return int
     */
    protected int endTag() {
        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() {
        try {
            request = TagUtil.getRequest(this.pageContext);
            resource = request.getResource();

            boolean authormode = ((WCMMode.fromRequest(request) == WCMMode.EDIT) || (WCMMode.fromRequest(request) == WCMMode.DESIGN)) && !(WCMMode.fromRequest(request) == WCMMode.PREVIEW);
            this.pageContext.setAttribute("isEditOrDesignMode", authormode);
            
            String countryNodePath = CommonUtils.convertPathInternalLink(request, UtilityTagFunctions.getLanguageNode(this.pageContext).getPath());
            this.pageContext.setAttribute("countryNodePath", countryNodePath);

            return startTag();
        } catch (Exception e) {
            log.error("Taglib for startTag method throws errors : ", e);
            return SKIP_BODY;
        }
    }

    @Override
    public int doEndTag() {
        try {
            return endTag();
        } catch (Exception e) {
            log.error("Taglib for endTag method throws errors : ", e);
            return SKIP_BODY;
        } finally {
            // tags are pooled, so clean up the instances here
            clearPageAttributes();
        }
    }

    /**
     * Set page-scoped attributes and track the keys The attributes can be
     * cleared later by {@link #clearPageAttributes()}
     *
     * @param key The name of attribute
     * @param value The value of attribute
     */
    protected final void setPageAttribute(String key, Object value) {
        pageContext.setAttribute(key, value);
        setComponentContextAttribute(key, value);
        if (variables == null) {
            variables = new HashSet<String>();
        }
        variables.add(key);
    }

    /**
     * Clear the page-scoped attributes which are previously set by
     * {@link #setPageAttribute(String, Object)}.
     */
    private void clearPageAttributes() {
        if (variables != null) {
            for (String key : variables) {
                pageContext.removeAttribute(key);
            }
            variables.clear();
        }
    }

    protected final void setComponentContextAttribute(String key, Object value) {
        getComponentContext().setAttribute(key, value);
    }

    public static String getComponentContextStringAttribute(String key, ComponentContext componentContext) {
        return getComponentContextAttribute(key, componentContext);
    }

    public static Image getComponentContextImageAttribute(String key, ComponentContext componentContext) {
        return getComponentContextAttribute(key, componentContext);
    }

    public static Object getComponentContextObjectAttribute(String key, ComponentContext componentContext) {
        return getComponentContextAttribute(key, componentContext);
    }

    public static <T> T getComponentContextAttribute(String key, ComponentContext componentContext) {
       if(componentContext.getAttribute(key) != null) {
           return (T)componentContext.getAttribute(key);
       }
       return null;
    }

    protected ComponentContext getComponentContext() {
        return WCMUtils.getComponentContext(TagUtil.getRequest(this.pageContext));
    }

}
