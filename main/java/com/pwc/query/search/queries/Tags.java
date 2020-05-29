package com.pwc.query.search.queries;

import com.day.cq.commons.RangeIterator;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.search.filters.FilterQuery;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tags extends SearchQuery{

    @Override
    public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException, IOException {

        ValueMap compProp = contrBean.getProperties();
        Page currentPage  = contrBean.getCurrentPage();
        ResourceResolver resolver = contrBean.getResourceResolver();

        String path = compProp.get(CollectionProps.TAGS_SEARCH_ROOT) !=null ?
                compProp.get(CollectionProps.TAGS_SEARCH_ROOT).toString():currentPage.getPath();

        String[] tags = (String[]) compProp.get(CollectionProps.TAGS);
        boolean matchAny = compProp.get(CollectionProps.TAGS_MATCH).toString().equals("any");

        TagManager tagMan = resolver.adaptTo(TagManager.class);
        RangeIterator<Resource> tagResultsIterator  =tagMan.find(path,tags,matchAny);

        List<String> paths = getPaths(tagResultsIterator);

        if((paths.isEmpty()) ) return null;

        // return getResources(paths,resolver);
        Session session = resolver.adaptTo(Session.class);
        StringBuilder  queryString = getQueryForSearch(paths.toArray(new String[0]),contrBean);

        QueryManager queryManager = session.getWorkspace().getQueryManager();
        javax.jcr.query.Query query = queryManager.createQuery(queryString.toString(), "JCR-SQL2" );

        List<String> result = getQueryResults(query);

        setNumberHits(result.size());

        return getResources(result,resolver);
    }

    private List<String> getPaths(RangeIterator<Resource> tagResultsIterator) {

        List<String> paths = new ArrayList<String>();
        while (tagResultsIterator.hasNext()) {
            paths.add(tagResultsIterator.next().getParent().getPath().replace("/jcr:content",""));
        }
        return paths;
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
        Page currentPage  = contrBean.getCurrentPage();
        return compProp.get(CollectionProps.TAGS_SEARCH_ROOT) !=null ?
                convertToArray(compProp.get(CollectionProps.TAGS_SEARCH_ROOT).toString()):
                convertToArray(currentPage.getPath());

    }


}
