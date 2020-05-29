package com.pwc.query.controllers.models.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.elements.ControllerElements;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.FacetedProps;
import com.pwc.query.enums.I18nCollection;
import com.pwc.query.enums.I18nFaceted;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.PageService;

public class FacetedContent extends ControllerContent {

	private static final Log LOGGER = LogFactory.getLog(FacetedContent.class);

	private List<ControllerElements> collectionList;
	private String searchText;
	private String selectedTags;
	private String filterTags;
	private String componentId;
	private String searchLabel;
	private String filterByLabel;
	private String resultsLabel;
	private String sortByLabel;
	private String mostPopularLabel;
	private String publishDateLabel;
	private String loadMoreLabel;
	private String resetFiltersLabel;
	private String viewAllLabel;
	private String loadingLabel;
	private String openModalLabel;
	private String applyFiltersLabel;
	private String cancelSearchLabel;
	private String viewTranscriptLabel;
	private Long numberHits;
	private Boolean displayDate;
	private Boolean deepLinkSearch;
	private Boolean displayDownload;
	private Boolean displayResultCount;
	
	private String resultPluralLabel;
	private String resultSingularLabel;
    private String downloadPdfLabel;

    private List<Content> fullContent;
    private ControllerBean controllerBean;
    private ValueMap componentProperties;

	public FacetedContent(List<Content> contents, List<Content> fullContent, ControllerBean controllerBean, String componentId, long numberHits) throws JSONException, org.json.JSONException{

		I18nPwC i18nPwC = controllerBean.getI18nPwC();
		ValueMap componentProperties = controllerBean.getProperties();
		this.componentProperties = componentProperties;
		this.controllerBean = controllerBean;

		boolean isDeepLinkSearch = controllerBean.isDeepLinkSearch();

		List<ControllerElements> collections = getElementList(contents,"faceted",controllerBean);
		this.fullContent = fullContent;

		this.numberHits = numberHits;
		this.componentId = componentId;
		this.collectionList= collections;
		this.displayDate = componentProperties.get(CollectionProps.DISPLAY_DATE) != null ? componentProperties.get(CollectionProps.DISPLAY_DATE).equals("true") : true;
        this.displayDownload = componentProperties.get(CollectionProps.DISPLAY_DOWNLOAD) != null ? componentProperties.get(CollectionProps.DISPLAY_DOWNLOAD).equals("true") : true;
		this.deepLinkSearch = isDeepLinkSearch;
		this.selectedTags = getJsonTags(controllerBean);
		this.filterTags = getFilterTags(controllerBean);
		this.searchText = isDeepLinkSearch ? controllerBean.getSearchText() : "";
        collectionJSONParsed = collections == null ? "" : CommonsUtils.toJSON(collections,numberHits,selectedTags,filterTags);
        this.displayResultCount = componentProperties.get(CollectionProps.DISPLAY_RESULT_COUNT) != null ? componentProperties.get(CollectionProps.DISPLAY_RESULT_COUNT).equals("true") : true;
        
		//i18n
		this.searchLabel   = i18nPwC.getPwC(I18nFaceted.SERARCH_LABEL.toString());
		this.filterByLabel = i18nPwC.getPwC(I18nFaceted.FILTER_BY_LABEL.toString());
		this.resultsLabel  = i18nPwC.getPwC(I18nFaceted.RESULTS_LABEL.toString());
		this.sortByLabel   = i18nPwC.getPwC(I18nFaceted.SORT_BY_LABEL.toString());
		this.mostPopularLabel = i18nPwC.getPwC(I18nFaceted.MOST_POPULAR_LABEL.toString());
		this.publishDateLabel = i18nPwC.getPwC(I18nFaceted.PUBLISH_DATE_LABEL.toString());
		this.loadMoreLabel    = i18nPwC.getPwC(I18nFaceted.LOAD_MORE.toString());
		this.resetFiltersLabel = i18nPwC.getPwC(I18nFaceted.RESET_FILTERS_LABEL.toString());
		this.viewAllLabel = i18nPwC.getPwC(I18nFaceted.VIEW_ALL_LABEL.toString());
		this.loadingLabel = i18nPwC.getPwC(I18nFaceted.LOADING_LABEL.toString());
		this.openModalLabel = i18nPwC.getPwC(I18nFaceted.OPEN_MODAL_LABEL.toString());
		this.applyFiltersLabel = i18nPwC.getPwC(I18nFaceted.APPLY_FILTERS_LABEL.toString());
		this.cancelSearchLabel = i18nPwC.getPwC(I18nFaceted.CANCEL_SEARCH_LABEL.toString());
		this.viewTranscriptLabel = i18nPwC.getPwC(I18nFaceted.VIEW_TRANSCRIPT_LABEL.toString());
		//i18n coll v2
		this.downloadPdfLabel =i18nPwC.getPwC(I18nCollection.DOWNLOAD_PDF.toString());
		this.resultPluralLabel  = i18nPwC.getPwC(I18nFaceted.RESULT_PLURAL_LABEL.toString());
		this.resultSingularLabel  = i18nPwC.getPwC(I18nFaceted.RESULT_SINGULAR_LABEL.toString());
	}


	public List<ControllerElements> getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(List<ControllerElements> collectionList) {
		this.collectionList = collectionList;
	}

	public String getSearchLabel() {
		return searchLabel;
	}

	public void setSearchLabel(String searchLabel) {
		this.searchLabel = searchLabel;
	}

	public String getFilterByLabel() {
		return filterByLabel;
	}

	public void setFilterByLabel(String filterByLabel) {
		this.filterByLabel = filterByLabel;
	}

	public String getResultsLabel() {
		return resultsLabel;
	}

	public String getResultPluralLabel() {
		return resultPluralLabel;
	}

	public String getResultSingularLabel() {
		return resultSingularLabel;
	}

	public void setResultsLabel(String resultsLabel) {
		this.resultsLabel = resultsLabel;
	}

	public String getSortByLabel() {
		return sortByLabel;
	}

	public void setSortByLabel(String sortByLabel) {
		this.sortByLabel = sortByLabel;
	}

	public String getMostPopularLabel() {
		return mostPopularLabel;
	}

	public void setMostPopularLabel(String mostPopularLabel) {
		this.mostPopularLabel = mostPopularLabel;
	}

	public String getPublishDateLabel() {
		return publishDateLabel;
	}

	public void setPublishDateLabel(String publishDateLabel) {
		this.publishDateLabel = publishDateLabel;
	}

	public String getLoadMoreLabel() {
		return loadMoreLabel;
	}

	public void setLoadMoreLabel(String loadMoreLabel) {
		this.loadMoreLabel = loadMoreLabel;
	}

	public String getResetFiltersLabel() {
		return resetFiltersLabel;
	}

	public void setResetFiltersLabel(String resetFiltersLabel) {
		this.resetFiltersLabel = resetFiltersLabel;
	}

	public String getViewAllLabel() {
		return viewAllLabel;
	}

	public void setViewAllLabel(String viewAllLabel) {
		this.viewAllLabel = viewAllLabel;
	}

	public String getLoadingLabel() {
		return loadingLabel;
	}

	public void setLoadingLabel(String loadingLabel) {
		this.loadingLabel = loadingLabel;
	}

	public String getOpenModalLabel() {
		return openModalLabel;
	}

	public void setOpenModalLabel(String openModalLabel) {
		this.openModalLabel = openModalLabel;
	}

	public String getApplyFiltersLabel() {
		return applyFiltersLabel;
	}

	public void setApplyFiltersLabel(String applyFiltersLabel) {
		this.applyFiltersLabel = applyFiltersLabel;
	}

	public String getCancelSearchLabel() {
		return cancelSearchLabel;
	}

	public void setCancelSearchLabel(String cancelSearchLabel) {
		this.cancelSearchLabel = cancelSearchLabel;
	}

	public String getViewTranscriptLabel() {
		return viewTranscriptLabel;
	}

	public void setViewTranscriptLabel(String viewTranscriptLabel) {
		this.viewTranscriptLabel = viewTranscriptLabel;
	}

	public String getComponentId() { return componentId; }

	public void setComponentId(String componentId) { this.componentId = componentId; }

	public Long getNumberHits() {
		return numberHits;
	}

	public void setNumberHits(Long numberHits) {
		this.numberHits = numberHits;
	}

	public Boolean getDisplayDate() {
		return displayDate;
	}
	
	public Boolean getDisplayResultCount() {
		return displayResultCount;
	}

	public String getSelectedTags() {
		return selectedTags;
	}

	public void setSelectedTags(String selectedTags) {
		this.selectedTags = selectedTags;
	}
	
	public String getFilterTags() {
        return filterTags;
    }

    public void setFilterTags(String filterTags) {
        this.filterTags = filterTags;
    }

    public Boolean getIsDeepLinkSearch() {
        return deepLinkSearch;
    }

    public void setIsDeepLinkSearch(Boolean deepLinkSearch) {
        this.deepLinkSearch = deepLinkSearch;
    }

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getDownloadPdfLabel() {
        return downloadPdfLabel;
    }

	// get Json for faceted menu in collectionV2 filtering by pages where exist the selected tags.
		public String getListTag(){

			JSONObject finalMenu = new JSONObject();
			JSONArray tagsValues;
			List<FilterTag> list  = new ArrayList<FilterTag>();
			int elipsispos = 24;
			try {
				List<String> filterMenu = componentProperties.get(FacetedProps.FILTER_MENU) !=null ?
	                    ( componentProperties.get(FacetedProps.FILTER_MENU) instanceof Object[] ?
	                            Arrays.asList((String[])componentProperties.get(FacetedProps.FILTER_MENU)) :
							Arrays.asList(componentProperties.get(FacetedProps.FILTER_MENU).toString()))
					: new ArrayList<String>();

	    		JSONObject opt = new JSONObject();
	    		JSONObject displayNames = new JSONObject();
	    		JSONArray fullMenu = new JSONArray();
	    		JSONArray tagListArray = new JSONArray();
	    		JSONArray titleFormattedarray = new JSONArray();

	            int k = 0;
			if (filterMenu != null && !filterMenu.isEmpty()) {
	            for(String tagsMenu : filterMenu){

	            	JSONObject tagsObject = new JSONObject(tagsMenu);
	                tagsValues = (JSONArray) tagsObject.get("tags");
	    			JSONArray IncludeSubTagArray = (JSONArray)tagsObject.get("includeSubTags");
	    			boolean includeSubTags = IncludeSubTagArray.length() == 0 ? false : ( IncludeSubTagArray.getString(0).equals("true") ? true : false);

	    			String menuTile = tagsObject.get("title").toString();

	    			TagManager tagManager = controllerBean.getResourceResolver().adaptTo(TagManager.class);

	    			PageService pageService = new PageService();
	    			Locale locale = new Locale(pageService.getLocale(controllerBean.getRequest(), controllerBean.getCurrentPage()));

	    			for(int i=0; i < tagsValues.length(); i++) {

	    				String tagName =  tagsValues.getString(i);
	    	        	Tag tag = tagManager.resolve(tagName);

	    	        	if(tag!=null) {

	    	        		if(isFacetedTag(tagName,fullContent,controllerBean)) {

	    	                	FilterTag filterTag = new FilterTag(tag.getTitle(locale),tag.getTagID());
	    	                	String tagsTitle = filterTag.getFilterText();
								tagsTitle = tagsTitle.length() > elipsispos ? _elipsedTags(24,tagsTitle) + "..." : tagsTitle;
		            			tagListArray.put(new JSONObject().put("tagsTitle", tagsTitle).put("tag", filterTag.getFilterValue()));
	    	                }

	    	                if(includeSubTags){

	    	                    Iterator<Tag> tagIte =  tag.listAllSubTags();
	    	                    while( tagIte.hasNext() ) {

	    	                        Tag subTag =  tagIte.next();
	    	                        if(!isFacetedTag(subTag.getTagID(),fullContent,controllerBean)) continue;

	    	                        FilterTag filterTag = new FilterTag(subTag.getTitle(locale),subTag.getTagID());
									String tagsTitle = filterTag.getFilterText();
									tagsTitle = tagsTitle.length() > elipsispos ? _elipsedTags(24,tagsTitle) + " ..." : tagsTitle;
	    	            			tagListArray.put(new JSONObject().put("tagsTitle", tagsTitle).put("tag", filterTag.getFilterValue()));
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
	    	        String titleFormatted = "menu_"+k;
	    	        opt.put(titleFormatted, tagListArray);
	    	        displayNames.put(titleFormatted, menuTile);
	    	        titleFormattedarray.put(titleFormatted);
	    	        fullMenu.put(new JSONObject().put("key", titleFormatted).put("title", menuTile).put("tags", tagListArray));
	    	        finalMenu.put("opt",opt);
	    	        finalMenu.put("displayNames",displayNames);
	    	        finalMenu.put("menuOrder",titleFormattedarray);
	    	        finalMenu.put("fullMenu",fullMenu);
	    	        finalMenu.put("tags",new JSONArray());
	    	        finalMenu.put("tagsTitle",new JSONArray());
	    	        tagListArray = new JSONArray();
	    	        k++;
				}
			}


			} catch (JSONException e) {
				// TODO Auto-generated catch block
				ExceptionLogger.logExceptionMessage("Faceted Menu getListTag error :",e);
			}
			return finalMenu.toString();
		}
	public String _elipsedTags(int elipsisPos, String tagtitle){
		int subpos = tagtitle.charAt(elipsisPos) == ' ' ? elipsisPos : tagtitle.lastIndexOf(" ",elipsisPos);
		return tagtitle.substring(0,subpos);
	};

		public String getViewAllLink(){
			String moreResultLink = componentProperties.get("resultslink") != null ? componentProperties.get("resultslink").toString() : "";
			moreResultLink = CommonUtils.convertUrl(controllerBean.getRequest(), moreResultLink);
		if (controllerBean.isEnabledTransformer() && StringUtils.isNotBlank(moreResultLink)) {
	            try {
					return controllerBean.getDefaultDomainConf() == null ?
					        CommonUtils.getExternalUrl(controllerBean.getRequest(), moreResultLink):
					        getExternalUrl(moreResultLink, controllerBean);
				} catch (Exception exception) {
					LOGGER.error("Link Transformation failed for the View All link of Collection V2 component!!", exception);
				}
	        }
			return moreResultLink;
		}
		
		private String getExternalUrl(String path, ControllerBean controllerBean ) throws Exception{

	        String defaultDomain = (String) controllerBean.getDefaultDomainConf().getProperties().get("domain");
	        String domainType = (String) controllerBean.getDefaultDomainConf().getProperties().get("domainType");
	        LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(controllerBean.getRepository(), defaultDomain, domainType);

	        return linkTransformerService.transformAEMUrl(path);
		}

		private boolean isFacetedTag(String tagName, List<Content> fullContentList,ControllerBean controllerBean) {

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

	private String getFilterTags(ControllerBean controllerBean) throws JSONException {

        JSONObject jsonTag = null;
		JSONObject mainObj = new JSONObject();
		
		PageService pageService = new PageService();
        Locale locale = new Locale(pageService.getLocale(controllerBean.getRequest(), controllerBean.getCurrentPage()));

		TagManager tagMan = controllerBean.getResourceResolver().adaptTo(TagManager.class);

		int i =0;
		List<List<String>> tagsList = controllerBean.getFilters();
		for(List<String> list : tagsList) {


			JSONArray jsonTagArray = new JSONArray();
			for(String stringTag : list) {

				Tag tag = tagMan.resolve(stringTag);
				
				if(tag == null || CommonsUtils.isInArray(jsonTagArray,tag.getTagID())) continue;
				
                jsonTag = new JSONObject();
				jsonTag.put("tagsTitle", tag.getTitle(locale));
				jsonTag.put("tagID", tag.getTagID());
				jsonTag.put("tag", stringTag);

				jsonTagArray.put(jsonTag);

			}
			mainObj.put("menu_"+i,jsonTagArray);
			i++;
		}

		return mainObj.toString();
	}
	private String getJsonTags(ControllerBean controllerBean) throws JSONException {

		JSONObject jsonTag = null;
		JSONObject mainObj = new JSONObject();

		int i =0;
		List<List<String>> tagsList = controllerBean.getFilters();

		for(List<String> list : tagsList) {

			JSONArray jsonTagArray = new JSONArray();
			for(String stringTag : list) {


				String[] backLinksArray = CommonsUtils.getTagBackLinks(controllerBean,stringTag);

				if(backLinksArray.length > 0){

					for(int k=0; k<backLinksArray.length; k++){

						if(k%2 == 0) {
							jsonTag = getJsonTag(backLinksArray[k].trim(),controllerBean);
							if(jsonTag != null) jsonTagArray.put(jsonTag);
						}
					}
				} else {

					jsonTag = getJsonTag(stringTag,controllerBean);
					if(jsonTag != null) jsonTagArray.put(jsonTag);
				}

			}
			mainObj.put("menu_"+i,jsonTagArray);
			i++;
		}

		return mainObj.toString();
	}

	private JSONObject getJsonTag(String tagId, ControllerBean controllerBean) throws JSONException {

		JSONObject jsonTag = new JSONObject();

		TagManager tagMan = controllerBean.getResourceResolver().adaptTo(TagManager.class);

		Tag tag = tagMan.resolve(tagId.trim());
		
		if(tag != null) return null;

		jsonTag.put("tagsTitle", tag.getTitle());
		jsonTag.put("tag", tagId);

		return jsonTag;
	}

}
