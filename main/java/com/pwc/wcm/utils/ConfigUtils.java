/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pwc.wcm.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.*;

/**
 * Reviewed 30/07/2012 Reviewed 20/08/2012
 */
public class ConfigUtils {

//	public static final String FORM_POST_DESTINATION_DEFAULT = "https://secure.echosign.com";
    public static final String FORM_POST_DESTINATION_DEFAULT = "https://test";
    public static final String PUBLISH_HOST_PROP = "PUBLISH_HOST";
    public static final String ENABLE_HTTPS = "ENABLE_HTTPS";
    public static final String SITE_MAP_URL_MAPPING_S_PROP = "SITE_MAP_URL_MAPPING_S";
    private static String DEFAULT_NODE = "default";
    private static String FORM_PROXY_URL = "/content/formProxy.form.html";
    private static String HTTPS_PROTOCOL = "https";
    private static Long DEFAULT_HTTPS_PORT = 443L;
    private static String CONFIG_PATH = "/etc/configs/echosign/";
    public static String runtimeMode = "default";

    public static String getFormProxyURL(SlingHttpServletRequest request) {
        StringBuilder proxyActionForm = new StringBuilder();
        Boolean enableHttps = getBoolean("ENABLE_HTTPS", request);
        if (enableHttps != null && enableHttps) {
            proxyActionForm.append(HTTPS_PROTOCOL);
            proxyActionForm.append("://");
            proxyActionForm.append(request.getServerName());
            Long port = getLong("HTTPS_PORT", request);
            if (port != null && !DEFAULT_HTTPS_PORT.equals(port)) {
                proxyActionForm.append(":");
                proxyActionForm.append(port);
            }
            proxyActionForm.append(FORM_PROXY_URL);
        } else {
            proxyActionForm.append(FORM_PROXY_URL);
        }

        return proxyActionForm.toString();
    }

    public static String getString(String key, SlingHttpServletRequest request) {
        Property property = getProperty(key, request);
        if (property != null) {
            try {
                return property.getString();
            } catch (ValueFormatException e) {
                // bypass when get value fail
            } catch (RepositoryException e) {
                // bypass when get value fail
            }
        }
        return null;
    }

    public static Boolean getBoolean(String key, SlingHttpServletRequest request) {
        Property property = getProperty(key, request);
        if (property != null) {
            try {
                return property.getBoolean();
            } catch (ValueFormatException e) {
                // bypass when get value fail
            } catch (RepositoryException e) {
                // bypass when get value fail
            }
        }
        return null;
    }

    public static Long getLong(String key, SlingHttpServletRequest request) {
        Property property = getProperty(key, request);
        if (property != null) {
            try {
                return property.getLong();
            } catch (ValueFormatException e) {
                // bypass when get value fail
            } catch (RepositoryException e) {
                // bypass when get value fail
            }
        }
        return null;
    }

    private static Property getProperty(String key, SlingHttpServletRequest request) {
        Resource resource = request.getResourceResolver().getResource(CONFIG_PATH + runtimeMode);
        if (resource != null) {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                try {
                    if (node.hasProperty(key)) {
                        return node.getProperty(key);
                    }
                } catch (PathNotFoundException e) {
                    // bypass when get value fail
                } catch (RepositoryException e) {
                    // bypass when get value fail
                }
            }
        }

        if (!runtimeMode.equals(DEFAULT_NODE)) {
            return getPropertyDefault(key, request);
        }

        return null;
    }

    public static Property getProperty(String key, String path, ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(path + runtimeMode);
        if (resource != null) {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                try {
                    if (node.hasProperty(key)) {
                        return node.getProperty(key);
                    }
                } catch (PathNotFoundException e) {
                    // bypass when get value fail
                } catch (RepositoryException e) {
                    // bypass when get value fail
                }
            }
        }

        if (!runtimeMode.equals(DEFAULT_NODE)) {
            return getPropertyDefault(key, path, resourceResolver);
        }

        return null;
    }

    public static ValueMap getProperties(ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(CONFIG_PATH + DEFAULT_NODE);
        if (resource != null) {
            return ResourceUtil.getValueMap(resource);
        }
        return null;
    }

    private static Property getPropertyDefault(String key, String path, ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(path + DEFAULT_NODE);
        if (resource != null) {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                try {
                    if (node.hasProperty(key)) {
                        return node.getProperty(key);
                    }
                } catch (PathNotFoundException e) {
                    // bypass when get value fail
                } catch (RepositoryException e) {
                    // bypass when get value fail
                }
            }
        }

        return null;
    }

    private static Property getPropertyDefault(String key, SlingHttpServletRequest request) {
        Resource resource = request.getResourceResolver().getResource(CONFIG_PATH + DEFAULT_NODE);
        if (resource != null) {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                try {
                    if (node.hasProperty(key)) {
                        return node.getProperty(key);
                    }
                } catch (PathNotFoundException e) {
                    // bypass when get value fail
                } catch (RepositoryException e) {
                    // bypass when get value fail
                }
            }
        }

        return null;
    }
}
