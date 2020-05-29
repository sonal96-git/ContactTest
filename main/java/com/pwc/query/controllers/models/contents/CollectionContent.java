package com.pwc.query.controllers.models.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.elements.ControllerElements;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.FileTypes;
import com.pwc.query.enums.I18nCollection;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.util.AuthoringUtils;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.PageService;

public class CollectionContent extends ControllerContent{

    private List<ControllerElements> list;
    private ControllerElements featureFirst;
    private String componentId;
    private String element;
    private String type;
    private String groupBy;
    private Boolean isTouch;
    private Boolean isEmpty;
    private Boolean isPaginating;
    private Boolean isVideoChecked;
    private String nextLink;
    private String previousLink;
    private int pageStart;
    private String queryString;
    private String[] filterAll;
    private List<FilterTag> filter;
    private List<PageLink> paginationLinks;
    private String lastPaginationLink;
    private String selectedTag;
    private String moreResultsLink;
    private String showMoreResults;
    private Long numberHits;
    private Boolean displayDate;
    private String displayDownload;

    //i18n collection
    private String previousLabel;
    private String currentLabel;
    private String filterAllLabel;
    private String navigationNextLabel;
    private String navigationPreviousLabel;
    private String loadMoreLabel;
    private String viewAllLabel;
    private String downloadPdfLabel;
    private String showAllLabel;
    private String orLabel;
    private String filterByLabel;

    public CollectionContent(List<Content> contents,List<Content>  fullContents,String componentId,
                             ControllerBean controllerBean,long numberHits) throws JSONException {


        SlingHttpServletRequest request = controllerBean.getRequest();
        ValueMap componentProperties = controllerBean.getProperties();

        List<Content>  contentList = contents;
        I18nPwC i18nPwC =  controllerBean.getI18nPwC();
        String selectedTag =  CommonsUtils.getFirstTag(controllerBean.getFilters());

        List<ControllerElements> elementList =getElementList(contentList,"collection",controllerBean);
        String resultsLink = ObjectUtils.firstNonNull( componentProperties.get(CollectionProps.MORE_RESULTS_LINK), "").toString();

        int offSet =  controllerBean.getOffSet() != null ? Integer.parseInt(controllerBean.getOffSet()) : 0;


        this.numberHits = numberHits;

        int pageMax = componentProperties.get(CollectionProps.PAGEMAX) != null ?
                Integer.parseInt(componentProperties.get(CollectionProps.PAGEMAX).toString()) : 0;
        int next= (elementList.size()+offSet);
        this.nextLink = elementList.size() != numberHits ?
                ( elementList.size()+offSet == numberHits ? null : "start=" +next ): null;

        int previous = offSet == 0 || pageMax == 0 ? -1 : offSet - pageMax;
        this.previousLink = previous == -1 ? null :"start=" +previous;

        this.paginationLinks = pageMax > 0 ? getPageLinks(numberHits,pageMax) :null;

        this.componentId = componentId;
        this.isEmpty = elementList.isEmpty();
        this.isTouch = AuthoringUtils.isTouch(request);
        this.groupBy = componentProperties.get(CollectionProps.GROUP_BY) != null ? componentProperties.get(CollectionProps.GROUP_BY).toString() :"";
        this.element = componentProperties.get(CollectionProps.ORDERED) == "true" ? "ol" : "ul";
        this.displayDate = componentProperties.get(CollectionProps.DISPLAY_DATE) != null ? componentProperties.get(CollectionProps.DISPLAY_DATE).equals("true") : true ;

        //TODO: Review this hardcoded true (also true in collection.js)
        this.isPaginating = true;
        this.moreResultsLink = CommonUtils.convertUrl(request,resultsLink);
        this.isVideoChecked = CommonsUtils.isFileTypeSelected(componentProperties.get(CollectionProps.FILE_TYPE), FileTypes.VIDEO.toString());
        this.showMoreResults =componentProperties.get(CollectionProps.MORE_RESULTS.toString()) !=null?
                componentProperties.get(CollectionProps.MORE_RESULTS.toString()).toString() : "";
        this.pageStart = request.getParameter("start") != null ? Integer.parseInt(request.getParameter("start")) : 0;
        this.queryString = ObjectUtils.firstNonNull(request.getParameter("filter"), request.getParameter("start"));
        this.type = ObjectUtils.firstNonNull(componentProperties.get(CollectionProps.DISPLAY_AS), CollectionProps.DISPLAY_AS_DEFAULT).toString();

        this.filter = componentProperties.get(CollectionProps.FILTER_VALUES) != null ?
                getListTags((String[]) componentProperties.get(CollectionProps.FILTER_VALUES),controllerBean,componentProperties,fullContents) : null;

        this.featureFirst = componentProperties.get(CollectionProps.FIRST_RESULT) != null &&
                ( componentProperties.get(CollectionProps.DISPLAY_AS) != null &&
                        componentProperties.get(CollectionProps.DISPLAY_AS).toString().equals("newsletter") ) && offSet == 0?
                elementList.remove(0) : null;

        this.list = elementList;

        List<String> elementBL = Arrays.asList(CommonsUtils.getTagBackLinks(controllerBean,selectedTag));
        elementBL = elementBL.stream().map(String :: trim).collect(Collectors.toList());
        
        this.selectedTag = !elementBL.isEmpty() ?elementBL.get(0):selectedTag;

        this.featureFirst = elementList.get(0);
        this.displayDownload = componentProperties.get(CollectionProps.DISPLAY_DOWNLOAD.toString()) !=null?
                componentProperties.get(CollectionProps.DISPLAY_DOWNLOAD.toString()).toString() : "";

        //i18n  Collection
        this.currentLabel = i18nPwC.getPwC(I18nCollection.CURRENT_LABEL.toString());
        this.viewAllLabel = i18nPwC.getPwC(I18nCollection.VIEWALL_LABEL.toString());
        this.previousLabel = i18nPwC.getPwC(I18nCollection.PREVIOUS_LABEL.toString());
        this.loadMoreLabel = i18nPwC.getPwC(I18nCollection.LOADMORE_LABEL.toString());
        this.filterAllLabel = i18nPwC.getPwC(I18nCollection.FILTERALL_LABEL.toString());
        this.navigationNextLabel = i18nPwC.getPwC(I18nCollection.NAVIGATIONNEXT_LABEL.toString());
        this.navigationPreviousLabel = i18nPwC.getPwC(I18nCollection.NAVIGATIONPREVIOUS_LABEL.toString());
        this.downloadPdfLabel =i18nPwC.getPwC(I18nCollection.DOWNLOAD_PDF.toString());
        this.showAllLabel =i18nPwC.getPwC(I18nCollection.SHOW_ALL_LABEL.toString());
        this.orLabel =i18nPwC.getPwC(I18nCollection.OR_LABEL.toString());
        this.filterByLabel =i18nPwC.getPwC(I18nCollection.FILTER_BY_LABEL.toString());

    }

    public List<ControllerElements> getList() {
        return list;
    }

    public void setList(List<ControllerElements> list) {
        this.list = list;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public Boolean getIsTouch() {
        return isTouch;
    }

    public void setIsTouch(Boolean touch) {
        isTouch = touch;
    }

    public Boolean getIsEmptyy() {
        return isEmpty;
    }

    public void setIsEmpty(Boolean empty) {
        isEmpty = empty;
    }

    public Boolean getIsPaginating() {
        return isPaginating;
    }

    public void setIsPaginating(Boolean paginating) {
        isPaginating = paginating;
    }

    public Boolean getIsVideoChecked() {
        return isVideoChecked;
    }

    public void setIsVideoChecked(Boolean video) {
        isVideoChecked = video;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public String getPreviousLink() {
        return previousLink;
    }

    public void setPreviousLink(String previousLink) {
        this.previousLink = previousLink;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String[] getFilterAll() {
        return filterAll;
    }

    public void setFilterAll(String[] filterAll) {
        this.filterAll = filterAll;
    }

    public List<FilterTag> getFilter() {
        return filter;
    }

    public void setFilter(List<FilterTag> filter) {
        this.filter = filter;
    }

    public List<PageLink> getPaginationLinks() {
        return paginationLinks;
    }

    public void setPaginationLinks(List<PageLink> paginationLinks) {
        this.paginationLinks = paginationLinks;
    }

    public String getLastPaginationLink() {
        return lastPaginationLink;
    }

    public void setLastPaginationLink(String lastPaginationLink) {
        this.lastPaginationLink = lastPaginationLink;
    }

    public String getMoreResultsLink() {
        return moreResultsLink;
    }

    public void setMoreResultsLink(String moreResultsLink) {
        this.moreResultsLink = moreResultsLink;
    }

    public String getShowMoreResults() {
        return showMoreResults;
    }

    public void setShowMoreResults(String showMoreResults) {
        this.showMoreResults = showMoreResults;
    }

    public String getPreviousLabel() {
        return previousLabel;
    }

    public void setPreviousLabel(String previousLabel) {
        this.previousLabel = previousLabel;
    }

    public String getCurrentLabel() {
        return currentLabel;
    }

    public void setCurrentLabel(String currentLabel) {
        this.currentLabel = currentLabel;
    }

    public String getFilterAllLabel() {
        return filterAllLabel;
    }

    public void setFilterAllLabel(String filterAllLabel) {
        this.filterAllLabel = filterAllLabel;
    }

    public String getNavigationNextLabel() {
        return navigationNextLabel;
    }

    public void setNavigationNextLabel(String navigationNextLabel) {
        this.navigationNextLabel = navigationNextLabel;
    }

    public String getNavigationPreviousLabel() {
        return navigationPreviousLabel;
    }

    public void setNavigationPreviousLabel(String navigationPreviousLabel) {
        this.navigationPreviousLabel = navigationPreviousLabel;
    }

    public String getLoadMoreLabel() {
        return loadMoreLabel;
    }

    public void setLoadMoreLabel(String loadMoreLabel) {
        this.loadMoreLabel = loadMoreLabel;
    }

    public String getViewAllLabel() {
        return viewAllLabel;
    }

    public void setViewAllLabel(String viewAllLabel) {
        this.viewAllLabel = viewAllLabel;
    }

    public Boolean getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(Boolean displayDate) {
        this.displayDate = displayDate;
    }

    public Long getNumberHits() {
        return numberHits;
    }

    public void setNumberHits(Long numberHits) {
        this.numberHits = numberHits;
    }

    public ControllerElements getFeatureFirst() {
        return featureFirst;
    }

    public void setFeatureFirst(ControllerElements featureFirst) {
        this.featureFirst = featureFirst;
    }

    public String getDisplayDownload() {
        return displayDownload;
    }

    public void setDisplayDownload(String displayDownload) {
        this.displayDownload = displayDownload;
    }

    public String getDownloadPdfLabel() {
        return downloadPdfLabel;
    }

    public void setDownloadPdfLabel(String downloadPdfLabel) {
        this.downloadPdfLabel = downloadPdfLabel;
    }

    public String getShowAllLabel() {
        return showAllLabel;
    }

    public void setShowAllLabel(String showAllLabel) {
        this.showAllLabel = showAllLabel;
    }

    public String getSelectedTag() {
        return selectedTag;
    }

    public void setSelectedTag(String selectedTag) {
        this.selectedTag = selectedTag;
    }
    
    public String getOrLabel() {
        return orLabel;
    }

    public void setOrLabel(String orLabel) {
        this.orLabel = orLabel;
    }

    public String getFilterByLabel() {
        return filterByLabel;
    }

    public void setFilterByLabel(String filterByLabel) {
        this.filterByLabel = filterByLabel;
    }

    private List<FilterTag> getListTags(String[] tagsValues, ControllerBean controllerBean,
                                        ValueMap componentProperties, List<Content> fullContentList) {

        boolean includeSubTags = componentProperties.get(CollectionProps.INCLUDE_SUB_TAGS) != null ?
                componentProperties.get(CollectionProps.INCLUDE_SUB_TAGS).equals("true") : false;

        List<FilterTag> list  = new ArrayList<FilterTag>();
        TagManager tagManager = controllerBean.getResourceResolver().adaptTo(TagManager.class);

        PageService pageService = new PageService();
        Locale locale = new Locale(pageService.getLocale(controllerBean.getRequest(), controllerBean.getCurrentPage()));		

        for(int i=0; i < tagsValues.length; i++) {

        	String tagName =  tagsValues[i].toString();
        	Tag tag = tagManager.resolve(tagName);

        	if(tag!=null) {
        		
        		if(isCollectionTag(tagName,fullContentList,controllerBean)) {

                	FilterTag filterTag = new FilterTag(tag.getTitle(locale),tag.getTagID());

                    if(!list.contains(filterTag)) list.add(filterTag);
                }

                if(includeSubTags){

                    Iterator<Tag> tagIte =  tag.listAllSubTags();
                    while( tagIte.hasNext() ) {

                        Tag subTag =  tagIte.next();
                        if(!isCollectionTag(subTag.getTagID(),fullContentList,controllerBean)) continue;

                        FilterTag filterTag = new FilterTag(subTag.getTitle(locale),subTag.getTagID());

                        if(!list.contains(filterTag)) list.add(filterTag);
                    }
                }
        	}

        }

        if (list.size() > 0) {
            Collections.sort(list, new Comparator<FilterTag>() {
                @Override
                public int compare(final FilterTag object1, final FilterTag object2) {
                    return object1.getFilterText().compareTo(object2.getFilterText());
                }
            });
        }
        return list;
    }

    private List<PageLink> getPageLinks(long numberHits, int pageMax) {
        List<PageLink> pageLinks = new ArrayList<PageLink>();

        int page = 1;
        for (int i = 0 ; i < numberHits ; i += pageMax) {
            PageLink pageL = new PageLink("start="+i,page++,i);
            pageLinks.add(pageL);
        }
        return pageLinks;
    }

    private boolean isCollectionTag(String tagName, List<Content> fullContentList,ControllerBean controllerBean) {
    	
        boolean tagExists =  false;

        List<String> elementBL = Arrays.asList(CommonsUtils.getTagBackLinks(controllerBean,tagName));
        elementBL = elementBL.stream().map(String :: trim).collect(Collectors.toList());
        
		if(elementBL.isEmpty()) return tagExists;
		
        for(Content content : fullContentList ) {
        	
            if(content.getTags() == null) continue;
            List<String> tags = Arrays.asList(content.getTags());
            
            if (!Collections.disjoint(elementBL, tags)) {
            	tagExists =  true;
                break;
            }
        }
        return tagExists;
    }

}
