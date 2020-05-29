package com.pwc.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
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

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCProductionAssetPublishInstanceChooserImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic production publish instance chooser depending on config",
    Constants.SERVICE_VENDOR + "= PwC",    
   "process.label=" + "PwC Asset Workflow Production Publish Instance Chooser"    
})
@Designate(ocd = PwCProductionAssetPublishInstanceChooserImpl.Config.class)
public class PwCProductionAssetPublishInstanceChooserImpl extends ActivatePageProcess {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> prod =  new ArrayList<>();
    
    @ObjectClassDefinition(name = "PwC Asset Workflow Production Publish Instance Chooser", description = "Implementation of PwC dynamic production publish instance chooser depending on config")
	@interface Config {
		@AttributeDefinition(name = "Process Label",
				description = "Implementation of PwC dynamic production publish instance chooser depending on config",
				type = AttributeType.STRING)
		String process_label() default "PwC Asset Workflow Production Publish Instance Chooser";
	}

    @Reference
    AdminResourceResolver adminResourceResolver;
    
    @Reference
    Replicator replicator;
    
    @Reference
    ConfigurationAdmin configAdmin;

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

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) throws WorkflowException {
        Session session=null;
        ResourceResolver adminResolver = null;

        try {
            String user=workItem.getWorkflow().getInitiator();
            List<HistoryItem> history = workflowSession.getHistory(workItem.getWorkflow());
            if(history!=null){
                int size=history.size();
                if(size!=0)
                    size=size-1;
                if(history.get(size)!=null && history.get(size).getUserId()!=null)
                    user=history.get(size).getUserId();
            }
            adminResolver = adminResourceResolver.getAdminResourceResolver();
            session= adminResolver.adaptTo(Session.class);
            WorkflowData data = workItem.getWorkflowData();
            String path = null;
            String type = data.getPayloadType();
            Configuration config = configAdmin.getConfiguration("com.pwc.workflow.PwCProductionPublishInstanceChooserImpl");
            String[] prop = (String[]) config.getProperties().get("agents");
            prod = new ArrayList<>(Arrays.asList(prop));

            if (type.equals(TYPE_JCR_PATH) && data.getPayload() != null) {
                String payloadData = (String) data.getPayload();
                if (session.itemExists(payloadData)) {
                    path = payloadData;
                	}
	         } else if (data.getPayload() != null && type.equals(TYPE_JCR_UUID)) {
	                Node node = session.getNodeByIdentifier((String) data.getPayload());
	                path = node.getPath();
	          }
            
            if (path != null && !prod.isEmpty()) {
	            ReplicationOptions opts = null;
	            String rev = data.getMetaDataMap().get("resourceVersion", String.class);
	            if (rev != null) {
	                opts = new ReplicationOptions();
	                opts.setRevision(rev);
	            }
	            opts = prepareOptions(opts);
          
                Node n=(Node) session.getItem(path);
                List<ResourceCollection> rcCollections = PwCWorkFlowUtil.getCollectionsForNode(n);
                List<String> paths = PwCWorkFlowUtil.getPaths(path, rcCollections);
                for (String aPath : paths) {
                    if (canReplicate(session, aPath)) {
                        if (opts != null) {
                            replicator.replicate(session, getReplicationType(), aPath, opts);
                        } else {
                            replicator.replicate(session, getReplicationType(), aPath);
                        }
                        session.save();
                    } else {
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
                logger.warn("Cannot activate page or asset because path is null for this workitem:{}",workItem.toString());
            }

        } catch (Exception e) {
            logger.error("PwCProductionAssetPublishInstanceChooserImpl", e);
        }  finally {
            if (session != null) {
                session.logout();
                session = null;
            }
            if (adminResolver != null) {
                adminResolver.close();
            }
        }
    }
}