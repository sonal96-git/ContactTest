package com.pwc.model.components.highlights;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import javax.inject.Inject;
import javax.inject.Named;

@Model(adaptables = Resource.class)
public class HighLightsItemsModel {
	
	
	
	@Inject 
    public @Optional String title;

    @Inject @Optional @Named("abstract")
    public String summary;

    @Inject @Optional
    public String text;

    @Inject @Optional
    public String fileReference;

    @Inject @Optional
    public String link_path;
    
    @Inject @Optional
    public String link_text;
    
    @Inject @Optional
    public String alttext;
    
    @Inject @Optional
    public boolean target;
          
       
    
    public String getTitle()
    {
    	return title;
    }    
    public String getSummary()
    {
    	return summary;
    }    
    public String getBio()
    {
    	return text;
    }    
    public String getImage()
    {    	
    	return fileReference;
    }    
    public String getLink()
    {
    	return link_path;
    } 
    public String getLink_text()
    {
    	return link_text;
    } 
    public Boolean getTarget()
    {
    	return target;    		
    }
    public String getAlttext()
    {
    	return alttext;    		
    }
   
}
