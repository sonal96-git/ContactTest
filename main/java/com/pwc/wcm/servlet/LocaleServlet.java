package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.AdminResourceResolver;
import com.pwc.model.Country;
import com.pwc.model.Locale;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Get all locales",
    "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    "sling.servlet.paths=" + "/bin/locale",
})
public class LocaleServlet extends SlingAllMethodsServlet {

	@Reference
	private ResourceResolverFactory resolverFactory;
	@Reference
	SlingRepository slingRepository;
	@Reference
	AdminResourceResolver adminResourceResolver;

	private List<Country> countries;
	private String selectedCountry;
	private Log logger = LogFactory.getLog(LocaleServlet.class);
	private void initData(SlingHttpServletRequest request) {
		countries = new ArrayList<Country>();
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = adminResourceResolver.getAdminResourceResolver();
			Session session = resourceResolver.adaptTo(Session.class);
			Node site = session.getRootNode().getNode("content/pwc/global/referencedata/territories");
			NodeIterator territories = site.getNodes();
			while(territories.hasNext()){
				Node n = territories.nextNode();
				Country newCountry = new Country();
				List<Locale> locales = new ArrayList<Locale>();
				NodeIterator localeIt = n.getNodes();
				while(localeIt.hasNext()){
					Node localeNode = localeIt.nextNode();
					Locale newLocale = new Locale();
					String localePath=localeNode.getPath();
					int lastIndex = localeNode.getPath().lastIndexOf("/")+1;
					newLocale.setLocale(localePath.substring(lastIndex, localePath.length()));
					locales.add(newLocale);
				}
				newCountry.setCountryName(n.getProperty("territoryName").getString());
				newCountry.setCountryCode(n.getProperty("countryCode").getString().toLowerCase());
				newCountry.setLocaleList(locales);
				countries.add(newCountry);
			}

			Collections.sort(countries, new Comparator<Country>() {
				@Override
				public int compare(Country o1, Country o2) {
					return o1.getCountryName().compareTo(o2.getCountryName());
				}
			});

		} catch (Exception e) {
			logger.error("error", e);
			e.printStackTrace();
		}finally {
			if(resourceResolver!=null)
				resourceResolver.close();
		}
	}

	/*

	private Country getCountry(List<Country> list, String cName)
	{
		Country c = null;
		for(Country each:list){
			if(each.getCountryName().toLowerCase().equals(cName)){
				c = each;
				break;
			}
		}
		return c;
	}*/

	@Override
	protected void doPost(SlingHttpServletRequest request,
						  SlingHttpServletResponse response) throws ServletException,
			IOException {
		/*Session session=request.getResourceResolver().adaptTo(Session.class);
		String nodePath = request.getContextPath();
		selectedCountry = request.getParameter("selectedCountry");
		try {
			Node root = session.getRootNode();
			String path = root.getPath();
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}*/
		//response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		//response.setDateHeader("Expires", 200);
		selectedCountry = request.getParameter("selectedCountry");
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		String nodeId = request.getParameter("nodeId");
		if (nodeId.toLowerCase().equals("root")) {
			initData(request);
			JSONArray array = new JSONArray();
			for(Country eachCountr:countries){
				JSONObject countryObj = new JSONObject();
				try {
					countryObj.put("text", eachCountr.getCountryName());
					countryObj.put("id", eachCountr.getCountryCode());
					if(!selectedCountry.equals("na")&&countryExistIn(eachCountr.getCountryCode())){
						countryObj.put("expanded", true);
					}
					JSONArray children = new JSONArray();
					for(Locale eachLocal: eachCountr.getLocaleList()){
						JSONObject localObj = new JSONObject();
						localObj.put("id", eachLocal.getLocale() + "_"+eachCountr.getCountryCode().toLowerCase());
						localObj.put("text", eachLocal.getLocale());
						localObj.put("leaf", false);

						if(selectedCountry!=null&&!selectedCountry.equals("na") && countryExistIn(eachCountr.getCountryCode().toLowerCase()) &&localeExistIn(eachLocal.getLocale().toLowerCase()))
							localObj.put("checked", true);
						else
							localObj.put("checked", false);
						children.put(localObj);
					}
					countryObj.put("children", children);
					array.put(countryObj);
				} catch (Exception e) {
					logger.error("error in doPOST ", e);
					e.printStackTrace();
				}
			}
			response.getWriter().write(array.toString());
		}
	}

	private boolean countryExistIn(String country){
		boolean exist=false;
		String[] locales = selectedCountry.split(",");
		for(String eachlocal: locales){
			if(country.toLowerCase().equals(eachlocal.split("_")[1]))
			{
				exist=true;
				break;
			}
		}
		return exist;
	}

	private boolean localeExistIn(String locale){
		boolean exist=false;
		String[] locales = selectedCountry.split(",");
		for(String eachlocal: locales){
			if(locale.toLowerCase().equals(eachlocal.split("_")[0]))
			{
				exist=true;
				break;
			}
		}
		return exist;
	}

}