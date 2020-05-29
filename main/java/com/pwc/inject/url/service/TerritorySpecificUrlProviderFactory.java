package com.pwc.inject.url.service;

import com.pwc.inject.url.enums.TerritorySpecificUrlType;

/**
 * Factory Service which returns the reference of type {@link TerritorySpecificUrlProvider} for the given {@link TerritorySpecificUrlType}.
 */
public interface TerritorySpecificUrlProviderFactory {
    
    public static String TERRITORY_SPECIFIC_URL_PROVIDER_PROPERTY = "type";
    
    /**
     * Factory method to return the reference of type {@link TerritorySpecificUrlProvider} for the given {@link TerritorySpecificUrlType}.
     * 
     * @param territorySpecificUrlType {@link TerritorySpecificUrlType}
     * @return {@link TerritorySpecificUrlProvider}
     */
    public TerritorySpecificUrlProvider getTerritorySpecificServiceProvider(TerritorySpecificUrlType territorySpecificUrlType);
    
}
