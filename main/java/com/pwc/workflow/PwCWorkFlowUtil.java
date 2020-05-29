package com.pwc.workflow;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Revision;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;
import java.security.Principal;
import java.util.*;

public class PwCWorkFlowUtil {

	private static final Logger log = LoggerFactory
			.getLogger(PwCWorkFlowUtil.class);
	private static final Random rand = new Random();
	public static  List<String> searchForReferenceActivationStatus(String[] paths,
			Session session, ReplicationOptions opts,ResourceResolver resolver,List<ReferenceProvider> referenceProviders,Replicator replicator)
			throws ReplicationException {
		
		List<String> references =  new ArrayList<String>();

		synchronized(session){

		Set<Reference> allReferences = new TreeSet<Reference>(
				new Comparator<Reference>() {
					public int compare(Reference o1, Reference o2) {
						return o1.getResource().getPath()
								.compareTo(o2.getResource().getPath());
					}
				});
		if (paths != null) {
			for (String path : paths) {
				if (path.length() > 0) {
					Resource r = resolver.getResource(path + "/"
							+ JcrConstants.JCR_CONTENT);
					if (r == null) {
						r = resolver.getResource(path);
					}

					if (r == null) {
						continue;
					}

					for (ReferenceProvider referenceProvider : referenceProviders) {
						allReferences.addAll(referenceProvider
								.findReferences(r));
					}
				}
			}
		}

		for (Reference reference : allReferences) {

			boolean published = false;
			boolean outdated = false;
			ReplicationStatus replStatus = null;
			final Resource resource = reference.getResource();
			boolean canReplicate = canReplicate(reference.getResource()
					.getPath(), session);
			long lastPublished = 0;
			if (resource != null) {
				replStatus = resource.adaptTo(ReplicationStatus.class);
				if (replStatus != null) {
					published = replStatus.isDelivered()
							|| replStatus.isActivated();
					if (published) {
						lastPublished = replStatus.getLastPublished()
								.getTimeInMillis();
						outdated = lastPublished < reference.getLastModified();
					}
				}

				log.debug(
						"Considering reference at {} . Published: {}, outdated: {} ( lastPublished: {}, lastModified: {} )",
						new Object[] { reference.getResource().getPath(),
								published, outdated, new Date(lastPublished),
								new Date(reference.getLastModified()) });
			}

			if (!published || outdated) {

				// writer.key("status").value(outdated ? "outdated" :
				// "not available");
				/*if (opts == null) {
					replicator.replicate(session,
							ReplicationActionType.ACTIVATE, resource.getPath());
				} else {
					replicator.replicate(session,
							ReplicationActionType.ACTIVATE, resource.getPath(),opts);
				}*/
				
				references.add(resource.getPath());

			}

		}
		}
		return references;
	}
	
    private static boolean canReplicate(String path, Session session) {
        try {
            AccessControlManager acMgr = session.getAccessControlManager();
            return session.getAccessControlManager().hasPrivileges(path, new Privilege[]{acMgr.privilegeFromName(Replicator.REPLICATE_PRIVILEGE)});
        } catch (RepositoryException e) {
            return false;
        }
    }

    
   
    
	public static void incrementVersion(String pagePath, 
			PageManager pageManager, String comment) {

		try {

			Page page = pageManager.getPage(pagePath);

			Revision revision = pageManager.createRevision(page, null, comment);

		} catch (WCMException e) {

			log.error(" Breakdown in version increment of page " + pagePath);
			e.printStackTrace();
		}

	}
	
	public static void restoreVersion(String pagePath, String versionToBeRestored,
			PageManager pageManager, String comment, Session session) throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {

		try {
			synchronized(session){

			Page page = pageManager.getPage(pagePath);
			
			Node srcContentNode = session.getNode(pagePath+"/"+WorkFlowConstants.CONTENT_ELEMENT);

			log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+versionToBeRestored);

			Page revision = pageManager.restore(pagePath, versionToBeRestored);
			
			session.save();
			
			overrideLastActiveVersionInfo(pagePath, versionToBeRestored,
					pageManager, session);
			
			
			
			
			session.save();

			}
		} catch (WCMException e) {

			log.error(" Breakdown in version restoration of page " + pagePath+" to version "+versionToBeRestored);
			e.printStackTrace();
		}

	}

	private static void overrideLastActiveVersionInfo(String pagePath,
			String versionToBeRestored, PageManager pageManager, Session session) {
		try {
			synchronized(session){
			Node sourceUrlNode = session.getNode(pagePath+"/"+WorkFlowConstants.CONTENT_ELEMENT);
			
			ArrayList<Revision> revisions = new ArrayList<Revision>(pageManager.getRevisions(pagePath, null));
			
			String lastPublishedVersionId = null,lastPublishedVersion=null;
			log.info("### version to be set ###"+versionToBeRestored);
			for(Revision r:revisions){
				
				 lastPublishedVersionId = r.getId();
				
				 if(versionToBeRestored.equals(lastPublishedVersionId)){
					 
					 lastPublishedVersion = r.getLabel();
					 log.info("            #######################################                   ");
					 log.info("            ### version id    on match          ###      "+lastPublishedVersionId);
					 log.info("            ### version label  on match         ###      "+r.getLabel());
					 log.info("            #######################################                   ");
					 
					 break;
				 }
				
			}
			
			log.info(" Setting property "+WorkFlowConstants.LAST_PUBLISHED_VERSION+" on page " + pagePath+"  "+lastPublishedVersion);
			
			sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION, lastPublishedVersion);
			
			log.info(" Setting property "+WorkFlowConstants.LAST_PUBLISHED_VERSION_ID+" on page " + pagePath+"  "+lastPublishedVersionId);
			
			sourceUrlNode.setProperty(WorkFlowConstants.LAST_PUBLISHED_VERSION_ID, lastPublishedVersionId);
			}
		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WCMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<String> getPaths(String path, List<ResourceCollection> rcCollections) {
        List<String> paths = new ArrayList<String>();
        if (rcCollections == null || rcCollections.size() == 0) {
            paths.add(path);
        } else {
            log.debug("ResourceCollections detected");
            for (ResourceCollection rcCollection : rcCollections) {
                // this is a resource collection. the collection itself is not
                // replicated. only its members
                try {
                    List<Node> members = rcCollection.list(new String[]{"cq:Page", "dam:Asset"});
                    for (Node member : members) {
                        String mPath = member.getPath();
                        paths.add(mPath);
                    }
                } catch (RepositoryException re) {
                    log.error("Cannot build path list out of the resource collection " + rcCollection.getPath());
                }
            }
        }
        return paths;
    }
	
	//*** Unsure why this is being used****//proper comment required
	public static List<ResourceCollection> getCollectionsForNode(Node baseNode) {
    	
	   	 List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
	   	
	   	 try {
	           // use a map to guarantee unique entries.
	           Map<String, ResourceCollection> resourceCollections = new HashMap<String, ResourceCollection>();
	           collections = new ArrayList(resourceCollections.values());
	       } catch (Exception e) {
	           log.error("Cannot get resource collections for node", e);
	       }
	       return collections;
	   }
    
	
	public static ResourceResolver getResourceResolver(final Session session,ResourceResolverFactory resourceResolverFactory) throws LoginException {
			// use the workflow session"s java.jcr.Session.
			final Map<String, Object> authenticationMap = new HashMap<String, Object>();
			authenticationMap.put(
					JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
			return resourceResolverFactory
					.getResourceResolver(authenticationMap);
		
	}
	
	public static Group createNotificationGroup(final String principalString,Session session)
			throws UnsupportedRepositoryOperationException,
			RepositoryException, AccessDeniedException,
			AuthorizableExistsException {
		synchronized(session){
		UserManager um = AccessControlUtil.getUserManager(session);

		Principal notificationWorkflowPrincipal = new Principal() {

			// PwCNotificationParticipantStepChooserImpl
			public String getName() {
				

				return principalString + rand.nextInt();
			}

		};

		return um.createGroup(notificationWorkflowPrincipal);
		}
		
	}
	
	public static void registerProperty(Session session,String srcPath,String propertyName,String propertyValue) {
		try {
			{
			Node sourceUrlNode = session.getNode(srcPath);
			Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			
		    
	         PropertyIterator iterator =(( Node)sourceUrlChildJcrNode).getProperties();
	                  
	                    Value vs[]=new Value[0];
	                    List<Value> registry=new ArrayList<Value>();
	                    boolean alreadyRegistered = false;
	                    if(sourceUrlChildJcrNode.hasProperty(propertyName)){
	                    	
	                    
		                    for(Value v : sourceUrlChildJcrNode.getProperty(propertyName).getValues()) {
		                       if(v!=null){
		                    	   if(v.getString().trim().equals(propertyValue.trim())){
		                    		   alreadyRegistered = true;
		                    	   }
		                    	   registry.add(v);
		                       }
		                    }
		                    if(!alreadyRegistered){
			                    ValueFactory vFactory=sourceUrlChildJcrNode.getSession().getValueFactory();
			                    Value newValue=vFactory.createValue(propertyValue);
			                    registry.add(newValue);
			                    Value[] finalValues=(Value[])registry.toArray(vs);
			                    sourceUrlChildJcrNode.setProperty(propertyName,finalValues);
		                    }
	                    }else{
	                    	 if(!alreadyRegistered){
				                    ValueFactory vFactory=sourceUrlChildJcrNode.getSession().getValueFactory();
				                    Value newValue=vFactory.createValue(propertyValue);
				                    registry.add(newValue);
				                    Value[] finalValues=(Value[])registry.toArray(vs);
				                    sourceUrlChildJcrNode.setProperty(propertyName,finalValues);
			                    }
	                    	 }
	                     
	                    session.save();
	                    
	                
			}
			
		} catch (PathNotFoundException e) {
			
		    log.error("Error in executing Syndication Workflow : ", e);
		} catch (RepositoryException e) {
			
			log.error("Error in executing Syndication Workflow : ", e);
		}
	}

	public static void setProperty(Session session,String srcPath,String propertyName,String propertyValue) {
		try {
			log.info("setProperty starting for " + propertyName + " " + propertyValue );
			Node sourceUrlNode = session.getNode(srcPath);
			Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			javax.jcr.Property prop = null;
			if (sourceUrlChildJcrNode.hasProperty(propertyName)) {
				prop = sourceUrlChildJcrNode.getProperty(propertyName);
				if (prop != null && prop.isMultiple()) {
					log.info("removing multi property for  " + propertyName);
					prop.remove();
					session.save();
				}
			}

			sourceUrlChildJcrNode.setProperty(propertyName, propertyValue);
				
	        session.save();
	                
		} catch (PathNotFoundException e) {
			
		    log.error("Error in settig property : ", e);
		} catch (RepositoryException e) {
			
			log.error("Error in setting property : ", e);
		}
	}

	/**
	 * Removes the given property from the given node
	 *
	 * @param session      {@link Session} current session.
	 * @param node         {@link Node} node from where property is to be removed.
	 * @param propertyName {@link String} name of property to be removed.
	 */
	public static void removeProperty(Session session, Node node, String propertyName) throws RepositoryException {
		if (session != null && node != null && propertyName != null && node.hasProperty(propertyName)) {
			node.getProperty(propertyName).remove();
			session.save();
		}
	}
}
