package com.pwc.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vimenon
 *
 */

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import com.day.cq.wcm.workflow.api.WcmWorkflowService;
import com.day.cq.wcm.workflow.process.ActivatePageProcess;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCInternalPublishInstanceChooserImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic publish instance chooser depending on configs",
    Constants.SERVICE_VENDOR + "= Adobe"
})
@Designate(ocd = PwCInternalPublishInstanceChooserImpl.Config.class)
public class PwCInternalPublishInstanceChooserImpl extends ActivatePageProcess {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String PROCESS_LABEL = "PwC Workflow Internal Publish Instance Chooser";

    @Reference
    ResourceResolverFactory rFact;
    
    @Reference
    AdminResourceResolver adminResourceResolver;
    
    @Reference
    Replicator replicator;

    @Reference(service = ReferenceProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private final List<ReferenceProvider> referenceProviders = new CopyOnWriteArrayList<ReferenceProvider>();

    private static ArrayList<String> internal =  new ArrayList<String>();
    
    @ObjectClassDefinition(name = "PwC Workflow Internal Publish Instance Chooser", 
    		description = "Configure internal publish server replication agent names")
    @interface Config {
        @AttributeDefinition(name = "Process Label",
                description = "....",
                type = AttributeType.STRING)
        String process_label() default PROCESS_LABEL;

        @AttributeDefinition(name = "Internal Publish Agents", 
                            description = "....",
                            type = AttributeType.STRING)
        String[] InternalPublishAgents();
    }

    @Override
    protected ReplicationOptions prepareOptions(ReplicationOptions opts) {


        opts = new ReplicationOptions();
        opts.setFilter(new AgentFilter(){


            @Override
			public boolean isIncluded(final Agent agent) {
                String id = agent.getId();
                boolean toPublish = false;
                for(String internalPublish:internal){
                    toPublish = id.equals(internalPublish);
                    if(toPublish){
                        return toPublish;
                    }
                }
                return toPublish;
            }


        });
        return opts;
    }


    @Activate
    protected void activate(PwCInternalPublishInstanceChooserImpl.Config properties)
            throws RepositoryException {
        //final Dictionary<?, ?> properties = context.getProperties();
        String[] prop = properties.InternalPublishAgents();
        internal = new ArrayList<String>(Arrays.asList(prop));

    }

    protected void bindReferenceProviders(ReferenceProvider referenceProvider) {

        referenceProviders.add(referenceProvider);
    }

    protected void unbindReferenceProviders(ReferenceProvider referenceProvider) {

        referenceProviders.remove(referenceProvider);
    }



    //ddutta :Added for replicate as participant
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        Session  participantSession = null;
        ResourceResolver adminResolver = null;
        Session session = null;
        try {
        	adminResolver = rFact.getAdministrativeResourceResolver(null);
            session = adminResolver.adaptTo(Session.class);
            synchronized(session){
                String user=workItem.getWorkflow().getInitiator();

                participantSession= session.impersonate(new SimpleCredentials(user,"".toCharArray()));

                WorkflowData data = workItem.getWorkflowData();
                String path = null;
                String type = data.getPayloadType();
                String payloadData = null;
                String isReplicated = null;
                if (type.equals(TYPE_JCR_PATH) && data.getPayload() != null) {
                    payloadData = (String) data.getPayload();
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

                    Node n=(Node) session.getItem(path);
                    if(n != null && n.hasNode("jcr:content")){
                        Node jcrNode = n.getNode("jcr:content");
                        if(jcrNode.hasProperty("cq:lastReplicated")){
                            isReplicated  = jcrNode.getProperty("cq:lastReplicated").getString();
                        }
                        //Fix for PR-3825: To remove unused reference from the page
                        PwCWorkFlowUtil.removeProperty(session,jcrNode,WorkFlowConstants.ASSETS_TO_ACTIVATE);
                    }

                    List<ResourceCollection> rcCollections = PwCWorkFlowUtil.getCollectionsForNode(n);//rcManager.getCollections(repository.loginAdministrative(null).getItem(path));//

                    List<String> paths = PwCWorkFlowUtil.getPaths(path, rcCollections);
                    String[] tempPaths = new String[1];

                    for (String aPath : paths) {
                        tempPaths[0] = aPath;
                        if (canReplicate(participantSession, aPath)) {


                            //get assets to be activated
                            List<String> assets = PwCWorkFlowUtil.searchForReferenceActivationStatus(tempPaths, session, null, getResourceResolver(session), referenceProviders, replicator);
                            if (opts != null) {

                                replicator.replicate(participantSession, getReplicationType(), aPath, opts);

                                //activate assets from search
                                for(String asset:assets){
                                    replicator.replicate(session,
                                            getReplicationType(), asset,opts);
                                    PwCWorkFlowUtil.registerProperty(session, payloadData, WorkFlowConstants.ASSETS_TO_ACTIVATE, asset);
                                }

//                         Fix for the importer publishing - PR1388 - Recompiling the assets
                                Resource res = getResourceResolver(session).getResource(aPath);
                                Node node = res.adaptTo(Node.class);
                                if(node != null && node.hasNode("jcr:content")){
                                    Node jcrNode = node.getNode("jcr:content");
                                    if(jcrNode.hasNode("contentPar/importer")){
                                        if(jcrNode.getNode("contentPar").getNode("importer").hasProperty("sling:resourceType") && jcrNode.getNode("contentPar").getNode("importer").getProperty("sling:resourceType").getString().equalsIgnoreCase("pwc/components/content/importer")){
                                            for(String asset:assets){
                                                replicator.replicate(session,
                                                        getReplicationType(), asset,opts);
                                                PwCWorkFlowUtil.registerProperty(session, payloadData, WorkFlowConstants.ASSETS_TO_ACTIVATE, asset);
                                            }
                                        }
                                    }
                                }

                                Object manual =  data.getMetaDataMap().get(WorkFlowConstants.SYNDICATION_DONE_MANUALLY);

                                //increment version if the workflow starts as a sub-workflow
                                if(manual!=null){

                                    PageManager pageManager = getResourceResolver(session).adaptTo(PageManager.class);

                                    PwCWorkFlowUtil.incrementVersion(aPath, pageManager, "Incrementing page version while launching the Syndication workflow for "+aPath);
                                }



                            } else {

                                replicator.replicate(participantSession, getReplicationType(), aPath);
                                //activate assets from search
                                for(String asset:assets){
                                    replicator.replicate(session,
                                            getReplicationType(), asset);
                                    PwCWorkFlowUtil.registerProperty(session, payloadData, WorkFlowConstants.ASSETS_TO_ACTIVATE, asset);

                                }

//                          Fix for the importer publishing - PR1388 - Recompiling the assets
                                Resource res = getResourceResolver(session).getResource(aPath);
                                Node node = res.adaptTo(Node.class);
                                if(node != null && node.hasNode("jcr:content")){
                                    Node jcrNode = node.getNode("jcr:content");
                                    if(jcrNode.hasNode("contentPar/importer")){
                                        if(jcrNode.getNode("contentPar").getNode("importer").hasProperty("sling:resourceType") && jcrNode.getNode("contentPar").getNode("importer").getProperty("sling:resourceType").getString().equalsIgnoreCase("pwc/components/content/importer")){
                                            for(String asset:assets){
                                                replicator.replicate(session,
                                                        getReplicationType(), asset,opts);
                                                PwCWorkFlowUtil.registerProperty(session, payloadData, WorkFlowConstants.ASSETS_TO_ACTIVATE, asset);
                                            }
                                        }
                                    }
                                }

                            }
                            //This section has been added for PR-1465
                            Node pageNode = (Node) session.getItem(aPath);
                            if(pageNode != null && pageNode.hasNode("jcr:content")){
                                Node jcrNode = pageNode.getNode("jcr:content");
                                if(jcrNode.hasProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL) || (isReplicated != null && !isReplicated.isEmpty())){

                                    if (jcrNode.hasProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL) && !jcrNode.getProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL).getString().equals("New-Preview")) {
                                        jcrNode.setProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL,"PrevPub-Preview");
                                    }else if(!jcrNode.hasProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL) && isReplicated != null && !isReplicated.isEmpty()){
                                        jcrNode.setProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL,"PrevPub-Preview");
                                    }
                                }else{
                                    jcrNode.setProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL,"New-Preview");
                                }

                                session.save();
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
            }

        } catch (RepositoryException repositoryException) {
            logger.error("PwCInternalPublishInstanceChooserImpl.execute: RepositoryException: {} occurred while setting the node properties for payload: {}", repositoryException,  workItem.getWorkflowData().getPayload());
            throw new WorkflowException(repositoryException);
        } catch (ReplicationException replicationException) {
            logger.error("PwCInternalPublishInstanceChooserImpl.execute: ReplicationException: {} occurred while replicating the node to internal publish for payload: {}", replicationException,  workItem.getWorkflowData().getPayload());
            throw new WorkflowException(replicationException);
        } catch (LoginException loginException) {
            logger.error("PwCInternalPublishInstanceChooserImpl.execute: LoginException: {} occurred while getting the admin resourceResolver for payload: {}", loginException, workItem.getWorkflowData().getPayload());
            throw new WorkflowException(loginException);
        } finally {
            /*if (participantSession != null && participantSession.isLive()) {
               // participantSession.logout();
                participantSession = null;
            }*/

            final WorkflowData data = workItem.getWorkflowData();

            Object manual = data.getMetaDataMap().get(WorkFlowConstants.SYNDICATION_DONE_MANUALLY);
            final WorkflowData wdata = workItem.getWorkflowData();
            logger.debug("About to set manual flag");
            if(manual == null) {
                logger.debug("Set manual to true");
                wdata.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
            }else{
                logger.debug("Set manual to false");
                wdata.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"false");
            }
            if (session != null) {
                session.logout();
            }
            if (adminResolver != null) {
                adminResolver.close();
            }

        }
    }


    private ResourceResolver getResourceResolver(Session session) {
        try {
            Map<String, Object> authInfo = new HashMap<String, Object>();
            authInfo.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
            return rFact.getResourceResolver(authInfo);
        } catch (Exception e) {
            logger.error("Failed to get ResourceResolver.", e);
        }
        return null;
    }

}
