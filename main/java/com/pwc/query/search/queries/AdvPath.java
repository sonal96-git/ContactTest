package com.pwc.query.search.queries;

import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.QueryTypes;
import com.pwc.query.search.filters.FilterQuery;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.List;

public class AdvPath extends SearchQuery {

    @Override
    public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException {

        ValueMap compProp = contrBean.getProperties();
        ResourceResolver resolver = contrBean.getResourceResolver();

        String[] path = compProp.get(CollectionProps.SOURCE_ADVANCED) instanceof String
                ? convertToArray(compProp.get(CollectionProps.SOURCE_ADVANCED).toString())
                : (String[]) compProp.get(CollectionProps.SOURCE_ADVANCED);

        boolean child = QueryTypes.valueOf(compProp.get(CollectionProps.QUERY_TYPE).toString().toUpperCase()) == QueryTypes.CHILDREN ? true : false;

        Session session = resolver.adaptTo(Session.class);
        StringBuilder  queryString = getQueryForSearch(path,child,contrBean);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query query = queryManager.createQuery(queryString.toString(), "JCR-SQL2" );

        List<String> result = getQueryResults(query);

        setNumberHits(result.size());

        return getResources(result,resolver);
    }

    private StringBuilder getQueryForSearch(String[] paths,boolean isChild,ControllerBean contrBean) {

        StringBuilder builder = new StringBuilder();
        StringBuilder builderUnion = new StringBuilder();
        List<StringBuilder> partialUnionQueries = new ArrayList<>();

        FilterQuery filterQuery = filterFactory.getFilterQuery(contrBean.getCompName());

        String queryType = isChild ? "ISCHILDNODE" : "ISDESCENDANTNODE";

        String rootPage =contrBean.getCurrentPage().getPath();

        if(paths != null && paths.length >= 1) {
            for (int i = 0; i < paths.length; i++) {

                rootPage = paths[i].isEmpty() ? rootPage : paths[i];

                builder.append(queryType + "( p,'" + rootPage + "') ");
                partialUnionQueries.add(filterQuery.getFilterPredicate(contrBean.getFilters(), contrBean, builder));
                builder.setLength(0);
            }
            builderUnion.append(String.join(" UNION ", partialUnionQueries));
        } else {
            builder.append(queryType + "( p,'" + rootPage + "') ");
            builderUnion.append(filterQuery.getFilterPredicate(contrBean.getFilters(), contrBean, builder));
        }


        return builderUnion;
    }

    @Override
    public String[] getPaths(ControllerBean contrBean){

        ValueMap compProp = contrBean.getProperties();
        Page currentPage  = contrBean.getCurrentPage();
        return compProp.get(CollectionProps.SOURCE_ADVANCED) instanceof String
                ? convertToArray(compProp.get(CollectionProps.SOURCE_ADVANCED).toString())
                : (String[]) compProp.get(CollectionProps.SOURCE_ADVANCED);

    }

}
