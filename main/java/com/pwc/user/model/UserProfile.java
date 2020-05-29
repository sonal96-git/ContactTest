package com.pwc.user.model;

import com.google.gson.Gson;
import com.pwc.access_control.model.AccessRequest;
import com.pwc.access_control.model.GroupRequest;

import java.util.List;
import java.util.Map;

/**
 * UserProfile POJO class that provides getter and setters for various properties of the user profile in AEM.
 */
public class UserProfile {
    
    private String firstName;
    private String lastName;
    private String email;
    private String country;
    private String organization;
    private String jobTitle;
    private String preferredLocale;
    private String territory;
    private String relationshipWithPwC;
    private Map<String, List<UserProfilePreference>> preferencesMap;
    private List<GroupRequest> requestList;
    private boolean isUserAdvisoryBoard;
    private String marketingConsent;
    private boolean isInternalUser;
    private boolean showSFMCPage;
    /**
     * Instantiates a new user profile.
     *
     * @param firstName {@link String} the first name
     * @param lastName {@link String} the last name
     * @param email {@link String} the email
     * @param country {@link String} the country
     * @param organization {@link String} the organization
     * @param jobTitle {@link String} the job title
     * @param preferredLocale {@link String} the preferred locale
     * @param territory {@link String} the territory
     * @param relationshipWithPwC {@link String} the relationship with PwC
     * @param preferencesMap {@link Map} the category to user preferences map
     * @param isUserAdvisoryBoard true, if the user like to be contacted
     * @param marketingConsent {@link String} value of marketing consent
     * @param isInternalUser true, if user in pwc internal user
     */
    public UserProfile(String firstName, String lastName, String email, String country, String organization, String jobTitle,
            String preferredLocale, String territory, String relationshipWithPwC, Map<String, List<UserProfilePreference>> preferencesMap,
                       boolean isUserAdvisoryBoard, String marketingConsent, boolean isInternalUser, List<GroupRequest> requestList, boolean showSFMCPage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.country = country;
        this.organization = organization;
        this.jobTitle = jobTitle;
        this.preferredLocale = preferredLocale;
        this.territory = territory;
        this.relationshipWithPwC = relationshipWithPwC;
        this.preferencesMap = preferencesMap;
        this.isUserAdvisoryBoard = isUserAdvisoryBoard;
        this.marketingConsent = marketingConsent;
        this.isInternalUser = isInternalUser;
        this.requestList = requestList;
        this.showSFMCPage = showSFMCPage;
    }
    
    /**
     * Gets the preferred locale.
     *
     * @return {@link String} the preferred locale
     */
    public String getPreferredLocale() {
        return preferredLocale;
    }
    
    /**
     * Sets the preferred locale.
     *
     * @param preferredLocale {@link String} the new preferred locale
     */
    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }
    
    /**
     * Checks if is user advisory board.
     *
     * @return true, if is user advisory board
     */
    public boolean isUserAdvisoryBoard() {
        return isUserAdvisoryBoard;
    }
    
    /**
     * Sets the user advisory board.
     *
     * @param isUserAdvisoryBoard the new user advisory board
     */
    public void setUserAdvisoryBoard(boolean isUserAdvisoryBoard) {
        this.isUserAdvisoryBoard = isUserAdvisoryBoard;
    }
    
    /**
     * Gets the first name.
     *
     * @return {@link String} the first name
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Sets the first name.
     *
     * @param firstName {@link String} the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Gets the last name.
     *
     * @return {@link String} the last name
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Sets the last name.
     *
     * @param lastName {@link String} the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Gets the email.
     *
     * @return {@link String} the email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email.
     *
     * @param email {@link String} the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the country.
     *
     * @return {@link String} the country
     */
    public String getCountry() {
        return country;
    }
    
    /**
     * Sets the country.
     *
     * @param country {@link String} the new country
     */
    public void setCountry(String country) {
        this.country = country;
    }
    
    /**
     * Gets the organization.
     *
     * @return {@link String} the organization
     */
    public String getOrganization() {
        return organization;
    }
    
    /**
     * Sets the organization.
     *
     * @param organization {@link String} the new organization
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    /**
     * Gets the job title.
     *
     * @return {@link String} the job title
     */
    public String getJobTitle() {
        return jobTitle;
    }
    
    /**
     * Sets the job title.
     *
     * @param jobTitle {@link String} the new job title
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    /**
     * Gets the relationship with PwC.
     *
     * @return {@link String} the relationship with PwC
     */
    public String getRelationshipWithPwC() {
        return relationshipWithPwC;
    }
    
    /**
     * Sets the relationship with PwC.
     *
     * @param relationShipWithPwC {@link String} the new relationship with PwC
     */
    public void setRelationshipWithPwC(String relationShipWithPwC) {
        this.relationshipWithPwC = relationShipWithPwC;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
    
    /**
     * Gets the territory.
     *
     * @return {@link String} the territory
     */
    public String getTerritory() {
        return territory;
    }
    
    /**
     * Sets the territory.
     *
     * @param territory {@link String} the new territory
     */
    public void setTerritory(String territory) {
        this.territory = territory;
    }
    
    /**
     * Gets the preference category to {@link UserProfilePreference} {@link Map}.
     *
     * @return the preference category to {@link UserProfilePreference} {@link Map}
     */
    public Map<String, List<UserProfilePreference>> getPreferencesMap() {
        return preferencesMap;
    }
    
    /**
     * Sets the preference category to {@link UserProfilePreference} {@link Map}.
     *
     * @param preferencesMap the preference category to {@link UserProfilePreference} {@link Map}
     */
    public void setPreferencesMap(Map<String, List<UserProfilePreference>> preferencesMap) {
        this.preferencesMap = preferencesMap;
    }
    /**
     * Gets the marketingConsent.
     *
     * @return {@link String} the marketingConsent
     */
    public String getMarketingConsent() {
        return marketingConsent;
    }
    /**
     * Sets the marketingConsent
     *
     * @param marketingConsent marketingConsent
     */
    public void setMarketingConsent(String marketingConsent) {
        this.marketingConsent = marketingConsent;
    }
    /**
     * Checks if is user is a internal pwc user
     *
     * @return true, if is user is a internal pwc user
     */
    public boolean isInternalUser() {
        return isInternalUser;
    }
    /**
     * Sets the isInternalUser true, if it is a internal user
     *
     * @param internalUser isInternalUser
     */
    public void setInternalUser(boolean internalUser) {
        isInternalUser = internalUser;
    }

    /**
     * Gets the list of user's access control requests.
     *
     * @return {@link List<AccessRequest>} requestList.
     */
    public List<GroupRequest> getRequestList() {
        return requestList;
    }

    /**
     * Sets the user's request List for access control pages.
     * .
     *
     * @param requestList list of user's access control request of type {@link List<AccessRequest>}.
     */
    public void setRequestList(List<GroupRequest> requestList) {
        this.requestList = requestList;
    }

    public boolean isShowSFMCPage() {
        return showSFMCPage;
    }

    public void setShowSFMCPage(boolean showSFMCPage) {
        this.showSFMCPage = showSFMCPage;
    }
}
