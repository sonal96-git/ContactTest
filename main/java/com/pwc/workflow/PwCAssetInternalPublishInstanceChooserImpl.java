package com.pwc.workflow;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.day.cq.dam.api.Asset;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.workflow.api.WcmWorkflowService;
import com.day.cq.wcm.workflow.process.ActivatePageProcess;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.google.common.base.Strings;
import com.pwc.AdminResourceResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCAssetInternalPublishInstanceChooserImpl",
        property = {
                Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic asset internal publish instance chooser depending on configs",
                Constants.SERVICE_VENDOR + "= PwC",
                "process.label=" + "PwC Workflow Asset Internal Publish Instance Chooser"
        })
public class PwCAssetInternalPublishInstanceChooserImpl extends ActivatePageProcess {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ArrayList<String> internal = new ArrayList<String>();

    @Reference
    ResourceResolverFactory rrf;

    @Reference
    AdminResourceResolver adminResourceResolver;

    @Reference
    Replicator replicator;

    @Override
    protected ReplicationOptions prepareOptions(ReplicationOptions opts) {
        opts = new ReplicationOptions();
        opts.setFilter(new AgentFilter() {


            @Override
            public boolean isIncluded(final Agent agent) {
                String id = agent.getId();
                boolean toPublish = false;
                for (String internalPublish : internal) {
                    toPublish = id.equals(internalPublish);
                    if (toPublish) {
                        return toPublish;
                    }
                }
                return toPublish;
            }


        });
        return opts;
    }

    protected void activate(ComponentContext context)
            throws RepositoryException {
        try {

            BundleContext bundleContext = context.getBundleContext();
            ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminReference);
            Configuration config = configurationAdmin.getConfiguration("com.pwc.workflow.PwCInternalPublishInstanceChooserImpl");

            //final Dictionary<?, ?> properties = context.getProperties();
            String[] prop = (String[]) config.getProperties().get("InternalPublishAgents");
            internal = new ArrayList<String>(Arrays.asList(prop));
        } catch (Exception ex) {
            logger.error("com.pwc.workflow.PwCAssetInternalPublishInstanceChooserImpl.Activate " + ex.getMessage());
        }

    }


    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        Session session = null;
        ResourceResolver adminResolver = null;
        String assetTitle = null;
        String assetType = "";
        Resource res = adminResourceResolver.getAdminResourceResolver().getResource(workItem.getWorkflowData().getPayload().toString());
        if (res != null) {
            Asset asset = res.adaptTo(Asset.class);
            assetTitle = asset.getMetadataValue("dc:title");
            assetType = asset.getMimeType();
        }
        if(assetType.contains("image/")) {
            if (Strings.isNullOrEmpty(assetTitle) || (StringUtils.isNotEmpty(assetTitle) && !assetTitle.trim().matches(".*\\s.*"))) {
                workItem.getWorkflowData().getMetaDataMap().put("startComment", "Asset Title should contain more than one word");
                workflowSession.suspendWorkflow(workItem.getWorkflow());
                throw new WorkflowException("Asset Title should be more than one word");

            }
        }

        try {

            //Session session = workflowSession.getSession();
            //String user=workItem.getWorkflow().getInitiator();
            adminResolver = adminResourceResolver.getAdminResourceResolver();
            session = adminResolver.adaptTo(Session.class);

            WorkflowData data = workItem.getWorkflowData();
            String path = null;
            String type = data.getPayloadType();

            if (type.equals(TYPE_JCR_PATH) && data.getPayload() != null) {
                String payloadData = (String) data.getPayload();
                if (session.itemExists(payloadData)) {
                    path = payloadData;
                }
            } else if (data.getPayload() != null && type.equals(TYPE_JCR_UUID)) {
                Node node = session.getNodeByIdentifier((String) data.getPayload());
                path = node.getPath();
            }

            ReplicationOptions opts = null;

            String rev = data.getMetaDataMap().get("resourceVersion", String.class);
            if (rev != null) {
                opts = new ReplicationOptions();
                opts.setRevision(rev);
            }

            opts = prepareOptions(opts);


            if (path != null) {

                Node n = (Node) session.getItem(path);

                List<ResourceCollection> rcCollections = PwCWorkFlowUtil.getCollectionsForNode(n);//rcManager.getCollections(repository.loginAdministrative(null).getItem(path));//

                List<String> paths = PwCWorkFlowUtil.getPaths(path, rcCollections);

                for (String aPath : paths) {
                    if (canReplicate(session, aPath)) {
                        workItem.getWorkflowData().getMetaDataMap().put("startComment", "");
                        if (opts != null) {

                            replicator.replicate(session, getReplicationType(), aPath, opts);
                        } else {

                            replicator.replicate(session, getReplicationType(), aPath);
                        }
                    } else {
                        // request for "replication action"
                        logger.info(session.getUserID() + " is not allowed to replicate " + "this page/asset " + aPath
                                + ". Issuing request for 'replication");
                        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
                        properties.put("path", aPath);
                        properties.put("replicationType", getReplicationType());
                        properties.put("userId", session.getUserID());
                        Event event = new Event(WcmWorkflowService.EVENT_TOPIC, properties);
                        eventAdmin.sendEvent(event);
                    }
                }
            } else {
                logger.warn("Cannot activate page or asset because path is null for this " + "workitem: "
                        + workItem.toString());
            }

        } catch (Exception e) {
            logger.error("PwCAssetInternalPublishInstanceChooserImpl", e);
        } finally {
            if (session != null) {
                session.logout();
            }
            if (adminResolver != null) {
                adminResolver.close();
            }
        }
    }

    @Deactivate
    protected void deactivate(final Map<String, Object> properties) {

    }
}
