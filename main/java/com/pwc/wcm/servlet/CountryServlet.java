package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.pwc.model.Country;
import com.pwc.model.DropDownEntity;
import com.pwc.util.CountryComparator;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Get all countries for contact",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + CountryServlet.SERVLET_PATH,
    "sling.servlet.resourceTypes=" + "pwc/components/content/datasource/bin/country",
})
public class CountryServlet extends SlingAllMethodsServlet {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CountryServlet.class);
    
    public static final String SERVLET_PATH = "/bin/country";
    
    private void initData(SlingHttpServletRequest request, List<DropDownEntity> countries) {
        String jcrPath = "/content/pwc/global/referencedata/countries";
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource res = resourceResolver.getResource(jcrPath);
        ValueMap territories = res.adaptTo(ValueMap.class);
        String[] territoryList = (String[]) territories.get("country-code-name");
        List<Country> countryList = new ArrayList<Country>();
        for (String t : territoryList) {
            String[] val = t.split(":");
            Country c = new Country(val[1], val[0]);
            countryList.add(c);
        }
        Collections.sort(countryList, new CountryComparator());
        countries.clear();
        for (Country c : countryList) {
            DropDownEntity territory = new DropDownEntity(c.getCountryName(), c.getCountryName(), c.getShortName());
            countries.add(territory);
        }
    }
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        Resource resource = request.getResource();
        List<DropDownEntity> countries = new ArrayList<DropDownEntity>();
        initData(request, countries);
        if (resource.getPath().equalsIgnoreCase(SERVLET_PATH))
            formatForClassic(response, countries);
        else
            formatForTouch(request, countries);
    }
    
    /**
     * Format the given {@link List} of {@link DropDownEntity} for classic UI and set the data in {@link SlingHttpServletResponse}.
     * 
     * @param response {@link SlingHttpServletResponse}
     * @param countries {@link List} of {@link DropDownEntity}
     * @throws IOException {@link IOException}
     */
    private void formatForClassic(SlingHttpServletResponse response, List<DropDownEntity> countries) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONArray countryList = new JSONArray();
        for (DropDownEntity c : countries) {
            Map<String, String> list = new HashMap<String, String>();
            list.put("text", c.getText());
            list.put("label", c.getLabel());
            list.put("value", c.getValue());
            countryList.put(list);
        }
        LOGGER.debug("CountryServlet formatForClassic() : Writing JSON Array of country in response {}", countryList);
        response.getWriter().write(countryList.toString());
    }
    
    /**
     * Format the given {@link List} of {@link DropDownEntity} for touch UI and set the data as attribute in
     * {@link SlingHttpServletRequest}.
     * 
     * @param request {@link SlingHttpServletRequest}
     * @param countries {@link List} of {@link DropDownEntity}
     */
    private void formatForTouch(final SlingHttpServletRequest request, final List<DropDownEntity> countries) {
        
        // set fallback
        request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
        ResourceResolver resolver = request.getResourceResolver();
        
        // Create an ArrayList to hold data
        List<Resource> countryArray = new ArrayList<Resource>();
        
        ValueMap valueMap = null;
        
        for (DropDownEntity country : countries) {
            valueMap = new ValueMapDecorator(new HashMap<String, Object>());
            valueMap.put("value", country.getValue());
            valueMap.put("text", country.getText());
            countryArray.add(new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
        }
        
        LOGGER.debug("CountryServlet formatForTouch() : Setting Array of country as attribute in request {}", countryArray);
        // Create a DataSource that is used to populate the drop-down control
        DataSource dataSource = new SimpleDataSource(countryArray.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
        
    }
    
}
