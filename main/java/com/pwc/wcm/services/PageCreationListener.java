package com.pwc.wcm.services;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.workflow.WorkFlowConstants;

@Component(service = EventListener.class)
public class PageCreationListener implements EventListener {
    private final Logger LOGGER = LoggerFactory.getLogger(PageCreationListener.class);

    @Reference
    private SlingRepository repository;
    @Reference
    private AdminResourceResolver adminResourceResolver;

    private Session session;
    private ObservationManager observationManager;
    ResourceResolver resourceResolver = null;
    protected void activate(ComponentContext context) throws Exception {
        resourceResolver = adminResourceResolver.getAdminResourceResolver();
        String[] nodetypes = {"cq:Page"};
        session = resourceResolver.adaptTo(Session.class);
        observationManager = session.getWorkspace().getObservationManager();

        observationManager.addEventListener(
                this, //handler
                Event.NODE_ADDED, //binary combination of event types
                "/content/pwc", //path
                true, //is Deep?
                null, //uuids filter
                null, //nodetype filter
                false
            );
        LOGGER.info("Added Page Creation Listener");

    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            if (observationManager != null) {
                observationManager.removeEventListener(this);
                LOGGER.info("Removed Page Creation Listener");
            }
        } catch (RepositoryException re) {
            LOGGER.error("Error removing page creation listener", re);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    @Override
	public void onEvent(EventIterator eventIterator) {
        try {
            while(eventIterator.hasNext()){
                Event newEvent = eventIterator.nextEvent();
                if(session.nodeExists(newEvent.getPath())&&session.getNode(newEvent.getPath()).getProperty("jcr:primaryType").getString().equals("cq:PageContent")){
                    Node contentNode = session.getNode(newEvent.getPath());
                    if(contentNode.getProperty("jcr:createdBy")!=null){
                        contentNode.setProperty(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,"Open");
                        contentNode.setProperty(WorkFlowConstants.SYNC_ELEMENT,"false");
                    }
                }
            }
            session.save();
            //LOGGER.info("Node is added");
        }catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return;
    }
}