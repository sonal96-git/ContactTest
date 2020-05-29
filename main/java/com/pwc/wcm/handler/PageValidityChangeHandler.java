package com.pwc.wcm.handler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import com.pwc.AdminResourceResolver;

/**
 * On change in Validity status (onTime or offTime reached) of a page(under /content/pwc/) or dam asset (under
 * /content/dam/pwc/), this handler service will purge the Akamai Cache for that resource.
 */
@Component(immediate = true, service = EventHandler.class,
		property = {
				EventConstants.EVENT_TOPIC + "=" + PageEvent.EVENT_TOPIC
		})
public class PageValidityChangeHandler implements EventHandler {
    private final Logger logger = LoggerFactory.getLogger(PageValidityChangeHandler.class);
    private static final String PWC_AKAMAI_PAGE_PURGE_WORKFLOW_MODEL = "/var/workflow/models/pwc-akamai-purge-page";
    private static final String PWC_AKAMAI_ASSET_PURGE_WORKFLOW_MODEL = "/var/workflow/models/pwc-akamai-purge-asset";
    private static final String AKAMAI_FLG = "akamai.enabled";
    private static final String PWC_AKAMAI_CACHE_PURGE_WORKFLOW_PROCESS_NAME = "com.pwc.workflow.PwCAkamaiPurgeCacheWorkflowImpl";
    private boolean akamaiFlag = false;

    private Set<String> currentRunModes;
    @Reference
    private SlingRepository repository;
    @Reference
    private AdminResourceResolver adminResourceResolver;
    @Reference
    private WorkflowService workflowService;
    @Reference
    private SlingSettingsService slingSettingsService;
    @Reference
    private ConfigurationAdmin configAdmin;
    
    @Activate
    protected void activate(final ComponentContext context) throws RepositoryException {
        currentRunModes = slingSettingsService.getRunModes();
        try {
            final Configuration config = configAdmin.getConfiguration(PWC_AKAMAI_CACHE_PURGE_WORKFLOW_PROCESS_NAME);
            if (config != null) {
                akamaiFlag = (Boolean) config.getProperties().get(AKAMAI_FLG);
            }
        } catch (final IOException ioExcep) {
            logger.error("Configurations can't be read for {}!", PWC_AKAMAI_CACHE_PURGE_WORKFLOW_PROCESS_NAME, ioExcep);
        }
    }
    
    /*
     * For 'PageValid' and 'PageInvalid' event on a page or dam asset, calls respective Akamai Cache Purging workflows.
     */
    @Override
    public void handleEvent(final Event event) {
        if (currentRunModes.contains("publish") && akamaiFlag) { // Current run mode is Publish and Akamai Flag is enabled
            final PageEvent pgEvent = PageEvent.fromEvent(event);
            final Iterator<PageModification> modifications = pgEvent.getModifications();
            while (modifications.hasNext()) {
                final PageModification modification = modifications.next();
                final ModificationType modType = modification.getType();
                if (modType.equals(PageModification.ModificationType.VALID)
                        || modType.equals(PageModification.ModificationType.INVALID)) {
                    final String resPath = modification.getPath();
                    purgeAkamaiCacheForPwcPagesAndAssets(modType, resPath);
                }
            }
        }
    }
    
    /**
     * Calls respective Akamai Cache Purging Workflows for PWC pages and dam assets.
     *
     * @param modType {@link ModificationType} specifies the type of event triggered
     * @param resPath {@link String} specifies path of the resource on which the event occurred
     */
    private void purgeAkamaiCacheForPwcPagesAndAssets(final ModificationType modType, final String resPath) {
        if (resPath.matches("\\/content(\\/dam)?\\/pwc\\/.*")) {
            final ResourceResolver adminResolver = adminResourceResolver.getAdminResourceResolver();
            if (adminResolver == null) {
                logger.error("Akamai cache purging failed! No system user configured!");
                return;
            }
            final Session session = adminResolver.adaptTo(Session.class);
            if (isPage(adminResolver, resPath)) {
                logger.info("Validation Status changed to '{}' for Page at '{}'. Purging Akamai Cache for it.", modType,
                        resPath);
                runCachePurgeWorkFlow(session, PWC_AKAMAI_PAGE_PURGE_WORKFLOW_MODEL, resPath);
            } else if (isDamAsset(adminResolver, resPath)) {
                logger.info("Validation Status changed to '{}' for Asset at '{}'. Purging Akamai Cache for it.",
                        modType, resPath);
                runCachePurgeWorkFlow(session, PWC_AKAMAI_ASSET_PURGE_WORKFLOW_MODEL, resPath);
            }
        }
    }
    
    /**
     * Returns true if a page exists at the given path.
     *
     * @param adminResolver {@link ResourceResolver} specifies a user with permissions to access the resource at the
     *            given path.
     * @param resPath {@link String} specifies path to check for an existing page
     * @return {@link Boolean} true if the page exists else false
     */
    private boolean isPage(final ResourceResolver adminResolver, final String resPath) {
        final PageManager pageManager = adminResolver.adaptTo(PageManager.class);
        return pageManager.getPage(resPath) != null;
    }
    
    /**
     * Returns true if a dam asset exists at the given path.
     *
     * @param adminResolver {@link ResourceResolver} specifies a user with permissions to access the resource at the
     *            given path.
     * @param resPath {@link String} specifies path to check for an existing asset
     * @return {@link Boolean} true if the asset exists else false
     */
    private boolean isDamAsset(final ResourceResolver adminResolver, final String resPath) {
        final Resource resource = adminResolver.resolve(resPath);
        return (resource != null) && DamUtil.isAsset(resource);
    }
    
    /**
     * Run the given Akamai cache purge workflow for given path as its payload.
     *
     * @param session {@link Session}
     * @param model {@link String}
     * @param resPath {@link String}
     */
    private void runCachePurgeWorkFlow(final Session session, final String model, final String resPath) {
        final WorkflowSession wfSession = workflowService.getWorkflowSession(session);
        try {
            final WorkflowModel wfModel = wfSession.getModel(model);
            final WorkflowData wfData = wfSession.newWorkflowData("JCR_PATH", resPath);
            wfSession.startWorkflow(wfModel, wfData);
        } catch (final WorkflowException wfExcep) {
            logger.error("Error starting workflow model {}", model, wfExcep);
        }
    }
    
}
