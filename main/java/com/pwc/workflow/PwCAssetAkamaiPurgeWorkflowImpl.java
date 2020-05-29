package com.pwc.workflow;

import java.io.IOException;

import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.wcm.services.AkaimaiPurge;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Created by rjiang022 on 6/17/2015.
 */
@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCAssetAkamaiPurgeWorkflowImpl",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC Akamai asset cache purge",
    Constants.SERVICE_VENDOR + "= PwC",
   "process.label=" + "PwC Akamai Asset Purge"    
})
@Designate(ocd = PwCAssetAkamaiPurgeWorkflowImpl.Config.class)
public class PwCAssetAkamaiPurgeWorkflowImpl implements WorkflowProcess {
	
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    private static final String IMG_BYPASS_CONST = "?imbypass=true";

    private boolean enabled;
    private String defaultDomain;
    private String domainType = "";
    private int ccuDelay=30;
    Session session;
    
    @ObjectClassDefinition(name = "PwC Asset Akamai Purge Cache", description = "Trigger Akamai to purge the asset cache")
	@interface Config {
		@AttributeDefinition(name = "Process Label",
				description = "Enable Akamai Asset Purge",
				type = AttributeType.STRING)
		String process_label() default "PwC  Asset Akamai Purge";
	}
    
    @Reference
    private SlingRepository repository;
    
    @Reference
    ConfigurationAdmin configAdmin;
    
    @Reference
    private Replicator replicator;
    
    @Reference
    private AkaimaiPurge akamaiPurge;

    @Override
    public void execute(WorkItem item, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        try {
            populateConfigParameters();
            WorkflowData workflowData = item.getWorkflowData();
            if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
                if(enabled) {
                    String path = workflowData.getPayload().toString();
                    session = workflowSession.getSession();
                    LinkTransformerService service = new LinkTransformerServiceImpl(repository, defaultDomain, domainType);
                    String transformedUrl = service.transformAEMUrl(path,"");
                    JSONObject obj = new JSONObject();
                    obj.put("action","remove");
                    JSONArray urls =populatePurgePaths(transformedUrl);
                    obj.put("objects", urls);
                    int count =0;
                    try {
                        while (true) {
                            if(count > 2) break;
                            else {
                                if (session != null && replicator != null) {
                                    ReplicationStatus replicationStatus = replicator.getReplicationStatus(session, path);
                                    if (replicationStatus.isDelivered()) //switch to isDelivered(), isDelivered is to check if the last action, active or deactivate.
                                        break;
                                    else {
                                        count++;
                                        Thread.sleep(30 * 500); //if the page is not activated yet, then sleep 30 seconds for re-check

                                    }
                                } else {
                                }
                            }
                        }
                    }catch(Exception ex){

                    }
                    if (urls.length() > 0)  {
                    	akamaiPurge.purge(obj);
                    }

                }
            }
        }
        catch(Exception ex){
            logger.error("com.pwc.workflow.PwCAssetAkamaiPurgeWorkflowImpl.execute error", ex);
        }
        logger.info("Asset Akamai is done");
    }
    
    
    private JSONArray populatePurgePaths(String path) {
    	JSONArray jArr=new JSONArray();
    	jArr.put(path);
    	jArr.put(path+IMG_BYPASS_CONST);
		if(path.contains("/content/dam/pwc")) {
			String trimPath=path.replace("/content/dam/pwc", "");
			jArr.put(trimPath);
			jArr.put(trimPath+IMG_BYPASS_CONST);
		}
		return jArr;
	}


	/**
     * Populates the required Configuration parameters
     * 
     * @throws IOException
     */
    private void populateConfigParameters() throws IOException {
          Configuration config = configAdmin.getConfiguration("PwC Default Domain");
          defaultDomain = (String)config.getProperties().get("domain");
          domainType = (String)config.getProperties().get("domainType");
          Configuration akamaiConifg = configAdmin.getConfiguration("com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl");
          this.enabled = (Boolean)akamaiConifg.getProperties().get("akamai.enabled");
          this.ccuDelay=Integer.parseInt(akamaiConifg.getProperties().get("akamai.ccudelay").toString());
	}

}
