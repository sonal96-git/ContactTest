package com.pwc.workflow;

/**
 * @author vimenon
 *
 */

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
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
import com.day.cq.workflow.exec.ParticipantStepChooser;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = ParticipantStepChooser.class, immediate = true, name = "com.pwc.workflow.PwCParticipantStepChooserImpl",
property = {
		Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic participant chooser depending on territory access.",    
		ParticipantStepChooser.SERVICE_PROPERTY_LABEL + "= PwC Form Participant Chooser"    
})
@Designate(ocd = PwCParticipantStepChooserImpl.Config.class)
public class PwCParticipantStepChooserImpl implements ParticipantStepChooser {

	private static final Logger logger = LoggerFactory.getLogger(PwCParticipantStepChooserImpl.class);
	private static final String PROCESS_LABEL = "PwC Workflow Participant Chooser";

	@Reference
	AdminResourceResolver adminResourceResolver;

	private PwCParticipantStepChooserImpl.Config properties;
	private String parentGroup;
	private Session session;

	//	private String PRINCIPAL = "notification-workflow-principal";
	//	private String PRINCIPAL_APPROVERS = "notification-workflow-principal-approvers";
	//	private String PRINCIPAL_AUTHORS = "notification-workflow-principal-authors";

	HashMap<String, String> groupMap ;

	private UserManager um;
	String principalString = null;
	Group notificationGroup;

	Group notificationGroupForApprovers,notificationGroupForAuthors;
	
	private String[] userMarkerApprovers;
	private String[] userMarkerAuthors;
	private String operatingGroup="";

	@ObjectClassDefinition(name = "PwC Workflow Participant Chooser", 
			description = "Implementation of PwC dynamic participant chooser depending on territory access.")
	@interface Config {
		@AttributeDefinition(name = "Process Label",
				description = "",
				type = AttributeType.STRING)
		public String chooser_label() default PROCESS_LABEL;

		@AttributeDefinition(name = "PwC marker approver group name string to be included in user group name for it to be considered for notification.", 
				description = "The marker string which indicates the group name to be considered for registration in notification workflow.e.g If the string is \"-approver\" then "
						+ "pwc-ca-approver could be a group name which could represent a valid approver group ",
				type = AttributeType.STRING)
		String[] user_marker_approvers() default {"approvers"};
		
		@AttributeDefinition(name = "PwC marker author group name string to be included in user group name for it to be considered for notification.", 
				description = "The marker string which indicates the group name to be considered for registration in notification workflow.e.g If the string is \"-author\" then "
						+ "pwc-ca-author could be a group name awhich is a valid author group.",
				type = AttributeType.STRING)
		String[] user_marker_authors();
		
		@AttributeDefinition(name = "Parent Group", 
				description = "",
				type = AttributeType.STRING)
		String parent_group() default "pwc-workflow";
		
		@AttributeDefinition(name = "PwC Network Content Admin Group", 
				description = "",
				type = AttributeType.STRING)
		String content_admin_group() default "pwc_network_content_admin";
	}

	@Override
	public String getParticipant(WorkItem item,
			WorkflowSession workflowSession, MetaDataMap map)
					throws WorkflowException {
		String participant = "";
		ResourceResolver adminResolver = null;
		Session session = null;

		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);
			userMarkerApprovers =  properties.user_marker_approvers();
			userMarkerAuthors = properties.user_marker_authors();
			parentGroup = properties.parent_group();
			operatingGroup = properties.content_admin_group();
			WorkflowData data = item.getWorkflowData();
			String payLoadPath = (String)data.getPayload();
			if(payLoadPath.contains("/content/dam")) {
				String assetPattern = "/content/dam/pwc/(\\w{2})/.*";
				Pattern pattern = Pattern.compile(assetPattern);
				Matcher matcher = pattern.matcher(payLoadPath);
				if (!matcher.find()) {
					participant = operatingGroup;
				}
				else{
					//validate territory
					participant = getLocaleString(payLoadPath,session)  + "-" + userMarkerApprovers[0];
				}
				//checks formStart path to get the territory author
			} else if(payLoadPath.contains("/content/usergenerated/content/pwc")){
				Object formStartPath = adminResolver.getResource(payLoadPath).getValueMap().get("formPath");
				payLoadPath = null == formStartPath ? payLoadPath : formStartPath.toString();
				participant = getLocaleString(payLoadPath,session)  + "-" + userMarkerApprovers[0];

			} else{
				participant = getLocaleString(payLoadPath,session)  + "-" + userMarkerApprovers[0];
			}
			if(data.getMetaDataMap().get(WorkFlowConstants.HAS_VERSION_BEEN_RESTORED)!=null){
				//participant = (notificationGroupForAuthors.getID());
				participant = getLocaleString(payLoadPath,session) + "-" +  userMarkerAuthors[0];
			}


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.logout();
			}
			if (adminResolver != null) { 
				adminResolver.close(); 
			} 
		}
		return participant;
	}


	private String getLocaleString(String payLoadPath, Session session2) {

		//String locale1 = payLoadPath.split("/")[2];

		//String locale2 = payLoadPath.split("/")[3];

		/*
			Modified by Rui Jiang July-29-2015, add an if statement to adapt asset publishing
		 */
		if(payLoadPath.contains("/content/dam")) //add this for asset publisher
			return  payLoadPath.split("/")[4];
		else
			return payLoadPath.split("/")[3];


	}


	/**
	 * @param grp
	 * @return
	 */
	private boolean checkGroupForNotificationMarker(String grp,String[] userMarker) {

		for(String marker:userMarker){
			if(grp.contains(marker)){
				return true;
			}
		}
		return false;
	}

	@Activate
	protected void activate(PwCParticipantStepChooserImpl.Config props) {
		this.properties = props;
		groupMap = new HashMap<String, String>();
		//network.content.admin
		//operatingGroup = (String)context.getProperties().get("network.content.admin");
	}



	private void populateNotificationGroups(String parentGroup, WorkItem item)
			throws RepositoryException {

		Authorizable auth = null;

		logger.info(" Populating notification group with id: "+notificationGroup.getID());
		String payload = (String) item.getWorkflowData().getPayload();
		// get the Jackrabbit access control manager
		AccessControlManager aMgr = session.getAccessControlManager();

		um = AccessControlUtil.getUserManager(session);

		// create a privilege set with jcr:all

		// get first applicable policy (for nodes w/o a policy)
		AccessControlPolicy[] itAcl = aMgr.getEffectivePolicies(payload);
		for (AccessControlPolicy ap : itAcl) {


			for (AccessControlEntry e : ((AccessControlList) ap)
					.getAccessControlEntries()) {
				logger.debug("PSTEPCH e : "+e.getPrincipal());
				for (Privilege p : e.getPrivileges()) {

					logger.debug("PSTEPCH privilege : " + p);

					if (	p.equals(aMgr.privilegeFromName(Privilege.JCR_WRITE))
							|| p.equals(aMgr.privilegeFromName(Privilege.JCR_ALL))
							|| p.getName().equals("rep:write")
							|| p.equals(aMgr.privilegeFromName(Privilege.JCR_MODIFY_PROPERTIES))) {
						auth = um.getAuthorizable(e.getPrincipal());
						Principal pr = auth.getPrincipal();

						if(pr!=null)
							logger.debug("PSTEPCH Check membership for " + pr.getName() + " for parent group " + parentGroup);

						pr = e.getPrincipal();

						if(pr!=null)
							if (checkGroupMembership(auth, parentGroup)) {
								logger.debug("PSTEPCH Adding member " + pr.getName() + " for parent group " + parentGroup);
								Authorizable prin = um.getAuthorizable(e.getPrincipal());
								if(authHasAllowPermission(auth,payload,session,um)){

									notificationGroup.addMember(prin);
									if (checkGroupForNotificationMarker(prin.getID(),userMarkerApprovers)){
										notificationGroupForApprovers.addMember(prin);
									}
									if (checkGroupForNotificationMarker(prin.getID(),userMarkerAuthors)){
										notificationGroupForAuthors.addMember(prin);
									}
								}
							}	
					}

				}

			}
		}
		logger.info("######### Populating notification group with id: "+notificationGroup.getID()+" is successful #######");

		session.save();

	}

	private boolean authHasAllowPermission(Authorizable auth, String payload,Session session, UserManager um2) throws PathNotFoundException, RepositoryException {


		//Node srcNode = session.getNode(payload);

		AccessControlManager acm = session.getAccessControlManager();

		AccessControlPolicy[] it = acm.getEffectivePolicies(payload);

		for(AccessControlPolicy pol:it){
			AccessControlList p = (AccessControlList)pol;

			AccessControlEntry[] acls = p.getAccessControlEntries();

			for(AccessControlEntry a:acls){

				Authorizable principal = um.getAuthorizable(a.getPrincipal());

				JackrabbitAccessControlEntry jacl = (JackrabbitAccessControlEntry)a;

				logger.info("###########********* acl entry : "+principal.getID()+" privelages *********###########");

				if(jacl.isAllow() && auth.getID().equals(principal.getID())){
					logger.info("###########*********   ALLOWED FOR "+auth.getID()+"  *********###########");
					return true;
				}



			}

		}



		return false;
	}


	/**
	 * @param auth
	 * @param parentGroup
	 */
	private boolean checkGroupMembership(Authorizable auth, String parentGroup) {
		if(parentGroup == null||parentGroup.isEmpty()){
			return true;
		}

		boolean memberOf = false;

		try {
			logger.info("PSTEPCH CHECKGM " + auth.getID());

			Iterator<Group> gitemp = auth.memberOf();

			memberOf = checkMembership(gitemp, parentGroup);

			if(!memberOf){

				Iterator<Group> gi = auth.declaredMemberOf();
				memberOf = checkMembership(gi, parentGroup);
			}


		} catch (RepositoryException e) {
			logger.info("Repository exception");
		}
		return memberOf;

	}

	private boolean checkMembership(Iterator<Group> gi, String parentGroup)
			throws RepositoryException {

		while (gi.hasNext()) {
			Authorizable g = gi.next();
			String id = g.getID();
			logger.debug("PSTEPCH Check group membership id in while loop for " + id);
			if (id.equals(parentGroup)) {
				logger.debug("PSTEPCH Check group membership true for " + id);					
				return true;
			}
		}
		return false;
	}


}
