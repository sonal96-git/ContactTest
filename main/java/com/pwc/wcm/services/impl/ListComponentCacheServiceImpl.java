package com.pwc.wcm.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Filter;
import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.services.ListComponentCacheService;
import com.pwc.wcm.services.model.ListCacheItem;

@Component(immediate = true, service = { ListComponentCacheService.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= List Component Cache Service",
        Constants.SERVICE_VENDOR + "= SolutionSet"
})
public class ListComponentCacheServiceImpl implements ListComponentCacheService {

    private static final Logger log = LoggerFactory.getLogger(ListComponentCacheService.class);

    private static final String CONTENT_ROOT_JCR_PATH            = "/content/pwc";

    private static final String DAM_JCR_ROOT_PREFIX              = "/content/dam";

    private static final String ANCESTOR_PAGE_PROPERTY           = "ancestorPage";

    private static final String SECONDARY_ANCESTOR_PAGE_PROPERTY = "secondaryAncestorPage";

    private static final String DAM_ASSET_TYPE                   = "dam:Asset";

    public static final String JCR_CONTENT_PATH_NAME             = "jcr:content";

    public static final String CQ_LAST_REPLICATION_ACTION        = "cq:lastReplicationAction";

    public static final String ACTIVATE                          = "Activate";


    private static final String FIND_LIST_COMPONENTS_SQL2        =
            "SELECT * FROM [nt:base] AS s  where (isdescendantnode (s, '%s') and s.[sling:resourceType] LIKE 'pwc/components/list%%')";

    private Map<String, Map<String, LinkedList<ListCacheItem>>> cache = new HashMap<String, Map<String, LinkedList<ListCacheItem>>>();

    private Map<String, Set<String>> parentPagePaths;

    @Reference
    private SlingRepository repository;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private AdminResourceResolver adminResourceResolver;
  

    @Override
	public boolean hasCached(String key) {
        return cache.containsKey(key);
    }

    @Override
	public List<ListCacheItem> getCollection(String key, String field) {
        return cache.containsKey(key) && cache.get(key) != null && cache.get(key).containsKey(field) ? cache.get(key).get(field) : null;
    }

    @Override
	public void cache(String key, List<ListCacheItem> list) {
        log.info("Storing collections for key " + key +"...");
        log.info("collection has " + list.size() + " elements.");
        if(parentPagePaths == null) {
            primeParentPathCache();
        }
        if(!cache.containsKey(key)) {
            LinkedList<ListCacheItem> orderByDateCopy = new LinkedList<ListCacheItem>();
            LinkedList<ListCacheItem> orderByTitleCopy = new LinkedList<ListCacheItem>();
            Iterator<ListCacheItem> it = list.iterator();
            while(it.hasNext()){
                ListCacheItem clone = it.next().clone();
                orderByDateCopy.add(clone);
                orderByTitleCopy.add(clone);
            }
            Collections.sort(orderByDateCopy,  new ListCacheItem.ListCacheItemComparator(ListCacheItem.ORDER_BY_DATE, true));
            Collections.sort(orderByTitleCopy, new ListCacheItem.ListCacheItemComparator(ListCacheItem.ORDER_BY_TITLE));
            Map map = new HashMap<String, LinkedList<ListCacheItem>>();
            map.put(ListCacheItem.ORDER_BY_DATE, orderByDateCopy);
            map.put(ListCacheItem.ORDER_BY_TITLE, orderByTitleCopy);

            log.info("collection " + ListCacheItem.ORDER_BY_DATE + " has " + orderByDateCopy.size() + " elements.");
            log.info("collection " + ListCacheItem.ORDER_BY_TITLE + " has " + orderByTitleCopy.size() + " elements.");
            synchronized(this) {
                cache.put(key, map);
            }
        }
        log.info("collection stored.");
    }

    @Override
	public void clear(String key) {
        if(cache.containsKey(key)) {
            synchronized(this) {
                cache.remove(key);
            }
        }
    }

    @Override
	public void addToCache(String key, ListCacheItem item) {
        if(!cache.containsKey(key)) {
            //primeListCache(key);
            return;
        }
        addToSortedListMap(cache.get(key), item, ListCacheItem.ORDER_BY_DATE);
        addToSortedListMap(cache.get(key), item, ListCacheItem.ORDER_BY_TITLE);
    }

    @Override
	public void removeFromCache(String key, ListCacheItem item) {
        if(!cache.containsKey(key)) { return; }
        if(cache.get(key).containsKey(ListCacheItem.ORDER_BY_DATE)) {
            removeFromSortedList(cache.get(key).get(ListCacheItem.ORDER_BY_DATE), item);
        }
        if(cache.get(key).containsKey(ListCacheItem.ORDER_BY_TITLE)) {
            removeFromSortedList(cache.get(key).get(ListCacheItem.ORDER_BY_TITLE), item);
        }
    }

    @Override
	public void updateInCache(String key, ListCacheItem item) {
        if(!cache.containsKey(key)) { return; }
        if(cache.get(key).containsKey(ListCacheItem.ORDER_BY_DATE)) {
            updateInSortedListMap(cache.get(key), item, ListCacheItem.ORDER_BY_DATE);
        }
        if(cache.get(key).containsKey(ListCacheItem.ORDER_BY_TITLE)) {
            updateInSortedListMap(cache.get(key), item, ListCacheItem.ORDER_BY_TITLE);
        }
    }

    @Override
	public Map<String, Set<String>> getParentPagePaths() {
        return parentPagePaths;
    }

    @Override
	public synchronized void setParentPagePaths(Map<String, Set<String>> parentPagePaths) {
        this.parentPagePaths = parentPagePaths;
    }

    @Override
	public void primeParentPathCache() {
        log.info("Priming parent path cache...");
        Session session = null;
        ResourceResolver adminResolver = null;

        try {
        	adminResolver = adminResourceResolver.getAdminResourceResolver();
            session = adminResolver.adaptTo(Session.class);

            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(String.format(FIND_LIST_COMPONENTS_SQL2, CONTENT_ROOT_JCR_PATH), Query.JCR_SQL2);
            QueryResult result = query.execute();
            RowIterator it = result.getRows();

            while (it.hasNext()) {
                Row node = it.nextRow();
                String componentPath = node.getPath();
                Resource listComponent = adminResolver.getResource(componentPath);
                if(listComponent == null) { continue; }
                ValueMap properties = listComponent.adaptTo(ValueMap.class);
                if(properties == null) { continue; }
                updateParentPagePath(properties.get(ANCESTOR_PAGE_PROPERTY, String.class), componentPath);
                updateParentPagePath(properties.get(SECONDARY_ANCESTOR_PAGE_PROPERTY, String.class), componentPath);
            }
        } catch (Exception e) {
            log.error("Failure loading parent page paths into cache!", e);
        } finally {
            if (session != null) session.logout();
        }
        log.info("Parent path cache primed. " + ((parentPagePaths == null) ? 0 : parentPagePaths.size()) +" entries.");
    }

    @Override
	public boolean existsInCache(String key, String path) {
        if(cache == null || cache.isEmpty()) { return false; }
        if(cache.get(key) == null || cache.get(key).isEmpty() ||
                cache.get(key).get(ListCacheItem.ORDER_BY_DATE) == null ||
                cache.get(key).get(ListCacheItem.ORDER_BY_DATE).isEmpty()) { return false; }

        Iterator <ListCacheItem> it = cache.get(key).get(ListCacheItem.ORDER_BY_DATE).iterator();
        while(it.hasNext()) {
            if(it.next().getPath().equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
	public void primeListCache(String key) {
        log.info("Priming list cache for key " + key + "...");
        
        Session session = null;
        ResourceResolver adminResolver = null;
        
        if(parentPagePaths == null) {
            primeParentPathCache();
        }

      
        try {
        	adminResolver = adminResourceResolver.getAdminResourceResolver();
            session = adminResolver.adaptTo(Session.class);
            Resource listComponent = adminResolver.getResource(key);
            ValueMap properties = listComponent.adaptTo(ValueMap.class);

            java.util.List<ListCacheItem> collection = new ArrayList<ListCacheItem>();

            String parentPath = (String)properties.get(ANCESTOR_PAGE_PROPERTY);
            String secondaryPath = (String)properties.get(SECONDARY_ANCESTOR_PAGE_PROPERTY);

            Iterator iterator = null;
            if(parentPath.startsWith(DAM_JCR_ROOT_PREFIX)) {
                log.info("list type is dam asset");
                log.info("parentPath: " + parentPath);
                log.info("secondaryPath: " + secondaryPath);
                Resource resource = adminResolver.getResource(parentPath);
                Resource secondaryResource = adminResolver.getResource(secondaryPath);
                if (parentPath != null && parentPath.startsWith(DAM_JCR_ROOT_PREFIX)) {
                    java.util.List<Asset> allChildren = new ArrayList<Asset>();
                    getAllDamAssets(resource, allChildren);
                    if (secondaryPath != null && secondaryPath.startsWith(DAM_JCR_ROOT_PREFIX)) {
                        getAllDamAssets(secondaryResource, allChildren);
                    }
                    log.info("total assets found: " + allChildren.size());
                    iterator = allChildren.iterator();
                } else {
                    iterator = EmptyIterator.INSTANCE;
                }
            } else {
                PageManager pm = getResourceResolver(session).adaptTo(PageManager.class);
                try {
                    Page startPage = pm.getContainingPage(parentPath);
                    if (startPage != null) {
                        Filter<Page> pageFilter = new PageFilter();
                        iterator = startPage.listChildren(pageFilter, true);
                    } else {
                        iterator = EmptyIterator.INSTANCE;
                    }
                } catch (Exception e) {
                    log.error("error creating page filterIterator", e);
                }
            }

            if(iterator == null || iterator == EmptyIterator.INSTANCE) {
                log.info("Iterator is empty!");
                return;
            }

            while(iterator.hasNext()) {
                ListCacheItem item = ListCacheItem.toListCacheItem((Adaptable) iterator.next(), getResourceResolver(session).adaptTo(TagManager.class));
                if(item.getPath() != null) {
                    collection.add(item);
                }
            }

            cache(key, collection);
        } catch (Exception e) {
            log.error("Failed to prime cache for list at " + key + "!", e);
        } finally {
            if(session != null) { session.logout(); }
        }
        log.info("Primed list cache.");
    }

    @Override
	public void flushCache(String cacheKey, boolean prime) {
        if(cache.containsKey(cacheKey)) {
            synchronized (this) {
                cache.remove(cacheKey);
            }
        }

        if(prime) {
            primeListCache(cacheKey);
        }
    }

    @Override
	public synchronized  void flushParentPathCache() {
        parentPagePaths.clear();
    }

    private synchronized void updateParentPagePath(String path, String componentPath) {
        if(parentPagePaths == null ) {
            parentPagePaths = new HashMap<String, Set<String>>();
        }
        if(path != null && !path.isEmpty()) {
            if(!parentPagePaths.containsKey(path)) {
                parentPagePaths.put(path, new HashSet<String>());
            }
            parentPagePaths.get(path).add(componentPath);
        }
    }

    @Override
	public synchronized void refresh() {
        log.info("Refreshing all cached items...");
        if(parentPagePaths != null) {
            log.info("purging parent path cache...");
            parentPagePaths.clear();
            log.info("parent path cache cleared");
        }
        if(cache != null) {
            log.info("purging list control cache...");
            cache.clear();
            log.info("list control cache cleared.");
        }
        log.info("priming parent path cache...");
        primeParentPathCache();
        log.info("parent path cache primed.");
        Set<String> cacheKeys = new HashSet<String>();
        Iterator<Set<String>> setIt = getParentPagePaths().values().iterator();
        while(setIt.hasNext()) {
            cacheKeys.addAll(setIt.next());
        }

        Iterator<String> cacheKeysIt = cacheKeys.iterator();
        while(cacheKeysIt.hasNext()) {
            String cacheKey = cacheKeysIt.next();
            log.info("priming list control cache for key '" + cacheKey + "'...");
            primeListCache(cacheKey);
            log.info("list cached for key '" + cacheKey + "'.");
        }
        log.info("Refresh complete!");
    }

    private <T> void getAllDamAssets(Resource resource, java.util.List<T> children) {
        if(resource == null) {
            return;
        }

        if(children == null) {
            children = new ArrayList<T>();
        }

        Iterator<Resource> childrenIter = resource.listChildren();

        if(childrenIter != null) {
        	while(childrenIter.hasNext()){
                Resource r = childrenIter.next();
                if(r.getResourceType().equals(DAM_ASSET_TYPE)) {
                    Resource content = r.getChild(JCR_CONTENT_PATH_NAME);
                    if(content == null) { continue; }
                    ValueMap map = content.adaptTo(ValueMap.class);
                    if(map == null) { continue; }
                    children.add((T)r);
                }
                getAllDamAssets(r, children);
            }
        }

    }

    @Override
	public void addToCache(String itemPath, Set<String> cacheKeys) {
        Iterator<String> it = cacheKeys.iterator();
        while(it.hasNext()) {
            addToCache(itemPath, it.next());
        }
    }

    @Override
	public void addToCache(String itemPath, String cacheKey) {
        Session session = null;
        ResourceResolver adminResolver = null;
        try {
            adminResolver = adminResourceResolver.getAdminResourceResolver();
            session = adminResolver.adaptTo(Session.class);
            Resource resource = adminResolver.getResource(itemPath);
            if( ResourceUtil.isNonExistingResource(resource)) {
                flushCache(cacheKey, false);
            } else {
                TagManager tagManager = getResourceResolver(session).adaptTo(TagManager.class);
                ListCacheItem item = null;
                if(itemPath.startsWith(DAM_JCR_ROOT_PREFIX)) {
                    Asset a = resource.adaptTo(Asset.class);
                    item = ListCacheItem.toListCacheItem(a, tagManager);
                } else {
                    Page p = resource.adaptTo(Page.class);
                    item = ListCacheItem.toListCacheItem(p, tagManager);
                }
                if(existsInCache(cacheKey, itemPath)) {
                    updateInCache(cacheKey, item);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update cache with item at " + itemPath + ".", e);
        } finally {
            if (session != null) 
            	session.logout();
        }
    }

    @Override
	public void removeFromCache(String path, Set<String> cacheKeys) {
        Iterator<String> it = cacheKeys.iterator();
        while(it.hasNext()) {
            String key = it.next();
            if(hasCached(key)) {
                removeFromCache(key, path);
            }
        }
    }

    @Override
	public void removeFromCache(String path, String cacheKey) {
        ResourceResolver adminresourceResolver = null;
        try {
           
            adminresourceResolver = adminResourceResolver.getAdminResourceResolver();
            ListCacheItem item = new ListCacheItem();
            item.setPath(path);
            if(hasCached(cacheKey)) {
                removeFromCache(cacheKey, item);
            }
        } catch (Exception e) {
            log.error("Failed to update cache with item at " + path + ".", e);
        } finally {
            if (adminresourceResolver != null) 
            	adminresourceResolver.close();
        }
    }

    private synchronized void addToSortedListMap(Map<String, LinkedList<ListCacheItem>> map, ListCacheItem item, String sortField) {
        LinkedList<ListCacheItem> list = null;
        if(!map.containsKey(sortField)) {
            list = new LinkedList<ListCacheItem>();
        } else {
            list = map.get(sortField);
        }

        if(list.size() == 0) {
            list.add(item);
            map.put(sortField, list);
            return;
        }

        Iterator<ListCacheItem> it = list.descendingIterator();
        int i = list.size() - 1;
        while(it.hasNext()) {
            ListCacheItem next = it.next();
            if(compare(next, item, sortField) <= 0) {
                list.add(i, item);
                return;
            }
            i--;
        }

    }

    private synchronized void removeFromSortedList(LinkedList<ListCacheItem> list, ListCacheItem item) {
        if(list == null || list.size() == 0) { return; }
        int i = findItemIndexByPath(item, list);
        if(i != -1) {
            list.remove(i);
        }
    }

    private int findItemIndexByPath(ListCacheItem item, LinkedList<ListCacheItem> list) {
        if(list == null) { return -1; }
        if(item == null) { return -1; }
        Iterator<ListCacheItem> it = list.iterator();
        int i = 0;
        while(it.hasNext()) {
            ListCacheItem compare = it.next();
            if(compare.getPath().equalsIgnoreCase(item.getPath())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void updateInSortedListMap(Map<String, LinkedList<ListCacheItem>> map, ListCacheItem item, String sortField) {
        if(!map.containsKey(sortField)) { return; }
        LinkedList<ListCacheItem> list = map.get(sortField);

        int i = 0;
        Iterator<ListCacheItem> it = list.iterator();
        while(it.hasNext()) {
            ListCacheItem next = it.next();
            if(next.getPath().equalsIgnoreCase(item.getPath())) {
                if((sortField.equals(ListCacheItem.ORDER_BY_TITLE) && !next.getTitle().equals(item.getTitle())) ||
                  (sortField.equals(ListCacheItem.ORDER_BY_DATE) && !next.getPublishDate().equals(item.getPublishDate()))){
                    removeFromSortedList(list, item);
                    addToSortedListMap(map, item, sortField);
                    return;
                } else {
                    list.set(i, item);
                    return;
                }
            }
            i++;
        }
    }

    private int compare(ListCacheItem left, ListCacheItem right, String field) {
        return field == ListCacheItem.ORDER_BY_DATE ? right.getPublishDate().compareTo(left.getPublishDate()) :
                left.getTitle().toLowerCase().compareTo(right.getTitle().toLowerCase());
    }

    private ResourceResolver getResourceResolver(Session session) {
        try {
            Map<String, Object> authInfo = new HashMap<String, Object>();
            authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
            return resourceResolverFactory.getResourceResolver(authInfo);
        } catch (Exception e) {
            log.error("Failed to get ResourceResolver.", e);
        }
        return null;
    }


}
