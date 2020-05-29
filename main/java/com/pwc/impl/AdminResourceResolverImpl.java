package com.pwc.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;

@Component(immediate = true, service = { AdminResourceResolver.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= A generic service returning AdminResourceResolver." })
public class AdminResourceResolverImpl implements AdminResourceResolver {
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    protected static final Logger LOG = LoggerFactory.getLogger(AdminResourceResolverImpl.class);
    
    @Override
    public ResourceResolver getAdminResourceResolver() {
	ResourceResolver resourceResolver = null;
	try {
	    final Map<String, Object> authInfo = new HashMap<String, Object>();
	    authInfo.put(ResourceResolverFactory.SUBSERVICE, "adminResourceResolver");
	    resourceResolver = resolverFactory.getServiceResourceResolver(authInfo);
	} catch (final LoginException loginExcp) {
	    LOG.error("Exception while getting resource resolver." + loginExcp);
	}

	return resourceResolver;
    }
}
