package com.pwc.wcm.listener;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.pwc.AdminResourceResolver;
import com.pwc.workflow.WorkFlowConstants;

/**
 * A Service Class which implements EventHandler and handles deactivation event of a page.
 */
@Component(
	    immediate = true,
	    service = EventHandler.class,
	    property = {
	        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC,
	    })
@Designate(ocd = PageDeactivationListener.Config.class)
public class PageDeactivationListener implements EventHandler {
        private Logger logger = LoggerFactory.getLogger(this.getClass());
        @Reference private AdminResourceResolver adminResourceResolver;

        ResourceResolver resourceResolver = null;
        private String rootPath;
        
        @ObjectClassDefinition(name = "PwC Page Deactivation Listener", description = "")
        @interface Config {
            @AttributeDefinition(name = "Domain Root Path", 
                                description = "root path from where activate method start searching pages for which deactivation is to be handled",
                                type = AttributeType.STRING)
            public String rootPath() default "/content/pwc";
        }
        
        /**
         * This method gets called by api to activate the service in osgi.
         *
         * @param componentContext {@link ComponentContext}
         */
        @Activate protected void activate(PageDeactivationListener.Config properties) {
                // final Dictionary<?, ?> properties = componentContext.getProperties();
                rootPath = properties.rootPath();
        }

        /**
         * This method is called by the EventAdmin service to notify the listener of an event and sets the "activatedInPublish" property of page to "false" for
         * deactivated page.
         *
         * @param event {@link Event} particular event that have been occurred..
         */
        @Override public void handleEvent(Event event) {
                ReplicationAction replicationAction = ReplicationAction.fromEvent(event);
                String pagePath = replicationAction.getPath();
                if (pagePath.startsWith(rootPath) && (ReplicationActionType.DEACTIVATE.toString()
                                                                                      .equals(replicationAction.getType().toString()))) {
                        Node sourceNode = null;
                        try {
                                resourceResolver = adminResourceResolver.getAdminResourceResolver();
                                sourceNode = (resourceResolver != null) ?
                                        resourceResolver.adaptTo(Session.class).getNode(pagePath + "/" + JcrConstants.JCR_CONTENT) :
                                        null;
                                if (null != sourceNode) {
                                        sourceNode.setProperty(WorkFlowConstants.ACTIVATED_IN_PUBLISH, false);
                                        resourceResolver.commit();
                                }
                        } catch (PathNotFoundException pathNotFoundException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: PathNotFoundException {} occurred while updating the property of deactivated  page for path : {} ",
                                        pathNotFoundException, pagePath);
                        } catch (ValueFormatException valueFormatException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: ValueFormatException {} occurred while updating the property of deactivated page for path : {}",
                                        valueFormatException, pagePath);
                        } catch (VersionException versionException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: VersionException {} occurred while updating the property of deactivated page for path : {}",
                                        versionException, pagePath);
                        } catch (ConstraintViolationException constraintViolationException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: ConstraintViolationException {} occurred while updating the property of deactivated page for path : {}",
                                        constraintViolationException, pagePath);
                        } catch (LockException lockException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: LockException {} occurred while updating the property of deactivated page for path : {}",
                                        lockException, pagePath);
                        } catch (RepositoryException repositoryException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: RepositoryException {} occurred while updating the property of deactivated page for path : {}",
                                        repositoryException, pagePath);
                        } catch (PersistenceException persistenceException) {
                                logger.error(
                                        "PageDeactivationListener.handleEvent: PersistenceException {} occurred while updating the property of deactivated page for path : {}",
                                        persistenceException, pagePath);
                        } finally {
                                if (resourceResolver != null)
                                        resourceResolver.close();
                        }
                }
        }
}
