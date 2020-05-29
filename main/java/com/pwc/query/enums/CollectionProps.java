package com.pwc.query.enums;

public enum CollectionProps {

	BUILD_LIST_USING("listFrom"),
	PROP_PARENT_PAGE("parentPage"),
	SOURCE_CHILDREN("children"),
	SOURCE_DESCENDANTS("rootPage"),
	SOURCE_ADVANCED("advancedpaths"),
	LIMIT("limit"),
	LIMIT_BYPASS("limit-bypass"),
	QUERY_TYPE("queryType"),
	PROP_PAGES("pages"),
	QUERYBUILDER("savedquery"),
	FILE_TYPE("fileType"),
	TAGS("tags"),
	TAGS_MATCH("tagsMatch"),
	TAGS_SEARCH_ROOT("tagsSearchRoot"),
	ADVANCED_TAGS("advancedtags"),
	ORDERED("ordered"),
	DISPLAY_AS("displayAs"),
	DISPLAY_TAGS("displayTags"),
	DISPLAY_SUB_TAGS("displaySubTags"),
	DISPLAY_AS_DEFAULT("default"),
	GROUP_BY("groupby"),
	MORE_RESULTS_LINK("resultsLink"),
	MORE_RESULTS("moreResults"),
	PAGEMAX_LOCAL("pageMax_local"),
	PAGEMAX("pageMax"),
	DATE_FORMAT("dateFormat"),
    DISPLAY_DATE("displayDate"),
    DISPLAY_RESULT_COUNT("displayResultCount"),
	FIRST_RESULT("firstResult"),
	DEEP_LINK("deeplink"),
	CREATED_DATE("jcr:created"),
	INCLUDE_SUB_TAGS("includeSubTags"),
	DISPLAY_DOWNLOAD("displayDownload"),
	FILTER_VALUES("filterValue");
	
	private final String prop;

    private CollectionProps(String type) {
        this.prop = type;
    }

    @Override
    public String toString() {
        return prop;
    }
}
