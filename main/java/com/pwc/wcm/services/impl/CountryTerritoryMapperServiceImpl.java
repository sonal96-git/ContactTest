package com.pwc.wcm.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.gson.Gson;
import com.pwc.AdminResourceResolver;
import com.pwc.model.Language;
import com.pwc.model.Territory;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.LocaleUtils;

@Component(immediate = true, service = { CountryTerritoryMapperService.class }, enabled = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Provides methods to expose territory or country reference data",
		EventConstants.EVENT_TOPIC + "= org/apache/sling/api/resource/Resource/*"
})
@Designate(ocd = CountryTerritoryMapperServiceImpl.Config.class)
public class CountryTerritoryMapperServiceImpl implements CountryTerritoryMapperService, EventHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryTerritoryMapperService.class);
    public static final String TERRITORY_NODES_BASE_PATH_PROPERTY = "territory.base.path";
    public static final String TERRITORY_NODES_BASE_PATH_VALUE = "/content/pwc/global/referencedata/territories";
    public static final String LANGUAGE_CODE_TO_IGNORE_PROPERTY = "language.code.ignore";
    public static final String LANGUAGE_CODE_TO_IGNORE_VALUE = "xx";
    public static final String TERRITORY_NAME_PROPERTY = "territoryName";
    public static final String COUNTRY_CODES_PROPERTY = "mappedCountry";
	public static final String COUNTRY_CODE = "countryCode";
    public static final String DEFAULT_LOCALE_PROPERTY = "defaultLocale";
    public static final String USERREG_CONTACT_PROPERTY = "userRegContacts";
    public static final String TERRITORY_I18NKEY_PROPERTY = "territoryi18nKey";
    public static final String TERRITORIES_LIST = "territories";
    public static final String ENABLE_USER_REG_PROPERTY = "enableUserReg";
    public static final String FORWARD_DOMAIN_PROPERTY = "forward-domain";
    public static final String TERRITORY_ALIAS_PROPERTY = "territoryAlias";
    public static final String TERRITORY_FINDER_PROPERTY = "territoryFinder";
    private static final String PWC_DEFAULT_DOMAIN_CONFIGURATION = "PwC Default Domain";
    private static final String DOMAIN_PROPERTY = "domain";
    private static final String DOMAIN_TYPE_PROPERTY = "domainType";
    private static final String TERRITORY_SELECTOR_NAME_KEY = "name";
    private static final String TERRITORY_SELECTOR_CODE_KEY = "code";
    private static final String TERRITORY_SELECTOR_ALIAS_KEY = "alias";
    private static final String TERRITORY_SELECTOR_DOMAIN_KEY = "domain";
    private static final String TERRITORY_SELECTOR_TERRITORy_FINDER_KEY = "territoryFinder";
    private static final String DTM_SCRIPT_URL_PROPERTY = "dtmScriptUrl";
    private static final String ENABLE_READING_LIST_PROPERTY = "enableReadingList";
    private static final String CONTACT_US_VERSION_PROPERTY = "contactusversion";
    private static final String PRIVACY_POLICY_VERSION_PROPERTY = "privacypolicy-version";
    private static final String TERRITORY_CODE = "territoryCode";
    private static final String ACG_PROPERTY = "access-controlled-groups";
    private static final String PROPERTY_PRINCIPALNAME = "rep:principalName";
    private static final String PROPERTY_GIVENNAME = "givenName";
    private static final String GROUP_PATH = "/home/groups/pwc-access-control-group";
    private static final String CODE_JSON_KEY = "value";
    private static final String NAME_JSON_KEY = "text";
    private static final String TYPE_GROUP = "rep:Group";
    private static final String EVENT_FILTER_VALUE = "(path=/content/pwc/global/referencedata/territories/*)";

    private String defaultDomain;
    private String domainType;
    
    @Reference
    private ConfigurationAdmin configurationAdmin;
    
    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    private String territoryNodesBasePath;
    private String languageCodeToIgnore;
    private Map<String, Territory> countryToTerritoryMap;
    private Map<String, Territory> territoryCodeToTerritoryMap;

    
    private JSONArray territorySelectorData;
	private JSONArray strategyTerritorySelectorData;
    
    @Reference
	QueryBuilder builder;

	@ObjectClassDefinition(name = "PwC Country Territory Mapper Service", 
			description = "Provides methods to expose territory or country reference data")
	@interface Config {
		@AttributeDefinition(name = "Territory Nodes Base Path", 
				description = "Content path under which territory's reference data is stored",
				type = AttributeType.STRING)
        String territory_base_path() default TERRITORY_NODES_BASE_PATH_VALUE;

		@AttributeDefinition(name = "Langauge Code To Ignore", 
				description = "Language code to remove from language list of the territory in country to territory map",
				type = AttributeType.STRING)
        String language_code_ignore() default LANGUAGE_CODE_TO_IGNORE_VALUE;

        @AttributeDefinition(name = "Event Filter For Territory Reference Data Listener",
                description = "This property has to be consistent with 'Territory Nodes Base Path' property (event.filter)",
                type = AttributeType.STRING)
        String event_filter() default EVENT_FILTER_VALUE;
	}

	@Activate
	@Modified
	protected final void activate(final CountryTerritoryMapperServiceImpl.Config properties) throws Exception {
		LOGGER.info("CountryTerritoryMapperService : Entered Activate/Modify");
		territoryNodesBasePath = properties.territory_base_path();
		languageCodeToIgnore = properties.language_code_ignore();
        Configuration defaultDomainConf = configurationAdmin.getConfiguration(PWC_DEFAULT_DOMAIN_CONFIGURATION);
        this.defaultDomain = PropertiesUtil.toString(defaultDomainConf.getProperties().get(DOMAIN_PROPERTY), "");
        this.domainType = PropertiesUtil.toString(defaultDomainConf.getProperties().get(DOMAIN_TYPE_PROPERTY), "");
        createCountryTerritoryMap();
    }
    
    @Override
    public Territory getTerritoryByCountry(String countryCode) {
        Territory territory = null;
        if (countryToTerritoryMap != null && countryCode != null)
            territory = countryToTerritoryMap.get(countryCode.trim().toUpperCase());
        return territory;
    }
    
    @Override
    public Map<String, Territory> getCountryToTerritoryMap() {
        return countryToTerritoryMap;
    }
    
    @Override
    public String getCountryToTerritoryMapJson() {
        return new Gson().toJson(countryToTerritoryMap);
    }
    
    @Override
    public Territory getTerritoryByTerritoryCode(String territoryCode) {
        Territory territory=null;
        if (territoryCodeToTerritoryMap != null && territoryCode != null)
            territory = territoryCodeToTerritoryMap.get(territoryCode.trim().toLowerCase());
        return  territory;
    }


    @Override
    public Map<String, Territory> getTerritoryCodeToTerritoryMap() {
        return territoryCodeToTerritoryMap;
    }
    
    @Override
    public String getTerritoryCodeToTerritoryMapJson() {
        return new Gson().toJson(territoryCodeToTerritoryMap);
    }
    
    @Override
    public Territory getTerritoryByCountry(String countryCode, String defaultTerritoryCode) {
        Territory territory = getTerritoryByCountry(countryCode);
        if (territory == null)
            territory = getTerritoryByTerritoryCode(defaultTerritoryCode);
        return territory;
    }
    
    @Override
    public Territory getTerritoryByTerritoryCode(String territoryCode, String defaultTerritoryCode) {
        Territory territory = getTerritoryByTerritoryCode(territoryCode);
        if (territory == null)
            territory = getTerritoryByTerritoryCode(defaultTerritoryCode);
        return territory;
    }
    
    @Override
    public JSONArray getTerritorySelectorData() {
        return territorySelectorData;
    }
    
	@Override
	public JSONArray getStatergyTerritorySelectorData() {
		return strategyTerritorySelectorData;
	}
	
    /**
     * Creates the country code to {@link Territory} {@link Map} and the territory code to {@link Territory} {@link Map}.
     */
    private void createCountryTerritoryMap() {
        countryToTerritoryMap = null;
        territoryCodeToTerritoryMap = null;
        List<JSONObject> territoryList = new ArrayList<JSONObject>();
		List<JSONObject> strategyTerritoryList = new ArrayList<JSONObject>();
        
        ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
        Resource territoryBasePathResource = resourceResolver.getResource(territoryNodesBasePath);
        if (territoryBasePathResource != null) {
            countryToTerritoryMap = new HashMap<String, Territory>();
            territoryCodeToTerritoryMap = new HashMap<String, Territory>();
            for (Resource territoryResource : territoryBasePathResource.getChildren()) {
                Territory territory = getTerritoryFromTerritoryResource(territoryResource);
                if (territory.getCountries() != null) {
                    for (String country : territory.getCountries()) {
                        countryToTerritoryMap.put(country.toUpperCase(), territory);
                    }
                }
                territoryCodeToTerritoryMap.put(territory.getTerritoryCode(), territory);
				if (StringUtils.isNumeric(territoryResource.getName())) {
					strategyTerritoryList.add(convertToJsonForTerritoryFinder(territory));
				} else {
                territoryList.add(convertToJsonForTerritoryFinder(territory));
            }
            }
            territorySelectorData = getTerritorySelectorDataFromTerritoryList(territoryList);
			strategyTerritorySelectorData = getTerritorySelectorDataFromTerritoryList(strategyTerritoryList);
        }
        resourceResolver.close();
        LOGGER.debug("CountryTerritoryMapperService : Country to territory Map {}", countryToTerritoryMap);
        LOGGER.debug("CountryTerritoryMapperService : Territory Code to Territory Map {}", territoryCodeToTerritoryMap);
        LOGGER.debug("CountryTerritoryMapperService : Territory selector data {}", territorySelectorData);
    }
    
    /**
     * Convert {@link Territory} to territory finder {@link JSONObject}.
     *
     * @param territory {@link Territory} the territory
     * @return {@link JSONObject} the JSON object
     */
    private JSONObject convertToJsonForTerritoryFinder(final Territory territory) {
        JSONObject territoryJson = new JSONObject();
        try {
            territoryJson.put(TERRITORY_SELECTOR_NAME_KEY, territory.getTerritoryName());
            territoryJson.put(TERRITORY_SELECTOR_CODE_KEY, territory.getTerritoryCode());
            territoryJson.put(TERRITORY_SELECTOR_DOMAIN_KEY, getTerritoryDomain(territory));
            String territoryFinder = territory.getTerritoryFinder();
            territoryJson.put(TERRITORY_SELECTOR_TERRITORy_FINDER_KEY,
                    StringUtils.isBlank(territoryFinder) || !territoryFinder.trim().equalsIgnoreCase("NO"));
            String[] alias = territory.getTerritoryAlias();
            if (alias.length > 0) {
                territoryJson.put(TERRITORY_SELECTOR_ALIAS_KEY, new JSONArray(Arrays.asList(alias)));
            }
        } catch (JSONException jsonException) {
            LOGGER.error("CountryTerritoryMapperService : convertToJsonForTerritoryFinder() : JSONException Occurred  {}", jsonException);
        }
        return territoryJson;
    }
    
    /**
     * Gets the territory domain. It gets the domain from the fowardDomain {@link Map} of the given territory as per the current
     * {@link #domainType}, if the {@link #domainType} is not set, it returns value of {@value #FORWARD_DOMAIN_PROPERTY} from fowardDomain
     * {@link Map}. If the domain is not present in map, it creates domain using the {@link #defaultDomain} value.
     *
     * @param territory {@link Territory} the territory
     * @return {@link String} the territory domain
     */
    private String getTerritoryDomain(final Territory territory) {
        String forwardDomainPoperty = StringUtils.isBlank(this.domainType) ? FORWARD_DOMAIN_PROPERTY
                : FORWARD_DOMAIN_PROPERTY + "-" + this.domainType;
        String currentDomain = territory.getForwardDomains().get(forwardDomainPoperty);
        return StringUtils.isBlank(currentDomain) // No TLD found
                || isTerritoryHavingExceptionalCases(territory) // or the Territory is an exceptional case
                ? getUrlAfterHandlingExceptionalCases(territory, currentDomain)
                : currentDomain;
    }
    
    /**
     * Returns true if the given Territory requires exception handling, else false.
     * 
     * @param territory {@link String}
     * @return {@link Boolean}
     */
    private Boolean isTerritoryHavingExceptionalCases(final Territory territory) {
		boolean isExceptionalCase = false;
		if (territory.getTerritoryCode().equals("gx")
				|| StringUtils.isNumeric(territory.getTerritoryCode())) { // To be handled as <defaultDomain>/gx
			isExceptionalCase = true;
        }
		return isExceptionalCase;
    }
    
    /**
     * Returns the URL after handling territory specific exceptional cases.
     *
     * @param territory {@link Territory} the territory
     * @param currentDomain 
     * @return {@link String} the URL after handling exceptional cases
     */
    private String getUrlAfterHandlingExceptionalCases(final Territory territory, String currentDomain) {
        String domain = StringUtils.isBlank(currentDomain) ? this.defaultDomain : currentDomain;
        String terrCode = territory.getTerritoryCode();
        String uri = terrCode;
        if ("Montenegro".equals(territory.getTerritoryName())) {
            uri = "me/en";
        } else if ("m1".equals(terrCode)) {
            uri = "m1/en";
        } else if(StringUtils.isNumeric(terrCode)) {
			uri = StringUtils.lowerCase(territory.getCountryCode());
        }
        else {
            uri = terrCode;
        }
        return domain + "/" + uri;
    }
    
    /**
     * Gets {@link Territory} from territory {@link Resource}.
     *
     * @param territoryResource {@link Resource} of the territory resource
     * @return {@link Territory}
     */
    private Territory getTerritoryFromTerritoryResource(final Resource territoryResource) {
        ValueMap territoryValueMap = territoryResource.getValueMap();
        Territory territory = new Territory();
        territory.setCountries(territoryValueMap.get(COUNTRY_CODES_PROPERTY, String[].class));
        territory.setTerritoryCode(territoryResource.getName());
        territory.setDefaultLocale(territoryValueMap.get(DEFAULT_LOCALE_PROPERTY, String.class));
        territory.setTerritoryName(territoryValueMap.get(TERRITORY_NAME_PROPERTY, String.class));
        territory.setUserRegContacts(territoryValueMap.get(USERREG_CONTACT_PROPERTY, String[].class));
        territory.setTerritoryI18nKey(territoryValueMap.get(TERRITORY_I18NKEY_PROPERTY, String.class));
        territory.setEnableUserReg(territoryValueMap.get(ENABLE_USER_REG_PROPERTY, false));
        territory.setLocaleToLanguageMap(getLocaleToLanguageMapFromTerritoryResource(territoryResource));
        territory.setForwardDomains(getTerritoryForwardDomains(territoryValueMap));
        territory.setTerritoryAlias(territoryValueMap.get(TERRITORY_ALIAS_PROPERTY, new String[0]));
        territory.setTerritoryFinder(territoryValueMap.get(TERRITORY_FINDER_PROPERTY, String.class));
        territory.setDtmScriptUrl(territoryValueMap.get(DTM_SCRIPT_URL_PROPERTY, String.class));
        territory.setEnableReadingList(territoryValueMap.get(ENABLE_READING_LIST_PROPERTY, true));
        territory.setContactUsVersion(territoryValueMap.get(CONTACT_US_VERSION_PROPERTY, String.class));
        territory.setPrivacyPolicyVersion(territoryValueMap.get(PRIVACY_POLICY_VERSION_PROPERTY,"1"));
        territory.setTerritoryCodeProperty(territoryValueMap.get(TERRITORY_CODE,territoryValueMap.get(TERRITORY_NAME_PROPERTY, String.class)));
		territory.setCountryCode(territoryValueMap.get(COUNTRY_CODE, String.class));
        territory.setAccessControlledGroups(territoryValueMap.get(ACG_PROPERTY,String[].class));
        return territory;
    }
    
    /**
     * Gets the territory forward domains {@link Map} where key is the forward domain property and value is the properties's value.
     *
     * @param territoryValueMap {@link ValueMap} the territory value map
     * @return {@link Map} the territory forward domains
     */
    private Map<String, String> getTerritoryForwardDomains(final ValueMap territoryValueMap) {
        Map<String, String> forwardDomains = new HashMap<String, String>();
        for (String property : territoryValueMap.keySet()) {
            if (property.startsWith(FORWARD_DOMAIN_PROPERTY)) {
                forwardDomains.put(property, territoryValueMap.get(property, String.class));
            }
        }
        return forwardDomains;
    }
    
    /**
     * Gets the locale to {@link Language} {@link Map} from territory {@link Resource}.
     *
     * @param territoryResource {@link Resource} of the territory resource
     * @return {@link Map}
     */
    private Map<String, Language> getLocaleToLanguageMapFromTerritoryResource(final Resource territoryResource) {
        Map<String, Language> localeToLangMap = new HashMap<String, Language>();
        for (Resource languageResource : territoryResource.getChildren()) {
            String languageCode = languageResource.getName();
            if (!languageCode.equals(languageCodeToIgnore)) {
                localeToLangMap.put(LocaleUtils.getLocale(territoryResource.getName(), languageCode),
                        languageResource.adaptTo(Language.class));
            }
        }
        return localeToLangMap;
    }
    
    /**
     * Gets the territory selector data from territory list. It also sorts the territories in ascending order of their name before creating
     * {@link {@link JSONArray}.
     *
     * @param territoryList {@link List }the territory list
     * @return {@link JSONArray} the territory selector data from territory list
     */
    private JSONArray getTerritorySelectorDataFromTerritoryList(final List<JSONObject> territoryList) {
        Collections.sort(territoryList, new Comparator<JSONObject>() {
            public int compare(JSONObject territoryJSONOne, JSONObject territoryJSONTwo) {
                int compare = 0;
                try {
                    compare = territoryJSONOne.getString(TERRITORY_SELECTOR_NAME_KEY)
                            .compareTo(territoryJSONTwo.getString(TERRITORY_SELECTOR_NAME_KEY));
                } catch (JSONException jsonException) {
                    LOGGER.error(
                            "CountryTerritoryMapperService : getTerritorySelectorDataFromTerritoryList() : JSONException Occurred while sorting territory selector data {}",
                            jsonException);
                }
                return compare;
            }
        });
        return new JSONArray(territoryList);
    }
    
    @Override
    public void handleEvent(Event event) {
        createCountryTerritoryMap();
    }
    
    @Override
    public JSONArray getACGByTerritoryCode(String territoryCode,Session session) {
        Territory currentTerritory = getTerritoryByTerritoryCode(territoryCode);
        
        JSONArray acgJSONArray = new JSONArray();
        try {
			
			String[] l_acg = currentTerritory.getAccessControlledGroups();
			Map<String, String> map = new HashMap<String, String>();
			List<Hit> records = new ArrayList<Hit>();
			if(l_acg!=null && l_acg.length>0) {
				map.put("path",GROUP_PATH);
				map.put("type", TYPE_GROUP);
				map.put("property",PROPERTY_PRINCIPALNAME);
				
				for(int i=0;i<l_acg.length;i++) {
					map.put("property."+(i+1)+"_value", l_acg[i]);
		        }
				
				map.put("p.limit", "-1");
				Query query = builder.createQuery(PredicateGroup.create(map), session);
				SearchResult result = query.getResult();
				records = result.getHits();
			}
			
			JSONObject jsonACGNone = new JSONObject();
			jsonACGNone.put(NAME_JSON_KEY,"None");
			jsonACGNone.put(CODE_JSON_KEY,"");
			acgJSONArray.put(jsonACGNone);
			for(Hit hit : records) {
				Resource res = hit.getResource();
				for(Resource childRes : res.getChildren()) {
					if(childRes.getName().equalsIgnoreCase("profile")) {
						ValueMap vm = childRes.getValueMap();
						String principalName = hit.getProperties().containsKey(PROPERTY_PRINCIPALNAME) ? hit.getProperties().get(PROPERTY_PRINCIPALNAME).toString() : "";
						String givenName = vm.containsKey(PROPERTY_GIVENNAME) ? vm.get(PROPERTY_GIVENNAME).toString() : principalName;
						if(!givenName.isEmpty()) {
							JSONObject jsonACG = new JSONObject();
							try {
								jsonACG.put(NAME_JSON_KEY,givenName);
								jsonACG.put(CODE_JSON_KEY,principalName);
								acgJSONArray.put(jsonACG);
		                    } catch (JSONException jsonException) {
		                        LOGGER.error("Territory ACG Servlet", jsonException);
		                    } 
						}
					}
				}
			}
	        
	        
	        return acgJSONArray;
		} catch (RepositoryException e) {
			LOGGER.error("Respository Exception while fetching access group : "+e.getMessage(), e);
		}catch (Exception e) {
			LOGGER.error("Exception while fetching access group : "+e.getMessage(), e);
		}finally {
			return acgJSONArray;
		}
    }
    
}
