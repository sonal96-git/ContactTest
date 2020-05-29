package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Predicate;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.servlets.AbstractPredicateServlet;
import com.day.cq.wcm.foundation.forms.FieldDescription;
import com.day.cq.wcm.foundation.forms.FieldHelper;
import com.day.cq.wcm.foundation.forms.FormsConstants;
import com.day.cq.wcm.foundation.forms.FormsHelper;


/**
 * Exports a list of form actions.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/wcm/foundation/forms/reports",
		"sling.servlet.extensions=" + "json",
		"sling.servlet.extensions=" + "html"
})
public class FormsListServlet extends AbstractPredicateServlet {

	/**
	 * default logger
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp,
			Predicate predicate)
					throws ServletException, IOException {
		try {


			if ( req.getRequestURI().contains("/reports") ) {
				// we collect the information and then redirect to the bulk editor
				final String path = req.getParameter("path");
				if ( path == null || path.trim().length() == 0 ) {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Path parameter is missing.");
					return;
				}
				final Resource formStartResource = req.getResourceResolver().getResource(path);
				if ( formStartResource == null ) {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
					return;
				}
				final ValueMap vm = ResourceUtil.getValueMap(formStartResource);
				final StringBuilder sb = new StringBuilder();
				sb.append(req.getContextPath());
				sb.append("/etc/importers/bulkeditor.html?rootPath=");
				String actionPath = vm.get(FormsConstants.START_PROPERTY_ACTION_PATH, "");
				actionPath=req.getParameter("actionPath");
				if( actionPath == null || actionPath.trim().length() == 0) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Missing '" + FormsConstants.START_PROPERTY_ACTION_PATH + "' property on node "
									+ formStartResource.getPath());
				}
				if ( actionPath.endsWith("*") ) {
					actionPath = actionPath.substring(0, actionPath.length() - 1);
				}
				if ( actionPath.endsWith("/") ) {
					actionPath = actionPath.substring(0, actionPath.length() - 1);
				}
				sb.append(FormsHelper.encodeValue(actionPath));



				sb.append("&initialSearch=true&contentMode=false&spc=true");
				sb.append("&cs=");
				sb.append("status");
				sb.append("&cv=");
				sb.append("status");

				sb.append("&cs=");
				sb.append("referenceNumber");
				sb.append("&cv=");
				sb.append("referenceNumber");
				final Iterator<Resource> elements = FormsHelper.getFormElements(formStartResource);
				while ( elements.hasNext() ) {
					final Resource element = elements.next();
					FieldHelper.initializeField(req, resp, element);
					final FieldDescription[] descs = FieldHelper.getFieldDescriptions(req, element);
					for(final FieldDescription desc : descs) {
						if ( !desc.isPrivate() ) {

							final String name = FormsHelper.encodeValue(desc.getName());
							sb.append("&cs=");
							sb.append(name);
							sb.append("&cv=");
							sb.append(name);
						}
					}
				}

				resp.sendRedirect(sb.toString());
			}
		} catch (Exception e) {
			logger.error("Error while generating JSON list", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	}


}