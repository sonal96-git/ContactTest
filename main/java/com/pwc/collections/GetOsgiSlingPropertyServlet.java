package com.pwc.collections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.TidyJSONWriter;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.extensions=" + "json",
    "sling.servlet.selectors=" + "get.collections.property"
})
public class GetOsgiSlingPropertyServlet extends SlingSafeMethodsServlet  {

	private static final long serialVersionUID = 1L;

	@Reference
	private OsgiCollectionsConfiguration osgiConfigProperties;
	
	private Map<String, Integer> map = new HashMap<String,Integer>();
	
	@Override
	protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws ServletException, IOException {
		map.put("collection_limit", this.osgiConfigProperties.getCollectionLimit());
		map.put("contact_collection_limit", this.osgiConfigProperties.getContactCollectionLimit());
		map.put("event_collection_limit", this.osgiConfigProperties.getEventCollectionLimit());
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");

		String prop = req.getParameter("propertyname");
		int res = (map.containsKey(prop)) ? res = map.get(prop) : osgiConfigProperties.COLLECTIONS_DEFAULT_LIMIT;
		
		try {
			new TidyJSONWriter(resp.getWriter()).object()
			.key("RESULT")
			.value(String.valueOf(res))
			.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
