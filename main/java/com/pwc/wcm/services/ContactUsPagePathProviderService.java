package com.pwc.wcm.services;

/**
 * Service provides contact us page path link used for the territory.
 */
public interface ContactUsPagePathProviderService {
    
    /**
     * Returns the contact us page path link with required query parameters for the given territory. If the
     * property "contactusversion" value on given territory reference data is 'new', contact us page
     * path links to new contact page otherwise it links to old contact page in the following conditions:
     * <ul>
     *  <li>Given territory is null
     *  <li>Given territory is not a valid territory
     *  <li>property "contactusversion" value on given territory reference data is 'old' or not present
     * </ul>.
     * 
     * @param territory {@link String} territory code on which the contact is displayed
     * @param parentPagePath {@link String} parentPagePath of the page which generally specifies the hierarchy on which the contact lies
     * @param contactLink {@link String} page link to the contact, it can be null as well if a query parameter for contactLink is not
     *            required
     * @param style {@link String}
     * @param locale {@link String} locale of the page on which the contact is displayed
     * @return {@link String}
     */
    public String getContactUsPagePath(String territory, String parentPagePath, String contactLink, String style, String locale);
    
}
