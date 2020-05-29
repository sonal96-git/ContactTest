package com.pwc.wcm.taglibs;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.scripting.jsp.util.TagUtil;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.pwc.wcm.model.List;

@SuppressWarnings("serial")
public class ListPagesTagLib  extends BaseTagLib {

    private static final String FLUSH_CACHE_URL  = "flushCacheUrl";

    @Override
    public int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(pageContext);

        Resource resource = request.getResource();
        ValueMap properties = ResourceUtil.getValueMap(resource);
        if(properties != null && properties.get(FLUSH_CACHE_URL, String.class) != null) {
            setComponentContextAttribute(FLUSH_CACHE_URL, properties.get(FLUSH_CACHE_URL, String.class));
        }

        List<Page> list = new List<Page>(request, Page.class, new PageFilter());
        request.setAttribute("list", list);

        return EVAL_BODY_INCLUDE;
    }


}
