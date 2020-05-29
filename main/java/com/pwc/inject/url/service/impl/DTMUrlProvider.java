package com.pwc.inject.url.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.inject.url.enums.TerritorySpecificUrlType;
import com.pwc.inject.url.service.TerritorySpecificUrlProvider;
import com.pwc.inject.url.service.TerritorySpecificUrlProviderFactory;
import com.pwc.model.Territory;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.CommonUtils;

@Component(immediate = true, service = { TerritorySpecificUrlProvider.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC DTM URL Provider Service",
        TerritorySpecificUrlProviderFactory.TERRITORY_SPECIFIC_URL_PROVIDER_PROPERTY + "=" + TerritorySpecificUrlType.Constants.DTM_VALUE       
})
@Designate(ocd = DTMUrlProvider.Config.class)
public class DTMUrlProvider implements TerritorySpecificUrlProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DTMUrlProvider.class);
    
    public static final String DEFAULT_DTM_URL = "default.dtm.url";
    public static final String DTM_URL_VALUE_NONE = "NONE";
    private String defaultDTMUrl;
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapper;
    
    @ObjectClassDefinition(name = "PwC DTM URL Provider Service ", description = "")
    @interface Config {
    	@AttributeDefinition(name = "Default DTM URL", 
    						description = "Default DTM URL to be used when territory specific DTM URL is provided in reference data",
    						type = AttributeType.STRING)
    	public String default_dtm_url();
    }
    
    @Activate
    @Modified
    protected final void activate(final DTMUrlProvider.Config properties) throws Exception {
        LOGGER.trace("DTMUrlProvider : Entered Activate/Modify");
        defaultDTMUrl = properties.default_dtm_url();
    }
    
    /**
     * Returns the DTM Script's URL as per the territory of the given pagePath. If the DTM script URL on the page's territory is empty or
     * not defined, it returns the default DTM script URL {@value #DEFAULT_DTM_URL} defined in the configuration.
     * 
     * @param pagePath {@link String}
     * @return {@link String}
     */
    @Override
    public String getTerritorySpecificUrl(String pagePath) {
        LOGGER.info("DTMUrlProvider.getUrl: Getting DTM URL for page path: {}", pagePath);
        String territoryCode = CommonUtils.getCurrentPageTerritory(pagePath);
        if (territoryCode == null)
            LOGGER.debug("DTMUrlProvider.getUrl: No territory code {} is present for page Path {}", territoryCode, pagePath);
        else {
            Territory territory = countryTerritoryMapper.getTerritoryByTerritoryCode(territoryCode);
            if (territory == null)
                LOGGER.debug("DTMUrlProvider.getUrl: No territory reference data found for territory code {} for page path {}",
                        territoryCode, pagePath);
            else {
                String dtmUrl = null;
                if (StringUtils.isBlank(territory.getDtmScriptUrl())) {
                    dtmUrl = defaultDTMUrl;
                    LOGGER.info("DTMUrlProvider.getUrl: dtmScriptUrl is null/empty, returning Default DTM Script URL : {} for page path {}",
                            dtmUrl, pagePath);
                } else {
                    if (territory.getDtmScriptUrl().equalsIgnoreCase(DTM_URL_VALUE_NONE)) {
                        dtmUrl = StringUtils.EMPTY;
                        LOGGER.info(
                                "DTMUrlProvider.getUrl: Returning DTM Script URL blank as value of DTM script URL found for territory is {} for page path {}",
                                territory.getDtmScriptUrl(), pagePath);
                    } else {
                        dtmUrl = territory.getDtmScriptUrl();
                        LOGGER.info("DTMUrlProvider.getUrl: Returning DTM Script URL {} found for territory for page path {}", dtmUrl,
                                pagePath);
                    }
                }
                return dtmUrl;
            }
        }
        LOGGER.info(
                "DTMUrlProvider.getUrl: Returning default DTM URL : {} for page path {} because either territory code is null or no corresponding territory is found",
                defaultDTMUrl, pagePath);
        return defaultDTMUrl;
    }
    
}
