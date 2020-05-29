package com.pwc.user.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.user.model.User;
import com.pwc.user.services.UserInformationService;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC User Information Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/user/information",
})
public class UserInformationServlet extends SlingSafeMethodsServlet {

	@Reference
	UserInformationService userInformationService;
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		User User = userInformationService.getUser(request);
		response.setContentType("text/JSON");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(User.toString());
	}

}
