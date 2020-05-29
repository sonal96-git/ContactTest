package com.pwc.wcm.taglibs;

import java.util.Arrays;

import javax.servlet.jsp.PageContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.cyscape.countryhawk.Country;
import com.day.cq.wcm.api.Page;
import com.pwc.wcm.utils.CommonUtils;

public class UtilityTagFunctions {

	public static String getSubNavElementCssClass(Integer columnCount) {
		switch(columnCount) {
			case 1:
				return "";
			case 2:
				return "half";
			case 3:
				return "third";
			case 4:
				return "quarter";
			case 5:
				return "one-fifth";
			case 6:
				return "one-sixth";
			default:
				return "";
		}
	}
	
	public static Page getLanguageNode(PageContext pc) {
		Page currentPage = (Page) pc.getAttribute("currentPage");
		if (currentPage != null) {
			Page languageNodePage = currentPage.getAbsoluteParent(2);
			if (languageNodePage != null)
				return languageNodePage;
		}
		return currentPage;
	}

	public static String getLinkForPath(SlingHttpServletRequest request, String path) {
        return CommonUtils.convertPathInternalLink(request, path);
	}
	public static String[] splitStrings(String input, String delimiter){
		return input.split(delimiter);
	}
	public static String getGeolocationByIp(String ip){
		  String result = "us";
		  try
		  {
			  Country c = Country.getCountry(ip);
		  	  result= c.getCode().toLowerCase();
			  if(result.equalsIgnoreCase("xx"))
				  result ="us";
		  }
		  catch(Exception ex){
			  
		  }
		  return result;
	}
	public static String getGeolocation(SlingHttpServletRequest request){
		 String result = "us";
		 try{
			 Country c = Country.getCountry(request);
			 result = c.getCode().toLowerCase();
			 if(result.equalsIgnoreCase("xx"))
				 result ="us";
		 }
		 catch(Exception ex){
			 
		 }
		 return result;
	}
	public static String getTerritoryFromPath(String contactPath){
		int startPos = contactPath.indexOf("/pwc/");
		return contactPath.substring(startPos+5,startPos+7);
	}
	public static String getCountryFullName(SlingHttpServletRequest request, String countryCode){
		String cjcrPath = "/content/pwc/global/referencedata/countries";
		String fullCountryName = "";
		ResourceResolver resourceResolver = request.getResourceResolver();
		Resource res = resourceResolver.getResource(cjcrPath);
		ValueMap territories = res.adaptTo(ValueMap.class);
		String[] territoryList = (String[])territories.get("country-code-name");
		
		
		for(String t: territoryList){
			
			String[] val = t.split(":");
			
			if(val[0].toLowerCase().equals(countryCode.toLowerCase()))
			{
				fullCountryName = val[1];
				break;
			}
			
		}
		return fullCountryName;
	}
	public static String[] sortStringArray(String[] vals){
		Arrays.sort(vals);
		return vals;
	}
}