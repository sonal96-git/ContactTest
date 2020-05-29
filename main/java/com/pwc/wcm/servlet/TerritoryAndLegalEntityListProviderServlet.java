package com.pwc.wcm.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.Contact;
import com.pwc.wcm.services.CountryTerritoryMapperService;

/**
 * Servlet to provide list of the territories and legal entities for contact profile.
 */
@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC Territory and Legal Entites List Provider Servlet",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/territoriesAndlegalentities"
})
public class TerritoryAndLegalEntityListProviderServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 3959444168490742341L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TerritoryAndLegalEntityListProviderServlet.class);
    private static final String CODE_JSON_KEY = "code";
    private static final String NAME_JSON_KEY = "name";
    private static final String LEGAL_ENTITY_IS_DEACTIVATED_PROPERTY = "isDeactivated";
    private static final String GLOBAL_TERRITORY_CODE = "gx";
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/JSON");
        response.setCharacterEncoding("UTF-8");
        JSONArray territoriesAndLegalEntitiesArray = getFilteredTerritoryList(countryTerritoryMapperService.getTerritorySelectorData());
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource legalEntitesResource = resourceResolver.getResource(Contact.LEGAL_ENTITY_BASE_PATH);
        addLegalEntitiesToTerritoryData(legalEntitesResource, territoriesAndLegalEntitiesArray);
        LOGGER.debug("TerritoryAndLegalEntityListProviderServlet : doGet() : Sending response {}", territoriesAndLegalEntitiesArray);
        response.getWriter().write(territoriesAndLegalEntitiesArray.toString());
    }
    
    /**
     * Adds the legal Entities to the territory data. A legal entity is included in the list if and only if it is not deactivated.
     * 
     * @param legalEntitesResource {@link Resource} under which the legal entities are present
     * @param territories {@link JSONArray} the list where the legal entities are to be added
     */
    private void addLegalEntitiesToTerritoryData(Resource legalEntitesResource, JSONArray territories) {
        if (legalEntitesResource != null && territories != null) {
            JSONObject legalEntityJson = null;
            for (Resource legalEntity : legalEntitesResource.getChildren()) {
                if(!legalEntity.getValueMap().get(LEGAL_ENTITY_IS_DEACTIVATED_PROPERTY, false)) {
                    legalEntityJson = new JSONObject();
                    try {
                        legalEntityJson.put(NAME_JSON_KEY, legalEntity.getValueMap().get(Contact.LEGAL_ENTITY_TITLE_PROPERTY, legalEntity.getName()));
                        legalEntityJson.put(CODE_JSON_KEY, legalEntity.getName());
                        territories.put(legalEntityJson);
                    } catch (JSONException jsonException) {
                        LOGGER.error(
                                "TerritoryAndLegalEntityListProviderServlet : addLegalEntitiesToTerritoryData() : JSONException occured while creating json object for legal entity for path {} : {}",
                                legalEntity.getPath(), jsonException);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the new filtered {@link JSONArray} of territories from given {@link JSONArray}.
     * 
     * @param territories {@link JSONArray} which is to be filtered
     * @return {@link JSONArray}
     */
    private JSONArray getFilteredTerritoryList(JSONArray territories) {
        JSONArray filteredTerritories = null;
        if (territories != null) {
            filteredTerritories = new JSONArray();
            for (int index = 0; index < territories.length(); index++) {
                try {
                    JSONObject territory = territories.getJSONObject(index);
                    if (territory.has(CODE_JSON_KEY) && !territory.getString(CODE_JSON_KEY).equals(GLOBAL_TERRITORY_CODE)) {
                        filteredTerritories.put(territory);
                    }
                    
                } catch (JSONException jsonException) {
                    LOGGER.error(
                            "TerritoryAndLegalEntityListProviderServlet : getFilteredTerritoryList() : JSONException occured while filtering territory json array {} : {}",
                            territories, jsonException);
                }
            }
        }
        return filteredTerritories;
    }
    
}
