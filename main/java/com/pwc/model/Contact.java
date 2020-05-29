package com.pwc.model;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import com.pwc.wcm.services.CountryTerritoryMapperService;

//some test

@Model(adaptables = Resource.class)
public class Contact {
    
    public static final String LEGAL_ENTITY_I18NKEY_PROPERTY = "legalEntityI18nKey";
    public static final String LEGAL_ENTITY_TITLE_PROPERTY = "legalEntityTitle";
    public static final String LEGAL_ENTITY_BASE_PATH = "/content/pwc/global/referencedata/legal-entities/";
    
    @Inject
    @Optional
    private String profileName;
    @Inject
    @Optional
    private String firstName;
    @Inject
    @Optional
    private String lastName;
    @Inject
    @Optional
    private String localtitle;
    @Inject
    @Optional
    private String specialty;
    @Inject
    @Optional
    private String bodyContent;
    @Inject
    @Optional
    private String education;
    @Inject
    @Optional
    private String experience;
    @Inject
    @Optional
    private String interest;
    @Inject
    @Optional
    private String email;
    @Inject
    @Optional
    private String fax;
    @Inject
    @Optional
    private String telephone;
    @Inject
    @Optional
    private String office;
    @Inject
    @Optional
    private String province;
    @Inject
    @Optional
    private String profileType;
    @Inject
    @Optional
    private String territory;
    @Inject
    @Optional
    private String country;
    @Inject
    @Optional
    private String designation;
    @Inject
    @Optional
    private String alternativeLinkText;
    @Inject
    @Optional
    private String alternativeLink;
    @Inject
    @Optional
    private String imageReference;
    @Inject
    @Optional
    private String globalTitle;
    @Inject
    @Optional
    private String collectionVisibility;
    @Inject
    @Optional
    private String linkedInUrl;
    @Inject
    @Optional
    private String twitter;
    @Inject
    private ResourceResolver resourceResolver;
    
    @Inject
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    public String getCollectionVisibility() {
        return collectionVisibility;
    }
    
    public String getGlobalTitle() {
        return globalTitle;
    }
    
    public String getImageReference() {
        return imageReference;
    }
    
    public String getAlternativeLinkText() {
        return alternativeLinkText;
    }
    
    public String getAlternativeLink() {
        return alternativeLink;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getTerritory() {
        return territory;
    }
    
    /**
     * Getter to get I18key for territory or legal entity given
     * 
     * @return territoryOrLegalEntityI18nKey ${@link String}
     */
    public  String getTerritoryOrLegalEntityI18nKey() {
        return getTerritoryOrLegalEntityI18nKey(territory, countryTerritoryMapperService, resourceResolver);
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getProfileType() {
        return profileType;
    }
    
    public String getProvince() {
        return province;
    }
    
    public String getOffice() {
        return office;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFax() {
        return fax;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public String getExperience() {
        return experience;
    }
    
    public String getEducation() {
        return education;
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public String getBodyContent() {
        return bodyContent;
    }
    
    public String getLocaltitle() {
        return localtitle;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getInterest() {
        return interest;
    }
    
    public String getTwitter() {
        return twitter;
    }
    
    public String getLinkedInUrl() {
        
        return linkedInUrl;
    }
    
    /**
     * To get I18key for territory or legal entity given
     * 
     * @param territory {@link String}
     * @param countryTerritoryMapperService {@link CountryTerritoryMapperService}
     * @param resourceResolver {@link ResourceResolver}
     * @return {@link String}
     */
    public static String getTerritoryOrLegalEntityI18nKey(String territory, CountryTerritoryMapperService countryTerritoryMapperService,
            ResourceResolver resourceResolver) {
        String territoryOrLegalEntityI18nKey = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(territory)) {
            Territory territoryObj = countryTerritoryMapperService.getTerritoryByTerritoryCode(territory);
            if (territoryObj == null) {
                Resource legalEntityResource = resourceResolver.getResource(LEGAL_ENTITY_BASE_PATH + territory);
                if (legalEntityResource != null) {
                    territoryOrLegalEntityI18nKey = legalEntityResource.getValueMap().get(LEGAL_ENTITY_I18NKEY_PROPERTY,
                            legalEntityResource.getValueMap().get(LEGAL_ENTITY_TITLE_PROPERTY, StringUtils.EMPTY));
                }
            } else {
                territoryOrLegalEntityI18nKey = StringUtils.isNotBlank(territoryObj.getTerritoryI18nKey())
                        ? territoryObj.getTerritoryI18nKey()
                        : territoryObj.getTerritoryName();
            }
        }
        return territoryOrLegalEntityI18nKey;
    }
    
}
