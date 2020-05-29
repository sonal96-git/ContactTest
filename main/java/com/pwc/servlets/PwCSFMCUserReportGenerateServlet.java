package com.pwc.servlets;

import com.pwc.schedulers.PwCSalesForceFeedScheduler;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.services.PwCSFMCUserReportGenerateService;
import com.pwc.wcm.utils.CommonUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;

/**
 * This class triggered by {@link PwCSalesForceFeedScheduler} to create csv file for DPE US users. It runs only on
 * publish environment.
 */
@Component(service = Servlet.class, immediate = true,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths=" + "/bin/dpe/generatesfmcreport",
        })
public class PwCSFMCUserReportGenerateServlet extends SlingAllMethodsServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(PwCSFMCUserReportGenerateServlet.class);

    @Reference
    PwCSFMCUserReportGenerateService pwCSFMCUserReportGenerateService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        LOGGER.info("Entered PwCSFMCUserReportGenerateServlet.doPost()");
        try {
            pwCSFMCUserReportGenerateService.generateCSVFile();
            CommonUtils.writeResponse(response, "SFMC user report CSV file generated successfully.");
        } catch (RepositoryException | IOException exception) {
            ExceptionLogger.logExceptionMessage("PwCSFMCUserReportGenerateServlet:doPost : Error while generating csv report", exception);
            try {
                response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final IOException ioException) {
                LOGGER.error("PwCSFMCUserReportGenerateServlet.doPost() : ioException occurs while sending the error 500", ioException);
            }
        }
    }

}
