package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.pwc.model.DropDownEntity;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Get all countries for contact",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/province"
})
public class ProvinceServlet extends SlingAllMethodsServlet {
	
	private Log logger = LogFactory.getLog(ProvinceServlet.class);
	private List<DropDownEntity> provinces = new ArrayList<DropDownEntity>();
	private void initData(String country,SlingHttpServletRequest request) throws RepositoryException{

		String cjcrPath = "/content/pwc/global/referencedata/countries";
		String fullCountryName = "";
		ResourceResolver resourceResolver = request.getResourceResolver();
		Resource res = resourceResolver.getResource(cjcrPath);
		ValueMap territories = res.adaptTo(ValueMap.class);
		String[] territoryList = (String[])territories.get("country-code-name");


		for(String t: territoryList){

			String[] val = t.split(":");

			if(val[0].toLowerCase().equals(country.toLowerCase()))
			{
				fullCountryName = val[1];
				break;
			}

		}
		provinces.clear();
		String jcrPath = "/content/pwc/global/referencedata/secondary";
		String queryString = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/content/pwc/global/referencedata/stateprovinces]) and s.[primary-reference-data]='" + fullCountryName+ "'";
		Session session = request.getResourceResolver().adaptTo(Session.class);

		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
		QueryResult nodes = query.execute();
		List<String> provineList = new ArrayList<String>();
		for (NodeIterator nodeResultsIt = nodes.getNodes(); nodeResultsIt.hasNext(); ){
			Node n = nodeResultsIt.nextNode();
			Value[] values =  n.getProperty("reference-data").getValues();
			for(Value p : values){
				String province = p.getString().trim();
				provineList.add(province);

			}
		}
		Collections.sort(provineList);
		for(String p: provineList){
			DropDownEntity de = new DropDownEntity(p, p, p);
			provinces.add(de);
		}

	}
	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
	IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		String country = request.getParameter("country");//http://localhost:4505/bin/province?country=us
		try {
			initData(country,request);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray jsonList = new JSONArray();
		for(DropDownEntity c: provinces){
			Map<String, String> list= new HashMap<String, String>();
			list.put("text", c.getText());
			list.put("label", c.getLabel());
			list.put("value", c.getValue());
			jsonList.put(list);
		}
		response.getWriter().write(jsonList.toString());
	}

}
