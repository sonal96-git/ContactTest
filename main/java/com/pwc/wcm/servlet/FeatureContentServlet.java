package com.pwc.wcm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
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
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.pwc.wcm.model.FeatureContentModel;

/**
 * Servlet to provide list of the territories and legal entities for contact
 * profile.
 */
@Component(service = Servlet.class, immediate = true, property = {
		Constants.SERVICE_DESCRIPTION + "= Data Storing Servlet for Feature content",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/storedatafeaturecontent" })
public class FeatureContentServlet extends SlingAllMethodsServlet {

	final String splitString = "_cq_dialog.html";

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureContentServlet.class);

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		ResourceResolver resolver = request.getResourceResolver();
		String dataSlides = request.getParameter("arr");
		String path = request.getParameter("path");
		String enablestaricon = request.getParameter("enablestaricon");
		String curPanelType = request.getParameter("curPanelType");
		Gson gson = new Gson();
		FeatureContentModel[] dataModel = gson.fromJson(dataSlides, FeatureContentModel[].class);
		int i = 1;
		Session session = resolver.adaptTo(Session.class);
		Resource parentResource = resolver.getResource(path);
		Node node = parentResource.adaptTo(Node.class);
		
		try {
			if(node.hasProperty("url")) {
				node.getProperty("url").remove();
			}
			if(StringUtils.isNotBlank(enablestaricon)) {
				node.setProperty("enablestaricon", enablestaricon);
			}
			if(node.hasProperty("fileReference")) {
				node.getProperty("fileReference").remove();
			}
			
			
			if(StringUtils.isNotBlank(curPanelType)) {
				if (curPanelType.equalsIgnoreCase("Solid Color Panel")) {
					curPanelType = "solid";
				}
				if (curPanelType.equalsIgnoreCase("Image Panel")) {
					curPanelType = "image";
				}
				if (curPanelType.equalsIgnoreCase("Stat Bar")) {
					curPanelType = "stat";
				}
				node.setProperty("curPanelType", curPanelType);
			}
			
			NodeIterator childrens = node.getNodes();
			if(childrens.hasNext()) {
				while(childrens.hasNext()) {
					Node childNode = childrens.nextNode();
					childNode.remove();
				}
			}
			for (FeatureContentModel data : dataModel) {
				String heading = data.getHeading();
				String link = data.getURL();
				String order = data.getOrder();
				String target = data.getTarget();
				String text = data.getText();
				String fileReference = data.getImage();
				String statLine1 = data.getStatLine1();
				String statLine2 = data.getStatLine2();
				String statLabel = data.getStatLabel();
				String sourceHeading = data.getSourceHeading();
				String sourceName = data.getSourceName();
				String sourceURL = data.getSourceURL();
				String typeOfPanel = data.getTypeOfPanel();
				
				String valign = "middle";
				String background = "bga85-tangerine";
				String halign = "center";
				
				i++;
				if(StringUtils.isNotBlank(typeOfPanel)) {
					String nodeName = "image".concat(Integer.toString(i));
					Node newAddedNode = node.addNode(nodeName,"nt:unstructured");
					
					newAddedNode.setProperty("background", background); 
					newAddedNode.setProperty("halign", halign); 
					if(StringUtils.isNotBlank(heading)) {
						if(newAddedNode.hasProperty("heading")) {
							newAddedNode.remove();
						}
						newAddedNode.setProperty("heading", heading);
					}
					if(StringUtils.isNotBlank(text)) {
						newAddedNode.setProperty("text", text);
					}
					newAddedNode.setProperty("link", link); 
					newAddedNode.setProperty("order", order); 
					newAddedNode.setProperty("target", target);
					newAddedNode.setProperty("valign", valign); 
					
					if(StringUtils.isNotBlank(fileReference)) {
						newAddedNode.setProperty("fileReference", fileReference); 
					}
					if(StringUtils.isNotBlank(statLine1)) {
						newAddedNode.setProperty("statLine1", statLine1);
					}
					if(StringUtils.isNotBlank(statLine2)) {
						newAddedNode.setProperty("statLine2", statLine2);
					}
					if(StringUtils.isNotBlank(statLabel)) {
						newAddedNode.setProperty("statLabel", statLabel);
					}
					if(StringUtils.isNotBlank(sourceHeading)) {
						newAddedNode.setProperty("statsourceheading", sourceHeading);
					}
					if(StringUtils.isNotBlank(sourceName)) {
						newAddedNode.setProperty("statLinkText", sourceName);
					}
					if(StringUtils.isNotBlank(sourceURL)) {
						newAddedNode.setProperty("statLinkUrl", sourceURL);
					}
				}

				LOGGER.debug("node added");
			}
			session.save(); 
			session.logout();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(e+"");
			e.printStackTrace();
		}
		out.flush();
	}
}
