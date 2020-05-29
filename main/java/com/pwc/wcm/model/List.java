package com.pwc.wcm.model;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.RangeIterator;
import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;
import com.day.text.Text;
import com.pwc.wcm.services.ListComponentCacheService;
import com.pwc.wcm.services.model.ListCacheItem;

/**
 * 
 * This class was copied from CQ OOTB. /libs/foundation/src/impl/src/main/java/com/day/cq/wcm/foundation/List.java
 * 
 * Modified for PWC List Pages Component.
 * 
 * Needs cleanup.
 */
public class List<T extends Adaptable> {

    public static final Logger log = LoggerFactory.getLogger(List.class);

    public static final String URL_EXTENSION = ".html";

    public static final String TYPE_PROPERTY_NAME = "listType";

    /**
     * Used to create a list from child listItems.
     */
    public static final String SOURCE_CHILDREN = "children";

    /**
     * Used to create a list from descendant listItems.
     */
    public static final String SOURCE_DESCENDANTS = "descendants";

    /**
     * Used to create a list from a fixed selection of listItems.
     */
    public static final String SOURCE_STATIC = "static";

    /**
     * Used for default rendering of the list items.
     */
    public static final String TYPE_DEFAULT = "default";

    public static final String ANCESTOR_PAGE_PROPERTY_NAME = "ancestorPage";
    
    public static final String SECONDARY_ANCESTOR_PAGE_PROPERTY_NAME = "secondaryAncestorPage";

    public static final String TAGS_SEARCH_ROOT_PROPERTY_NAME = "tagsSearchRoot";

    public static final String TAGS_PROPERTY_NAME = "tags";
    
    public static final String TAGS_MATCH_PROPERTY_NAME                   = "tagsMatch";

    public static final String SEARCH_IN_PROPERTY_NAME                    = "searchIn";

    public static final String LIMIT_PROPERTY_NAME                        = "limit";

    public static final String PAGE_MAX_PROPERTY_NAME                     = "pageMax";

    public static final String ORDER_BY_PROPERTY_NAME                     = "orderBy";

    public static final String ORDERED_PROPERTY_NAME                      = "ordered";

    public static final String LIST_START_PARAM_NAME                      = "start";

    public static final String LIST_MAX_PARAM_NAME                        = "max";

    public static final String FILTER_ONE_TAGS_PROPERTY                   = "filterOneTags";

    public static final String FILTER_ONE_TAGS_USE_DESCENDANTS_PROPERTY   = "filterOneTagsUseDescendants";

    public static final String FILTER_TWO_TAGS_PROPERTY                   = "filterTwoTags";

    public static final String FILTER_TWO_TAGS_USE_DESCENDANTS_PROPERTY   = "filterTwoTagsUseDescendants";

    public static final String FILTER_THREE_TAGS_PROPERTY                 = "filterThreeTags";

    public static final String FILTER_THREE_TAGS_USE_DESCENDANTS_PROPERTY = "filterThreeTagsUseDescendants";

    public static final String ORDER_BY_PROPERTY                          = "orderBy";


    public static final String NAME_MAP_KEY                               = "name";

    public static final String TAGS_MAP_KEY                               = "tags";


    public static final String DAM_ASSET_TYPE                             = "dam:Asset";

    public static final String NT_UNSTRUCTURED_TYPE                       = "nt:unstructured";

    public static final String DAM_CONTENT_PATH_PREFIX                    = "/content/dam/";

    public static final String DAM_TITLE_KEY                              = "dc:title";

    public static final String DAM_MODIFY_DATE_KEY                        = "xmp:ModifyDate";

    public static final String DAM_CREATE_DATE_KEY                        = "xmp:CreateDate";

    public static final String DAM_PUBLISH_DATE_KEY                       = "dc:publishDate";

    public static final String DAM_EXTERNAL_URL_KEY                       = "dc:externalUrl";

    public static final String ARTICLE_TYPES_NAME                         = "article-types";




    private boolean                            inited;

    private boolean                            ordered;


    private int                                limit = -1;

    private int                                listMaximum = -1;

    private int                                listStart = 0;


    private String                             id;

    private String                             source;

    private String                             startIn;

    private String                             type;

    private String                             orderBy;


    private Class                              typeParameterClass;

    private Resource                           resource;

    private ValueMap                           properties;

    private PageFilter                         pageFilter;

    private SlingHttpServletRequest            request;

    private TagManager                         tagManager;

    private java.util.List<Resource>           resources;

    private java.util.List<ListCacheItem>      listItems;

    private Iterator<Node>                     nodeIterator;

    private Comparator<T>                      orderComparator;

    private java.util.List<String>             filterOneTags;

    private java.util.List<String>             filterTwoTags;

    private java.util.List<String>             filterThreeTags;

    private java.util.List<String>             filterTags;

    private ArrayList<HashMap<String, Object>> filterByCriteria = new ArrayList<HashMap<String, Object>>();

    /**
     * Creates a <code>List</code> instance based on the specified request.
     * @param request The request
     */
    public List(SlingHttpServletRequest request, Class<T> typeParameterClass) {
        this(request, typeParameterClass, null);
    }

    /**
     * Creates a <code>List</code> instance based on the specified request.
     *
     * @param request The request
     * @param pageFilter Page filter
     */
    public List(SlingHttpServletRequest request, Class<T> typeParameterClass, PageFilter pageFilter) {
        this.request = request;
        this.typeParameterClass = typeParameterClass;
        this.pageFilter = pageFilter;
        resource = request.getResource();
        generateId();
    }

    private void initConfig() {
        // get config from content
        properties = ResourceUtil.getValueMap(resource);

        filterOneTags   = convertToArrayList(properties.get(FILTER_ONE_TAGS_PROPERTY, new String[]{}));
        filterTwoTags   = convertToArrayList(properties.get(FILTER_TWO_TAGS_PROPERTY, new String[]{}));
        filterThreeTags = convertToArrayList(properties.get(FILTER_THREE_TAGS_PROPERTY, new String[]{}));

        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        java.util.List<String> selectors = Arrays.asList(requestPathInfo.getSelectors());

        if(!containsSelector(selectors, FILTER_ONE_TAGS_PROPERTY)   &&
           !containsSelector(selectors, FILTER_TWO_TAGS_PROPERTY)   &&
           !containsSelector(selectors, FILTER_THREE_TAGS_PROPERTY))
        {
            source = SOURCE_DESCENDANTS;
        } else {
            source = "";
            filterTags = new ArrayList<String>();

            if(containsSelector(selectors, FILTER_ONE_TAGS_PROPERTY)) {
                filterTags.add(urlDecode(getSelectorValue(selectors, FILTER_ONE_TAGS_PROPERTY)).replace("@@", "/"));
            }
            if(containsSelector(selectors, FILTER_TWO_TAGS_PROPERTY)) {
                filterTags.add(urlDecode(getSelectorValue(selectors, FILTER_TWO_TAGS_PROPERTY)).replace("@@", "/"));
            }
            if(containsSelector(selectors, FILTER_THREE_TAGS_PROPERTY)) {
                filterTags.add(urlDecode(getSelectorValue(selectors, FILTER_THREE_TAGS_PROPERTY)).replace("@@", "/"));
            }
        }

        if(containsSelector(selectors, ORDER_BY_PROPERTY)) {
            orderBy = getSelectorValue(selectors, ORDER_BY_PROPERTY);
        } else if(!properties.get(ORDER_BY_PROPERTY, "").isEmpty()) {
            orderBy = properties.get(ORDER_BY_PROPERTY).toString();
        } else {
            orderBy = ListCacheItem.ORDER_BY_TITLE;
        }

        //orderComparator = orderBy.equals(ListCacheItem.ORDER_BY_DATE) ? createComparator(orderBy, true) : createComparator(orderBy);

        if (startIn == null) {
            startIn = properties.get(SEARCH_IN_PROPERTY_NAME, Text.getAbsoluteParent(resource.getPath(), 1));
        }
        if (type == null) {
            type = properties.get(TYPE_PROPERTY_NAME, TYPE_DEFAULT);
        }
        if (orderBy == null) {
            orderBy = properties.get(ORDER_BY_PROPERTY_NAME, null);
        }
        if (limit < 0) {
            limit = properties.get(LIMIT_PROPERTY_NAME, 500);
        }
        if (listMaximum < 0) {
            listMaximum = properties.get(PAGE_MAX_PROPERTY_NAME, -1);
        }
        ordered = properties.get(ORDERED_PROPERTY_NAME, ordered);

        // get config from request parameters
        if (containsSelector(selectors,prefixWithId(LIST_START_PARAM_NAME))) {
            try {
                listStart = Integer.parseInt(getSelectorValue(selectors, prefixWithId(LIST_START_PARAM_NAME)));
            } catch (Throwable t) {
                // ignore
            }
        }
        if (containsSelector(selectors, prefixWithId(LIST_MAX_PARAM_NAME))) {
            try {
                listMaximum = Integer.parseInt(getSelectorValue(selectors, prefixWithId(LIST_MAX_PARAM_NAME)));
            } catch (Throwable t) {
                // ignore
            }
        }

        filterByCriteria = new ArrayList<HashMap<String, Object>>();

        // populate filter tags for drop downs

        if (filterOneTags != null && filterOneTags.size() > 0) {
            addToFilterByCriteria(FILTER_ONE_TAGS_PROPERTY, filterOneTags, properties.get(FILTER_ONE_TAGS_USE_DESCENDANTS_PROPERTY, false).equals(true));
        }
        if (filterTwoTags != null && filterTwoTags.size() > 0) {
            addToFilterByCriteria(FILTER_TWO_TAGS_PROPERTY, filterTwoTags, properties.get(FILTER_TWO_TAGS_USE_DESCENDANTS_PROPERTY, false).equals(true));
        }
        if (filterThreeTags != null && filterThreeTags.size() > 0) {
            addToFilterByCriteria(FILTER_THREE_TAGS_PROPERTY, filterThreeTags, properties.get(FILTER_THREE_TAGS_USE_DESCENDANTS_PROPERTY, false).equals(true));
        }

        tagManager = request.getResourceResolver().adaptTo(TagManager.class);

    }

    @SuppressWarnings("unchecked")
    private boolean init() {
        if (!inited) {
            initConfig();

            // check the cache for the collection.
            java.util.List<ListCacheItem> collection = getListComponentCacheService().getCollection(resource.getPath(), orderBy);

            if(collection == null) {
                // prime the cache
                getListComponentCacheService().primeListCache(resource.getPath());
                // load the collection
                collection = getListComponentCacheService().getCollection(resource.getPath(), orderBy);
            }

            if(collection == null || collection.isEmpty()) { return true; }

            listItems = new ArrayList<ListCacheItem>();


            if (!SOURCE_CHILDREN.equals(source) && !SOURCE_DESCENDANTS.equals(source)){  // filter the cached results
                String parentPath = properties.get(ANCESTOR_PAGE_PROPERTY_NAME, resource.getPath());
                String secondaryPath = properties.get(SECONDARY_ANCESTOR_PAGE_PROPERTY_NAME, resource.getPath());

                Iterator<String> filterIterator = getFilterIterator(parentPath, secondaryPath);
                Set<String> filterSet = new HashSet<String>();
                if(filterIterator != EmptyIterator.INSTANCE) {
                    while(filterIterator.hasNext()) {
                        filterSet.add(filterIterator.next());
                    }
                    if(!filterSet.isEmpty()) {
                        Iterator<ListCacheItem> cacheItemIterator = collection.iterator();
                        while(cacheItemIterator.hasNext()){
                            ListCacheItem item = (ListCacheItem)cacheItemIterator.next();
                            if(filterSet.contains(item.getPath())) {
                                listItems.add(item);
                            }
                        }
                    }
                }
            } else {
                listItems = collection;
            }

            // apply limit
            if (listItems.size() > limit) {
                listItems = listItems.subList(0, limit);
            }
            int remainder = listItems.size()%listMaximum;
            listStart = Math.min(listStart, listItems.size() - (remainder == 0 ? listMaximum : remainder));

            inited = true;

        }
        return true;
    }

   private Iterator<String> getFilterIterator(String parentPath, String secondaryPath) {

       Iterator<String> filterIterator = EmptyIterator.INSTANCE;

       Resource resource = request.getResourceResolver().getResource(parentPath);
       Resource secondaryResource = request.getResourceResolver().getResource(secondaryPath);

       PageManager pm = request.getResourceResolver().adaptTo(PageManager.class);

       String[] tags = filterTags.toArray(new String[0]);
       boolean matchAny = properties.get(TAGS_MATCH_PROPERTY_NAME, "all").equals("any");

        if(typeParameterClass == Page.class) {
            resource = pm.getContainingPage(parentPath).adaptTo(Resource.class);
        }

       if (resource != null && tags.length > 0) {
           TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
           RangeIterator<Resource> results = tagManager.find(resource.getPath(), tags, matchAny);

           Set<String> paths = new HashSet<String>();
           while (results.hasNext()) {
               Resource r = results.next();
               if(typeParameterClass == Page.class) {
                   Page page = pm.getContainingPage(r);
                   if (page != null && (pageFilter == null || pageFilter.includes(page))) {
                       paths.add(page.getPath());
                   }
               } else {
                   try {
                       if(!r.getResourceType().equals(DAM_ASSET_TYPE)) {
                           // TagManager find matches in DAM asset metadata, i.e. two nodes down the tree
                           r = r.getParent().getParent();
                       }
                   } catch (Exception e) {
                       log.error("Error getting to asset", e);
                       return null;
                   }

                   paths.add(r.getPath());
               }
           }
           if(secondaryResource != null) {
               results = tagManager.find(secondaryResource.getPath(), tags, matchAny);

               while (results.hasNext()) {
                   Resource r = results.next();
                   paths.add(r.getParent().getParent().getPath());
               }
           }
           filterIterator = paths.iterator();
       }
       return filterIterator;
   }

    private void generateId() {
        String path = resource.getPath();
        String rootMarker = "jcr:content/";
        int root = path.indexOf(rootMarker);
        if (root >= 0) {
            path = path.substring(root + rootMarker.length());
        }
        id = path.replace('/', '_');
    }

    private String getParameter(String name) {
        return request.getParameter(id + "_" + name);
    }

    /**
     * Returns the list items as listItems, respecting both starting index and
     * maximum number of list items if specified.
     * @return The listItems
     */
    public Iterator<ListCacheItem> getListItems() {
        if (init() && listItems.size() > 0) {
            ArrayList<ListCacheItem> plist = new ArrayList<ListCacheItem>();
            int c = 0;
            for (int i = 0; i < listItems.size(); i++) {
                if (i < listStart) {
                    continue;
                }
                plist.add(listItems.get(i));
                c++;
                if (listMaximum > 0 && c == listMaximum) {
                    break;
                }
            }
            return plist.iterator();
        } else {
            return null;
        }
    }

    public Iterator<Node> getNodes() {
        return init() ? nodeIterator : null;
    }

    public String getCacheKey() {
        return resource.getPath();
    }

    /**
     * States whether the list is ordered.
     * @return <code>true</code> if list is ordered, <code>false</code> otherwise
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * States whether the list is empty.
     * @return <code>true</code> if list is empty, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return !init() || listItems == null || listItems.isEmpty();
    }

    /**
     * Returns the number of list items.
     * @return The size of the list
     */
    public int size() {
        return init() ? listItems.size() : 0;
    }

    /**
     * States whether the list is paginating, i.e. has a starting index
     * and/or a maximum numbe rof list items per page defined.
     * @return <code>true</code> if list is paginating, <code>false</code> otherwise
     */
    public boolean isPaginating() {
        return listStart > 0 || (listMaximum > 0 && size() > listMaximum);
    }

    /**
     * Returns the starting index for list items on this page.
     * Used for pagination.
     * @return The start index of
     */
    public int getListStart() {
        return listStart;
    }

    /**
     * Returns the maximum of list items to return per page.
     * Used for pagination.
     * @return The maximum per page
     */
    public int getListMaximum() {
        return listMaximum;
    }

    /**
     * Returns the link to the page with the next set of list items.
     * Used for pagination.
     * @return The link to the next page
     */
    public String getNextPageLink() {
        if (isPaginating() && listMaximum > 0) {
            if (listStart + listMaximum < size()) {
                int start = listStart + listMaximum;
                PageLink link = new PageLink(request);
                link.setParameter(LIST_START_PARAM_NAME, start);
                return link.toString();
            }
        }
        return null;
    }

    /**
     * Returns the link to the page with the previous set of list items.
     * Used for pagination.
     * @return The link to the previous page
     */
    public String getPreviousPageLink() {
        if (isPaginating()) {
            if (listStart > 0) {
                int start = listMaximum > 0 && listStart > listMaximum ?
                        listStart - listMaximum : 0;
                PageLink link = new PageLink(request);
                link.setParameter(LIST_START_PARAM_NAME, start);
                return link.toString();
            }
        }
        return null;
    }

    public String getCurrentItemsString() {
        if (isPaginating()) {
            return (listStart + 1) + "-" + Math.min((listStart + listMaximum), size());
        }
        return null;
    }

    public java.util.List<String> getPageLinks() {
        java.util.List<String> pageLinks = new java.util.ArrayList<String>();
        if(isPaginating()) {
            PageLink pageLink = new PageLink(request);
            int numPages = (int)Math.ceil(size() / (double)listMaximum);
            for(int i = 0; i < numPages; i++) {
                pageLink.setParameter(LIST_START_PARAM_NAME, i * listMaximum);
                pageLinks.add(pageLink.toString());
            }
        }
        return pageLinks;
    }

    public boolean isCurrentPaginationPage(int page) {
        return (page -1) * listMaximum == listStart;
    }

    public void setSource(String src) {
        source = src;
    }

    /**
     * Sets the the of the page to start searching on.
     * @param start The start page path
     */
    public void setStartIn(String start) {
        startIn = start;
    }

    /**
     * Returns the type of the list. This can be used to select the
     * script that renders the list items.
     * @return The list type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the list.
     * This is an optional setter, the default value will be
     * taken from the property defined by {@link #TYPE_PROPERTY_NAME}.
     * @param t The list type
     */
    public void setType(String t) {
        type = t;
    }

    /**
     * Sets the comparator used to order the list with.
     * This is an optional setter and overrides #setOrderBy(String).
     * @param obc The comparator to order the list with
     */
    public void setOrderComparator(Comparator<T> obc) {
        orderComparator = obc;
    }

    /**
     * Sets the property to order the list by.
     * This is an optional setter, the default value will be
     * taken from the property defined by {@link #ORDER_BY_PROPERTY_NAME}.
     * @param ob The property to order the list by
     */
    public void setOrderBy(String ob) {
        orderBy = ob;
    }

    /**
     * Sets the list style (numbered or not).
     * This is an optional setter, the default value will be <code>false</code>
     * @param o <code>true</code> if list should be rendered with numbered items,
     *          <code>false</code> otherwise
     */
    public void setOrdered(boolean o) {
        ordered = o;
    }

    /**
     * Sets the limit for list items.
     * This is an optional setter, the default value will be
     * taken from the property defined by {@link #LIMIT_PROPERTY_NAME}.
     * @param l The limit
     */
    public void setLimit(int l) {
        limit = l;
    }

    /**
     * Sets the maximum list items to return. Enables pagination.
     * This is an optional setter, the default value will be
     * taken from the property defined by {@link #PAGE_MAX_PROPERTY_NAME}.
     * @param pm The maximum number of listItems per page
     */
    public void setListMaximum(int pm) {
        listMaximum = pm;
    }

    /**
     * Sets the starting index to be respected when returning
     * list items. Used when paginating.
     * @param psi The starting index (defaults to 0).
     */
    public void setListStart(int psi) {
        listStart = psi;
    }


//    /**
//     * Sets the page filterIterator to generate the list from.
//     * @param iter The filterIterator
//     */
//    public void setIterator(Iterator<T> iter) {
//        filterIterator = iter;
//        setSource(SOURCE_STATIC);
//    }

    private ListComponentCacheService getListComponentCacheService() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference factoryRef = bundleContext.getServiceReference(ListComponentCacheService.class.getName());
        return (ListComponentCacheService) bundleContext.getService(factoryRef);
    }


    /**
     * A helper class to create links with query parameters.
     *
     * Page here means pages of results
     */
    private class PageLink {

        private String path;
        private HashMap<String, Object> params;

        public PageLink(SlingHttpServletRequest request) {
            path = request.getPathInfo();

            // Get containing page from path and replace path with its path
            PageManager pm = request.getResourceResolver().adaptTo(PageManager.class);
            Page page = pm.getContainingPage(path);
            if (page != null) {
                path = page.getPath() + URL_EXTENSION;
            }

            initParams(request.getQueryString());
        }

        public void addParameter(String name, Object value) {
            name = prefixName(name);
            params.put(name, value);
        }

        public void setParameter(String name, Object value) {
            name = prefixName(name);
            if (params.containsKey(name)) {
                params.remove(name);
            }
            addParameter(name, value);
        }

        public String toString() {
            String url = path;
            for (String param : params.keySet()) {
                url = appendParam(url, param, params.get(param));
            }
            return url;
        }

        private String prefixName(String name) {
            if (!name.startsWith(id + "_")) {
                name = id + "_" + name;
            }
            return name;
        }

        private void initParams(String query) {
            params = new HashMap<String, Object>();
            String[] pairs = Text.explode(query, '&');
            for (String pair : pairs) {
                String[] param = Text.explode(pair, '=', true);
                params.put(param[0], param[1]);
            }
        }

        private String appendParam(String url, String name, Object value) {
            char delim = url.indexOf('#') > 0 ? '&' : '#';
            return new StringBuffer(url)
                    .append(delim)
                    .append(name)
                    .append('=')
                    .append(value).toString();
        }

    }

    public ArrayList<HashMap<String, Object>> getFilterCriteria() {
        if (!inited) {
        	initConfig();
        }
    	return filterByCriteria;
    }

    private void addToFilterByCriteria(String name, java.util.List<String> selectedTags, boolean useDescendants) {
        if(useDescendants) {
            addDescendantsToFilterCriteria(name, selectedTags);
        } else {
            addToFilterCriteria(name, selectedTags);
        }
    }

    private void addDescendantsToFilterCriteria(String name, java.util.List<String> selectedTags) {
        TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        for (int i = 0; i < selectedTags.size(); i++) {
            Tag t = tagManager.resolve(selectedTags.get(i));

            Iterator<Tag> tags = t.listAllSubTags();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(NAME_MAP_KEY, name);
            //map.put(TAGS_MAP_KEY, stripUnusedTags(convertIteratorToList(tags)));
            map.put(TAGS_MAP_KEY, convertIteratorToList(tags));
            filterByCriteria.add(map);
        }
    }

    private void addToFilterCriteria(String name, java.util.List<String> selectedTags) {
        TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        java.util.List<Tag> tags = new ArrayList<Tag>();
        for (int i = 0; i < selectedTags.size(); i++) {
            tags.add(tagManager.resolve(selectedTags.get(i)));
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(NAME_MAP_KEY, name);
        //map.put(TAGS_MAP_KEY, stripUnusedTags(tags));
        map.put(TAGS_MAP_KEY, tags);
        filterByCriteria.add(map);
    }

    private static Date parseDateString(String dateAsString){
        try {
            return new SimpleDateFormat("MM/dd/yy").parse(dateAsString);
        } catch(Exception e) {
            return new Date(Long.MIN_VALUE);
        }
    }

    private static String getDamAssetProperty(Asset asset, String property) {
        String value = "";
        if(asset == null) { return value; }
        if(asset.getMetadata(property) instanceof Object[]) {
            value = ((Object[])asset.getMetadata(property))[0].toString();
        } else {
            value = asset.getMetadataValue(property);
        }
        return value;
    }

	private ArrayList<Tag> convertIteratorToList(Iterator<Tag> tags) {
		ArrayList<Tag> returnList = new ArrayList<Tag>();
		
		if (tags != null && tags.hasNext()) {
			while (tags.hasNext()) {
				returnList.add(tags.next());
			}
		}
		return returnList;
	}

    private <T> java.util.List<T> convertToArrayList(T[] array){
        return new ArrayList<T>(Arrays.asList(array));
    }

    private String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "ASCII");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private boolean containsSelector(java.util.List<String> selectors, String selector){
        if(selectors == null) { return false; }
        for(String s : selectors) {
            if(s.startsWith(selector)) {
                return true;
            }
        }
        return false;
    }

    private String getSelectorValue(java.util.List<String> selectors, String selector) {
        if(selectors == null || selector == null || selector.isEmpty()) { return null; }
        for(String s : selectors) {
            if(s.startsWith(selector)) {
                return s.substring(s.indexOf('=') + 1);
            }
        }
        return null;
    }

    private String prefixWithId(String name) {
        return id + "_" + name;
    }
}
