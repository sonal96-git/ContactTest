package com.pwc.wcm.services;

import com.day.cq.replication.ReplicationStatus;

/**
 * Created by rjiang022 on 12/2/2015.
 *  To Call it in JSP
 *  ReplicationQueueService replicationQueueService = sling.getService(ReplicationQueueService.class);
    ReplicationStatus status = replicationQueueService.getReplicationStatus(currentPage.getPath());
 */
public interface ReplicationQueueService {
    public ReplicationStatus getReplicationStatus(String nodePath);
}
