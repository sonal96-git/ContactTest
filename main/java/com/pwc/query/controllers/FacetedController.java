package com.pwc.query.controllers;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.contents.ControllerContent;
import com.pwc.query.controllers.models.contents.FacetedContent;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.FacetedProps;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.util.ExceptionLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import java.util.Arrays;
import java.util.List;

public class FacetedController extends QueryController {

    private final String COMPONENT_NAME = "Faceted";

    public ControllerContent getContent() {

        FacetedContent output = null;

        Resource resource = controllerBean.getResource();
        ValueMap componentProperties = controllerBean.getProperties();

        try {
            controllerBean.setCompName(COMPONENT_NAME);

            Node currentNode = resource.adaptTo(javax.jcr.Node.class);
            String componentId = CommonsUtils.getComponentId(currentNode);

            List<String> filterMenu = componentProperties.get(FacetedProps.FILTER_MENU) !=null ?
                    ( componentProperties.get(FacetedProps.FILTER_MENU) instanceof Object[] ?
                            Arrays.asList((String[])componentProperties.get(FacetedProps.FILTER_MENU)) :
                            Arrays.asList(componentProperties.get(FacetedProps.FILTER_MENU).toString()) ) : null;

            boolean isDeepLinkSelected = componentProperties.get(CollectionProps.DEEP_LINK) != null ?
                    componentProperties.get(CollectionProps.DEEP_LINK).equals("true") : false ;

            if(!isServletCall() && isDeepLinkSelected) {

                CommonsUtils.setFiltersControllerBean(filterMenu, controllerBean);
                if(controllerBean.getFilters().size() >0 ||
                   StringUtils.isNotBlank(controllerBean.getSearchText())) controllerBean.setDeepLinkSearch(true);

            }

            List<Content> contents = getContentList();
            List<Content> fullContent = getFullContentList();

            output = new FacetedContent(contents, fullContent, controllerBean,componentId,getHits());

        } catch (Exception e) {
            ExceptionLogger.logException(e);
            controllerBean.getPwcLogger().logMessage("Faceted getContent() error : " + e.toString());
        }
        return output;
    }



}
