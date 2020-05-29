package com.pwc.wcm.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.services.TerminateAuthorWorkflow;

/**
 * Servlet to check if a workflow can be terminated by author from sidekick.
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/isWorkflowTerminable",
})
public class AuthorWorkflowHandler extends SlingAllMethodsServlet {
   Logger logger = LoggerFactory.getLogger(this.getClass());
   @Reference
   TerminateAuthorWorkflow terminateAuthorWorkflowConfig;

    private static final String CONTENT_TYPE = "application/json";
    private static final String ENCODING = "UTF-8";
    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
         String workflowName = req.getParameter("workflowName");
        boolean isTerminable = isWorkflowTerminable(workflowName);
        logger.info("isWorkFlow Terminable " +isTerminable);
        
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(ENCODING);
        resp.getWriter().write(isTerminable?"terminable":"interminable");

    }


    /**
     * Method returns true, if a given workflow can be terminated by author from sidekick.
     * @param workFlowName
     * @return true/false
     */
   private boolean isWorkflowTerminable(String workFlowName){
        String[] workFlowNames=terminateAuthorWorkflowConfig.getWorkflowNames();
        for(String wf:workFlowNames){
            if(wf.contains(workFlowName)) return true;
        }
        return false;
   }
}