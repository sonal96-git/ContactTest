package com.pwc.model.components.awards;


import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;


@Model(adaptables = Resource.class)
public class AwardsModel {

	@Inject @Optional
    public String heading;
	
	@Inject @Optional
    public String displayColumns;	

	@Inject @Optional
	public boolean displayImagesOnly;
	
	@Inject @Optional
    public String[]  awards_items;	
	
	
	
	public String getHeading() {
		return heading;
	}	
	public String getDisplayColumns() {
		return StringUtils.isNotBlank(displayColumns) ? displayColumns:"3";
	}	
	public boolean getDisplayImagesOnly() {
		displayImagesOnly = displayImagesOnly == true ? displayImagesOnly : false; 
		return displayImagesOnly;
	}
	public String[] getAwards_items() {
		return awards_items;	
	}	
	
}
