package com.pwc.wcm.services.impl;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.services.PwCStartWorkflow;

@Component(immediate = true, service = { PwCStartWorkflow.class }, enabled = true)
public class StartWorkflowImpl implements PwCStartWorkflow  {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Reference
    private WorkflowService workflowService;

    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    @Reference
    private SlingRepository repository;
    
    protected void bindRepository(SlingRepository repository) {
    	    this.repository = repository;
    }
    		
   protected void unbindRepository(SlingRepository repository) {
    	    this.repository = null;
   }
    
    @Override
	public  void process(String workflowModelPath,String payloadPath) throws Exception {
        
        if(workflowModelPath == null) {
            log.debug("No workflow model path supplied, not starting workflow");
        } else {
            // Start workflow on supplied content
            // Using admin session as user might not have rights to do that
            log.info("Starting workflow {} on path {}", workflowModelPath, payloadPath);
            ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
            final Session s =resourceResolver.adaptTo(Session.class);
            try {
                final WorkflowSession wfs = workflowService.getWorkflowSession(s);
                final WorkflowModel model = wfs.getModel(workflowModelPath);
                if(model == null) {
                    throw new WorkflowException("Workflow Model with ID '" + workflowModelPath + "' not found");
                }
                
                final WorkflowData data = wfs.newWorkflowData("JCR_PATH", payloadPath);
                
                wfs.startWorkflow(model, data);
            }finally {
                s.logout();
            }
        }
    }
    
   
}