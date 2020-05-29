package com.pwc.workflow.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
//import com.pwc.util.GlobalParam;
import com.pwc.util.ResourceHelper;
import com.pwc.workflow.WorkFlowConstants;

@Component(service = WorkflowProcess.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= PwC Contact Decider",
   "process.label=" + "PwC Contact Decider"    
})
public class ContactsDecider implements WorkflowProcess {

	    @Reference
	    private ResourceResolverFactory rf;
	   
		public static final Logger log  = LoggerFactory.getLogger(ContactsDecider.class);
	  
		private static final String TYPE_JCR_PATH = "JCR_PATH";
		private Session jcrSession;
		private ResourceResolver resourceResolver;
	 
	    @Override
		public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
	    	try {
	    		log.info("ENTERED CONTACTS DECIDER");
	    		
	    		WorkflowData workflowData = item.getWorkflowData();
				resourceResolver = session.adaptTo(ResourceResolver.class);

				//ResourceResolver rr=rf.getAdministrativeResourceResolver(null);
	    		if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
	    			String path = workflowData.getPayload().toString();
	    			
	    			
		            if(workflowData.getMetaDataMap().get("listOfNodes")!=null )
		            {
		            	//log.error("called from  pwc resend mail and archive workflow");
		            	List<String> listOfNodes= (List<String>)workflowData.getMetaDataMap().get("listOfNodes");
		        		//Integer count=(Integer) workflowData.getMetaDataMap().get("count");
		        		path=listOfNodes.get(0);
		        		
		            }
	    			
	    			String reprocess="";
	    			String contactType = args.get("PROCESS_ARGS", String.class);
	    			String[] contacttypearr=contactType.split(",");
	    			if(contacttypearr.length==2)
	    			{
	    				contactType=contacttypearr[0];
	    				reprocess=contacttypearr[1];
	    				
	    			}
	    			if(contacttypearr.length==1)
	    			{
	    				contactType=contacttypearr[0];
	    			}


	    			ResourceHelper rh=new ResourceHelper();
	    			String contactPath="/content/pwc/global/referencedata/territories";//GlobalParam.getContactUsParams("territoryContactPath", rr);
	    			log.info("Moon contactPath "+contactPath );
	    			ValueMap contactProp=rh.getResourceProperty(resourceResolver, contactPath);
	    			
	    			log.info("Moon  contactProp "+contactProp );
	    			ValueMap props=rh.getResourceProperty(resourceResolver, path);
	    			
	    			log.info("Moon  ValueMap props "+ props );


	    			String territory = props.get("visitor-country","");//GlobalParam.getContactUsProperty(rr, props, "visitorCountry", null);
	    			
	    			log.info("Moon  territory "+ territory );
	    			String defaultTerritory="gx";//GlobalParam.getContactUsParams("defaultTerritory", rr);

	    			log.info("Moon  defaultTerritoryy "+ defaultTerritory );

	    			if(territory==null)
            			territory="gx";//GlobalParam.getContactUsProperty(rr, props, "territory", defaultTerritory);
            			
            		territory=contactPath+"/"+territory;

            		log.info("Moon  territory "+ territory );

	    			ValueMap terProp =rh.getResourceProperty(resourceResolver, territory);
	    			
	    			log.info("Moon  ValueMap terProp "+ terProp );
	            	String inquiryType=props.get("visitor-inquiryType","");//GlobalParam.getContactUsProperty(rr, props, "visitorInquiryType", null);
	            	
	            	log.info("Moon  inquiryType "+ inquiryType );
	            	
	            	String contactEmail=props.get("contact_email",""); //GlobalParam.getContactUsProperty(rr, props, "contactEmail", null);
	            	String contactCC= props.containsKey("contactCC")&& props.get("contactCC")!=null && props.get("contactCC").toString().trim()!="null"?props.get("contactCC").toString():"";//"contactCC";
	            	String formcurrentsstaus=props.get("status","");
	            	//IsNormalForm
	            	//contact_path
	            	//contact_path
	            	//mailCount
	            	
	            	
	            	
	            	Integer mailcount=props.get("mailCount",0);
	            	
	            	log.info("new code " +mailcount +reprocess);//PR 1669
	            	
	            	if(formcurrentsstaus.trim().equals("Delivery Failed") || mailcount>0 || reprocess.equals("reprocess"))
	            	{
	            		log.info("mailcount "+ mailcount + ", "+ reprocess);
	            		String IsNormalForm=props.get("IsNormalForm","no");
	            		
	            		log.info("IsNormalForm "+ IsNormalForm);
	            		if(IsNormalForm.equalsIgnoreCase("no"))
	            		{
	            			String contact_path=props.get("contact_path","");
	            			if(contact_path!=""){
	            			ValueMap specificcontactprops=rh.getResourceProperty(resourceResolver, contact_path);
	            			if(specificcontactprops!=null)
	            			{
	            				contactCC=specificcontactprops.get("contactcc","");
	            				contactEmail=specificcontactprops.get("email","");
	            				log.info("specificcontactprops  "+ contactEmail);
	            				
	            			}
	            			//contactcc
	            			//email
	            			
	            			}
	            			
	            		}
	            		
	            	}








	            	log.info("Moon  contactEmail "+ contactEmail );

	            	log.info("Moon  contactCC "+ contactCC );

	            	List<String> hcList=null;
            		String[] hc=terProp.get("hcContacts",null);//get(GlobalParam.getContactUsParams("hc", rr),null);
            		if(hc!=null && hc.length>0) hcList = Arrays.asList(hc);
            		String[] defaultEmp=terProp.get("defaultContacts",null);//GlobalParam.getContactUsParams("default", rr),null);
            		List<String> defaultList =null;
            		if(defaultEmp!=null && defaultEmp.length>0) defaultList = Arrays.asList(defaultEmp);
            		String[] rfp=terProp.get("rfpContacts",null);//GlobalParam.getContactUsParams("rfp", rr),null);
            		List<String> rfpList=null;
            		if(rfp!=null && rfp.length>0)  rfpList = Arrays.asList(rfp);
            		ArrayList<String> contact = new ArrayList<String>();
            		String[] dpe=contactProp.get("dpeOperationContacts",null);//GlobalParam.getContactUsParams("dpe", rr),String[].class);
            		List<String> dpeList=null;

            		log.info("Moon  hcList "+ hcList );
            		log.info("Moon  defaultList "+ defaultList );
            		log.info("Moon  rfpList "+ rfpList );



            		if(dpe!=null)
            			dpeList=Arrays.asList(dpe);
            		jcrSession = session.adaptTo(Session.class);
	                Node node = (Node) jcrSession.getItem(path);

	                log.info("Moon  dpeList "+ dpeList );

	                log.info("Moon  contactType "+ contactType
	                		);

	                log.info("Moon  contactEmail "+ contactEmail
	                		);

	                log.info("Moon  inquiryType "+ inquiryType
	                		);

	            	if(contactType!=null && contactType.equals("contact")){//GlobalParam.getContactUsParams("contactFormDeciderParameter", rr))){

	            		if(!inquiryType.equals("hcContacts")){//reverted code for PR 1664
	            		if(contactEmail!=null && !"null".equalsIgnoreCase(contactEmail)){
	            			String separator="";
	            			if(contactEmail.contains(","))separator=",";
	            			if(contactEmail.contains(";"))separator=";";
	            			if(separator.length()>0)
	            			{
	            				String[] mailids=contactEmail.split(separator);
	            				for(int z=0;z<mailids.length;z++)
	            				{
	            					contact.add(mailids[z]);
	            				}
	            				
	            			}
	            			else{	
	            			contact.add(contactEmail); }
	            			}
	            		if(contactCC!=null && !"".equals(contactCC.trim()) && !"null".equalsIgnoreCase(contactCC.trim()))
	            			
	            		{
	            			String separator="";
	            			if(contactCC.contains(","))separator=",";
	            			if(contactCC.contains(";"))separator=";";
	            			if(separator.length()>0)
	            			{
	            			
	            				String[] mailids=contactCC.split(separator);
	            				for(int z=0;z<mailids.length;z++)
	            				{
	            					contact.add(mailids[z]);
	            				}
	            			}
	            			else
	            			{
	            				        			
	            			contact.add(contactCC);
	            			}
	            		
	            		}
	            	}
	            		if(inquiryType.equals("hcContacts")){//GlobalParam.getContactUsParams("hc", rr))){
	            			if(hcList!=null && !hcList.isEmpty() )
	            				contact.addAll(hcList);
	            			else if(defaultList!=null && !defaultList.isEmpty())
	            				contact.addAll(defaultList);
	            			else if(dpeList!=null)//ideally dpeList should never be null
	            				contact.addAll(dpeList);
	            		}
	            		
	            	
	            	}
	            	else
	            	{
	            		if(inquiryType.equals("defaultContacts")){//GlobalParam.getContactUsParams("default", rr))){
	            			if(defaultList!=null && !defaultList.isEmpty())
	            				contact.addAll(defaultList);
	            			else if(dpeList!=null)
	            				contact.addAll(dpeList);
	            		}	
	            		else if(inquiryType.equals("rfpContacts")){//GlobalParam.getContactUsParams("rfp", rr))){
	            			if(rfpList!=null && !rfpList.isEmpty())
	            				contact.addAll(rfpList);
	            			else if(defaultList!=null && !defaultList.isEmpty())
	            				contact.addAll(defaultList);
	            			else if(dpeList!=null)//ideally dpeList should never be null
	            				contact.addAll(dpeList);
	            				
	            		}
	            		else if(inquiryType.equals("hcContacts"))//GlobalParam.getContactUsParams("hc", rr)))
	            		{
	            			if(hcList!=null && !hcList.isEmpty())
	            				contact.addAll(hcList);
	            			else if(defaultList!=null && !defaultList.isEmpty())
	            				contact.addAll(defaultList);
	            			else if(dpeList!=null)//ideally dpeList should never be null
	            				contact.addAll(dpeList);
	            		}


	            	}


	            	String contactArr[]=new String[contact.size()];
	            	contactArr=contact.toArray(contactArr);
	            	node.setProperty(WorkFlowConstants.CONTACTFORM_SENDEMAILTO,contactArr);
	            	
	            	log.info("Moon "+ contactArr);
	            	jcrSession.save();
	            	
	            	log.info("EXITING CONTACTS DECIDER , CONTACT ARRAY = "+contactArr );
	            
	            
	            }
	    		
	    	}catch(Exception e){
	            	log.error(e.getMessage(),e);
	            	
	            	
	        }
	     }
	    
	    

}
