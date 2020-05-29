package com.pwc.wcm.taglibs;

import com.pwc.model.Contact;
import com.pwc.model.PageContact;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import java.util.*;

/**
 * Created by rjiang022 on 2/19/2016.
 */
public class RightRailContactValidLinkTagLib {
    private static Log logger = LogFactory.getLog(RightRailContactTagLib.class);

    public static String[] getValidLinks(String[] links, SlingHttpServletRequest request) {
        LinkedList<String> users = new LinkedList<String>();
        if (links != null) {
            for (int i = 0; i < links.length; i++) {
                try {
                    Resource resource = request.getResourceResolver().getResource(links[i]);
                    if (resource != null && resource.getPath() != null)
                        users.add(links[i]);
                } catch (Exception ex) {

                }
            }
        }
        return users.toArray(new String[users.size()]);
    }

    public static String[] getContactLinks(String[] contactLinks, SlingHttpServletRequest request, String sortBy) {
        LinkedList<PageContact> users = new LinkedList<PageContact>();
        List<String> stringList = new ArrayList<>();
        if (contactLinks != null) {
            for (String eachLink : contactLinks) {
                Resource page_resource = request.getResourceResolver().getResource(eachLink);
                Resource new_page_resource = request.getResourceResolver().getResource(eachLink + "/jcr:content/contact-profile-par/contact");
                Resource classic_page_resource = request.getResourceResolver().getResource(eachLink + "/jcr:content/contact");
                if (new_page_resource!=null) {
                    ValueMap properties = new_page_resource.adaptTo(ValueMap.class);
                    String lastName = properties.get("lastName", "");
                    PageContact pageContact = new PageContact();
                    pageContact.setLastName(lastName);
                    pageContact.setLink(eachLink);
                    users.add(pageContact);
                } else if (classic_page_resource!=null) {
                    ValueMap properties = classic_page_resource.adaptTo(ValueMap.class);
                    String lastName = properties.get("lastName", "");
                    PageContact pageContact = new PageContact();
                    pageContact.setLastName(lastName);
                    pageContact.setLink(eachLink);
                    users.add(pageContact);
                }
            }
            if(sortBy!=null && sortBy.equalsIgnoreCase("lastname")){
                Collections.sort(users,new Comparator<PageContact>(){
                    @Override
                    public int compare(PageContact o1, PageContact o2) {
                        return o1.getLastName().compareTo(o2.getLastName());
                    }
                });
            }

            for(PageContact eachPageContact: users){
                stringList.add(eachPageContact.getLink());
            }
        }
        return stringList.toArray(new String[stringList.size()]);
    }
}
