package com.pwc;

import org.apache.sling.api.resource.ResourceResolver;

public interface AdminResourceResolver {
    /**
     * Returns ResourceResolver with 'admin' rights.
     *
     * @return {@link ResourceResolver}
     */
    public ResourceResolver getAdminResourceResolver();
}
