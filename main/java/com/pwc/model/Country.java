package com.pwc.model;

import java.util.List;

public class Country {
	
	public Country(){
		
	}

	public Country(String countryName, String shortName){
		this.countryName = countryName;
		this.shortName = shortName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}


	public void setLocaleList(List<Locale> localeList) {
		this.localeList = localeList;
	}

	private String countryName;
	private String shortName;
	
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	private List<Locale> localeList;

	public String getCountryName() {
		return countryName;
	}

	public List<Locale> getLocaleList() {
		return localeList;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	private String countryCode;



}
