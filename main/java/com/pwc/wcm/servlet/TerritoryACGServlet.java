package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.wcm.services.CountryTerritoryMapperService;

/**
 * Servlet to provide list of the territories and legal entities for contact profile.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Territory and Legal Entites List Provider Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/territoriesACG"
})
public class TerritoryACGServlet extends SlingSafeMethodsServlet {

    private static final String PAGEPATH = "pagepath";
    private static final String PATTERN = "/content/(?:dam/pwc|pwc)/(\\w{2})*";
    
    @Reference
    private CountryTerritoryMapperService countryTerritoryMapperService;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/JSON");
        response.setCharacterEncoding("UTF-8");
        JSONArray jsonArray = new JSONArray();
        String pageURL = request.getParameter(PAGEPATH);
        String territoryCode = "";
        if(pageURL !=null) {
    		String territoryPattern = PATTERN;
    		Pattern p = Pattern.compile(territoryPattern); 
            Matcher m = p.matcher(pageURL);
            if(m.find()) {
            	territoryCode = m.group(1);
            }
        }
        Session session = request.getResourceResolver().adaptTo(Session.class);
        if(territoryCode!=null && territoryCode!="" )
        	jsonArray = countryTerritoryMapperService.getACGByTerritoryCode(territoryCode,session);
        response.getWriter().write(jsonArray.toString());
    }
}
