package com.pwc.wcm.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pwc.wcm.services.model.ListCacheItem;

/**
 * Created with IntelliJ IDEA.
 * User: ken.mitsumoto
 * Date: 2/13/14
 * Time: 12:34 PM
 */
public interface ListComponentCacheService {

    public boolean hasCached(String key);

    public boolean existsInCache(String key, String path);

    public List<ListCacheItem> getCollection(String key, String order);

    public void cache(String key, List<ListCacheItem> list);

    public void clear(String key);

    public void addToCache(String key, ListCacheItem item);

    public void removeFromCache(String key, ListCacheItem item);

    public void updateInCache(String key, ListCacheItem item);

    public Map<String, Set<String>> getParentPagePaths();

    public void setParentPagePaths(Map<String, Set<String>> parentPagePaths);

    public void primeParentPathCache();

    public void flushParentPathCache();

    public void primeListCache(String key);

    public void flushCache(String key, boolean prime);

    public void refresh();

    public void addToCache(String itemPath, Set<String> listControlPaths);

    public void addToCache(String itemPath, String listControlPath);

    public void removeFromCache(String path, Set<String> listControlPaths);

    public void removeFromCache(String path, String listControlPath);

}
