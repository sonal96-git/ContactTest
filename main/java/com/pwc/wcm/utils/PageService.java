package com.pwc.wcm.utils;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

@Component(immediate = true, service = PageService.class)
public class PageService {
	 protected final Logger log = LoggerFactory.getLogger(this.getClass());
	 
	public String getLocale(SlingHttpServletRequest slingRequest,Page currentPage) {
		String locale ="en_us";

    	Page p = currentPage;
		Resource currentPageResource =slingRequest.getResourceResolver().getResource(currentPage.getPath());
		Node cn = currentPageResource.adaptTo(Node.class);
		Node jcrcontent;
		try {
			jcrcontent = cn.getNode("jcr:content");
			if(jcrcontent.hasProperty("jcr:language")){
				locale = jcrcontent.getProperty("jcr:language").getString().toLowerCase();
			}
			else{
	      			
	                while(p.getParent()!=null){
						p = p.getParent();
						currentPageResource = slingRequest.getResource().getResourceResolver().getResource(p.getPath());
	                     cn = currentPageResource.adaptTo(Node.class);
	                    if(cn.hasNode("jcr:content")){
						 jcrcontent = cn.getNode("jcr:content");
							if(jcrcontent.hasProperty("jcr:language")){
								locale = jcrcontent.getProperty("jcr:language").getString().toLowerCase();
								break;
							}

	                   }
	                }
	            }
		} catch (PathNotFoundException e) {
			log.error(e.getMessage(),e);
		} catch (RepositoryException e) {
			log.error(e.getMessage(),e);
		}
		
		return locale;
	}
	public static String getTerritory(Page currentPage) {
		int depth=currentPage.getDepth();
		int parentLevel=1;
		//comparing with 2 as /content/pwc/<territory>
		if(depth>3)
	        parentLevel=depth-3;
		Page terPage=currentPage.getParent(parentLevel);
		String terPageString=terPage.getPath();
		terPageString=terPageString.replace("/content/pwc/","");

		
		return terPageString;
	}
	
public static Set<String> getAvailableSocialChannelforTerritory(SlingHttpServletRequest slingRequest,String territory_Name){
	
	Set<String> s_availableSocialChannels = new HashSet<String>();

	try {
		ResourceResolver resourceResolver = slingRequest.getResourceResolver();
		System.out.println(resourceResolver);
		String territoryPath="/content/pwc/global/referencedata/territories/"+territory_Name;
		Resource resource = resourceResolver.getResource(territoryPath);
		Node tnode = resource.adaptTo(Node.class);

		if(tnode.hasProperty("readingListSocialChannels")) {
			Value[] values = tnode.getProperty("readingListSocialChannels").getValues();
			for(Value val : values) {
				
				s_availableSocialChannels.add(val.getString());
			}	
			return s_availableSocialChannels;
		}else {
			
			return null;
		}
		
	}catch(Exception e) {
		e.printStackTrace();
	}
	return null;
	
}
}
