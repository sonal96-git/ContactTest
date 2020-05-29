package com.pwc.model;

import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.*;

@Model(adaptables = Resource.class)
public class RightRailContact {
	@Inject @Optional
	private String[] links;
	@Inject @Optional
	private String contactBoxTitle;
	@Inject @Optional
	private String moreContactsTitle;
	@Inject @Optional
	private String moreContactsUrl;
	@Inject @Optional 
	private String enableGeolocation;
	@Inject @Optional
	private String enableTerritoryddlFilter;
	@Inject @Optional
	private String displayPhoto;
	@Inject @Optional
	private String enableBusinessCardOverlay;
	@Inject @Optional
	private String sortBy;
	@Inject @Optional
	private String displayBy;
	@Inject @Optional
	private String criteria;
	@Inject @Optional
	private String searchContacts;
	
	public String getSearchContacts() {
		return searchContacts;
	}
	public String getCriteria() {
		return criteria;
	}

	public String getDisplayBy() {
		return displayBy;
	}

	public String getSortBy() {
		return sortBy;
	}

	public String getEnableBusinessCardOverlay() {
		return enableBusinessCardOverlay;
	}

	public String getDisplayPhoto() {
		return displayPhoto;
	}

	public String getEnableGeolocation() {
		return enableGeolocation;
	}

	public String getEnableTerritoryddlFilter() {
		return enableTerritoryddlFilter;
	}

	public String isEnableGeolocation() {
		return enableGeolocation;
	}

	public String getMoreContactsUrl() {
		return moreContactsUrl;
	}

	public String getMoreContactsTitle() {
		return moreContactsTitle;
	}

	public String getContactBoxTitle() {
		return contactBoxTitle;
	}

	public String[] getLinks() {
		return links;
	}
	

}
