package com.pwc.util;

import java.util.HashMap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalParam {
	public static final Logger log  = LoggerFactory.getLogger(ResourceHelper.class);
	public static String contactUsPath="/content/pwc/global/referencedata/propertyNode/contactUs";
	
	public static String getContactUsProperty(ResourceResolver resourceResolver,ValueMap props,String param,String defaultValue)
	{
		String value= getContactUsParams( param, resourceResolver);
		value=props.get(value,defaultValue);
		return value;
	}
	
	public static String getContactUsParams(String param,ResourceResolver resourceResolver)
	{
		String value= getParams(param,resourceResolver,contactUsPath);
		return value;
	}
	private static String getParams(String param,ResourceResolver resourceResolver,String path)
	{
		String value=null;
		try{
		
			Resource res = resourceResolver.resolve(path);
			ValueMap prop = res.adaptTo(ValueMap.class);
			value= prop.get(param,null);
		}
		catch(Exception e){
			log.error(e.getMessage(),e);
		}
		return value;
	}
	
	
	
	//PR1509
	public static Object[] getFieldsToHide(String locale,ResourceResolver resourceResolver)
	{
		Object[] formfieldsToShow=new String[] {"ContactUs-notation","ContactUs_title","ContactUsForm8940_visitor-name","ContactUsForm8940_visitor-email", "ContactUsForm8940_visitor-telephone" 
				
				, "ContactUsForm8940_visitor-org","ContactUsForm8940_visitor-role","ContactUsForm8940_visitor-country","ContactUsForm8940_visitor-inquiryType"
			,"ContactUsForm8940_visitor-subject",	"ContactUsForm8940_visitor-queries",};
		//HashMap<String, String> formfielsToHideMap=new HashMap<String , String>();
		//String[] allFields=new String[] {"ContactUs-notation","ContactUs_title","visitor-telephone","visitor-org","visitor-role","visitor-country","visitor-inquiryType","visitor-subject"};
		HashMap<String, String> allformfieldsMap=new HashMap<String , String>();
		allformfieldsMap.put("ContactUs-notation", "ContactUs-notation");
		allformfieldsMap.put("ContactUs_title", "ContactUs_title");
		allformfieldsMap.put("visitor-telephone", "ContactUsForm8940_visitor-telephone");
		allformfieldsMap.put("visitor-org", "ContactUsForm8940_visitor-org");
		allformfieldsMap.put("visitor-role", "ContactUsForm8940_visitor-role");
		allformfieldsMap.put("visitor-country", "ContactUsForm8940_visitor-country");
		allformfieldsMap.put("visitor-inquiryType", "ContactUsForm8940_visitor-inquiryType");
		allformfieldsMap.put("visitor-subject", "ContactUsForm8940_visitor-subject");
		allformfieldsMap.put("visitor-name", "ContactUsForm8940_visitor-name");
		allformfieldsMap.put("visitor-email", "ContactUsForm8940_visitor-email");
		allformfieldsMap.put("visitor-queries", "ContactUsForm8940_visitor-queries");
		
		HashMap<String, String> formfieldsMapToShowMap=new HashMap<String,String>();//allformfieldsMap;
		log.info("parentPagePath locale"+ locale);
		if(locale==null || locale.trim().length()<1) locale="gx";
		
		//if(parentPagePath!=null && !parentPagePath.equalsIgnoreCase("null"))
		//{
			try{
				//String locale=parentPagePath.substring(13, 15);
				log.info("locale "+ locale);
				String territory ="/content/pwc/global/referencedata/territories/"+locale;
				String[] adminProvidedValues;
				log.info("territory "+ territory);
				Resource res = resourceResolver.resolve(territory);
				ValueMap prop = res.adaptTo(ValueMap.class);
				adminProvidedValues= prop.get("contactUsFormShowFields",null);
				log.info("adminProvidedValues "+ adminProvidedValues + " , country code = "+ prop.get("countryCode",null));
				if(adminProvidedValues!=null )
				{
					formfieldsMapToShowMap.put("visitor-name", "ContactUsForm8940_visitor-name");
					formfieldsMapToShowMap.put("visitor-email", "ContactUsForm8940_visitor-email");
					formfieldsMapToShowMap.put("visitor-queries", "ContactUsForm8940_visitor-queries");
				for(int x=0; x<adminProvidedValues.length;x++ )
				{
					if(adminProvidedValues[x]!=null  && allformfieldsMap.containsKey(adminProvidedValues[x].trim())){
						log.info("adminProvidedValues[x] "+ adminProvidedValues[x]);
						
						formfieldsMapToShowMap.put(adminProvidedValues[x].trim(),allformfieldsMap.get(adminProvidedValues[x].trim()));
						
					}
					
				}
				
				formfieldsToShow=formfieldsMapToShowMap.values().toArray();
				log.info("formfieldsToHide "+ formfieldsToShow.length);	
				}
				
					
			}
			catch(Exception e){
				log.error(e.getMessage(),e);
			}
			
			
		//}
		
		
		
		return formfieldsToShow;
		
	}
	
	

}
