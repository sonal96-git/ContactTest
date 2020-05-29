package com.pwc.query.search.factories;

import com.pwc.query.enums.ListFrom;
import com.pwc.query.search.queries.AdvPath;
import com.pwc.query.search.queries.AdvTags;
import com.pwc.query.search.queries.Child;
import com.pwc.query.search.queries.Descendants;
import com.pwc.query.search.queries.FixList;
import com.pwc.query.search.queries.QueryBuilderSearch;
import com.pwc.query.search.queries.SearchQuery;
import com.pwc.query.search.queries.Tags;

public class SearchQueryFactory {

	private SearchQuery searchQuery;

	public SearchQuery getSearchQuery(String query){

		switch (ListFrom.valueOf(query.toUpperCase())) {
	        case CHILDREN:
	        	searchQuery = new Child();
	            break;
	        case TREE://descendants
	        	searchQuery = new Descendants();
	            break;
	        case STATIC :
	        	searchQuery = new FixList();
	            break;
	        case QUERYBUILDER :
	        	searchQuery = new QueryBuilderSearch();
	            break;
	        case ADVANCEDTAGS :
	        	searchQuery = new AdvTags();
	            break;
	        case ADVANCEDPATHS :
	        	searchQuery = new AdvPath();
	            break;
	        case TAGS :
	        	searchQuery = new Tags();
	            break;
	        default:
	            throw new IllegalArgumentException("Invalid query " + query);
		}
		
		return searchQuery;
	}
	
}
