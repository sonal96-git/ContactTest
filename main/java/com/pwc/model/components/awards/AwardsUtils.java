package com.pwc.model.components.awards;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.pwc.util.ExceptionLogger;

public class AwardsUtils {
	
	private final String CSSCLASS = "col-sm-";
	private final String FILE_REFERENCE_IMAGE_KEY = "fileReference";	
	
	public AwardsUtils() {
		
	}
	
	public String getCssClassRendition(Resource resource) {  				
		AwardsModel awards = resource.adaptTo(AwardsModel.class);
    	String rendition = awards.displayColumns.equals("3") ? "4" : "3";
    	String cssClass = CSSCLASS+rendition;    	
    	return cssClass;
    }	
	
	public String getImage(Resource resource, ResourceResolver resolver) {
 		String imagePath = "";
		String resourcePath = resource.getPath()+"/image";
		Resource imageResource = resolver.getResource(resourcePath);		
		
		if(imageResource != null){
			ValueMap properties = imageResource.getValueMap();
			imagePath = properties.get(FILE_REFERENCE_IMAGE_KEY) != null ? properties.get(FILE_REFERENCE_IMAGE_KEY).toString() : "";
		}			 
		return imagePath;
 	}
	
	public String DecodeString(String text) { 
		String result = "";
		try{
			result = URLEncoder.encode(text, "ISO-8859-1" );
			result = URLDecoder.decode(result, "UTF-8" );
		}catch(Exception e){
			ExceptionLogger.logExceptionMessage("AwardsUtils DecodeString error :",e);	
		}    
		return result;
     }
}
