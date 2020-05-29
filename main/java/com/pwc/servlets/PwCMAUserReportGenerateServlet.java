package com.pwc.servlets;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.schedulers.PwCMAUserReportSchedular;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.services.PwCMAUserReportGenerateService;
import com.pwc.wcm.utils.CommonUtils;

/**
 * This class is triggered by {@link PwCMAUserReportSchedular} to create csv file for DPE MA users . It runs only on
 * publish environment.
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    "sling.servlet.paths=" + "/bin/pwc/mauserreportgenerator",
})
public class PwCMAUserReportGenerateServlet extends SlingAllMethodsServlet {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PwCMAUserReportGenerateServlet.class);
	
	@Reference
	PwCMAUserReportGenerateService userReportGenerateService;
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
		
		LOGGER.trace("Entered PwCMAUserReportGenerateServlet.doPost()");
		try {
			
			userReportGenerateService.generateCSVfile();
			CommonUtils.writeResponse(response, "MA user report CSV file generated succesfully");
			
		} catch (RepositoryException | IOException e) {
			ExceptionLogger.logExceptionMessage("PwCMAUserReportGenerateServlet:doPost. Error while getting csv report", e);
			try {
				response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (final IOException ioExcep) {
				LOGGER.error("PwCMAUserReportGenerateServlet.doPost() :: Can not send error 500!", ioExcep);
			}
		}
	}
	
}
