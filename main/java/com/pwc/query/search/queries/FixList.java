package com.pwc.query.search.queries;

import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.search.filters.FilterQuery;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.List;

public class FixList extends SearchQuery {

	@Override
	public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException {
		
		ValueMap compProp = contrBean.getProperties();
		ResourceResolver resolver = contrBean.getResourceResolver();

		String[] paths = compProp.get(CollectionProps.PROP_PAGES) instanceof String
				? convertToArray(compProp.get(CollectionProps.PROP_PAGES).toString())
				: (String[]) compProp.get(CollectionProps.PROP_PAGES);

        if((paths.length == 0) ) return null;

		Session session = resolver.adaptTo(Session.class);
        StringBuilder  queryString = getQueryForSearch(paths,contrBean);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query query = queryManager.createQuery(queryString.toString(), "JCR-SQL2" );

        List<String> result = getQueryResults(query);

        setNumberHits(result.size());

        return getResources(result,resolver);
	}

	private StringBuilder getQueryForSearch(String[] paths,ControllerBean contrBean) {

		StringBuilder builder = new StringBuilder();
		StringBuilder builderUnion = new StringBuilder();
		List<StringBuilder> partialUnionQueries = new ArrayList<>();

		FilterQuery filterQuery = filterFactory.getFilterQuery(contrBean.getCompName());

		String rootPage =contrBean.getCurrentPage().getPath();

		for (int i = 0; i < paths.length; i++) {

			rootPage = paths[i].isEmpty() ? rootPage : paths[i];

			builder.append("[jcr:path]  = '" + rootPage + "' ");
			partialUnionQueries.add(filterQuery.getFilterPredicate(contrBean.getFilters(), contrBean, builder));
			builder.setLength(0);
		}
		builderUnion.append(String.join(" UNION ", partialUnionQueries));

		return builderUnion;
	}

	@Override
	public String[] getPaths(ControllerBean contrBean){

		ValueMap compProp = contrBean.getProperties();
		return compProp.get(CollectionProps.PROP_PAGES) instanceof String
				? convertToArray(compProp.get(CollectionProps.PROP_PAGES).toString())
				: (String[]) compProp.get(CollectionProps.PROP_PAGES);

	}


}
