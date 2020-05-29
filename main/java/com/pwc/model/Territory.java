package com.pwc.model;

import java.util.Map;

import com.google.gson.Gson;

/**
 * PwC Territory POGO that provides getters and setters for the properties of the territory.
 */
public class Territory {
    
    private String territoryCode;
    private String territoryName;
    private String[] countries;
    private String defaultLocale;
    private Map<String, Language> localeToLanguageMap;
    private String[] userRegContacts;
    private String territoryI18nKey;
    private boolean enableUserReg;
    private Map<String, String> forwardDomains;
    private String[] territoryAlias;
    private String territoryFinder;
    private String dtmScriptUrl;
    private boolean enableReadingList;
    private String contactUsVersion;
    private String privacyPolicyVersion;
    private String territoryCodeProperty;
	private String countryCode;
    private String[] accessControlledGroups;
    
    /**
     * Constructor that initializes the territory with the given properties.
     *
     * @param territoryCode territory code
     * @param territoryName territory name
     * @param countries country codes
     * @param defaultLocale default locale code
     * @param localeToLanguageMap Locale key to {@link Language} Map
     * @param userRegContacts User Registration contacts(email ID's)
     * @param territoryI18nKey i18n key
     * @param enableUserReg whether user registration is enable or not for territory
     * @param forwardDomains
     * @param territoryAlias
     * @param territoryFinder
     * @param dtmScriptUrl
     */
    public Territory(String territoryCode, String territoryName, String[] countries, String defaultLocale,
            Map<String, Language> localeToLanguageMap, String[] userRegContacts, String territoryI18nKey, boolean enableUserReg,
            Map<String, String> forwardDomains, String[] territoryAlias, String territoryFinder, String dtmScriptUrl,
            boolean enableReadingList, String contactUsVersion,String privacyPolicyVersion,String territoryCodeProperty,String [] accessControlledGroups) {
        this.territoryCode = territoryCode;
        this.territoryName = territoryName;
        this.countries = countries;
        this.defaultLocale = defaultLocale;
        this.localeToLanguageMap = localeToLanguageMap;
        this.userRegContacts = userRegContacts;
        this.territoryI18nKey = territoryI18nKey;
        this.enableUserReg = enableUserReg;
        this.forwardDomains = forwardDomains;
        this.territoryAlias = territoryAlias;
        this.territoryFinder = territoryFinder;
        this.dtmScriptUrl = dtmScriptUrl;
        this.enableReadingList = enableReadingList;
        this.contactUsVersion = contactUsVersion;
        this.privacyPolicyVersion = privacyPolicyVersion;
        this.territoryCodeProperty = territoryCodeProperty;
        this.accessControlledGroups = accessControlledGroups;
    }
    

	/**
     * Returns the {@link Map} where the key is the territory's locale code and the value is the properties of Language node.
     * 
     * @return Locale key to locale title Map
     */
    public Map<String, Language> getLocaleToLanguageMap() {
        return localeToLanguageMap;
    }
    
    /**
     * Sets the territory's locale key to {@link Language} {@link Map}.
     * 
     * @param localeToTitleMap Locale key to {@link Language} Map
     */
    public void setLocaleToLanguageMap(Map<String, Language> localeToLanguageMap) {
        this.localeToLanguageMap = localeToLanguageMap;
    }
    
    /**
     * Constructor that initializes the territory with properties to null.
     */
    public Territory() {
    }
    
    /**
     * Returns the territory code.
     * 
     * @return territory code
     */
    public String getTerritoryCode() {
        return territoryCode;
    }
    
    /**
     * Sets the territory code for territory.
     * 
     * @param territoryCode territory code
     */
    public void setTerritoryCode(String territoryCode) {
        this.territoryCode = territoryCode;
    }
    
    /**
     * Returns the territory name.
     * 
     * @return territory name
     */
    public String getTerritoryName() {
        return territoryName;
    }
    
    /**
     * Sets the territory name for territory.
     * 
     * @param territoryName territory name
     */
    public void setTerritoryName(String territoryName) {
        this.territoryName = territoryName;
    }
    
    /**
     * Returns the country codes mapped to the territory.
     * 
     * @return country codes
     */
    public String[] getCountries() {
        return countries;
    }
    
    /**
     * Set the country codes mapped to the territory.
     * 
     * @param countries country codes
     */
    public void setCountries(String[] countries) {
        this.countries = countries;
    }
    
    /**
     * Returns the default locale of the territory.
     * 
     * @return default locale code
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }
    
    /**
     * Sets the default locale code for the territory.
     * 
     * @param defaultLocale default locale code
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
    
    /**
     * The method has been overridden to provide the JSON equivalent String for the territory.
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
    
    /**
     * Returns the list of the User Registration Contacts(email IDs) of the territory.
     * 
     * @return contacts(email ID's)
     */
    public String[] getUserRegContacts() {
        return userRegContacts;
    }
    
    /**
     * Sets the list of the User Registration Contacts(email IDs) for the territory.
     * 
     * @param userRegContacts contacts(email ID's)
     */
    public void setUserRegContacts(String[] userRegContacts) {
        this.userRegContacts = userRegContacts;
    }
    
    /**
     * Returns the i18n key of the territory.
     * 
     * @return i18n key
     */
    public String getTerritoryI18nKey() {
        return territoryI18nKey;
    }
    
    /**
     * Sets the i18n key for the territory.
     * 
     * @param territoryI18nKey i18n key
     */
    public void setTerritoryI18nKey(String territoryI18nKey) {
        this.territoryI18nKey = territoryI18nKey;
    }
    
    /**
     * Returns whether user registration is enable or not for territory.
     * 
     * @return
     */
    public boolean isEnableUserReg() {
        return enableUserReg;
    }
    
    /**
     * Sets whether user registration is enable or not for territory.
     * 
     * @param enableUserReg
     */
    public void setEnableUserReg(boolean enableUserReg) {
        this.enableUserReg = enableUserReg;
    }
    
    public Map<String, String> getForwardDomains() {
        return forwardDomains;
    }
    
    public void setForwardDomains(Map<String, String> forwardDomains) {
        this.forwardDomains = forwardDomains;
    }
    
    public String[] getTerritoryAlias() {
        return territoryAlias;
    }
    
    public void setTerritoryAlias(String[] territoryAlias) {
        this.territoryAlias = territoryAlias;
    }
    
    public String getTerritoryFinder() {
        return territoryFinder;
    }
    
    public void setTerritoryFinder(String territoryFinder) {
        this.territoryFinder = territoryFinder;
    }
    
    public String getDtmScriptUrl() {
        return dtmScriptUrl;
    }
    
    public void setDtmScriptUrl(String dtmScriptUrl) {
        this.dtmScriptUrl = dtmScriptUrl;
    }
    
    public boolean isEnableReadingList() {
        return enableReadingList;
    }
    
    public void setEnableReadingList(boolean enableReadingList) {
        this.enableReadingList = enableReadingList;
    }
    
    public String getContactUsVersion() {
        return contactUsVersion;
    }
    
    public void setContactUsVersion(String contactUsVersion) {
        this.contactUsVersion = contactUsVersion;
    }
    
    /**
     * Returns privacy policy version of territory.
     * 
     * @return {@link String} privacyPolicyVersion
     */
    public String getPrivacyPolicyVersion() {
		return privacyPolicyVersion;
	}

	/**
	 *  Sets privacy policy version of territory.
	 * 
	 * @param privacyPolicyVersion
	 */
	public void setPrivacyPolicyVersion(String privacyPolicyVersion) {
		this.privacyPolicyVersion = privacyPolicyVersion;
	}
	/**
	 *  Returns territoryCode Property from territory.
	 * 
	 * @return territoryCodeProperty
	 */
	public String getTerritoryCodeProperty() {
		return territoryCodeProperty;
	}

	
	/**
	 *  Sets territoryCode Property on territory node.
	 * 
	 * @param privacyPolicyVersion
	 */
	public void setTerritoryCodeProperty(String territoryCodeProperty) {
		this.territoryCodeProperty = territoryCodeProperty;
	}
    /**
     * Returns list of access controlled groups for territory.
     * 
     * @return {@link String[]} accessControlledGroups - list of access controlled groups
     */
	public String[] getAccessControlledGroups() {
		return accessControlledGroups;
	}
	/**
	 * Returns countryCode of territory.
	 * 
	 * @return String
	 */
	public String getCountryCode() {
		return countryCode;
	}

	
	/**
	 * Sets county code of territory.
	 * 
	 * @param countryCode
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
    

	/**
	 *  Sets list of access controlled groups for territory.
	 * 
	 * @param accessControlledGroups - List of access controlled groups
	 */
	public void setAccessControlledGroups(String[] accessControlledGroups) {
		this.accessControlledGroups = accessControlledGroups;
	}
	
	
    
}
