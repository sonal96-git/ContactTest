package com.pwc.actueel.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.pwc.actueel.service.RssFeedGenerator;
import com.pwc.actueel.xml.model.Channel;

/**
 * Fetches language-codes from the URL’s query parameters and passes on to RssFeedGenerator service and writes the
 * returned response XML into the response.
 */
@Component(service = { Servlet.class }, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
					"sling.servlet.paths=" + "/bin/servlet/pwcRssFeed",
})
@Designate(ocd = RssFeedServlet.Config.class)
public class RssFeedServlet extends SlingSafeMethodsServlet {
	
    private static final String CONTENT_TYPE_XML = "text/xml";
    private static final String QUERY_PARAM_LANGUAGE_CODE = "lang";
    private static final String DEFAULT_LANGUAGE_CODE = "nl";
    private static final String SUPPORTED_EXTENSION = "rss";
    private static final String EMPTY_EXTENSION_ERROR_MSG = "Unsupported Operation. Extension not found.";
    private static final String UNSUPPORTED_EXTENSION_ERROR_MSG = "Unsupported Extension Found: ";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String EDGE_CONTROL = "Edge-Control";
    public static final String ENABLE_DISPATCHER_CACHING = "Enable Dispatcher Caching";
    public static final String DISPATCHER_RESP_HEADER = "Dispatcher";
    
    @ObjectClassDefinition(name = "PwC Actueel Rss Feed Servlet ", description = "")
    @interface Config {
    	@AttributeDefinition(name = CACHE_CONTROL,
    						description = "Time in seconds to cache response in the browser in the format max-age=<seconds>. Example: max-age=500",
    						type = AttributeType.STRING)
    	public String cache_control();
    	
    	@AttributeDefinition(name = EDGE_CONTROL,
				description = "Time to cache response in Akamai in the format max-age=<numeric-time><unit-of-time>. "
	                      + "Unit of time can be h for hours, m for minutes and d for days. Example: max-age=8h for 8 hours.",
				type = AttributeType.STRING)
    	public String edge_control();
    	
    	@AttributeDefinition(name = ENABLE_DISPATCHER_CACHING,
				description = "Enable caching the response in Dispatcher",
				type = AttributeType.BOOLEAN)
    	public boolean dispatcher_caching();
    }
    
    private static String cacheControl = "no-cache, max-age=0";
    private static String edgeControl = "max-age=8h";
    private static Boolean cacheInDispatcher = false;
    
    @Reference
    private RssFeedGenerator rssFeedGenerator;

    @Activate
    @Modified
    protected void activate(final RssFeedServlet.Config context) {
    	cacheControl = context.cache_control();
        edgeControl = context.edge_control();
        cacheInDispatcher = context.dispatcher_caching();
    }
    
    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final String extension = request.getRequestPathInfo().getExtension();
        if (SUPPORTED_EXTENSION.equals(extension)) {
            response.setContentType(CONTENT_TYPE_XML);
            response.setHeader(CACHE_CONTROL, cacheControl);
            response.setHeader(EDGE_CONTROL, edgeControl);
            if (!cacheInDispatcher) {
                response.setHeader(DISPATCHER_RESP_HEADER, "no-cache");
            }
            String[] languageCodes = request.getParameterValues(QUERY_PARAM_LANGUAGE_CODE);
            languageCodes = languageCodes == null || languageCodes.length == 0 ? new String[] { DEFAULT_LANGUAGE_CODE }
                    : languageCodes;
            final PrintWriter writer = response.getWriter();
            final Channel channel = rssFeedGenerator
                    .getChannelContainingAllArticlesForTerritory(request.getResourceResolver(), languageCodes);
            rssFeedGenerator.marshalIntoWriter(channel, writer);
        } else {
            final String errMsg = extension == null ? EMPTY_EXTENSION_ERROR_MSG
                    : UNSUPPORTED_EXTENSION_ERROR_MSG + extension;
            response.sendError(SlingHttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, errMsg);
        }
    }
}
