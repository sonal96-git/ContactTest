package com.pwc.wcm.services.impl;

import java.util.Dictionary;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.wcm.services.TerritoryPrivacyPolicy;

/**
 * Created by jiang on 4/12/2017.
 */
@Component(immediate = true, service = { TerritoryPrivacyPolicy.class }, enabled = true)
public class TerritoryPrivacyPolicyImpl implements TerritoryPrivacyPolicy {
    private static final Logger log = LoggerFactory.getLogger(TerritoryPrivacyPolicyImpl.class);

    @Reference
    private AdminResourceResolver adminResourceResolver;

    private String policyPath;


    @Override
    public boolean hasPolicy(String pagePath) {

        return false;
    }

    @Override
    public String getTerritoryPolicyVersion(String territory, boolean isMicrosite, String micrositeName) {
        String version = null;
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = adminResourceResolver.getAdminResourceResolver();
            String ref_path = "/content/pwc/global/referencedata/territories/" + territory;
            if (isMicrosite) {
                ref_path = ref_path + "/website/" + micrositeName;
            }
            Resource resource = resourceResolver.getResource(ref_path);
            if (resource != null) {
                ValueMap valueMap = resource.adaptTo(ValueMap.class);
                version = valueMap.get("privacypolicy-version", null);
            }
        }catch (Exception ex){
            log.error(this.getClass().toString(), ex);
        }finally {
            if(resourceResolver!=null)
                resourceResolver.close();
        }
        return version;
    }

    @Override
    public String getDefaultPrivatePolicyPagePath() {
        return this.policyPath;
    }

    @Activate
    protected void activate(ComponentContext context) throws Exception{
        final Dictionary<?, ?> properties = context.getProperties();
        BundleContext bundleContext = context.getBundleContext();
        ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminReference);
        Configuration config = configurationAdmin.getConfiguration("PwC Privacy Policy");
        policyPath = (String) config.getProperties().get("policy.page.path");
    }
}
