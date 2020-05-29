package com.pwc.query.search.queries;

import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.search.filters.FilterQuery;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.List;

public class Child extends SearchQuery {

	@Override
	public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException {

		ValueMap compProp = contrBean.getProperties();
		Page currentPage  = contrBean.getCurrentPage();
		ResourceResolver resolver = contrBean.getResourceResolver();
		
		String path = compProp.get(CollectionProps.PROP_PARENT_PAGE)!=null ?compProp.get(CollectionProps.PROP_PARENT_PAGE).toString():currentPage.getPath();
		
		Session session = resolver.adaptTo(Session.class);
        StringBuilder  queryString = getQueryForSearch(path,contrBean);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(queryString.toString(), "JCR-SQL2" );

        List<String> result = getQueryResults(query);

		setNumberHits(result.size());

		return getResources(result,resolver);
	}


	private StringBuilder getQueryForSearch(String path,ControllerBean contrBean) {

		StringBuilder paths = new StringBuilder();
		FilterQuery filterQuery = filterFactory.getFilterQuery(contrBean.getCompName());

        paths.append(" ISCHILDNODE( p, '"+path+"') ");
        StringBuilder queryString =filterQuery.getFilterPredicate(contrBean.getFilters(),contrBean,paths);

		
		return queryString;
	}

	@Override
	public String[] getPaths(ControllerBean contrBean){

		ValueMap compProp = contrBean.getProperties();
		Page currentPage  = contrBean.getCurrentPage();
		return compProp.get(CollectionProps.PROP_PARENT_PAGE)!=null ?
				convertToArray(compProp.get(CollectionProps.PROP_PARENT_PAGE).toString()):
				convertToArray(currentPage.getPath());

	}
	

}
