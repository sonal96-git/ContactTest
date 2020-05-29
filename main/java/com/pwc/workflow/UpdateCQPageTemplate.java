/*
 * 
 */
package com.pwc.workflow;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.UpdateCQPageTemplate",
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC Change Template WF",
    Constants.SERVICE_VENDOR + "= PwC",
   "process.label=" + "PwC Workflow to change template of page"    
})
@Designate(ocd = UpdateCQPageTemplate.Config.class)
public class UpdateCQPageTemplate implements WorkflowProcess {
	Logger logger = LoggerFactory.getLogger(UpdateCQPageTemplate.class);

	@Reference
	private AdminResourceResolver adminResourceResolver;
	
	private List<String> templateList;
	
	@ObjectClassDefinition(name = "PwC Change Template Workflow", description = "PwC Change the template of page")
    @interface Config {
        @AttributeDefinition(name = "Template List", 
                            description = "Description for process.label",
                            type = AttributeType.STRING)
        public String[] templateList();

		@AttributeDefinition(name = "process.label",
				description = "Implementation of PwC dynamic production publish instance chooser depending on config",
				type = AttributeType.STRING)
		String process_label() default "PwC Workflow to change template of page";
    }
	
	@Override
	public void execute(final WorkItem workItem, final WorkflowSession session, final MetaDataMap args) throws WorkflowException {
		String cqTemplate = null;
		String slingResourceType = null;
		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		path = data.getPayload().toString();
		logger.info("path is " + path);
		logger.info("path is ------------ " + path);
		try {
			final ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
			logger.info("resorceResolver " + resourceResolver);
			final Resource resource = resourceResolver.getResource(path + "/jcr:content");
			if (resource != null) {
				logger.info("Resource is ::: " + resource);
				// this.logger.info("resource.getPath() " + resource.getPath());
				final Node contentNode = resource.adaptTo(Node.class);
				logger.info(" === " + contentNode.hasProperty("templatepath"));
				if ((contentNode != null) && (contentNode.hasProperty("templatepath"))) {
					cqTemplate = contentNode.getProperty("templatepath").getString();
				}
				this.logger.info("cqTemplate ::: " + cqTemplate);
				final Resource resourceTemplate = resourceResolver.getResource(cqTemplate + "/jcr:content");

				if (resourceTemplate != null) {
					final Node templateNode = resourceTemplate.adaptTo(Node.class);
					if ((templateNode != null) && (templateNode.hasProperty("sling:resourceType"))) {
						this.logger.info("sling:resourceType :: " + templateNode.getProperty("sling:resourceType").getString());
						slingResourceType = templateNode.getProperty("sling:resourceType").getString();
					}
				}
				if ((cqTemplate != null) && (slingResourceType != null)) {
					this.logger.info("cq:template ::: " + cqTemplate + "slingResourceType :::: " + slingResourceType);
					final Session jcrSession = contentNode.getSession();
					contentNode.getProperty("cq:template").setValue(cqTemplate);
					contentNode.getProperty("sling:resourceType").setValue(slingResourceType);
					if(templateList.contains(cqTemplate))
						contentNode.setProperty("colorsEnabled","true");
					jcrSession.save();
				}
			}
		} catch (final Exception e) {
			this.logger.error("Error in Change Template :: " + e);
		}
	}
	
	@Activate
	protected void activate(UpdateCQPageTemplate.Config properties) {
		templateList =  Arrays.asList(properties.templateList());
	}
}

