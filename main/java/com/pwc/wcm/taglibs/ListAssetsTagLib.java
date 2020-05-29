package com.pwc.wcm.taglibs;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.jsp.util.TagUtil;

import com.day.cq.dam.api.Asset;
import com.pwc.wcm.model.List;

@SuppressWarnings("serial")
public class ListAssetsTagLib  extends BaseTagLib {
    @Override
    public int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(pageContext);

        List<Asset> list = new List<Asset>(request, Asset.class);
        request.setAttribute("list", list);

        return EVAL_BODY_INCLUDE;
    }
}