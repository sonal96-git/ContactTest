package com.pwc.wcm.services.impl;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.services.ReplicationQueueService;

/**
 * Created by rjiang022 on 12/2/2015.
 */
/*@Component(label = "ReplicationQueueService", immediate = true, metatype = false)
@Properties(
        @Property(name = "replicationQueueService", value="Replication Service")
)
@Service(value=ReplicationQueueService.class)*/
@Component(immediate = true, service = { ReplicationQueueService.class }, enabled = true, 
property = {
		"replicationQueueService=" + "Replication Service"
})
public class ReplicationQueueServiceImp implements ReplicationQueueService {
    
	@Reference
    private Replicator replicator;
    
	@Reference
    private ResourceResolverFactory resourceResolverFactory;
    
	@Reference
    private AdminResourceResolver adminResourceResolver;

    @Override
    public ReplicationStatus getReplicationStatus(String nodePath) {
        ReplicationStatus status = null;
        try {
            ResourceResolver resolver = adminResourceResolver.getAdminResourceResolver();
            Session session = resolver.adaptTo(Session.class);
             status = replicator.getReplicationStatus(session, nodePath);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }
}
