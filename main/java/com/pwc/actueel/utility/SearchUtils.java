package com.pwc.actueel.utility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

/**
 * Utility used for querying Pages for RSS Feed.
 */
public class SearchUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchUtils.class);
    
    /**
     * Return a list of pages present under given rootPath list which aren't expired.
     *
     * @param resolver {@link ResourceResolver}
     * @param rootPaths {@link List} Pages under all these rootPaths are selected
     * @return {@link List} List of Pages under given rootPaths which aren't expired
     */
    public static List<Hit> getPageResults(final ResourceResolver resolver, final List<String> rootPaths) {
        final Map<String, String> map = IntStream.range(0, rootPaths.size()).boxed()
                .collect(Collectors.toMap(i -> "1_group." + ++i + "_path", rootPaths::get));
        map.put("1_group.p.or", "true");
        map.put("type", "cq:Page");
        map.put("p.offset", "0");
        map.put("p.limit", "-1");
        map.put("p.guessTotal", "true");
        addDateRangeConditionsForPropertyToMap(map, "offTime", 2, "lower", ">");
        addDateRangeConditionsForPropertyToMap(map, "onTime", 3, "upper", "<=");
        final Session session = resolver.adaptTo(Session.class);
        final QueryBuilder builder = resolver.adaptTo(QueryBuilder.class);
        final Query query = builder.createQuery(PredicateGroup.create(map), session);
        LOGGER.debug("Querying for conditions: " + query.getPredicates());
        final SearchResult result = query.getResult();
        return result.getHits();
    }

    /**
     * Adds the conditions for checking date ranges to the given Map.
     *
     * @param map {@link Map} The map these conditions are added to
     * @param property {@link String} The property name for which the date range is checked
     * @param groupNo The group all these conditions should belong to
     * @param rangeCondition {@link String} The condition can be 'Upper' or 'Lower' depending on which range is to be
     *            checked for
     * @param operation {@link String} Operation that is to be performed, like '>=', '<='
     */
    private static void addDateRangeConditionsForPropertyToMap(final Map<String, String> map, final String property,
            final int groupNo, final String rangeCondition, final String operation) {
        map.put(groupNo + "_group.p.or", "true");
        map.put(groupNo + "_group.1_property", "@jcr:content/" + property);
        map.put(groupNo + "_group.1_property.operation", "not");
        map.put(groupNo + "_group.2_daterange.property", "@jcr:content/" + property);
        map.put(groupNo + "_group.2_daterange." + rangeCondition + "Bound", LocalDateTime.now().toString());
        map.put(groupNo + "_group.2_daterange." + rangeCondition + "Operation", operation);
    }
}
