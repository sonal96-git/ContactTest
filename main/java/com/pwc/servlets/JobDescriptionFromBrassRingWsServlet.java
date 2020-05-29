/*
 * 
 */
package com.pwc.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pwc.model.xml.response.Job;
import com.pwc.service.webservice.JobResultsFromBrassRingWS;
import com.pwc.util.UnmarshallXml;

/**
 * Based on the Input Parameters, this servlet receives a response-Xml
 * containing Job Description from JobResultsFromBrassRingWS, feeds it further
 * to the UnmarshallXml utility and finally converts the result into Jason using
 * Google's Gson Api and writes it to the SlingHttpServletResponse object.
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "pwc/components/content/job-description-api",
    "sling.servlet.extensions=" + "service"    
})
public class JobDescriptionFromBrassRingWsServlet extends SlingAllMethodsServlet {
	
	@Reference
	private JobResultsFromBrassRingWS xmlResponseService;
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(JobDescriptionFromBrassRingWsServlet.class);

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
		final ValueMap resourceProperties = request.getResource().adaptTo(ValueMap.class);
		final String clientId = resourceProperties.get("clientid", String.class);
		final String siteId = resourceProperties.get("siteid", String.class);

		final String jobId = request.getParameter("jobid");
		final String keywordCode = resourceProperties.get("qkeyword", String.class);
		final String jobDetailQues=resourceProperties.get("qjobDetailQues",String.class);
		final String rendition=resourceProperties.get("selectRendition",String.class);
		String responseXml;
		if(rendition.equals("m1career"))
		  responseXml= xmlResponseService.generateInputAndGetJobDescriptionFromBrassRingWS(clientId, siteId, jobId, keywordCode,jobDetailQues);
		else
		  responseXml=xmlResponseService.generateInputAndGetJobDescriptionFromBrassRingWS(clientId, siteId, jobId, keywordCode);
		
		
		if (responseXml.isEmpty()) {
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "searchagain");
			return;
		} else {
			final List<Job> listOfJobs = UnmarshallXml.unmarshallXml(responseXml);
			final List<Map<String, String>> listOfColValues = new ArrayList<Map<String, String>>();
			if (listOfJobs != null) {
				for (final Job job : listOfJobs) {
					job.setQuestions(job.getQuestions());
					final Map<String, String> questionMap = job.getQuestionsMap();
					questionMap.put("JobDescription", job.getJobDescription().getValue());
					questionMap.put("JobDetailLink", job.getJobDetailLink());
					listOfColValues.add(questionMap);
				}
			}
			if (!listOfColValues.isEmpty()) {
				writeResponseAsJson(response, listOfColValues);
			}
		}
	}

	private void writeResponseAsJson(final SlingHttpServletResponse response, final List<Map<String, String>> listOfColValues) {
		final String json = new Gson().toJson(listOfColValues);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().write(json);
		} catch (final IOException ioExp) {
			logger.error(ioExp.getMessage(), ioExp);
		}
	}
}
