package com.pwc.wcm.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jcr.RepositoryException;

import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.transformer.impl.LinkTransformer;

@Component(immediate = true, service = { TransformerFactory.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Rewrite certain links",
        "pipeline.type=" + "mylinktransformer"
})
@Designate(ocd = LinkTransformerFactory.Config.class)
public class LinkTransformerFactory implements TransformerFactory {

    private static final Logger log = LoggerFactory.getLogger(LinkTransformerFactory.class);
    
    /*@Property(
            value = "mylinktransformer",
            propertyPrivate = true)
    static final String PIPELINE_TYPE = "pipeline.type";
    
    @Property(unbounded= PropertyUnbounded.ARRAY,description="....")
    private static final String LINKS = "links";*/
    
    private boolean enabled;
    private ArrayList<String> links;
    private static final String PWC_365_CONFIGURATION_PID = "PwC 365 Configurations";
    private static final String PWC_365_DOMAIN = "pwc365.domain";

    @Reference
    private ConfigurationAdmin configurationAdmin;
    
    @Reference
    SlingRepository repository;
    private String defaultDomain;
    private String domainType;
    private String pwc365Domain;
    
    @ObjectClassDefinition(name = "PwC Link Transformer", description = "Rewrite certain links")
    @interface Config {
        @AttributeDefinition(name = "Enable Link Transformer", 
                            description = "Enable Link Transformer",
                            type = AttributeType.BOOLEAN)
        public boolean linktransformer_enabled();
        
        @AttributeDefinition(name = "Links", 
                description = "....",
                type = AttributeType.STRING)
        public String[] links();
    }

    @Activate
    protected void activate(LinkTransformerFactory.Config properties)
            throws RepositoryException {
        links = new ArrayList<>();
        
        try {	// TODO: Getting the configuration via Referenced configurationAdmin [Need to Check]
            /*BundleContext bundleContext = context.getBundleContext();
            ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin)bundleContext.getService(configAdminReference);*/
            Configuration config = configurationAdmin.getConfiguration("PwC Default Domain");
            this.defaultDomain = (String)config.getProperties().get("domain");
            this.domainType = (String)config.getProperties().get("domainType");
            //log.info("LinkTransformerFactory.activate  default-domain=" + defaultDomain);
            //log.info("LinkTransformerFactory.activate  forward-domain-type=" + domainType);
            //final Dictionary<?, ?> properties = context.getProperties();
            enabled = properties.linktransformer_enabled();
            //log.info("linktransformer.enabled  enabled=" + enabled);
            if(properties.links()!=null) {
                String[] prop = properties.links();
                links = new ArrayList<String>(Arrays.asList(prop));
            }
            this.pwc365Domain = getPwC365Domain();
        }
        catch(Exception ex){
            //log.error("LinkTransformerFactory.active", ex);
        }
    }


    @Override
    public Transformer createTransformer() {
        return new LinkTransformer(links, repository,enabled,defaultDomain,domainType, pwc365Domain);
    }
    
    private String getPwC365Domain() {
        String pwc365Domain = this.defaultDomain;
        try {
            final Configuration config = configurationAdmin.getConfiguration(PWC_365_CONFIGURATION_PID);
            if (config != null) {
                pwc365Domain = PropertiesUtil.toString(config.getProperties().get(PWC_365_DOMAIN), this.defaultDomain);
            }
        } catch (final IOException ioExcep) {
            log.error("Link Transformer Factory: Configurations can't be read for PID: " + PWC_365_CONFIGURATION_PID, ioExcep);
        }
        return pwc365Domain;
    }
}
