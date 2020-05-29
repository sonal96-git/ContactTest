package com.pwc.model.components.highlights;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;


@Model(adaptables = Resource.class)
public class HighLightsModel {

	@Inject @Optional
    public String heading;

	@Inject @Optional
    public Boolean enableColors;
	
	@Inject @Optional
    public String style;
	
	@Inject @Optional
    public String[]  highlight_items;
	
	
	public String getHeading()
	{
		return heading;
	}
	public Boolean getEnableColors()
	{
		return enableColors;
	}
	public String getStyle()
	{
		return style;
	}
	public String[] getHighlight_items()
	{
		return highlight_items;	
	}
}
