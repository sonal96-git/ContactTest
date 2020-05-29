package com.pwc.wcm.servlet;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Update Page Property",
		"sling.servlet.methods=" + HttpConstants.METHOD_POST,
		"sling.servlet.paths=" + "/bin/updatePageProperty"
})
public class UpdatePagePropertyServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -4253435574843240583L;

	private Log logger = LogFactory.getLog(UpdatePagePropertyServlet.class);

	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException, IOException {
		logger.info("POST!!!!!!!!!!!!!!!!!");
		String pagePath = request.getParameter("pagePath");
		String tagsParam = request.getParameter("tags");
		logger.info("PARAMS " + pagePath  + "   " + tagsParam);

		Session session =  request.getResourceResolver().adaptTo(Session.class);
		Node pageNode = null;
		try {
			pageNode = session.getNode(pagePath);
			ValueFactory valueFactory = session.getValueFactory();
			JSONArray jsonArray = new JSONArray(tagsParam);
			if(jsonArray!=null && jsonArray.length() >0) {
				Value[] tagsValue = new Value[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					tagsValue[i] = valueFactory.createValue(jsonArray.get(i).toString());
				}
				pageNode.setProperty("cq:tags",tagsValue);
			}
			else {
				pageNode.setProperty("cq:tags",(Value[])null);
			}
			session.save();
			session.logout();
			response.setStatus(200);
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			logger.info("Error converting JSON Array");
		}


	}
}
