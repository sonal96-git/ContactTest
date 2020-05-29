package com.pwc.wcm.services.impl;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.services.TerminateAuthorWorkflow;

/**
 * Provides list of workflow names which can be terminated by Author using SideKick.
 */
@Component(immediate = true, service = { TerminateAuthorWorkflow.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC Terminate Workflow Configuration" })
@Designate(ocd = TerminateAuthorWorkflowImpl.Config.class)
public class TerminateAuthorWorkflowImpl implements TerminateAuthorWorkflow {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /*BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    ServiceReference serviceRef = bundleContext.getServiceReference(TerminateAuthorWorkflow.class.getName());*/

    String[] workflowNames = {};
    
    @ObjectClassDefinition(name = "PwC Terminate Workflow Configuration", description = "")
    @interface Config {
        @AttributeDefinition(name = "WorkFlows which can be terminated by author.", 
                            description = "The configuration defines the path of workflows which can be terminated by Author using SideKick.",
                            type = AttributeType.STRING)
        public String[] terminableWorkflows();
    }
    
    @Activate
    protected void activate(TerminateAuthorWorkflowImpl.Config properties) {
    	workflowNames = properties.terminableWorkflows();
    }
    
    @Override
    public String[] getWorkflowNames() {
        log.info("workflows: "+workflowNames);
        return this.workflowNames;
    }
}
