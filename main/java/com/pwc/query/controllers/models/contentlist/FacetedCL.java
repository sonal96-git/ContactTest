package com.pwc.query.controllers.models.contentlist;


import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.AssetProps;
import com.pwc.query.enums.PageProps;
import com.pwc.query.utils.CommonsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

public class FacetedCL extends ContentList {


    public  List<Content> getContentList(List<Resource> resources, ControllerBean controllerBean, List<String> fallbackImages ) throws Exception {

        String desc = "";
        String title = "";
        String collTitle = "";
        boolean isSearchText = StringUtils.isNotBlank(controllerBean.getSearchText());

        List<Content> contentList = new ArrayList<Content>();

        for (Resource resource : resources) {

            if ( resource.getValueMap().get(PageProps.PRIMARY_TYPE).equals(PageProps.PAGE.toString()) ) {

                if (!CommonsUtils.isValidPage(resource,controllerBean)) continue;

                if( isSearchText) {

                    Page page = controllerBean.getPageManager().getContainingPage(resource.getPath());
                    ValueMap pageProp = page.getProperties();

                    title = pageProp.get(PageProps.TITLE) != null ? (String) pageProp.get(PageProps.TITLE) : "";
                    collTitle = pageProp.get(PageProps.PWC_RVP_TITLE) != null ? (String) pageProp.get(PageProps.PWC_RVP_TITLE) : "";
                    desc = pageProp.get(PageProps.DESCRIPTION) != null ? (String) pageProp.get(PageProps.DESCRIPTION) : "";


                    if(!(CommonsUtils.isEquivalentMatch(controllerBean.getSearchText(),title) ||
                         CommonsUtils.isEquivalentMatch(controllerBean.getSearchText(),desc) ||
                         CommonsUtils.isEquivalentMatch(controllerBean.getSearchText(),collTitle))) continue;

                    Content content = new Content();
                    content.setPageAttr(resource,controllerBean, fallbackImages);
                    contentList.add(content);

                } else {
                    Content content = new Content();
                    content.setPageAttr(resource,controllerBean, fallbackImages);
                    contentList.add(content);
                }


            } else if ( resource.getValueMap().get(AssetProps.PRIMARY_TYPE).equals(AssetProps.ASSET.toString()) ) {

                Node node = resource.getResourceResolver().getResource(resource.getPath() + JCR_CONTENT ).adaptTo(Node.class);

                if (!CommonsUtils.isValidAsset(node,controllerBean)) continue;

                if(isSearchText) {

                    title = node.hasProperty(AssetProps.TITLE.toString()) ? (node.getProperty(AssetProps.TITLE.toString()).isMultiple() ?
                            node.getProperty(AssetProps.TITLE.toString()).getValues()[0].toString() : node.getProperty(AssetProps.TITLE.toString()).getValue().toString()):resource.getName();

                    desc = node.hasProperty(AssetProps.DESCRIPTION.toString()) ? (node.getProperty(AssetProps.DESCRIPTION.toString()).isMultiple() ?
                            node.getProperty(AssetProps.DESCRIPTION.toString()).getValues()[0].toString() : node.getProperty(AssetProps.DESCRIPTION.toString()).getValue().toString()):"";

                    if(!(CommonsUtils.isEquivalentMatch(controllerBean.getSearchText(),title) ||
                         CommonsUtils.isEquivalentMatch(controllerBean.getSearchText(),desc)) ) continue;

                    Content content = new Content();
                    content.setAssetAttr(resource,node,controllerBean);
                    contentList.add(content);

                } else {
                    Content content = new Content();
                    content.setAssetAttr(resource,node,controllerBean);
                    contentList.add(content);
                }
            }
        }

        setFullContentList(contentList);
        setHits(contentList.size());
        contentList = processContentList(contentList,controllerBean);

        return contentList;
    }

}
