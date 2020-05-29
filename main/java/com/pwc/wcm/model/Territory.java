package com.pwc.wcm.model;

import java.util.ArrayList;

/**
 * Created by rjiang022 on 6/12/2015.
 */
public class Territory {
    private String territoryName;
    private String forwardDomain;
    private String forwardRegex;
    private boolean isMultiLanguage;
    private ArrayList<Locale> locales;
    private boolean showLangugageInUrl;
    private boolean isTLD;
    private String territoryCodeProperty;

    /**
	 * @return the territoryCodeProperty
	 */
	public String getTerritoryCodeProperty() {
		return territoryCodeProperty;
	}

	/**
	 * @param territoryCodeProperty the territoryCodeProperty to set
	 */
	public void setTerritoryCodeProperty(String territoryCodeProperty) {
		this.territoryCodeProperty = territoryCodeProperty;
	}

	public String getTerritoryName() {
        return territoryName;
    }

    public void setTerritoryName(String territoryName) {
        this.territoryName = territoryName;
    }

    public String getForwardDomain() {
        return forwardDomain;
    }

    public void setForwardDomain(String forwardDomain) {
        this.forwardDomain = forwardDomain;
    }

    public String getForwardRegex() {
        return forwardRegex;
    }

    public void setForwardRegex(String forwardRegex) {
        this.forwardRegex = forwardRegex;
    }

    public boolean isMultiLanguage() {
        return isMultiLanguage;
    }

    public void setIsMultiLanguage(boolean isMultiLanguage) {
        this.isMultiLanguage = isMultiLanguage;
    }

    public ArrayList<Locale> getLocales() {
        return locales;
    }

    public void setLocales(ArrayList<Locale> locales) {
        this.locales = locales;
    }

    public boolean isShowLangugageInUrl() {
        return showLangugageInUrl;
    }

    public void setShowLangugageInUrl(boolean showLangugageInUrl) {
        this.showLangugageInUrl = showLangugageInUrl;
    }


    public boolean isTLD() {
        return isTLD;
    }

    public void setIsTLD(boolean isTLD) {
        this.isTLD = isTLD;
    }
}
