package com.pwc.workflow;
/**
 * @author vimenon
 *
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
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

@Component(service = WorkflowProcess.class, immediate = true, name = "com.pwc.workflow.PwCSyndicationWorkflowImpl",
property = {
		Constants.SERVICE_DESCRIPTION + "= Implementation of PwC syndication workflow",
		Constants.SERVICE_VENDOR + "= Adobe"
})
@Designate(ocd = PwCSyndicationWorkflowImpl.Config.class)
public class PwCSyndicationWorkflowImpl implements WorkflowProcess {

	private String SRC_URL = "";
	private String[] contentPars;

	@Reference
	private ResourceResolverFactory resourceResolverFactory ;

	@Reference
	private AdminResourceResolver adminResourceResolver;

	private Session session = null;

	private String[] contentParsWithCheck;

	private String[] userMarker;
	//read srcUrl And syndicationflag
	private static final Logger log = LoggerFactory.getLogger(PwCSyndicationWorkflowImpl.class);

	@ObjectClassDefinition(name = "PwC Workflow Syndication Configuration", 
			description = "Configure parsyses to be syndicated - templateName=parsys1,parsys2")
	@interface Config {
		@AttributeDefinition(name = "Process label",
				description = "Description For Process label",
				type = AttributeType.STRING)
		String process_label() default "PwC Syndication Workflow";

		@AttributeDefinition(name = "PwC Template content parsys configuration", 
				description = "....",
				type = AttributeType.STRING)
		public String[] contentPars();

		@AttributeDefinition(name = "PwC Template configuration with teplate equality check", 
				description = "This is a configuration which lists the names of those templates to be syndicated which have a"
						+ " check for source page template == target page template.Pages are syndicate only if this check is passed.Otherwise the workflow ends.",
						type = AttributeType.STRING)
		public String[] contentParsWithCheck();

		@AttributeDefinition(name = "PwC marker group name string to be included in user group name for it to be considered for notification.", 
				description = "The marker string which indicates the group name to be considered for registration in notification workflow.e.g If the string is \"-author\" then "
						+ "pwc-ca-author could be a group name added to registered users property \"pwcUserList\" in the source page.",
						type = AttributeType.STRING)
		public String[] userMarker(); 
	}

	@Activate
	protected void activate(PwCSyndicationWorkflowImpl.Config properties)
			throws RepositoryException {

		log.info("Syndication Workflow service  started");
		// final Dictionary<?, ?> properties = context.getProperties();
		// Enumeration el = properties.elements();
		/*while(el.hasMoreElements()){
			//javax.jcr.Property p = (javax.jcr.Property)properties.elements().nextElement();
			//log.info("Syndication Workflow service  property : "+p.getName()+p.getString());
			log.info("Syndication Workflow service  property : "+el.nextElement());
		}*/		
		log.info("Syndication Workflow service  property : " + properties.contentPars());
		log.info("Syndication Workflow service  property : " + properties.contentParsWithCheck());
		log.info("Syndication Workflow service  property : " + properties.userMarker());
		contentPars = properties.contentPars();
		contentParsWithCheck = properties.contentParsWithCheck();
		userMarker = properties.userMarker();
	}

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
			throws WorkflowException {

		ResourceResolver adminResolver = null;

		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();
		String srcPath=null ,targetPath= (String)data.getPayload(),templateName = null,templateNameTarget = null,syndicationFlag= WorkFlowConstants.SYNDICATION_OPEN;
		Node sourceUrlNode,targetUrlNode;


		try {
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			session = adminResolver.adaptTo(Session.class);

			targetUrlNode = session.getNode(targetPath);
			Node targetUrlChildJcrNode = targetUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
			srcPath = targetUrlChildJcrNode.getProperty(WorkFlowConstants.SRC_ELEMENT).getString();
			templateName = session.getNode(srcPath).getNode(WorkFlowConstants.CONTENT_ELEMENT).getProperty(WorkFlowConstants.TEMPLATE_ELEMENT).getString();
			templateName = templateName.substring(templateName.lastIndexOf("/")+1);

			templateNameTarget = targetUrlChildJcrNode.getProperty(WorkFlowConstants.TEMPLATE_ELEMENT).getString();

			templateNameTarget = templateNameTarget.substring(templateNameTarget.lastIndexOf("/")+1);

			syndicationFlag = session.getNode(srcPath).getNode(WorkFlowConstants.CONTENT_ELEMENT).getProperty(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT).getString();
			Object manual = data.getMetaDataMap().get(WorkFlowConstants.SYNDICATION_DONE_MANUALLY);
			final WorkflowData wdata = workItem.getWorkflowData();
			log.debug("About to set manual flag");
			if(manual == null) {
				log.debug("Set manual to true");
				wdata.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
			}else{
				log.debug("Set manual to false");
				wdata.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"false");
			}

		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow : ", e);
			workItem.getWorkflowData().getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow : ", e);
			// wdata.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
			workItem.getWorkflowData().getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
		}

		// boolean syndicationSuccess = false; 
		// MLUKIC
		boolean syndicationSuccess = true;

		if(srcPath==null || srcPath.isEmpty()|| srcPath.trim().endsWith(targetPath.trim())){
			data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,WorkFlowConstants.INVALID_INPUT);
			data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
			return;
		}

		if(syndicationFlag.equals(WorkFlowConstants.SYNDICATION_LOCKED)){
			data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,WorkFlowConstants.SYNDICATION_LOCKED);
			data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_DONE_MANUALLY,"true");
			return;
		}

		List<String> contentparListWithCheck = Arrays.asList(contentParsWithCheck);
		for(String par:contentPars ){
			log.info("Par Properties for page type: "+par);

			if(par.split("=")[0].trim().equals(templateName)){
				if(!contentparListWithCheck.contains(templateName)){
					syndicationSuccess = syndicateContent(srcPath,
							targetPath, syndicationSuccess, par);
				}else{
					if(templateNameTarget.trim().equals(templateName.trim())){
						syndicationSuccess = syndicateContent(srcPath,
								targetPath, syndicationSuccess, par);
					}else{
						data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,WorkFlowConstants.INVALID_INPUT);
						return;
					}
				}


			}

		}


		try {

			if(syndicationSuccess){
				registerDestinationWithSource(srcPath,(String)data.getPayload());
				registerUserDetailsWithSource(srcPath,workItem);
			}

			saveMetaInformationForSyndicationFlag(workItem,args,syndicationFlag);
		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow : ", e);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			log.error("Error in executing Syndication Workflow : ", e);
		} finally {
			if (session != null) {
				session.logout();
			}
			if (adminResolver != null) { 
				adminResolver.close(); 
			}
		}

	}

	/**
	 * @param srcPath
	 * @param targetPath
	 * @param syndicationSuccess
	 * @param par
	 * @return
	 */
	private boolean syndicateContent(String srcPath, String targetPath,
			boolean syndicationSuccess, String par) {
		String[] body = par.split("=")[1].split(",");
		for(String bodyPar:body){

			// MLUKIC
			// if(!syndicationSuccess)
			if(syndicationSuccess)
				syndicationSuccess = syndicate(srcPath, targetPath,bodyPar);
		}
		return syndicationSuccess;
	}

	private void registerUserDetailsWithSource(String srcPath,WorkItem workItem) throws RepositoryException {

		synchronized(session){
			String uname = workItem.getWorkflow().getInitiator(),email = "";

			ResourceResolver resourceResolver = getResourceResolver(session);
			UserManager userManager = resourceResolver.adaptTo(UserManager.class);

			/* to get the current user */
			Authorizable auth = userManager.getAuthorizable(session.getUserID());

			/* to get the property of the authorizable. Use relative path */
			Value[] names = auth.getProperty("./profile/email");



			//registerProperty(srcPath,WorkFlowConstants.USERNAME_REGISTRY,uname);
			registerUserInfoPostMarkerCheck(srcPath, userManager, uname);

			if(names!=null){
				for(Value v:names){
					String uid = v.getString();
					registerUserInfoPostMarkerCheck(srcPath, userManager, uid);

				}
			}
		}



	}

	/**
	 * @param srcPath
	 * @param userManager
	 * @param uid
	 * @throws RepositoryException
	 */
	private void registerUserInfoPostMarkerCheck(String srcPath,
			UserManager userManager, String uid) throws RepositoryException {
		Authorizable auth;
		auth = userManager.getAuthorizable(uid);
		Iterator<Group> groups = auth.declaredMemberOf();

		while(groups.hasNext()){
			String grp = groups.next().getID();
			if(checkGroupForNotificationMarker(grp)){
				PwCWorkFlowUtil.registerProperty(session,srcPath,WorkFlowConstants.USERNAME_REGISTRY,grp);
			}
		}
	}

	/**
	 * @param grp
	 * @return
	 */
	private boolean checkGroupForNotificationMarker(String grp) {

		for(String marker:userMarker){
			if(grp.contains(marker)){
				return true;
			}
		}
		return false;
	}


	private void saveMetaInformationForSyndicationFlag(WorkItem workItem, MetaDataMap args,String syndicationFlag) throws PathNotFoundException, RepositoryException{

		final WorkflowData data = workItem.getWorkflowData();
		String path = null;
		String type = data.getPayloadType();

		synchronized(session){
			if (type.equals(WorkFlowConstants.TYPE_JCR_PATH  ) && data.getPayload() != null) {
				String payloadData = (String) data.getPayload();
				if (session.itemExists(payloadData)) {
					path = payloadData;
					Node node = session.getNode(path);


					if( WorkFlowConstants.SYNDICATION_OPEN.equals(syndicationFlag)){

						data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,WorkFlowConstants.SYNDICATION_OPEN);
					}else{
						data.getMetaDataMap().put(WorkFlowConstants.SYNDICATION_FLAG_ELEMENT,WorkFlowConstants.SYNDICATION_RESTRICTED);
					}
				}
			}

		}
	}

	private void registerDestinationWithSource(String srcPath,String destinationUrl) {
		synchronized(session){
			try {
				Node sourceUrlNode = session.getNode(srcPath);
				Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);




				Value vs[]=new Value[0];
				List<Value> registry=new ArrayList<Value>();
				boolean alreadyRegistered = false;
				if(sourceUrlChildJcrNode.hasProperty(WorkFlowConstants.URL_REGISTRY_PROP)){


					for(Value v : sourceUrlChildJcrNode.getProperty(WorkFlowConstants.URL_REGISTRY_PROP).getValues()) {
						if(v!=null){
							if(v.getString().trim().equals(destinationUrl.trim())){
								alreadyRegistered = true;
							}
							registry.add(v);
						}
					}
					if(!alreadyRegistered){
						ValueFactory vFactory=sourceUrlChildJcrNode.getSession().getValueFactory();
						Value newValue=vFactory.createValue(destinationUrl);
						registry.add(newValue);
						Value[] finalValues=registry.toArray(vs);
						sourceUrlChildJcrNode.setProperty(WorkFlowConstants.URL_REGISTRY_PROP,finalValues);
					}
				}else{
					if(!alreadyRegistered){
						ValueFactory vFactory=sourceUrlChildJcrNode.getSession().getValueFactory();
						Value newValue=vFactory.createValue(destinationUrl);
						registry.add(newValue);
						Value[] finalValues=registry.toArray(vs);
						sourceUrlChildJcrNode.setProperty(WorkFlowConstants.URL_REGISTRY_PROP,finalValues);
					}
				}

				session.save();




			} catch (PathNotFoundException e) {
				// TODO Auto-generated catch block
				log.error("Error in executing Syndication Workflow : ", e);
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				log.error("Error in executing Syndication Workflow : ", e);
			}

		}
	}


	private boolean syndicate(String sourceUrl, String payloadData,String contentPar) {

		synchronized(session){

			try{
				// Create a connection to the CQ repository running on local host 
				String sourceUrlNodePath = sourceUrl;
				String payloadDataNodePath = payloadData;

				Node sourceUrlpageJcrNode = null;
				Node payloadDatatext = null;

				//setProperty(java.lang.String name, Node value) 

				boolean sourceUrlNodeFlag = session.nodeExists(sourceUrlNodePath);
				boolean payloadDataNodeFlag = session.nodeExists(payloadDataNodePath);
				if (sourceUrlNodeFlag && payloadDataNodeFlag) {
					//System.out.println("to infinity and beyond");
					Node sourceUrlNode = session.getNode(sourceUrlNodePath);
					Node payloadDataNode = session.getNode(payloadDataNodePath);


					Node sourceUrlChildJcrNode = sourceUrlNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);
					Node payloadDataChildJcrNode = payloadDataNode.getNode(WorkFlowConstants.CONTENT_ELEMENT);

					// now we fetch the main-component nodes. 

					Node sourceUrlChildJcrNodeCenter = sourceUrlChildJcrNode.getNode(contentPar);

					Node payloadDataJcrNodeCenter = null;
					if(payloadDataChildJcrNode.hasNode(contentPar)){
						payloadDataJcrNodeCenter = payloadDataChildJcrNode.getNode(contentPar);
						payloadDataJcrNodeCenter.remove();
						session.save();
						payloadDataJcrNodeCenter = payloadDataChildJcrNode.addNode(contentPar);
					}else{
						payloadDataJcrNodeCenter = payloadDataChildJcrNode.addNode(contentPar);
					}

					session.getWorkspace().copy(sourceUrlChildJcrNodeCenter.getPath(), payloadDataJcrNodeCenter.getPath());


					session.save();
					return true;
				}

			}catch(RepositoryException e){
				log.error("Error in executing Syndication Workflow : ", e);

			}
			return false;
		}
	}

	protected ResourceResolver getResourceResolver(final Session session) {
		try {
			// use the workflow session"s java.jcr.Session.
			final Map<String, Object> authenticationMap = new HashMap<String, Object>();
			authenticationMap.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
			return resourceResolverFactory.getResourceResolver(authenticationMap);
		} catch (final Exception e) {
			// exception handling
			// todo: throw a workflow exception,
			return null;
		}
	}

}
