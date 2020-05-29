package com.pwc.wcm.services.impl;

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

import com.pwc.model.Territory;
import com.pwc.wcm.services.ContactUsPagePathProviderService;
import com.pwc.wcm.services.CountryTerritoryMapperService;

@Component(immediate = true, service = { ContactUsPagePathProviderService.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Provides contact us page path used for the territory" 
})
@Designate(ocd = ContactUsPagePathProviderServiceImpl.Config.class)
public class ContactUsPagePathProviderServiceImpl implements ContactUsPagePathProviderService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactUsPagePathProviderService.class);
    
    public static final String OLD_CONTACT_US_PATH_PROPERTY = "contact.old.path";
    public static final String NEW_CONTACT_US_PATH_PROPERTY = "contact.new.path";
    public static final String OLD_CONTACT_US_PATH_VALUE = "/content/pwc/global/forms/contactUs";
    public static final String NEW_CONTACT_US_PATH_VALUE = "/content/pwc/global/forms/contactUsNew";
    public static final String CONTACT_US_VERSION_PROPERTY_VALUE_NEW = "new";
    
    private String oldContactUsPagePath;
    private String newContactUsPagePath;
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @ObjectClassDefinition(name = "PwC Contact Us Page Path Provider", description = "Provides contact us page path used for the territory")
    @interface Config {
        @AttributeDefinition(name = "Old Contact Us Page Path", 
                            description = "Old Version Page Path for Contact Us",
                            type = AttributeType.STRING)
        public String oldContactUsPagePath() default OLD_CONTACT_US_PATH_VALUE;
        
        @AttributeDefinition(name = "New Contact Us Page Path", 
                description = "New Version Page Path for Contact Us",
                type = AttributeType.STRING)
        public String newContactUsPagePath() default NEW_CONTACT_US_PATH_VALUE;
    }
    
    @Activate
    @Modified
    protected final void activate(final ContactUsPagePathProviderServiceImpl.Config properties) throws Exception {
        LOGGER.info("ContactUsPagePathProviderService : Entered Activate/Modify");
        oldContactUsPagePath = properties.oldContactUsPagePath();
        newContactUsPagePath = properties.newContactUsPagePath();
    }
    
    @Override
    public String getContactUsPagePath(String territoryCode, String parentPagePath, String contactLink, String style, String locale) {
        boolean isOldVersion = true;
        if (StringUtils.isNotEmpty(territoryCode)) {
            Territory territory = countryTerritoryMapperService.getTerritoryByTerritoryCode(territoryCode);
            if (territory == null) {
                LOGGER.debug(
                        "ContactUsPagePathProviderServiceImpl : getContactUsPagePath() : Territory object for Territory Code {} is null",
                        territoryCode);
            } else {
                if (StringUtils.isNotEmpty(territory.getContactUsVersion())
                        && territory.getContactUsVersion().equals(CONTACT_US_VERSION_PROPERTY_VALUE_NEW)) {
                    LOGGER.debug(
                            "ContactUsPagePathProviderServiceImpl : getContactUsPagePath() : contactusversion property value '{}'for Territory Code {}",
                            territory.getContactUsVersion(), territoryCode);
                    isOldVersion = false;
                } else {
                    LOGGER.debug(
                            "ContactUsPagePathProviderServiceImpl : getContactUsPagePath() : contactusversion property value '{}'for Territory Code {} is either 'old' or not existing",
                            territory.getContactUsVersion(), territoryCode);
                }
            }
        } else {
            LOGGER.debug("ContactUsPagePathProviderServiceImpl : getContactUsPagePath() : Territory Code {} is null/empty", territoryCode);
        }
        String contactUsPagePath = getContactUsPagePath(territoryCode, parentPagePath, contactLink, style, locale, isOldVersion);
        LOGGER.debug("ContactUsPagePathProviderServiceImpl : getContactUsPagePath() : Returning contactUs path {} for territory {}",
                contactUsPagePath, territoryCode);
        return contactUsPagePath;
    }
    
    /**
     * Returns the contact us page path link with required query parameters. If the isOldVersion is true, the contact us page
     * path links to old contact page otherwise links to new contact page.
     * 
     * @param territory {@link String} territory code on which the contact is displayed
     * @param parentPagePath {@link String} parentPagePath of the page which generally specifies the hierarchy on which the contact lies
     * @param contactLink {@link String} page link to the contact, it can be null as well if a query parameter for contactLink is not
     *            required
     * @param style {@link String}
     * @param locale {@link String} locale of the page on which the contact is displayed
     * @return {@link String}
     */
    private String getContactUsPagePath(String territory, String parentPagePath, String contactLink, String style, String locale, boolean isOldVersion) {
        String contactUsPagePath = isOldVersion ? oldContactUsPagePath + "." + locale   : newContactUsPagePath;
		contactUsPagePath = contactUsPagePath + ".html?parentPagePath=" + parentPagePath + "&style=" + style + "&territory=" + territory;
        if(contactLink != null)
            contactUsPagePath += "&contactLink=" + contactLink;
        return contactUsPagePath;
    }
    
}
