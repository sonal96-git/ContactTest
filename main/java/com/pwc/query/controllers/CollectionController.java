package com.pwc.query.controllers;

import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.contents.CollectionContent;
import com.pwc.query.controllers.models.contents.ControllerContent;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.util.ExceptionLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

public class CollectionController extends QueryController {


	private final String COMPONENT_NAME = "Collection";

    @Override
    public void activate() throws Exception {

        super.activate();

    }
	public ControllerContent getContent() {

		CollectionContent fetch = null;

        Resource resource = controllerBean.getResource();
        SlingHttpServletRequest request = controllerBean.getRequest();

		try {

            Node currentNode = resource.adaptTo(javax.jcr.Node.class);
            String componentId = CommonsUtils.getComponentId(currentNode);

            String offset = request.getParameter("start");

            controllerBean.setOffSet(offset);

            List<String> filterList = new ArrayList<>();
            String filter = request.getParameter("filter") ;

            if( StringUtils.isNotBlank(filter) ) {

                if (!filter.toLowerCase().equals("all")) {
                    filterList.add(filter);

                    List<List<String>> filtersList =  new ArrayList<>();
                    filtersList.add(filterList);

                    controllerBean.setFilters(filtersList);
                }
            }

            controllerBean.setCompName(COMPONENT_NAME);

            List<Content> contents = getContentList();
            List<Content> fullContentList =  getFullContentList();

            fetch = new CollectionContent(contents,fullContentList, componentId, controllerBean,getHits());

		} catch (Exception e) {
			ExceptionLogger.logException(e);
            controllerBean.getPwcLogger().logMessage("AdvCollection getContent() error : " + e.toString());
		}
		return fetch;
	}

}
