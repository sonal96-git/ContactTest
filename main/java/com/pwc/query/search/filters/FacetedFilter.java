package com.pwc.query.search.filters;

import com.day.cq.search.PredicateGroup;
import com.pwc.query.controllers.models.ControllerBean;

import java.util.HashMap;
import java.util.List;

public class FacetedFilter extends FilterQuery {

	@Override
	public StringBuilder getFilterPredicate(List<List<String>> filter,ControllerBean contrBean,StringBuilder paths) {


		StringBuilder filterQuery = getTagsPredicate(contrBean,filter);
		StringBuilder queryString = getTypesPredicate(contrBean,filterQuery,paths);

		return queryString;
	}

	@Override
	public HashMap<String, String> getFilterMap(HashMap<String, String> map,ControllerBean contrBean) {
        map.put("p.limit","-1");
		return map;
	}


}