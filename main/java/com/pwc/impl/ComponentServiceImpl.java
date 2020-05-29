package com.pwc.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.pwc.ComponentService;

@Component(service = ComponentService.class, immediate = false)
public class ComponentServiceImpl implements ComponentService {

	@Override
	public String getComponent(ResourceResolver resourceResolver, Page page, Node node) {
		String path = "";
		try {
			path= node.getPath();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}

	

}
