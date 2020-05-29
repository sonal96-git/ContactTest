package com.pwc.wcm.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.utils.CommonUtils;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Sample implementation of a Sling All Methods Servlet.",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "shorturl",
    "sling.servlet.extensions=" + "json"
})
public class ShortUrlServlet extends SlingAllMethodsServlet implements OptingServlet {
  
	private static final long serialVersionUID = -4675793650166224312L;

	private static final Logger log = LoggerFactory.getLogger(ShortUrlServlet.class);

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private AdminResourceResolver adminResourceResolver;

    private final String BITLY_ENABLED = "enabled";
    private final String BITLY_ACCESS = "bitly_access";
    private static final String CURRENT_URL_PARAM = "currentUrl";

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws
            ServletException, IOException {

        response.setContentType("application/json");
        
        String currentUrl = request.getParameter(CURRENT_URL_PARAM);
        Map<String, Object> queryParamMap = new HashMap<String, Object>(request.getParameterMap());
        queryParamMap.remove("currentUrl");

        JSONObject jsonResponse = new JSONObject();

        try {
    	   Configuration configSocial =  configAdmin.getConfiguration("PwC Social");

     	   final String bitlyToken = (String) configSocial.getProperties().get(BITLY_ACCESS);
     	   final boolean bitlyEnabled = PropertiesUtil.toBoolean(configSocial.getProperties().get(BITLY_ENABLED), false);

    	   PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    	   Page page = pageManager.getContainingPage(request.getResource());

    	   if (page == null) throw new Exception("Resource is not a page.");

			InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(page.getContentResource());
			Boolean isCommunitySite = inheritanceValueMap.getInherited("cq:isCommunitySite", false);
			String externalUrl;
			
			if (!isCommunitySite) {
				if (queryParamMap.size() > 0)
					throw new Exception("Query string not supported");
				externalUrl = CommonUtils.getExternalUrl(adminResourceResolver, configAdmin, page.getPath() + ".html");
				
			} else {
				externalUrl = currentUrl;
			}
			
			final String encodedExternalUrl = URLEncoder.encode(externalUrl, "UTF-8");
			
    	   String shortUrl = encodedExternalUrl;
    	   if (bitlyEnabled) {
    		   log.info("Calling bitly API for: " + externalUrl);
    		   shortUrl = CommonUtils.getShortUrl(bitlyToken, encodedExternalUrl);
    	   }

            jsonResponse.put("success", !encodedExternalUrl.equals(shortUrl));
            jsonResponse.put("short-url", shortUrl);
            jsonResponse.put("requestTime", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

            response.getWriter().write(jsonResponse.toString(2));
         } catch (Exception e) {
            log.error("Could not get short URL for: " + request.getResource().getPath());

            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(e.getMessage());
         }

    }

    @Override
    public final boolean accepts(SlingHttpServletRequest request) {
        return true;
    }

}
