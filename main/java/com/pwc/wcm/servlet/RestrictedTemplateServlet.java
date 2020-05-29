package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.wcm.api.Page;
import com.pwc.AdminResourceResolver;

/**
 * Created by rjiang on 2017-02-09.
 */
@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Restricted Template Copy",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/restrictedTemplate",
})
public class RestrictedTemplateServlet extends SlingAllMethodsServlet {

	@Reference
	AdminResourceResolver adminResourceResolver;

	private ArrayList<String> dpeGroup;
	private ArrayList<String> restrictedTemplateList;

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
	IOException {

		String userName = request.getParameter("userName");
		String page = request.getParameter("page");
		ResourceResolver adminResolver = null;
		//Session session = null;
		try {
			//ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			adminResolver = adminResourceResolver.getAdminResourceResolver();
			//UserManager userManager = adminResolver.adaptTo(UserManager.class);
			//Authorizable auth = userManager.getAuthorizable(userName);
			//ResourceResolver resolver = request.getResourceResolver();
			//session = resolver.adaptTo(Session.class);
			Resource pageRes = adminResolver.getResource(page);
			Page currentPage = pageRes.adaptTo(Page.class);
			String templatePath = currentPage.getTemplate().getPath();
			/*AccessControlManager acMgr = session.getAccessControlManager();
            boolean hasAccess = session.getAccessControlManager().hasPrivileges(templatePath, new Privilege[]{ acMgr.privilegeFromName(Privilege.JCR_READ)});
            boolean hasPermission = hasAccess;*/
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			JSONObject object = new JSONObject();
			object.put("userName", userName);
			object.put("template", templatePath);
			String objectString = object.toString();
			response.getWriter().write(objectString);
		} catch (Exception exception) {

		}


	}

	/*
    private boolean isInRestrictedGroup(ArrayList<String> dpeGroups, ArrayList<String> groups){
        boolean isInRestrictedGroup = false;
        for(String eachGroup: groups){
            for(String dpegroup: dpeGroups){
                Pattern pattern = Pattern.compile(dpegroup, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(eachGroup);
                if(matcher.find()){
                    isInRestrictedGroup = true;
                    break;
                }
            }
            if(isInRestrictedGroup)break;
        }
        return isInRestrictedGroup;
    }*/

	@Activate
	protected void activate(ComponentContext context) throws RepositoryException {
		try {

			//final Dictionary<?, ?> properties = context.getProperties();
			BundleContext bundleContext = context.getBundleContext();
			ServiceReference configAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
			ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configAdminReference);
			Configuration config = configurationAdmin.getConfiguration("PwC Restricted Template");
			String[] userGroupProp = (String[])config.getProperties().get("userGroup");
			String[] prop = (String[]) config.getProperties().get("restrictedTemplates");
			restrictedTemplateList = new ArrayList<String>(Arrays.asList(prop));
			dpeGroup = new ArrayList<String>(Arrays.asList(userGroupProp));
		} catch (Exception ex) {
		}
	}
}
