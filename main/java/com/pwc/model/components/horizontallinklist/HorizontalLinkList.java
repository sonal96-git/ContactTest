package com.pwc.model.components.horizontallinklist;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONObject;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.designer.Style;

public class HorizontalLinkList extends WCMUsePojo {
    private final String LIST_TITLE_KEY = "horizontalLinkListTitle";
    private final String LINK_LIST_KEY = "linkItems";
    private final String ENABLE_COMPONENT = "enableHorizontalLinkList";
    private Resource resource;

    private List<HorizontalLink> links;

    private String listTitle;

    private Boolean enabled;

    @Override
    public void activate() throws Exception {
        //INFO: We need the parent resource because the component is always inside another component (also its dialog) so we need the parent's properties
        Boolean enabledComponent = true;
        ValueMap properties = null;
        if(this.getResource().adaptTo(ValueMap.class) != null)
        {
            this.resource = this.getResource();
            if(this.resource == null) return;
            properties = this.resource.getValueMap();
        }
        else
        {
            this.resource = this.getResource().getParent();
            if(this.resource == null) return;
            properties = this.resource.getValueMap();
            Style currentStyle = this.getCurrentDesign().getStyle(this.resource);
            enabledComponent = currentStyle.get(ENABLE_COMPONENT, false);
        }



        this.enabled = enabledComponent && (properties.containsKey(ENABLE_COMPONENT) ? properties.get(ENABLE_COMPONENT, Boolean.class) : Boolean.valueOf(false));
        if(properties.containsKey(LIST_TITLE_KEY)) {
            this.listTitle = properties.get(LIST_TITLE_KEY, String.class);
        }
        if(properties.containsKey(LINK_LIST_KEY)) {
            Object linkList = properties.get(LINK_LIST_KEY);
            if(linkList == null) return;
            this.links = new ArrayList<>();
            ResourceResolver resourceResolver = this.resource.getResourceResolver();
            if(linkList instanceof String[]) {
                   for(String link : (String[])linkList) {
                        JSONObject jsonLink = new JSONObject(link);
                       this.links.add(new HorizontalLink(jsonLink, resourceResolver));
                   }
            } else {
                JSONObject jsonLink = new JSONObject((String)linkList);
                this.links.add(new HorizontalLink(jsonLink, resourceResolver));
            }
        }
        this.enabled = this.enabled && this.links != null && !this.links.isEmpty();
    }

    public String getListTitle() {
        return listTitle;
    }

    public List<HorizontalLink> getLinks() {
        return links;
    }

    public Boolean getIsEnabled() {
        return enabled;
    }
}
