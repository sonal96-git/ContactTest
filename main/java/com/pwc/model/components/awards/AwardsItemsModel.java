package com.pwc.model.components.awards;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import javax.inject.Inject;
import javax.inject.Named;

@Model(adaptables = Resource.class)
public class AwardsItemsModel {
	
	
	
	@Inject 
    public @Optional String title;

    @Inject @Optional @Named("abstract")
    public String summary;    

    @Inject @Optional
    public String fileReference;

    @Inject @Optional
    public String link_path;
    
    @Inject @Optional
    public boolean target;
    
    @Inject @Optional
    public boolean updated;
         
    
    public String getTitle() {
    	return title;
    }    
    public String getSummary() {
    	return summary;
    }  
    public String getImage() {    	
    	return fileReference;
    }    
    public String getLink() {
    	return link_path;
    }    
    public Boolean getTarget() {
    	return target;    		
    }
    public Boolean getUpdated() {
    	return updated;    		
    }
   
}
