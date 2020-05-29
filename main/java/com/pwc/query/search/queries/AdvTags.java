package com.pwc.query.search.queries;

import com.day.cq.commons.RangeIterator;
import com.day.cq.search.QueryBuilder;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.search.filters.FilterQuery;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdvTags extends SearchQuery {

    @Override
    public List<Resource> getResults(ControllerBean contrBean) throws RepositoryException {

        ValueMap compProp = contrBean.getProperties();
        Page currentPage  = contrBean.getCurrentPage();
        ResourceResolver resolver = contrBean.getResourceResolver();

        Session session = resolver.adaptTo(Session.class);
        TagManager tagMan = resolver.adaptTo(TagManager.class);
        QueryBuilder builder = resolver.adaptTo(QueryBuilder.class);

        List<String> list = new ArrayList<>();
        List<String> blackList = new ArrayList<>();

        try {


            String[] dataCollection = compProp.get(CollectionProps.ADVANCED_TAGS) instanceof String
                    ? convertToArray(compProp.get(CollectionProps.ADVANCED_TAGS).toString())
                    : (String[]) compProp.get(CollectionProps.ADVANCED_TAGS);

            String path;
            String[] tags;
            String[] rootPaths;
            boolean matchAny;
            boolean notIncluded;

            JSONObject data;

            RangeIterator<Resource> tagResultsIterator ;

            //iterating over json collection. e.g [{rootPaths:[],tags:[],match:[],not:[]},{[]},{[]},...,{[]}]
            for (int i=0; i < dataCollection.length; i++) {

                data = new JSONObject(dataCollection[i]);

                tags = getArray((JSONArray) data.get("tags")) ;
                rootPaths   = getArray((JSONArray) data.get("rootPaths"));
                matchAny    = data.get("match").equals("any")  ;
                notIncluded = ((JSONArray)data.get("not")).length() == 0 ? false:true ;

                //iterating over rootPaths inside the collection  e.g rootPaths:[]
                for (int j=0; j < rootPaths.length; j++ ) {

                    path = !rootPaths[j].isEmpty() ? rootPaths[j] : currentPage.getPath() ;
                    tagResultsIterator  = tagMan.find(path,tags,matchAny);
                    if(!notIncluded){
                        list.addAll(getPaths(tagResultsIterator));
                    } else {
                        blackList.addAll(getPaths(tagResultsIterator));
                    }
                }
            }

            list.removeAll(blackList);


        } catch (JSONException e) {
            contrBean.getPwcLogger().logMessage(new Date()+" AdvTags getResults error :"+e);
        }

        if(list.isEmpty()) return null;

        StringBuilder  queryString = getQueryForSearch(list.toArray(new String[0]),contrBean);

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

    private List<String> getPaths(RangeIterator<Resource> tagResultsIterator) {

        List<String> paths = new ArrayList<String>();
        while (tagResultsIterator.hasNext()) {
            paths.add(tagResultsIterator.next().getParent().getPath().replace("/jcr:content",""));
        }
        return paths;
    }

    private String[] getArray(JSONArray jsonArray) throws JSONException{

        String[] array = new String[jsonArray.length()];
        for (int i =0; i < array.length; i++) {
            array[i] = jsonArray.getString(i);
        }

        return array;
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
