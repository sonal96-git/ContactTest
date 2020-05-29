package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Get all leader and team member",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.methods=" + HttpConstants.METHOD_POST,
		"sling.servlet.paths=" + "/bin/leaders",
})
public class LeaderTeamMemberServlet extends SlingAllMethodsServlet {
	private Map<String, String[]> map;

	private void initData(SlingHttpServletRequest request) {
		map = new HashMap<String, String[]>();
		String field1 ="Industry" + "_" + request.getParameter("type").toLowerCase();
		String field2 ="Service" + "_" + request.getParameter("type").toLowerCase();
		if(request.getParameter("type").toLowerCase().equals("global")){
			String[] subField1 = new String[2];
			subField1[0] = "Industry 1" + "_" +request.getParameter("type").toLowerCase();
			subField1[1] = "Industry 2" + "_" +request.getParameter("type").toLowerCase();
			map.put(field1, subField1);
			
			String[] subField2 = new String[2];
			subField2[0] = "Service 1" + "_" +request.getParameter("type").toLowerCase();
			subField2[1] = "Service 2" + "_" +request.getParameter("type").toLowerCase();
			map.put(field2, subField2);
			
		}
		else if(request.getParameter("type").toLowerCase().equals("local")){
				String[] subField1 = new String[2];
				subField1[0] = "Industry 1" + "_" +request.getParameter("type").toLowerCase();
				subField1[1] = "Industry 2" + "_" +request.getParameter("type").toLowerCase();
				map.put(field1, subField1);
				
				String[] subField2 = new String[2];
				subField2[0] = "Service 1" + "_" +request.getParameter("type").toLowerCase();
				subField2[1] = "Service 2" + "_" +request.getParameter("type").toLowerCase();
				map.put(field2, subField2);
				
			}
	}

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		String nodeId = request.getParameter("nodeId");
		if (nodeId.toLowerCase().equals("root")) {
			initData(request);
			JSONArray array = new JSONArray();
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String field = (String) pairs.getKey();
				String[] subfields = (String[]) pairs.getValue();
				JSONObject teams = new JSONObject();
				try {
					teams.put("text", field);
					teams.put("id", field);
					teams.put("expanded", true);
					if (subfields.length > 0) {
						JSONArray children = new JSONArray();
						for (String eachSubField : subfields) {
							JSONObject subfield = new JSONObject();
							subfield.put("id", eachSubField);
							subfield.put("text", eachSubField);
							subfield.put("leaf", false);
							subfield.put("checked", false);
							children.put(subfield);
						}
						teams.put("children", children);
					}

					array.put(teams);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				it.remove(); // avoids a ConcurrentModificationException
			}
			String arrayString = array.toString();
			response.getWriter().write(arrayString);

		}
	}

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		initData(request);
		JSONArray array = new JSONArray();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String country = (String) pairs.getKey();
			Value[] locales = (Value[]) pairs.getValue();
			JSONObject countryObj = new JSONObject();
			try {
				countryObj.put("text", country);
				countryObj.put("id", country);
				if (locales.length > 0) {
					JSONArray children = new JSONArray();
					for (Value local : locales) {
						JSONObject localObj = new JSONObject();
						localObj.put("id", local.getString());
						localObj.put("text", local.getString());
						localObj.put("leaf", false);
						localObj.put("checked", false);
						children.put(localObj);
					}
					countryObj.put("children", children);
				}

				array.put(countryObj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ValueFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		String arrayString = array.toString();
		response.getWriter().write(arrayString);

	}
}