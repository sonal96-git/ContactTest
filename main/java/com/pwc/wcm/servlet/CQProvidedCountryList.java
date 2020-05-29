package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.pwc.model.DropDownEntity;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Get all countries list provided under libs",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "pwc/components/content/datasource/bin/defaultCountryList",
        "sling.servlet.paths="+ "/bin/defaultCountryList"
})

public class CQProvidedCountryList extends SlingAllMethodsServlet {
    
    public static final String SERVLET_PATH = "/bin/defaultCountryList";
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CQProvidedCountryList.class);
    
    private Object[] initData(SlingHttpServletRequest request, Map<String, String> treeMap) {
        final Resource resource = request.getResourceResolver().getResource("/apps/wcm/core/resources/languages");
        final Iterator<Resource> defaultcountryList = resource.listChildren();
        
        while (defaultcountryList.hasNext()) {
            final Resource languageNode = defaultcountryList.next();
            final ValueMap languageMap = languageNode.getValueMap();
            final String name = languageNode.getName().replace("_", "-");
            LOGGER.debug("Language Node Name:  " + name);
            final String language = (String) languageMap.get("language");
            LOGGER.debug(" Language Name:   " + language);
            if (language != null && !language.trim().equals("")) {
                // treemap used for sorting
                treeMap.put(language, name);
            }
        }
        
        Set keys = treeMap.keySet();
        Object[] keysArr = keys.toArray();
        return keysArr;
    }
    
    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> treeMap = new TreeMap<String, String>();
        Object[] keysArr = initData(request, treeMap);
        Resource resource = request.getResource();
        if (resource.getPath().equalsIgnoreCase(SERVLET_PATH))
            formatForClassic(response, keysArr, treeMap);
        else
            formatForTouch(request, keysArr, treeMap);
    }
    
    /**
     * Format the given {@link List} of {@link DropDownEntity} for Classic UI and write the data in {@link SlingHttpServletResponse}.
     * 
     * @param response {@link SlingHttpServletResponse}
     * @param keysArr Array of {@link Object}
     * @param treeMap
     * @throws IOException {@link IOException}
     */
    private void formatForClassic(final SlingHttpServletResponse response, final Object[] keysArr, final Map<String, String> treeMap)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        final JSONArray countryList = new JSONArray();
        for (int c = 0; c < keysArr.length; c++) {
            final Map<String, String> countryMap = new HashMap<String, String>();
            countryMap.put("text", keysArr[c].toString());
            countryMap.put("value", treeMap.get(keysArr[c].toString()));
            countryList.put(countryMap);
        }
        LOGGER.debug("CQProvidedCountryList formatForClassic() : Writing Array of country in response {}", countryList);
        response.getWriter().write(countryList.toString());
    }
    
    /**
     * Format the given country data of Array {@link Object} & treeMap for touch UI and set the data as attribute in
     * {@link SlingHttpServletRequest}.
     * 
     * @param request {@link SlingHttpServletRequest}
     * @param keysArr Array of {@link Object}
     * @param treeMap
     */
    private void formatForTouch(final SlingHttpServletRequest request, final Object[] keysArr, final Map<String, String> treeMap) {
        
        // set fallback
        request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
        ResourceResolver resolver = request.getResourceResolver();
        
        // Create an ArrayList to hold data
        List<Resource> countryArray = new ArrayList<Resource>();
        
        ValueMap valueMap = null;
        
        for (Object key : keysArr) {
            valueMap = new ValueMapDecorator(new HashMap<String, Object>());
            valueMap.put("text", key.toString());
            valueMap.put("value", treeMap.get(key.toString()));
            countryArray.add(new ValueMapResource(resolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, valueMap));
        }
        
        LOGGER.debug("CQProvidedCountryList formatForTouch() : Setting Array of country as attribute in request {}", countryArray);
        // Create a DataSource that is used to populate the drop-down control
        DataSource dataSource = new SimpleDataSource(countryArray.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }
    
}
