package com.pwc.tagmodels;

import com.pwc.AdminResourceResolver;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: intelligrape
 * Date: 9/1/14
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocialShareValueMap extends SimpleTagSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialShareValueMap.class);
	private static final Map<String,String> socialFollowi18Map;
	static
    {
		socialFollowi18Map = new HashMap<String, String>();
		socialFollowi18Map.put("Facebook Follow","SocialFollow_Facebook");
		socialFollowi18Map.put("Twitter Follow","SocialFollow_Twitter");
		socialFollowi18Map.put("Linkedin Follow","SocialFollow_Linkedin");
		socialFollowi18Map.put("YouTube Follow","SocialFollow_YouTube");
		socialFollowi18Map.put("Instagram Follow","SocialFollow_Instagram");
		socialFollowi18Map.put("Weibo Follow","SocialFollow_Weibo");
		socialFollowi18Map.put("WeChat Follow","SocialFollow_WeChat");		
		socialFollowi18Map.put("Pinterest Follow","SocialFollow_Pinterest");
    }
	
    public void doTag() throws JspException{
        Resource resource = (Resource)getJspContext().getAttribute("resource");
        PageContext pageContext = (PageContext)getJspContext();
        String path =resource.getPath() +"/options";

        SlingBindings bindings = (SlingBindings) getJspContext().findAttribute(SlingBindings.class.getName());
        SlingScriptHelper scriptHelper = bindings.getSling();
        ResourceResolver adminResourceResolver = scriptHelper.getService(AdminResourceResolver.class).getAdminResourceResolver();
        Resource resource1 = adminResourceResolver.getResource(path);
        LinkedHashMap<String,ArrayList<String>> map = new LinkedHashMap<>();
        ArrayList<String> list;
       if(resource1!=null){
           try {
               Iterator<Resource> iterator = resource1.listChildren();
               while (iterator.hasNext()) {
                   List list1;
                   final Resource childResource = iterator.next();
                   final ValueMap childValueMap = childResource.adaptTo(ValueMap.class);
                   String typeIcon = childValueMap.get("pathicon", String.class);
                   if (typeIcon.contains("googleplus")) {
                       adminResourceResolver.delete(childResource);
                       adminResourceResolver.commit();
                   } else {
                       list = new ArrayList<>();
                       String pathUrl = childValueMap.get("optionval", String.class);
                       list.add(pathUrl);
                       typeIcon = typeIcon.replace("[", "");
                       typeIcon = typeIcon.replace("]", "");
                       typeIcon = typeIcon + " Follow";
                       list1 = Arrays.asList(typeIcon.split(","));
                           list.addAll(list1);
                           list.add(socialFollowi18Map.get(list1.get(1).toString().trim()));
                           String resourceName = childResource.getName();
                           map.put(resourceName, list);
                   }
               }
           } catch (PersistenceException persistenceException) {
               LOGGER.error("SocialShareValueMap.doTag: PersistenceException {} occurred while removing the child of node {}", persistenceException, path);
           }finally {
               if(adminResourceResolver.isLive())
                   adminResourceResolver.close();
           }
        }
        pageContext.setAttribute("socialAttributes",map );
    }
}

