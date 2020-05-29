package com.pwc.model;

import java.util.List;

import org.apache.sling.api.resource.*;
import org.json.JSONException;
import org.json.JSONException;
import org.apache.sling.models.annotations.*;

import javax.annotation.PostConstruct;
import javax.inject.*;

@Model (adaptables=Resource.class)
public class LeftNavigation {
	@Inject
	private String listFrom;
	
	public String getListFrom() {
		return listFrom;
	}
	
	@Inject 
	private String[] links;

	public String[] getLinks() {
		return links;
	}
	
}
