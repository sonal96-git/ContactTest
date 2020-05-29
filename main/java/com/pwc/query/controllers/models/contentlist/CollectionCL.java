package com.pwc.query.controllers.models.contentlist;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;

import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.AssetProps;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.PageProps;
import com.pwc.query.utils.CommonsUtils;

public class CollectionCL extends ContentList{


    public List<Content> getContentList(List<Resource> resources, ControllerBean controllerBean, List<String> fallbackImages) throws Exception {

        List<Content> contentList = new ArrayList<Content>();
        ValueMap componentProperties = controllerBean.getProperties();

        for (Resource resource : resources) {

            if ( resource.getValueMap().get(PageProps.PRIMARY_TYPE).equals(PageProps.PAGE.toString()) ) {

                if (!CommonsUtils.isValidPage(resource,controllerBean)) continue;

                Content content = new Content();
                content.setPageAttr(resource,controllerBean, fallbackImages);
                contentList.add(content);


            } else if ( resource.getValueMap().get(AssetProps.PRIMARY_TYPE).equals(AssetProps.ASSET.toString()) ) {

                Node node = resource.getResourceResolver().getResource(resource.getPath() + JCR_CONTENT ).adaptTo(Node.class);

                if (!CommonsUtils.isValidAsset(node,controllerBean)) continue;

                Content content = new Content();
                content.setAssetAttr(resource,node,controllerBean);
                contentList.add(content);
            }
        }

        setFullContentList(contentList);

        boolean isDeepLinkSelected = componentProperties.get(CollectionProps.DEEP_LINK) != null ?
                componentProperties.get(CollectionProps.DEEP_LINK).equals("true") : false ;

        if(isDeepLinkSelected && isDeepLinkSearch(controllerBean)) {

            String selectedTag =  CommonsUtils.getFirstTag(controllerBean.getFilters());
            contentList = getFilterContentList(selectedTag, contentList,controllerBean);
        }

        setHits(contentList.size());
        contentList = processContentList(contentList,controllerBean);

        return contentList;
    }

    private List<Content> getFilterContentList(String tagName, List<Content> contents,ControllerBean contrBean) throws JSONException {

        List<Content> contentList =  new ArrayList<>();
        List<String> backLinks = Arrays.asList(CommonsUtils.getTagBackLinks(contrBean,tagName));
        backLinks = backLinks.stream().map(String :: trim).collect(Collectors.toList());
        
        for(Content content : contents ) {
        	String[] contentTags =  content.getTags();
        	
            if(contentTags == null) continue;
            
            List<String> tags = Arrays.asList(contentTags);
            tags = tags.stream().map(String :: trim).collect(Collectors.toList());
            
            if (!Collections.disjoint(backLinks, tags)) {
                contentList.add(content);
            }
        }
        return contentList;
    }

    private boolean isDeepLinkSearch(ControllerBean contrBean) throws JSONException,RepositoryException, org.json.JSONException{

        boolean isDeepLinkSearch =  false ;
        SlingHttpServletRequest request = contrBean.getRequest();


        ValueMap componentProperties = contrBean.getProperties();
        GregorianCalendar createdDate = componentProperties.get(CollectionProps.CREATED_DATE) != null ?
                (GregorianCalendar)componentProperties.get(CollectionProps.CREATED_DATE) : null;

        String seconds = String.valueOf(createdDate.get(Calendar.SECOND));
        String milliSec = String.valueOf(createdDate.get(Calendar.MILLISECOND));

        String deepLinkId =  seconds+milliSec;

        List<String> filterValues = componentProperties.get(CollectionProps.FILTER_VALUES) !=null ?
                ( componentProperties.get(CollectionProps.FILTER_VALUES) instanceof Object[] ?
                        Arrays.asList((String[])componentProperties.get(CollectionProps.FILTER_VALUES)) :
                        Arrays.asList(componentProperties.get(CollectionProps.FILTER_VALUES).toString()) ) : null;

        String filter = StringUtils.isNotBlank(request.getParameter("tags"+deepLinkId)) ? request.getParameter("tags"+deepLinkId) : "{}";

        List<List<String>> tagsList  = CommonsUtils.getTagsList(filter);
        String firstTag = CommonsUtils.getFirstTag(tagsList);

        if(StringUtils.isNotBlank(firstTag) && !firstTag.equals("all") && CommonsUtils.isValidTagCollection(firstTag,filterValues,contrBean)) {
            isDeepLinkSearch = true;
            contrBean.setFilters(tagsList);
        }

        return isDeepLinkSearch;
    }
}
