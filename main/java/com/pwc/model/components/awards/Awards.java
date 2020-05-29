package com.pwc.model.components.awards;


import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.json.JSONObject;

import com.adobe.cq.sightly.WCMUsePojo;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.wcm.utils.I18nPwC;


public class Awards extends WCMUsePojo{
    
    private final String LOAD_MORE_LABEL_KEY = "Awards_LoadMoreLabel";
    private final String ID_KEY = "id";  
    private final String CSS_IMAGE_ONLY = "awards-comp--image-only"; 
    private AwardsModel model;
    private Resource resource;
    private SlingHttpServletRequest request;  
    private OsgiCollectionsLogger pwcLogger;
      
    @Override
    public void activate() throws Exception {    	
    	this.resource = getResource();
    	this.request = getRequest();    	
        this.pwcLogger =  getSlingScriptHelper().getService(OsgiCollectionsLogger.class);    	
    	if(this.resource == null) 
    		return;
    	else
		    this.model = this.resource.adaptTo(AwardsModel.class);
    }  
    
    public String getCssClassImageOnly() {    		    	
    	String cssClass = this.model.displayImagesOnly ? CSS_IMAGE_ONLY : "";	
    	return cssClass;
    }	        
        
    public List<List<String>> getLimitedAwardsItems() {   int awardsItemSize = this.model.getAwards_items().length;
        int columnRendition = Integer.parseInt(this.model.displayColumns) * 2;
    	int endPosition = columnRendition > awardsItemSize ? awardsItemSize : columnRendition;
    	return getAwardsItemsNested(0,endPosition);
    }
    
    public List<List<String>> getExtendedAwardsItems() {
    	int startPosition = Integer.parseInt(this.model.displayColumns) * 2;
    	return getAwardsItemsNested(startPosition,this.model.getAwards_items().length);
    }
    
    public List<List<String>> getAllAwardsItems() {    	
    	return getAwardsItemsNested(0 , this.model.getAwards_items().length);
    }
    
    public String getCssClassRendition() {  				
		AwardsUtils awardsUtils = new AwardsUtils();
		return awardsUtils.getCssClassRendition(this.resource);
    }
    
    public List<List<String>> getAwardsItemsNested(int startPosition,int endPosition) {        
		String[] listAwardsItems = this.model.getAwards_items();		
    	if(listAwardsItems == null) return null;
    	List<String> listAwardsItemsNames = new ArrayList<>();
    	List<List<String>> listAwardsItemsNamesNested = new ArrayList<>();    	
		try{
			int displayColumn = Integer.parseInt(this.model.displayColumns);
			int iterator = 0;
	    	for(int i = startPosition; i < endPosition; i++) {	
	    		JSONObject jsonLink = new JSONObject(listAwardsItems[i]); 
            	listAwardsItemsNames.add(jsonLink.optString(ID_KEY));     
            	iterator++;	            
	           if(iterator == displayColumn || i+1 == endPosition) {
	        	   listAwardsItemsNamesNested.add(listAwardsItemsNames);
		           listAwardsItemsNames = new ArrayList<>();
		           iterator = 0;
	            } 
	    	}
	    	//DeleteNodeWhenDeleteItems(listAwardsItems);
		 } catch(Exception e) {
            pwcLogger.logMessage("Awards getAwardsItemsName error : " + e.toString());
		 }
		 
	     return listAwardsItemsNamesNested;
    }         
    
    public Boolean getHasList() {
    	return this.model.getAwards_items() != null;    	
    }  
    
    public Boolean getLoadMore() {
    	int columnRendition = Integer.parseInt(this.model.displayColumns) * 2;
    	return this.model.getAwards_items().length > columnRendition;  	
    } 
    
    public String getLoadMoreLabel() {
    	I18nPwC i18nPwC = new I18nPwC(this.request, resource);
    	return i18nPwC.getPwC(LOAD_MORE_LABEL_KEY);
    }   
    
    public AwardsModel getModel() {
    	return this.model;
    }
   
}
