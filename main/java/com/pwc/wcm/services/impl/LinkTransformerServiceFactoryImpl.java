package com.pwc.wcm.services.impl;

import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.LinkTransformerServiceFactory;

@Component(immediate = true, service = { LinkTransformerServiceFactory.class }, enabled = true)
public class LinkTransformerServiceFactoryImpl implements LinkTransformerServiceFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTransformerServiceFactory.class);
    private static final String DOMAIN_PROPERTY = "domain";
    private static final String DOMAIN_TYPE_PROPERTY = "domainType";
    private static final String PWC_DEFAULT_DOMAIN_CONFIGURATION_NAME = "PwC Default Domain";
    private static final String PWC_LINK_TRANSFORMER_CONFIGURATION_NAME = "com.pwc.wcm.transformer.LinkTransformerFactory";
    private static final String LINK_TRANSFORMER_ENABLED_PROPERTY = "linktransformer.enabled";
    
    @Reference
    private ConfigurationAdmin configurationAdmin;
    
    private String domain;
    private String domainType;
    private boolean isLinkTransformerEnabled = false;
    
    @Activate
    @Modified
    protected final void activate(final Map<String, String> properties) throws Exception {
        Configuration defaultDomainConf = configurationAdmin.getConfiguration(PWC_DEFAULT_DOMAIN_CONFIGURATION_NAME);
        this.domain = (String) defaultDomainConf.getProperties().get(DOMAIN_PROPERTY);
        this.domainType = (String) defaultDomainConf.getProperties().get(DOMAIN_TYPE_PROPERTY);
        Configuration linkTransformerConf = configurationAdmin.getConfiguration(PWC_LINK_TRANSFORMER_CONFIGURATION_NAME);
        this.isLinkTransformerEnabled = (Boolean) linkTransformerConf.getProperties().get(LINK_TRANSFORMER_ENABLED_PROPERTY);
    }
    
    @Override
    public LinkTransformerService getLinkTransformerService(Object transformerType) {
        LinkTransformerService linkTransformerService = null;
        if (transformerType != null) {
            try {
                if (transformerType instanceof  SlingRepository) {
                    linkTransformerService = new LinkTransformerServiceImpl((SlingRepository) transformerType, domain, domainType);
                } else if (transformerType instanceof  Session) {
                    linkTransformerService = new LinkTransformerServiceImpl((Session) transformerType, domain, domainType);
                }
            } catch (Exception exception) {
                LOGGER.error(
                        "LinkTransformerServiceFactory : getLinkTransformerService() : Error while getting Link Transformer Service  {}",
                        exception);
            }
        } else {
            linkTransformerService = new LinkTransformerServiceImpl();
        }
        return linkTransformerService;
    }
    
    @Override
    public String getDefaultDomain() {
        return this.getDefaultDomain();
    }
    
    @Override
    public String getDomainType() {
        return this.domain;
    }

    @Override
    public LinkTransformerService getLinkTransformerServiceIfTransformerEnabled(Object transformerType) {
        if(isLinkTransformerEnabled) {
            return getLinkTransformerService(transformerType);
        }
        return null;
    }
    
}
