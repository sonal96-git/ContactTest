package com.pwc.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.service.webservice.JobResultsFromBrassRingWS;

/**
 * Based on the Input Parameters, this servlet returns the request or response
 * XML based on Query String parameter(output) receives a response-XML using
 * GSON API.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= XmlRequestAndResponseFromWS",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.resourceTypes=" + "pwc/components/content/job-description-api",
		"sling.servlet.extensions=" + "debug"    
})
public class XMLRequestAndResponseFromBrassRingWsServlet extends SlingAllMethodsServlet {
	
    @Reference
    private JobResultsFromBrassRingWS xmlResponseService;
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(XMLRequestAndResponseFromBrassRingWsServlet.class);

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
	final ValueMap resourceProperties = request.getResource().adaptTo(ValueMap.class);

	final String clientId = resourceProperties.get("clientid", String.class);
	final String siteId = resourceProperties.get("siteid", String.class);

	final String jobId = request.getParameter("jobid");
	final String keywordCode = resourceProperties.get("qkeyword", String.class);

	final String output = request.getParameter("output");
	
	if (null != output && output.equalsIgnoreCase("request")) {
	    final String inputXml = xmlResponseService.createInputXmlForJobDescription(clientId, siteId, jobId,
	            keywordCode);
	    writeResponse(response, inputXml, "application/xml");
	} else {
	    final String responseXml = xmlResponseService.generateInputAndGetJobDescriptionFromBrassRingWS(clientId,
	            siteId, jobId, keywordCode);

	    if (responseXml.isEmpty()) {
		response.sendError(HttpServletResponse.SC_NO_CONTENT, "searchagain");
		return;
	    } else {
		writeResponse(response, responseXml, "application/xml");
	    }
	}
	
    }
    
    private void writeResponse(final SlingHttpServletResponse response, final String dataToBeWritten,
            final String contentType) {
	response.setContentType(contentType);
	try {
	    response.getWriter().write(dataToBeWritten);
	} catch (final IOException ioExp) {
	    logger.error(ioExp.getMessage(), ioExp);
	}
    }
}
