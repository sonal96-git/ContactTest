package com.pwc.wcm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
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

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.pwc.model.Contact;
import com.pwc.model.DropDownEntity;
import com.pwc.wcm.services.CountryTerritoryMapperService;

/**
 * Servlet to provide list of the territories and legal entities for contact profile.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Territories dropdown List Provider Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.resourceTypes=" + "pwc/components/content/datasource/bin/territory"
		})
public class TerritoryDropdownResponseServlet extends SlingSafeMethodsServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TerritoryDropdownResponseServlet.class);

	private static final String GLOBAL_TERRITORY_CODE = "gx";
	private static final String CODE_JSON_KEY = "code";
	private static final String NAME_JSON_KEY = "name";
	private static final String LEGAL_ENTITY_IS_DEACTIVATED_PROPERTY = "isDeactivated";

	@Reference
	private CountryTerritoryMapperService countryTerritoryMapperService;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		try {
			JSONArray territoriesAndLegalEntitiesArray = countryTerritoryMapperService.getTerritorySelectorData();
			ResourceResolver resolver = request.getResourceResolver();
			Resource legalEntitesResource = resolver.getResource(Contact.LEGAL_ENTITY_BASE_PATH);
	        addLegalEntitiesToTerritoryData(legalEntitesResource, territoriesAndLegalEntitiesArray);
			
			final List<DropDownEntity> territories = GetListForDropdown(territoriesAndLegalEntitiesArray, resolver);
			request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
			ValueMap valueMap = null;
			List<Resource> territoryArray = new ArrayList<Resource>();

			for (DropDownEntity country : territories) {
				valueMap = new ValueMapDecorator(new HashMap<String, Object>());
				valueMap.put("value", country.getValue());
				valueMap.put("text", country.getText());
				territoryArray.add(new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
			}
			DataSource dataSource = new SimpleDataSource(territoryArray.iterator());
			LOGGER.debug("TerritoryAndLegalEntityListProviderServlet : doGet() : Sending response {}", territoriesAndLegalEntitiesArray);
			request.setAttribute(DataSource.class.getName(), dataSource);;

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}


	private List<DropDownEntity> GetListForDropdown(JSONArray territoriesArray, ResourceResolver resolver) {
		final List<DropDownEntity> territories = new ArrayList<DropDownEntity>();
		for(int index =0; index < territoriesArray.length(); index++) {
			try {
				JSONObject territory = territoriesArray.getJSONObject(index);
				if (territory.has(CODE_JSON_KEY) && !territory.getString(CODE_JSON_KEY).equals(GLOBAL_TERRITORY_CODE)) {
					DropDownEntity territoryObj = new DropDownEntity(territory.getString(NAME_JSON_KEY), territory.getString(NAME_JSON_KEY), territory.getString(CODE_JSON_KEY));
					territories.add(territoryObj);
				}
			} catch (Exception e) {
				LOGGER.error(
						"TerritoryDropdownResponseServlet : GetValueMapForDropdown() : JSONException occured while creating java object for territories Array {} : {}", e);
			}
		}
		return territories;
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
}
