package com.pwc.util;

import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResourceHelper {
	public static final Logger log  = LoggerFactory.getLogger(ResourceHelper.class);

	public ValueMap getResourceProperty(ResourceResolver resourceResolver, String path)
	{
		ValueMap props =null;
		try{

		Resource res = resourceResolver.getResource(path);
		props= ResourceUtil.getValueMap(res);

		}
		
		catch(Exception e){
			
			log.error(e.getMessage(),e);
		}
		return props;
		
	}

	public Resource getResource(ResourceResolver resourceResolver, String path)
	{
		Resource res=null;
		try{
			res = resourceResolver.getResource(path);
		}
		catch(Exception e){

			log.error(e.getMessage(),e);
		}
		return res;
	}

	/*public Resource getResource(ResourceResolverFactory resolverFactory, String path)
	{
		Resource res=null;
		try{
		ResourceResolver resourceResolver=resolverFactory.getAdministrativeResourceResolver(null);
		 res = resourceResolver.getResource(path);


		}
		catch(Exception e){
			
			log.error(e.getMessage(),e);
		}
		return res;
		
	}
	public ValueMap getParentResourceProperty(ResourceResolverFactory resolverFactory, String path)
	{
		ValueMap props=null;
		try{
		ResourceResolver resourceResolver=resolverFactory.getAdministrativeResourceResolver(null);
		Resource res = resourceResolver.getResource(path);
		props = ResourceUtil.getValueMap(res.getParent());
    	
    	
		}
		catch(Exception e){
			
			log.error(e.getMessage(),e);
		}
		return props;
		
	}*/
}
