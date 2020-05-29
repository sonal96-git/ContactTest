package com.pwc.query.controllers.models.contentlist;


import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.utils.CommonsUtils;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ContentList {

    private final String ORDER_BY_PROP = "orderBy";
    private final String DEFAULT_ORDER_BY = "pwcReleaseDate";

    private final String ORDER_BY_DATE_ASC 			= "pwcReleaseDate_Asc";
    private final String ORDER_BY_DATE_DESC 		= "pwcReleaseDate";
    private final String ORDER_BY_COLLECTION_TITLE 	= "jcr:title";

    protected final String JCR_CONTENT = "/jcr:content/metadata";

    private int hits;
    private List<Content> fullContentList = new ArrayList<Content>();

    public abstract List<Content> getContentList(List<Resource>  resources,ControllerBean controllerBean, List<String> fallbackImages) throws Exception;

    protected List<Content> processContentList(List<Content> contentList,ControllerBean controllerBean) throws Exception {

        String orderBy = controllerBean.getProperties().get(ORDER_BY_PROP) != null ?
                controllerBean.getProperties().get(ORDER_BY_PROP).toString():DEFAULT_ORDER_BY;

        if (!contentList.isEmpty()) {

            switch (orderBy) {

                case ORDER_BY_DATE_ASC:
                    Collections.sort(contentList);
                    break;
                case ORDER_BY_DATE_DESC:
                    Collections.sort(contentList, Content.Comparators.DATE_DESC);
                    break;
                case ORDER_BY_COLLECTION_TITLE:
                    Collections.sort(contentList, Content.Comparators.TITLE);
                    break;
            }

            for (Content content: contentList) {
                content.setDateValue(CommonsUtils.formattedDate(controllerBean,content.getDateValue()));
            }

            int limit = getNumberResultsToShow(controllerBean);
            int from = controllerBean.getOffSet() != null ? Integer.valueOf(controllerBean.getOffSet()): 0;
            int to = contentList.size() > limit ? (limit + from > contentList.size()? contentList.size():limit + from ):contentList.size();
            contentList = contentList.subList(from,to);
        }

        return contentList;
    }

    private int getNumberResultsToShow(ControllerBean contrBean ) {

        int limit = CommonsUtils.getLimit(contrBean);
        int resultsToShow = limit;

        int max = contrBean.getProperties().get(CollectionProps.PAGEMAX_LOCAL) != null ?
                Integer.valueOf(contrBean.getProperties().get(CollectionProps.PAGEMAX_LOCAL).toString()) :
                (contrBean.getProperties().get(CollectionProps.PAGEMAX) != null ?
                        Integer.valueOf(contrBean.getProperties().get(CollectionProps.PAGEMAX).toString()) : -1);
        if (max != -1) {

            //max = Integer.valueOf(contrBean.getProperties().get(CollectionProps.PAGEMAX_LOCAL).toString());
            int offSet = contrBean.getOffSet() != null ? Integer.valueOf(contrBean.getOffSet()) : 0;

            // if 'limit' is less than 'offSet', then the total of result to show is equal to 'limit'
            // else, if '(limit - offSet)' is greater than 'max', then the total of result to show is equal to 'max'
            // else the total of result to show is equal to '(limit - offSet)'
            resultsToShow = limit < offSet ? limit : ((limit - offSet) >= max ? max : (limit - offSet));
        }

        return resultsToShow;
    }

    public int getHits() {
        return hits;
    }
    public void setHits(int hits) {
        this.hits = hits;
    }
    public List<Content> getFullContentList() {
        return fullContentList;
    }
    public void setFullContentList(List<Content> fullContentList) {
        this.fullContentList = fullContentList;
    }
}
