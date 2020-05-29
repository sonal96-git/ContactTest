package com.pwc.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Revision;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.workflow.api.WcmWorkflowService;
import com.day.cq.wcm.workflow.process.ActivatePageProcess;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.HistoryItem;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, name= "com.pwc.workflow.PwCProductionPublishInstanceChooserImpl")
@Designate(ocd = PwCProductionPublishInstanceChooserImpl.Config.class)
public class PwCProductionPublishInstanceChooserImpl extends ActivatePageProcess {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    ResourceResolverFactory rFact;
    
    @Reference
    AdminResourceResolver adminResourceResolver;

    @Reference
    Replicator replicator;

    private static ArrayList<String> prod =  new ArrayList<String>();
    private String process_label;
    
    @ObjectClassDefinition(name = "PwC Workflow Production Publish Instance Chooser", 
    		description = "Implementation of PwC dynamic production publish instance chooser depending on config")
    @interface Config {
        @AttributeDefinition(name = "Production Publish Agents", 
                            description = "....",
                            type = AttributeType.STRING)
        public String[] agents();
        
        @AttributeDefinition(name = "PwC Workflow Production Publish Instance Chooser", 
                description = "....",
                type = AttributeType.STRING)
        public String process_label();
    }

    @Override
    protected ReplicationOptions prepareOptions(ReplicationOptions opts) {

        opts = new ReplicationOptions();
        opts.setFilter(new AgentFilter(){


            @Override
			public boolean isIncluded(final Agent agent) {
                String id = agent.getId();
                boolean toPublish = false;
                for(String internalPublish:prod){
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
    protected void activate(PwCProductionPublishInstanceChooserImpl.Config properties)
            throws RepositoryException {
        // TODO Auto-generated method stub

        //final Dictionary<?, ?> properties = context.getProperties();
        String[] prop = properties.agents();
        prod = new ArrayList<String>(Arrays.asList(prop));
        process_label=properties.process_label();

    }
    //ddutta :Added for replicate as participant
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        Session  participantSession = null;
        ResourceResolver adminResolver = null;
        Session session = null;
        try {
            adminResolver =  rFact.getAdministrativeResourceResolver(null);
            session = adminResolver.adaptTo(Session.class);

            String user=workItem.getWorkflow().getInitiator();
            /*
            logger.info("inititaor:"+user);
            logger.info("user:"+workflowSession.getUser().getID());
            logger.info("current user:"+session.getUserID());
            */
            List<HistoryItem> history = workflowSession.getHistory(workItem.getWorkflow());
            if(history!=null){
                int size=history.size();
                if(size!=0)
                    size=size-1;
                if(history.get(size)!=null && history.get(size).getUserId()!=null)
                    user=history.get(size).getUserId();
            }

            participantSession= session.impersonate(new SimpleCredentials(user,"".toCharArray()));

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

                Node n=(Node) session.getItem(path);

                List<ResourceCollection> rcCollections = PwCWorkFlowUtil.getCollectionsForNode(n);//rcManager.getCollections(repository.loginAdministrative(null).getItem(path));//

                List<String> paths = PwCWorkFlowUtil.getPaths(path, rcCollections);

                for (String aPath : paths) {

                    if (canReplicate(participantSession, aPath)) {
                        if (opts != null) {

                            replicator.replicate(participantSession, getReplicationType(), aPath, opts);

                            //Replicate assets from property WorkFlowConstants.ASSETS_TO_ACTIVATE
                            activateAssetsAndClear(participantSession,getReplicationType(),aPath,opts,session);

//                          Fix for the importer publishing - PR1388 - Recompiling the assets
                            Resource res = getResourceResolver(session).getResource(aPath);
                            Node node = res.adaptTo(Node.class);
                            if(node != null && node.hasNode("jcr:content")){
                                Node jcrNode = node.getNode("jcr:content");
                                if(jcrNode.hasNode("contentPar/importer")){
                                    if(jcrNode.getNode("contentPar").getNode("importer").hasProperty("sling:resourceType") && jcrNode.getNode("contentPar").getNode("importer").getProperty("sling:resourceType").getString().equalsIgnoreCase("pwc/components/content/importer")){
                                        activateAssetsAndClear(participantSession,getReplicationType(),aPath,opts,session);
                                    }
                                }
                            }

                        } else {

                            replicator.replicate(participantSession, getReplicationType(), aPath);
                            //Replicate assets from property WorkFlowConstants.ASSETS_TO_ACTIVATE
                            activateAssetsAndClear(participantSession,getReplicationType(),aPath,opts,session);

//                          Fix for the importer publishing - PR1388 - Recompiling the assets
                            Resource res = getResourceResolver(session).getResource(aPath);
                            Node node = res.adaptTo(Node.class);
                            if(node != null && node.hasNode("jcr:content")){
                                Node jcrNode = node.getNode("jcr:content");
                                if(jcrNode.hasNode("contentPar/importer")){
                                    if(jcrNode.getNode("contentPar").getNode("importer").hasProperty("sling:resourceType") && jcrNode.getNode("contentPar").getNode("importer").getProperty("sling:resourceType").getString().equalsIgnoreCase("pwc/components/content/importer")){
                                        activateAssetsAndClear(participantSession,getReplicationType(),aPath,opts,session);
                                    }
                                }
                            }

                        }

                        storeLastPublishedVersionLabelinProperties(aPath,session);

                        session.save();
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

        } catch (RepositoryException repositoryException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.execute: RepositoryException: {} occurred while setting the node properties for payload: {}", repositoryException,  workItem.getWorkflowData().getPayload());
            throw new WorkflowException(repositoryException);
        } catch (ReplicationException replicationException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.execute: ReplicationException: {} occurred while replicating the node to production publish for payload: {}", replicationException,  workItem.getWorkflowData().getPayload());
            throw new WorkflowException(replicationException);
        } catch (LoginException loginException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.execute: LoginException: {} occurred while getting the admin resourceResolver for payload: {}", loginException, workItem.getWorkflowData().getPayload());
            throw new WorkflowException(loginException);
        } finally {
            if (participantSession != null && participantSession.isLive()) {
                participantSession.logout();
                participantSession = null;
            }
            if (session != null) {
                session.logout();
            }
            if (adminResolver != null) {
                adminResolver.close();
            }
        }
    }



    private void activateAssetsAndClear(Session participantSession,
                                        ReplicationActionType replicationType, String srcPage,
                                        ReplicationOptions opts,Session session)  {
        //Extract asset names from propertiies
        try{
            Node srcNodeContent = participantSession.getNode(srcPage+"/"+WorkFlowConstants.CONTENT_ELEMENT);

            javax.jcr.Property assetProperty = srcNodeContent.getProperty(WorkFlowConstants.ASSETS_TO_ACTIVATE);

            Value[] assets = assetProperty.getValues();

            for(Value a:assets){
                String asset = a.getString();

                if(opts!=null){

                    replicator.replicate(participantSession, replicationType, asset, opts);

                }else{

                    replicator.replicate(participantSession, replicationType, asset);


                }

            }


            session.save();

        }catch(Exception e){
            e.printStackTrace();
        }

    }



    private void storeLastPublishedVersionLabelinProperties(String srcPath,Session session) {


        try {
            Node sourceUrlNode = session.getNode(srcPath+"/"+WorkFlowConstants.CONTENT_ELEMENT);
            PageManager pageManager = getResourceResolver(session).adaptTo(PageManager.class);

            ArrayList<Revision> revisions = new ArrayList<Revision>(pageManager.getRevisions(srcPath, Calendar.getInstance()));

            if(!revisions.isEmpty()){
                String lastPublishedVersion = revisions.get(0).getLabel();
                String lastPublishedVersionId = revisions.get(0).getId();
                sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION, lastPublishedVersion);
                sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION_ID, lastPublishedVersionId);
                sourceUrlNode.setProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL, "In Publish");
                sourceUrlNode.setProperty(WorkFlowConstants.ACTIVATED_IN_PUBLISH, true);
            }else{
                sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION, "null");
                sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION_ID, "null");
                sourceUrlNode.setProperty(WorkFlowConstants.PREVIEW_STATUS_LABEL, "null");
            }
            //Fix for PR-3825: To remove unused reference from the page
            PwCWorkFlowUtil.removeProperty(session,sourceUrlNode,WorkFlowConstants.ASSETS_TO_ACTIVATE);
        } catch (PathNotFoundException pathNotFoundException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.storeLastPublishedVersionLabelinProperties: PathNotFoundException: {} occurred while fetching the node for path: {}", pathNotFoundException, srcPath);
        } catch (RepositoryException repositoryException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.storeLastPublishedVersionLabelinProperties: RepositoryException: {} occurred while setting the node properties for path: {}", repositoryException, srcPath);
        } catch (WCMException wcmException) {
            logger.error("PwCProductionPublishInstanceChooserImpl.storeLastPublishedVersionLabelinProperties: WCMException: {} occurred while setting the node properties for path: {}", wcmException, srcPath);
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
