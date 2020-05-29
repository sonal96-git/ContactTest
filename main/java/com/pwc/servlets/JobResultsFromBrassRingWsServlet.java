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
 * containing Job Results from JobResultsFromBrassRingWS, feeds it further to
 * the UnmarshallXml utility and finally converts the result into Jason using
 * Google's Gson Api and writes it to the SlingHttpServletResponse object.
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "pwc/components/content/job-results-api",
    "sling.servlet.extensions=" + "service"
})
public class JobResultsFromBrassRingWsServlet extends SlingAllMethodsServlet {
	
	@Reference
	private JobResultsFromBrassRingWS xmlResponseService;
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(JobResultsFromBrassRingWsServlet.class);

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
		final ValueMap resourceProperties = request.getResource().adaptTo(ValueMap.class);
		String responseXml = null;
		final String clientId = resourceProperties.get("clientid", String.class);
		final String siteId = resourceProperties.get("siteid", String.class);

		String grade = null;
		String industry = null;
		String gradeCode = null;
		String industryCode = null;
		String localregion = null;
		String applicationdeadline = null;
		String entryroute = null;
		String entryroutegroup = null;
		String intakeyear = null;
		String programmetype = null;
		String startseason = null;

		String localregionCode = null;
		String applicationdeadlineCode = null;
		String entryrouteCode = null;
		String entryroutegroupCode = null;
		String intakeyearCode = null;
		String programmetypeCode = null;
		String startseasonCode = null;

		final String country = request.getParameter("country");
		final String location = request.getParameter("location");
		final String service = request.getParameter("service");
		final String specialism = request.getParameter("specialism");
		final String keyword = request.getParameter("keyword");

		final String countryCode = resourceProperties.get("qcountry", String.class);
		final String locationCode = resourceProperties.get("qlocation", String.class);
		final String serviceCode = resourceProperties.get("qservice", String.class);
		final String specialismCode = resourceProperties.get("qspecialism", String.class);
		final String keywordCode = resourceProperties.get("qkeyword", String.class);

		final String selectedRendition = resourceProperties.get("selectRendition", String.class);
		if (!"Student".equals(selectedRendition)) {
			grade = request.getParameter("grade");
			industry = request.getParameter("industry");
			gradeCode = resourceProperties.get("qgrade", String.class);
			industryCode = resourceProperties.get("qindustry", String.class);
		} else {
			localregion = request.getParameter("localregion");
			applicationdeadline = request.getParameter("applicationdeadline");
			entryroute = request.getParameter("entryroute");
			entryroutegroup = request.getParameter("entryroutegroup");
			intakeyear = request.getParameter("intakeyear");
			programmetype = request.getParameter("programmetype");
			startseason = request.getParameter("startseason");

			localregionCode = resourceProperties.get("sqregion", String.class);
			applicationdeadlineCode = resourceProperties.get("sqdeadline", String.class);
			entryrouteCode = resourceProperties.get("sqentryroute", String.class);
			entryroutegroupCode = resourceProperties.get("sqentryroutegroup", String.class);
			intakeyearCode = resourceProperties.get("sqintakeyear", String.class);
			programmetypeCode = resourceProperties.get("sqprogramme", String.class);
			startseasonCode = resourceProperties.get("sqstartseason", String.class);
		}

		if (!"Student".equals(selectedRendition)) {
			responseXml = xmlResponseService.generateInputAndGetJobResultsFromBrassRingWS(clientId, siteId, country, location, service, specialism,
					industry, keyword, grade, countryCode, locationCode, serviceCode, specialismCode, industryCode, keywordCode, gradeCode);
		} else {
			responseXml = xmlResponseService.generateInputAndGetJobResultsFromBrassRingWS(clientId, siteId, country, location, service, specialism,
					keyword, localregion, applicationdeadline, entryroute, entryroutegroup, intakeyear, programmetype, startseason, countryCode,
					locationCode, serviceCode, specialismCode, keywordCode, localregionCode, applicationdeadlineCode, entryrouteCode,
					entryroutegroupCode, intakeyearCode, programmetypeCode, startseasonCode);
		}
		if (responseXml.isEmpty()) {
			response.sendError(HttpServletResponse.SC_NO_CONTENT, "searchagain");
			return;
		} else {
			final List<Job> listOfJobs = UnmarshallXml.unmarshallXml(responseXml);
			final List<Map<String, String>> listOfColValues = new ArrayList<Map<String, String>>();
			if (listOfJobs != null) {
				for (final Job job : listOfJobs) {
					job.setQuestions(job.getQuestions());
					listOfColValues.add(job.getQuestionsMap());
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
