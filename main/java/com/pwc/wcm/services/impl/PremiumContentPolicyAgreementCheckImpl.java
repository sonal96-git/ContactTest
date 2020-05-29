package com.pwc.wcm.services.impl;

import java.util.Dictionary;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONObject;
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

import com.day.cq.wcm.api.Page;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.model.PageProperty;
import com.pwc.wcm.services.PremiumContentPolicyAgreementCheck;
import com.pwc.wcm.services.TerritoryPrivacyPolicy;
import com.pwc.wcm.utils.HttpUtils;
import com.pwc.wcm.utils.PathUtil;
import com.pwc.wcm.utils.UrlSecurity;

/**
 * Created by rjiang on 2017-04-11.
 */
@Component(immediate = true, service = { PremiumContentPolicyAgreementCheck.class }, enabled = true)
public class PremiumContentPolicyAgreementCheckImpl implements PremiumContentPolicyAgreementCheck{
    
	private static final Logger log = LoggerFactory.getLogger(PremiumContentPolicyAgreementCheckImpl.class);
    
	@Reference
    private AdminResourceResolver adminResourceResolver;
    
	@Reference
    private TerritoryPrivacyPolicy territoryPrivacyPolicy;
    private String service_url;


    @Override
    public boolean redirectToAgreement(String userID, Page page) {
        ResourceResolver resourceResolver = null;
        boolean redirectToAgreement = true;
        try {
            resourceResolver = adminResourceResolver.getAdminResourceResolver();
            String path = page.getPath();
            PageProperty pageProperty = PathUtil.getPageProperty(path);

            String micrositeName = null;
            if (pageProperty.isMicrosite())
                micrositeName = pageProperty.getMicrositeName();
            boolean isMicrosite = pageProperty.isMicrosite();
            String territory = pageProperty.getTerritory();
            String service_url_getUser = service_url + "/getUser/" + userID;
            JSONObject jsonObject = HttpUtils.getGETResponse(service_url_getUser);
            if (jsonObject != null) {
                if (jsonObject.has("SamAccountName") && jsonObject.has("EmailAddress")) {
                    String aem_userId = jsonObject.getString("EmailAddress");
                    Session session = resourceResolver.adaptTo(Session.class);
                    UserManager userManager = ((JackrabbitSession) session).getUserManager();
                    Authorizable auth = getAEMUserByEmail(aem_userId, session);
                    if (auth != null) {
                        Node user_node = session.getNode(auth.getPath());
                        String user_territory = user_node.getPath() + "/preferences/dpe_privacypolicy/" + pageProperty.getTerritory();
                        Resource user_territory_res = resourceResolver.getResource(user_territory);
                        if (user_territory_res != null) {
                            ValueMap user_map = user_territory_res.adaptTo(ValueMap.class);
                            String user_version = user_map.get("version-accepted", null);
                            String version = territoryPrivacyPolicy.getTerritoryPolicyVersion(territory, isMicrosite, micrositeName);
                            redirectToAgreement = !user_version.equalsIgnoreCase(version);
                        }
                    }
                }
            }
        }catch(Exception ex){
            log.error("com.pwc.wcm.services.impl.PremiumContentPolicyAgreementCheckImpl.redirectToAgreement", ex);
        }finally {
            if(resourceResolver!=null)
                resourceResolver.close();
        }
         return redirectToAgreement;
    }

    public Authorizable getAEMUserByEmail(String email, Session session) {
        Authorizable user = null;
        try {
            UserManager userManager = ((JackrabbitSession) session).getUserManager();
            Iterator<Authorizable> users = userManager.findAuthorizables(new org.apache.jackrabbit.api.security.user.Query() { // 6.2
                @Override
				public <T> void build(org.apache.jackrabbit.api.security.user.QueryBuilder<T> builder) {
                    try {
                        builder.setCondition(builder.eq("profile/email", session.getValueFactory().createValue(email)));
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                }
            });
            if(users.hasNext()){
                user = users.next();
            }
        }catch(Exception ex){

        }
        return user;
    }


    @Activate
    protected void activate(ComponentContext context) throws Exception{
        final Dictionary<?, ?> properties = context.getProperties();
        BundleContext bundleContext = context.getBundleContext();
        ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminReference);
        Configuration config = configurationAdmin.getConfiguration("com.pwc.userreg.wcm.services.impl.RestServiceImpl");
        service_url = UrlSecurity.decode((String) config.getProperties().get("baseURL"));
    }
}
