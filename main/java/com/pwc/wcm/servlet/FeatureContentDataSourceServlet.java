package com.pwc.wcm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pwc.wcm.model.FeatureContentModel;

/**
 * Servlet to provide list of the territories and legal entities for contact
 * profile.
 */
@Component(service = Servlet.class, immediate = true, property = {
		Constants.SERVICE_DESCRIPTION + "= Data Storing Servlet for Feature content",
		"sling.servlet.methods=" + HttpConstants.METHOD_POST, "sling.servlet.paths=" + "/bin/featuredataservlet" })
public class FeatureContentDataSourceServlet extends SlingAllMethodsServlet {

	final String splitString = "_cq_dialog.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureContentDataSourceServlet.class);

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		ResourceResolver resolver = request.getResourceResolver();
		String path = request.getParameter("path");
		Session session = resolver.adaptTo(Session.class);
		Resource parentResource = resolver.getResource(path);
		if (parentResource != null) {
			Node node = parentResource.adaptTo(Node.class);
			FeatureContentModel dataModel = new FeatureContentModel();
			JSONArray array = new JSONArray();
			try {
				NodeIterator childrens = node.getNodes();
				String heading = "";
				String text = "";
				String link = "";
				String order = "";
				String target = "";
				String statLine1 = "";
				String statLine2 = "";
				String statLabel = "";
				String statLinkText = "";
				String statsourceheading = "";
				String statLinkUrl = "";
				String panelType = "";
				String imagePath = "";
				if (node.hasProperty("panelType")) {
					panelType = node.getProperty("panelType").getString();
				}
				if (panelType.equalsIgnoreCase("solid")) {
					panelType = "Solid Color Panel";
				}
				if (panelType.equalsIgnoreCase("image")) {
					panelType = "Image Panel";
				}
				if (panelType.equalsIgnoreCase("stat")) {
					panelType = "Stat Bar";
				}
				if (childrens.hasNext()) {
					int index = 0;
					while (childrens.hasNext()) {
						index ++;
						Node childNode = childrens.nextNode();
						
						if (childNode.hasProperty("heading")) {
							heading = childNode.getProperty("heading").getString();
						}else {
							heading = "";
						}
						
						if (childNode.hasProperty("text")) {
							text = childNode.getProperty("text").getString();
						}else {
							text = "";
						}
						
						if (childNode.hasProperty("link")) {
							link = childNode.getProperty("link").getString();
						}else {
							link = "";
						}
						
						if (childNode.hasProperty("order")) {
							order = childNode.getProperty("order").getString();
						}else {
							order = "";
						}

						if (childNode.hasProperty("target")) {
							target = childNode.getProperty("target").getString();
						}
						else {
							target = "";
						}
						
						if(childNode.hasProperty("fileReference")) {
							imagePath = childNode.getProperty("fileReference").getString();
						}
						else {
							imagePath = "";
						}
						
						if (childNode.hasProperty("statLine1")) {
							statLine1 = childNode.getProperty("statLine1").getString();
						}else {
							statLine1 = "";
						}
						
						if (childNode.hasProperty("statLine2")) {
							statLine2 = childNode.getProperty("statLine2").getString();
						}else {
							statLine2 = "";
						}
						
						if (childNode.hasProperty("statLabel")) {
							statLabel = childNode.getProperty("statLabel").getString();
						}else {
							statLabel = "";
						}
						
						if (childNode.hasProperty("statLinkText")) {
							statLinkText = childNode.getProperty("statLinkText").getString();
						}else {
							statLinkText = "";
						}
						
						if (childNode.hasProperty("statsourceheading")) {
							statsourceheading = childNode.getProperty("statsourceheading").getString();
						}else {
							statsourceheading = "";
						}
						
						if (childNode.hasProperty("statLinkUrl")) {
							statLinkUrl = childNode.getProperty("statLinkUrl").getString();
						}
						else {
							statLinkUrl = "";
						}
						
						dataModel.setTypeOfPanel(panelType);
						dataModel.setHeading(heading);
						dataModel.setText(text);
						dataModel.setURL(link);
						dataModel.setOrder(order);
						dataModel.setTarget(target);
						dataModel.setImage(imagePath);
						dataModel.setStatLine1(statLine1);
						dataModel.setStatLine2(statLine2);
						dataModel.setStatLabel(statLabel);
						dataModel.setSourceURL(statLinkUrl);
						dataModel.setSourceHeading(statsourceheading);
						dataModel.setSourceName(statLinkText);
						JSONObject jsonObj = new JSONObject(dataModel);
						array.put(index,jsonObj);
					}
				}
				
				LOGGER.debug("obj : " + array);
				out.println(array);
				session.save();
				session.logout();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error while getting data from node : " + e);
				e.printStackTrace();
			}
		}
		out.flush();
	}
}
