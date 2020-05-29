package com.pwc.topic.redirect.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.pwc.topic.redirect.service.TopicTerritoryMapper;

/**
 * Fetches language-codes from the URL’s query parameters and passes on to RssFeedGenerator service and writes the
 * returned response XML into the response.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Topic To Territory Mapping Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/servlet/getTopicConfigs",
})
public class TopicTerritoryMappingServlet extends SlingSafeMethodsServlet {
    private static final String CONTENT_TYPE_RESPONSE = "application/json";
    private static final String PARAM_TOPIC_TYPE = "type";
    private static final String PARAM_TERRITORY_CODE = "territory";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_DEFAULT_LOCALE = "default";
    private static final String SUPPORTED_EXTENSION = "json";
    private static final String EMPTY_EXTENSION_ERROR_MSG = "Unsupported Operation. Extension not found.";
    private static final String UNSUPPORTED_EXTENSION_ERROR_MSG = "Unsupported Extension Found: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicTerritoryMappingServlet.class);
    
    @Reference
    private TopicTerritoryMapper topicTerritoryMapper;
    
    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final String extension = request.getRequestPathInfo().getExtension();
        if (SUPPORTED_EXTENSION.equalsIgnoreCase(extension)) {
            response.setContentType(CONTENT_TYPE_RESPONSE);
            String topicType = request.getParameter(PARAM_TOPIC_TYPE);
            String territoryCode = request.getParameter(PARAM_TERRITORY_CODE);
            String locale = request.getParameter(PARAM_LOCALE);
            
            if (topicType == null) {
                writeResponseAsJson(response, topicTerritoryMapper.getAllTopics());
            } else if (territoryCode == null) {
                writeResponseAsJson(response, topicTerritoryMapper.getAllTerritoriesForTopic(topicType));
            } else if (locale == null) {
                writeResponseAsJson(response, topicTerritoryMapper.getAllLocaleMappingsForTopicAndTerritory(topicType, territoryCode));
            } else if (locale.trim().equalsIgnoreCase(PARAM_DEFAULT_LOCALE)) {
                writeResponseAsJson(response, topicTerritoryMapper.getDefaultHomepageUrlForTopicAndTerritory(topicType, territoryCode));
            } else if (StringUtils.isNotBlank(locale)) {
                writeResponseAsJson(response,
                        topicTerritoryMapper.getTopicHomepageUrlForTerritoryAndLocale(topicType, territoryCode, locale));
            } else {
                response.sendError(SlingHttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Please check the passed parameter values for query parameters: " + PARAM_TOPIC_TYPE + ", " + PARAM_TERRITORY_CODE
                                + ", " + PARAM_LOCALE);
            }
        } else {
            final String errMsg = extension == null ? EMPTY_EXTENSION_ERROR_MSG : UNSUPPORTED_EXTENSION_ERROR_MSG + extension;
            response.sendError(SlingHttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, errMsg);
        }
    }
    
    private void writeResponseAsJson(final SlingHttpServletResponse response, final Object outputModelRoot) {
        final String json = new GsonBuilder().setPrettyPrinting().create().toJson(outputModelRoot);
        response.setContentType(CONTENT_TYPE_RESPONSE);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(json);
        } catch (final IOException ioExp) {
            LOGGER.error(ioExp.getMessage(), ioExp);
        }
    }
}
