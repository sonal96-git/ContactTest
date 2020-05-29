package com.pwc.wcm.services;

import java.util.Map;

import javax.jcr.Session;

import org.json.JSONArray;

import com.pwc.model.Territory;

/**
 * The Interface CountryTerritoryMapperService Provides Country and territory {@link Map} extracted from the data in territory reference
 * nodes.
 */
public interface CountryTerritoryMapperService {

    /**
     * Gets the {@link Territory} for the given country code.
     *
     * @param countryCode {@link String} Code of the country
     * @return {@link Territory} returns null if no territory exists for the country code
     */
    public Territory getTerritoryByCountry(String countryCode);

    /**
     * Gets the country code to {@link Territory} {@link Map}.
     *
     * @return {@link Map}
     */
    public Map<String, Territory> getCountryToTerritoryMap();

    /**
     * Gets the country code to {@link Territory} {@link Map} JSON String.
     *
     * @return {@link String}
     */
    public String getCountryToTerritoryMapJson();

    /**
     * Gets the {@link Territory} for the given territory code.
     *
     * @param {@link String} Code of the territory
     * @return {@link Territory} returns null if no territory exists for the territory code
     */
    public Territory getTerritoryByTerritoryCode(String territoryCode);

    /**
     * Gets the territory code to {@link Territory} {@link Map}.
     *
     * @return {@link Map}
     */
    public Map<String, Territory> getTerritoryCodeToTerritoryMap();

    /**
     * Gets the territory code to {@link Territory} {@link Map} JSON String.
     *
     * @return {@link String}
     */
    public String getTerritoryCodeToTerritoryMapJson();

    /**
     * Gets the {@link Territory} for the given country code. If no territory is present for the given country, the {@link Territory} for
     * the given default territory code will be returned and if no territory exists for the default territory code, null is returned.
     *
     * @param countryCode {@link String} Code of the country
     * @param defaultTerritoryCode {@link String} Code of the territory
     * @return {@link Territory}
     */
    public Territory getTerritoryByCountry(String countryCode, String defaultTerritoryCode);

    /**
     * Gets the {@link Territory} for the given territory code. If no territory is present for the given territory, the {@link Territory}
     * for the given default territory code will be returned and if no territory exists for the default territory code, null is returned.
     *
     * @param territoryCode {@link String} Code of the territory
     * @param defaultTerritoryCode {@link String} Code of the territory
     * @return {@link Territory}
     */
    public Territory getTerritoryByTerritoryCode(String territoryCode, String defaultTerritoryCode);

    /**
     * Gets the data for territory selector
     *
     * @return territory selector data as JSONArray
     */
    public JSONArray getTerritorySelectorData();
	
	/**
	 * Gets the data for Statergy& territory selector
	 *
	 * @return territory selector data as JSONArray
	 */
	public JSONArray getStatergyTerritorySelectorData();
    
    /**
     * Gets the access control groups for territory by territory code.
     *
     * @return access control group as JSONArray
     */
    public JSONArray getACGByTerritoryCode(String territoryCode,Session session);
}
