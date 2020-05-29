package com.pwc.wcm.services.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;
import com.pwc.AdminResourceResolver;
import com.pwc.ApplicationConstants;
import com.pwc.wcm.services.ListAllParentPages;

@Component(
		immediate = true,
		service = {EventHandler.class,ListAllParentPages.class},
		property = {
				Constants.SERVICE_DESCRIPTION + "= PwC Get Or Update List Of Parent Pages",
				EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC
		})
@Designate(ocd = ParentPagesList.Config.class)
public class ParentPagesList implements ListAllParentPages, EventHandler {

	@Reference
	private AdminResourceResolver adminResourceResolver;

	private Set<String> pathList = null;

	ResourceResolver resourceResolver = null;

	private String path;
	
	@ObjectClassDefinition(name = "PwC Get Or Update List Of Parent Pages", description = "PwC Get Or Update List Of Parent Pages")
    @interface Config {
        @AttributeDefinition(name = "Domain Root Path", 
                            description = "root path from where activate method start searching for file to create sitemap.",
                            type = AttributeType.STRING)
        public String domainRootPath() default "/content/pwc";
    }

	/**
	 * This method gets called by api to activate the service in osgi.
	 * 
	 * @param componentContext
	 */
	@Activate
	protected void activate(ParentPagesList.Config properties) {
		//final Dictionary<?, ?> properties = componentContext.getProperties();
		path = properties.domainRootPath();
		this.pathList = new HashSet<String>();
		AddPathOfAllParentPageToList(path);
	}

	/**
	 * This method generates a set of parent page path.
	 * 
	 * @param rootPath
	 *            . The path from where it will start searching for pages with
	 *            property sitemap_domain.
	 * @return Set<String>. return the set of all parent pages having
	 *         sitemap_domain property.
	 */
	private Set<String> AddPathOfAllParentPageToList(String rootPath) {
		Page page = null;
		try {
			resourceResolver = adminResourceResolver.getAdminResourceResolver();
			Resource resource = resourceResolver.getResource(rootPath);
			if (resource != null) {
				page = resource.adaptTo(Page.class);
			}
			if (page == null) {
				Iterator<Resource> resourceChildren = resource.listChildren();
				while (resourceChildren.hasNext()) {
					Resource pageResource = resourceChildren.next();
					page = pageResource.adaptTo(Page.class);
					if (page != null) {
						getListOfPage(page);
					}
				}
			} else {
				getListOfPage(page);
			}
		}finally {
			if (resourceResolver != null) {
				resourceResolver.close();
			}
		}
		return pathList;
	}

	/**
	 * Iterate over pages to add into List of path
	 */
	private void getListOfPage(Page page) {
		Iterator<Page> children = page.listChildren(new PageFilter(), true);
		while (children.hasNext()) {
			addPathOfPageToList(children.next());
		}
	}

	/**
	 * Add path of page to Set.
	 * 
	 * @param child
	 *            page to add in the list of parent page
	 */
	public void addPathOfPageToList(Page child) {
		ValueMap valueMap = child.getProperties();
		if ("true".equals(valueMap.get(ApplicationConstants.SITE_MAP_DOMAIN_PROPERTY, ""))) {
			pathList.add(child.getPath());
		}
	}

	@Override
	public Set<String> getPathOfAllParentPage() {
		return this.pathList;
	}

	/**
	 * This method is called whenever a property is added to a node
	 * 
	 * @param events
	 *            Takes Event.
	 */
	@Override
	public void handleEvent(Event event) {
		ReplicationAction action = ReplicationAction.fromEvent(event);
		String pagePath = action.getPath();
		if (ReplicationActionType.ACTIVATE.toString().equals(
				action.getType().toString())) {
			try {
				resourceResolver = adminResourceResolver.getAdminResourceResolver();
				PageManager pageManager = resourceResolver
						.adaptTo(PageManager.class);
				Page currentPage = pageManager.getPage(pagePath);
				if (currentPage != null) {
					addPathOfPageToList(currentPage);
				}
			} finally {
				if (resourceResolver != null) {
					resourceResolver.close();
				}
			}
		} else if (ReplicationActionType.DEACTIVATE.toString().equals(
				action.getType().toString())
				|| ReplicationActionType.DELETE.toString().equals(
						action.getType().toString())) {
			this.pathList.remove(pagePath);
		}
	}

}

