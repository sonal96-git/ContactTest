package com.pwc.schedulers;

import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Route;
import com.day.cq.workflow.exec.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpamCheckScheduler implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private Session session;
    private String user;
    private String queryStoreLocation;
    private String subCount;
    private WorkflowService workflowService;
    private String spamPeriod;
    static boolean isMaster = false;
    private boolean releaseEnabled = false;

    public SpamCheckScheduler (Boolean isMas) {
        isMaster=isMas;
    }

    public SpamCheckScheduler (Session session, WorkflowService workflowService, String period, String user, String location, String subCount, String spamPeriod, Boolean releaseEnabled) {

        this.session = session;
        this.queryStoreLocation=location;
        this.user=user;
        this.subCount=subCount;
        this.workflowService=workflowService;
        this.spamPeriod=spamPeriod;
        this.releaseEnabled = releaseEnabled;

    }

    /***Thread that will run with a particular time interval********/
    public void run() {

        if(isMaster){
            log.info("PwC Scheduler Started");
            checkSpam();
            log.info("PwC Scheduler Completed");

        }//if Master
    }

    /***Method with all node operations********/
    public void checkSpam()
    {
        log.info("PwC Scheduler in Check Spam");
        try
        {
            Long cts=System.currentTimeMillis();
            log.info("PwC Scheduler Getting User Session for " + user);
            Session  userSession= session.impersonate(new SimpleCredentials(user,"".toCharArray()));
            WorkflowSession wfSession = workflowService.getWorkflowSession(userSession);
            WorkItem[] workItems=wfSession.getActiveWorkItems();
            log.info("PwC Scheduler Work Items Count is " + workItems.length);
            Map<String,Integer> formSubmissiosn=new HashMap<String,Integer>();
            for(int i=0;i<workItems.length;i++)
            {
                String fP=workItems[i].getWorkflowData().getPayload().toString()	;
                log.info("fp = " +  fP);
                String[] formPaths=fP.split("/");
                int length=formPaths.length;
                Long pts = (long)0;
                Long spamPer = (long)0;
                try {
                    pts  =Long.parseLong(formPaths[length-1]);
                    spamPer =Long.parseLong(spamPeriod);
                }catch(Exception ex){

                }
                spamPer=spamPer*1000;
                if(cts-pts>spamPer){
                    String formId=formPaths[length-3];
                    if(formSubmissiosn.containsKey(formId))
                    {
                        Integer count=(Integer)formSubmissiosn.get(formId);
                        count=count+1;
                        formSubmissiosn.put(formId,count);
                    }
                    else
                    {
                        formSubmissiosn.put(formId,1);
                    }
                }
            }

            for(int i=0;i<workItems.length;i++)
            {
                String fP=workItems[i].getWorkflowData().getPayload().toString()	;
                String[] formPaths=fP.split("/");
                int length=formPaths.length;
                String formId=formPaths[length-3];
                Node node=session.getNode(fP);
                Integer count=(Integer)formSubmissiosn.get(formId);
                Long pts = (long)0;
                Long spamPer = (long)0;
                Long spamCount = (long)0;
                try {
                    pts  =Long.parseLong(formPaths[length-1]);
                    spamPer =Long.parseLong(spamPeriod);
                    spamCount=Long.parseLong(subCount);
                }catch(Exception ex){

                }
                spamPer=spamPer*1000;
                if(releaseEnabled && node.getParent().getParent().hasProperty(SchedulerConstants.STRING_SPAM) && node.getParent().getParent().getProperty(SchedulerConstants.STRING_SPAM).getValue().toString().equals("true"))
                {
                    node.setProperty("spam","true");
                    node.getSession().save();
                    List<Route> routes = wfSession.getRoutes(workItems[i], true);
                    wfSession.complete(workItems[i], routes.iterator().next());

                }
                else if(cts-pts>spamPer){
                    if(count>spamCount){
                        node.setProperty("spam","true");
                        // if marking of parent node as spam is enabled
                        if(releaseEnabled) {
                            node.getParent().getParent().setProperty("spam", "true");
                        }
                    }
                    else
                    {
                        node.setProperty("spam","false");
                    }
                    node.getSession().save();
                    List<Route> routes = wfSession.getRoutes(workItems[i], true);
                    wfSession.complete(workItems[i], routes.iterator().next());
                }
            }
                 /*
                 if(workItems.length>0 && !formSubmissiosn.isEmpty() && queryStoreLocation!=null && queryStoreLocation !=""){
                	 
                	 String path=queryStoreLocation+"/"+cts.toString();
                	 Node ctsNode = JcrUtil.createPath(path, "sling:Folder", session);
                 
                	 ctsNode.setProperty("spamQueryResult", formSubmissiosn.toString());
                	 ctsNode.setProperty("spamDeciderCount",subCount);
                	 session.save();
                 }
                 */

        }//try
        catch (Exception e)
        {
            log.error("PwC Scheduler Exception",e);
        }
    }
}//class
