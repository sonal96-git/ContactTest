package com.pwc.model.components.typepadblog;

import com.day.cq.wcm.api.WCMMode;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = SlingHttpServletRequest.class)
public class TypePadBlog {

    private static final Long DEFAULT_MAX_RESULTS = Long.valueOf(5);
    private static final String DEFAULT_LINK_TARGET_TYPE = "_blank";
    private static final String DEFAULT_DISPLAY = "list";
    private static final String LIST_DESCRIPTION_DISPLAY_VALUE = "listdescription";
    private TypePadModel model;

    private String containerId;

    private Boolean active;

    private Boolean wcmmode;

    private Boolean hasDescription;
    
    private String idIndex;

    public TypePadBlog(SlingHttpServletRequest request) {
        this.wcmmode = (WCMMode.fromRequest(request) != WCMMode.DISABLED) && (WCMMode.fromRequest(request) != WCMMode.PREVIEW);
        Resource resource = request.getResource();
        if(resource == null) return;
        this.model = resource.adaptTo(TypePadModel.class);
        String uniqSuffix = resource.getPath().replaceAll("/","-").replaceAll(":","-");
        this.containerId = "typepad" + uniqSuffix;
        this.idIndex = UUID.randomUUID().toString();
        this.active = this.model != null && !StringUtils.isBlank(this.model.widgetId);
        if(this.model != null) {
            if(this.model.maxresults == null || this.model.maxresults == 0) {
                this.model.maxresults = DEFAULT_MAX_RESULTS;
            }
            if(StringUtils.isBlank(this.model.linktargettype)) {
                this.model.linktargettype = DEFAULT_LINK_TARGET_TYPE;
            }
            if(StringUtils.isBlank(this.model.display)) {
                this.model.display = DEFAULT_DISPLAY;
            }
            this.hasDescription = this.model.display.equals(LIST_DESCRIPTION_DISPLAY_VALUE);
        }
    }

    public TypePadModel getModel() {
        return model;
    }

    public String getContainerId() {
        return containerId;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getWcmmode() {
        return wcmmode;
    }

    public Boolean getHasDescription() {
        return hasDescription;
    }
    
    public String getIdIndex() {
        return idIndex;
    }
}
