package com.pwc.query.enums;

public enum I18nFaceted {
	
	SERARCH_LABEL("Faceted_Navigation_Search"),
	FILTER_BY_LABEL("Faceted_Navigation_FilterBy"),
	RESULTS_LABEL("Faceted_Navigation_Results"),
	SORT_BY_LABEL("Faceted_Navigation_SortBy"),
	MOST_POPULAR_LABEL("Faceted_Navigation_MostPopular"),
	PUBLISH_DATE_LABEL("Faceted_Navigation_PublishDate"),
	LOAD_MORE("Faceted_Navigation_LoadMore"),
	RESET_FILTERS_LABEL("Faceted_Navigation_ResetFilters"),
	VIEW_ALL_LABEL("Faceted_Navigation_View_All"),
	LOADING_LABEL("Faceted_Navigation_Loading"),
	OPEN_MODAL_LABEL("Faceted_Navigation_OpenModal"),
	APPLY_FILTERS_LABEL("Faceted_Navigation_ApplyFilters"),
	CANCEL_SEARCH_LABEL("Faceted_Navigation_CancelSearch"),
	VIEW_TRANSCRIPT_LABEL("Faceted_Navigation_ViewTranscript"),
	
	RESULT_PLURAL_LABEL("Collection_Result_Plural"),
	RESULT_SINGULAR_LABEL("Collection_Result_Singular");
	
	private final String i18n;

    private I18nFaceted(String i18n) {
        this.i18n = i18n;
    }

    @Override
    public String toString() {
        return i18n;
    }

}
