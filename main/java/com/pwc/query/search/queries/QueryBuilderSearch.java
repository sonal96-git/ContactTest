package com.pwc.query.search.queries;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.facets.Bucket;
import com.day.cq.search.facets.buckets.SimpleBucket;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.FileTypes;
import com.pwc.query.enums.QueryLabels;
import com.pwc.query.search.filters.FilterQuery;
import com.pwc.query.utils.CommonsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilderSearch extends SearchQuery {

    private int index;

	@Override
	public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException, IOException {

        setNumberHits(0);

		Resource resource = contrBean.getResource();
		ResourceResolver resolver = contrBean.getResourceResolver();
		
		Session session = resolver.adaptTo(Session.class);
	    QueryBuilder builder = resolver.adaptTo(QueryBuilder.class);
        
        Query query = builder.loadQuery(resource.getPath()+"/"+CollectionProps.QUERYBUILDER,session);


        PredicateGroup defaultPredicatesGroup = getDefaultPredicateGroup(contrBean) ;
		PredicateGroup predicateFilter = getTagsPredicate(defaultPredicatesGroup, contrBean.getFilters());

        if(!predicateFilter.isEmpty()) {
            Bucket bucket = new SimpleBucket(predicateFilter, "");
            query = query.refine(bucket);

        }

        if(query == null) return null ;

        SearchResult result = query.getResult();
        setNumberHits(result.getTotalMatches());

		return getResourcesQB(result.getHits(),resolver);
 	}

	private PredicateGroup getTagsPredicate(PredicateGroup predicateGroup, List<List<String>> filter){

		index = 0;
		HashMap<String,String> map2 = null;

		for (List<String> list : filter) {

			map2 = new HashMap<String,String>();
			for (String stringTag : list) {

				// searching into page properties
				map2.put("group."+(index)+"_property", QueryLabels.JCR_CONTENT_PAGE_TAGSQB.toString().replace("[","").replace("]",""));
				map2.put("group."+(index)+"_property.value",stringTag );

				index++;

				// searching into asset properties
				map2.put("group."+(index)+"_property",QueryLabels.JCR_CONTENT_ASSET_TAGSQB.toString().replace("[","").replace("]",""));
				map2.put("group."+(index)+"_property.value", stringTag );

				index++;
				map2.put("group.p.or",QueryLabels.TRUE.toString());

			}
			PredicateGroup predicateGroup2 = PredicateGroup.create(map2);
			predicateGroup.add(predicateGroup2);
		}

		return  predicateGroup;
	}

	/**
	 * Returns default predicate group for query against html page.
	 *
	 * @param controllerBean {@link ControllerBean} Controller Bean Object
	 * @return defaultPredicateGroup {@link PredicateGroup} to eliminate expired and hidden in collection pages.
	 */
	private PredicateGroup getDefaultPredicateGroup(ControllerBean controllerBean) {
		PredicateGroup predicateGroup = new PredicateGroup();
		final ValueMap beanProperties = controllerBean.getProperties();
		if (CommonsUtils.isFileTypeSelected(beanProperties.get(CollectionProps.FILE_TYPE), FileTypes.HTML.toString())) {
			Map predicateMap = new HashMap();
			predicateMap.put("1_group.1_property", QueryLabels.HIDE_LEVEL_PROPERTY.toString());
			predicateMap.put("1_group.1_property.operation", QueryLabels.NOT.toString());
			predicateMap.put("1_group.2_property", QueryLabels.HIDE_LEVEL_PROPERTY.toString());
			predicateMap.put("1_group.2_property.operation", QueryLabels.UNEQUALS.toString());
			predicateMap.put("1_group.2_property.value", String.valueOf(3));
			predicateMap.put("1_group.p.or", QueryLabels.TRUE.toString());
			PredicateGroup defaultPredicateGroup = PredicateGroup.create(predicateMap);
			predicateGroup.add(defaultPredicateGroup);
		}
		return predicateGroup;
	}

    private List<Resource> getResourcesQB(List<Hit> hits, ResourceResolver resolver) throws RepositoryException{

        List<Resource> resources =new ArrayList<Resource>();
        for (Hit hit : hits) {
            Resource resource = resolver.getResource(hit.getPath());
            resources.add(resource);
        }
        return resources;

    }

	@Override
	public String[] getPaths(ControllerBean contrBean){
		return null;
	}
}
