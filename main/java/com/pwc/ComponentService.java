package com.pwc;

import javax.jcr.Node;

import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.wcm.api.Page;

public interface ComponentService {
	public String getComponent(ResourceResolver resourceResolver, Page page, Node node);
}
