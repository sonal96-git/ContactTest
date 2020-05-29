package com.pwc.workflow;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;

public class PwCAssetReferenceActivationUtil {

	
	private static final Logger log = LoggerFactory
			.getLogger(PwCAssetReferenceActivationUtil.class);
	
	

    public static void searchForReferenceActivationStatus(String[] paths,Session session,ReplicationOptions opts,  ResourceResolver resolver,List<ReferenceProvider> referenceProviders, Replicator replicator) throws ReplicationException
        {

       

      
   
       
        Set<Reference> allReferences = new TreeSet<Reference>(new Comparator<Reference>() {
            public int compare(Reference o1, Reference o2) {
                return o1.getResource().getPath().compareTo(o2.getResource().getPath());
            }
        });
        if (paths != null) {
            for (String path: paths) {
                if (path.length() > 0) {
                    Resource r = resolver.getResource(path + "/" + JcrConstants.JCR_CONTENT);
                    if (r == null) {
                        r = resolver.getResource(path);
                    }

                    if (r == null) {
                        continue;
                    }

                    for (ReferenceProvider referenceProvider : referenceProviders) {
                        allReferences.addAll(referenceProvider.findReferences(r));
                    }
                }
            }
        }

                  
            for (Reference reference : allReferences) {

                boolean published = false;
                boolean outdated = false;
                ReplicationStatus replStatus = null;
                final Resource resource = reference.getResource();
                boolean canReplicate = canReplicate(reference.getResource().getPath(), session);
                long lastPublished = 0;
                if (resource != null) {
                    replStatus = resource.adaptTo(ReplicationStatus.class);
                    if (replStatus != null) {
                        published = replStatus.isDelivered() || replStatus.isActivated();
                        if (published) {
                            lastPublished = replStatus.getLastPublished().getTimeInMillis();
                            outdated = lastPublished < reference.getLastModified();
                        }
                    }

                    log.debug("Considering reference at {} . Published: {}, outdated: {} ( lastPublished: {}, lastModified: {} )", new Object[] {
                            reference.getResource().getPath(), published, outdated , new Date(lastPublished), new Date(reference.getLastModified())});
                }

                if (!published || outdated) {
                   
                   // writer.key("status").value(outdated ? "outdated" : "not available");
                	if(opts==null){
                		replicator.replicate(session, ReplicationActionType.ACTIVATE, resource.getPath());
                	}else{
                		replicator.replicate(session, ReplicationActionType.ACTIVATE, resource.getPath());
                	}
                    
                }
            
            }
            

            
         
    }
    
    private static boolean canReplicate(String path, Session session) {
        try {
            AccessControlManager acMgr = session.getAccessControlManager();
            return session.getAccessControlManager().hasPrivileges(path, new Privilege[]{acMgr.privilegeFromName(Replicator.REPLICATE_PRIVILEGE)});
        } catch (RepositoryException e) {
            return false;
        }
    }
	
}
