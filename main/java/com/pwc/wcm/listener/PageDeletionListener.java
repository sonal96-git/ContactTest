package com.pwc.wcm.listener;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Preprocessor;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.pwc.wcm.services.AkaimaiPurge;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Created by Rui on 2016/11/28.
 */
@Component(immediate = true)
public class PageDeletionListener implements Preprocessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Reference
    private SlingRepository repository;
    @Reference
    private AkaimaiPurge akaimaiPurge;
    //@Reference
    //private AdminResourceResolver adminResourceResolver;

    ResourceResolver resourceResolver;
    Session session;
    private String defaultDomain;
    private String domainType;
    @Override
    public void preprocess(ReplicationAction action, ReplicationOptions replicationOptions) throws ReplicationException {
        try {
            ReplicationActionType actionType = action.getType();
            if (actionType.equals(ReplicationActionType.DELETE)) {
                LinkTransformerServiceImpl linkTransformer = new LinkTransformerServiceImpl(repository,defaultDomain,domainType);
                List<String> externalPages = new ArrayList<>();
                /*
                resourceResolver = getResourceResolver(session);
                Resource resource = resourceResolver.resolve(action.getPath());
                Page currentPage = resource.adaptTo(Page.class);

                Iterator<Page> pages = currentPage.listChildren(null, true);
                */
                String rootPage = linkTransformer.transformAEMUrl(action.getPath()) +".html";
                externalPages.add(rootPage);
                /*while (pages.hasNext()) {
                    Page childPage = pages.next();
                    String externalUrl = linkTransformer.transformAEMUrl(childPage.getPath()) +".html";
                    externalPages.add(externalUrl);
                }*/
                akaimaiPurge.purge(externalPages);
            }
        }catch(Exception ex){
            logger.error("preprocess " , ex);
        }
    }
    protected void activate(ComponentContext ctx) {
        try {
           // ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
           // session = resourceResolver.adaptTo(Session.class);
            BundleContext bundleContext = ctx.getBundleContext();
            ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminReference);
            Configuration config = configurationAdmin.getConfiguration("PwC Default Domain");
            defaultDomain = (String) config.getProperties().get("domain");
            domainType = (String) config.getProperties().get("domainType");
        }catch(Exception ex){
            logger.error("PageDeactivationListener", ex);
        }
    }
    /*
    private ResourceResolver getResourceResolver(Session session) throws org.apache.sling.api.resource.LoginException {
        return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                session));
    }*/
}
