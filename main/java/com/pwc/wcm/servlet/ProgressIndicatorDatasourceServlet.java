package com.pwc.wcm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import org.apache.commons.lang3.StringUtils;
import com.day.cq.commons.jcr.JcrConstants;
import com.google.gson.Gson;

/**
 * Servlet to provide list of the territories and legal entities for contact
 * profile.
 */
@Component(service = Servlet.class, immediate = true, property = { Constants.SERVICE_DESCRIPTION + "= Get Id from Page",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/sectionidlist" })
public class ProgressIndicatorDatasourceServlet extends SlingSafeMethodsServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressIndicatorDatasourceServlet.class);

	final String splitString = "_cq_dialog.html";

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		ResourceResolver resolver = request.getResourceResolver();
		String path = request.getParameter("path");
		Session session = resolver.adaptTo(Session.class);
		Resource parentResource = resolver.getResource(path);
		Node node = parentResource.adaptTo(Node.class);
		NodeIterator childrens;
		try {
			childrens = node.getNodes();
			String outerNodeName ="";
			if (childrens.hasNext()) {	
				outerNodeName = childrens.nextNode().getName();
			}
			Resource childrenResource = resolver.getResource(path+"/"+outerNodeName);
			Node childNode = childrenResource.adaptTo(Node.class);
			NodeIterator childrensItr = childNode.getNodes();
			JSONArray array = new JSONArray();
			if (childrensItr.hasNext()) {
				while (childrensItr.hasNext()) {
					Node childs = childrensItr.nextNode();
					String id = childs.getProperty("id").getString();
					String title = childs.getProperty("title").getString();
					JSONObject obj = new JSONObject();
					obj.put("id", id);
					obj.put("title", title);
					array.put(obj);
				}
			}
			LOGGER.debug(array+"");
			out.println(array);
			session.save();
			session.logout();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
