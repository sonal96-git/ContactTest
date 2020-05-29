package com.pwc.query.search.factories;

import com.pwc.query.enums.FilterTypes;
import com.pwc.query.search.filters.FacetedFilter;
import com.pwc.query.search.filters.FilterQuery;

public class FiltersFactory {

	private FilterQuery filterQuery;

	public FilterQuery getFilterQuery(String query) {

		switch (FilterTypes.valueOf(query.toUpperCase())) {
	        case COLLECTION:
				filterQuery = new FacetedFilter();
	            break;
	        case CONTACT:
	        	//filterQuery = new ContactFilter();
	            break;
	        case FACETED :
	        	filterQuery = new FacetedFilter();
	            break;
	        default:
	            throw new IllegalArgumentException("Invalid filter " + query);
		}
		
		return filterQuery;
	}
}
