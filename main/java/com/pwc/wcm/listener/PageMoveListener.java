package com.pwc.wcm.listener;

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
import com.pwc.wcm.services.PageCreationListener;

/**
 * Created by Rui on 2016/12/12.
 */
@Component(immediate = true, service = EventListener.class)
public class PageMoveListener implements EventListener {
    private final Logger logger = LoggerFactory.getLogger(PageCreationListener.class);

    @Reference
    private SlingRepository repository;
    @Reference
    private AdminResourceResolver adminResourceResolver;

    private Session session;
    private ObservationManager observationManager;
    private ResourceResolver resourceResolver1 = null;
    protected void activate(ComponentContext context) throws Exception {
        String[] nodetypes = {"cq:Page"};
        resourceResolver1 = adminResourceResolver.getAdminResourceResolver();
        session = resourceResolver1.adaptTo(Session.class);
        observationManager = session.getWorkspace().getObservationManager();

        observationManager.addEventListener(
                this, //handler
                Event.NODE_MOVED, //binary combination of event types
                "/content/pwc", //path
                true, //is Deep?
                null, //uuids filter
                null, //nodetype filter
                false
        );
        logger.info("Added Page Move Listener");
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            if (observationManager != null) {
                observationManager.removeEventListener(this);
                logger.info("Removed Page Move Listener");
            }
        } catch (RepositoryException re) {
            logger.error("Error removing page move listener", re);
        } finally {
            if (resourceResolver1 != null) {
                resourceResolver1.close();
            }
        }
    }

    @Override
	public void onEvent(EventIterator eventIterator) {
        try {
            while(eventIterator.hasNext()){
                //logger.info("---------------" + newEvent.getPath() + "-----------------");
            }
            session.save();
            //LOGGER.info("Node is added");
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return;
    }
}
