package com.pwc.inject.url.service;

/**
 * It provides method to get any territory specific URL.
 *
 */
public interface TerritorySpecificUrlProvider {
    
    /**
     * Returns the territory specific URL for the given pagePath.
     * @param pagePath {@link String}
     * @return {@link String}
     */
    public String getTerritorySpecificUrl(String pagePath);
    
}
