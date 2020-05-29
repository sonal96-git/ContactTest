package com.pwc.wcm.taglibs;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.model.Link;
import com.pwc.wcm.utils.BuildLinkField;

@SuppressWarnings("serial")
public class TabsTagLib extends BaseTagLib {
    
    private static final Logger log = LoggerFactory.getLogger(TabsTagLib.class);
    
    @Override
    public int startTag() {
        ValueMap properties = ResourceUtil.getValueMap(resource);
        try {
            String[] tabsLinks = properties.get("tabLinks", String[].class);
            if(tabsLinks != null && tabsLinks.length > 0){
                List<Link> tabsLinksList = new ArrayList<Link>(tabsLinks.length);
                for (String tabLink : tabsLinks) {
                	tabsLinksList.add(BuildLinkField.buildLink(request, tabLink));
                }
                pageContext.setAttribute("tabsLinksList", tabsLinksList);
            }
            
            boolean isPlain = properties.get("isPlain", false);
            String infoText = properties.get("infoText", "");
            pageContext.setAttribute("infoText", (StringUtils.isBlank(infoText)) ? "" : "<p>" + infoText + "</p>" );
            pageContext.setAttribute("capabilityClass", (isPlain) ? "" : "capability");
            pageContext.setAttribute("isPlain", isPlain);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return EVAL_BODY_INCLUDE;
    }
}