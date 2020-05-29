package com.pwc.user.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.pwc.user.model.PreferenceOption;
import com.pwc.user.services.PreferencesListService;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.LocaleUtils;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= PwC Preferences List Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/preferences/list",
})
public class PreferenceListServlet extends SlingSafeMethodsServlet {

	@Reference
	PreferencesListService preferenceListService;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {

		String locale = request.getParameter("locale");
		String preferencesListJSON = "{}";
		if (locale != null) {
			String territory = LocaleUtils.getTerritoryFromLocale(locale);
			if (territory != null) {
				Locale pageLang = new Locale(locale);
				I18nPwC i18n = new I18nPwC(request, request.getResourceBundle(pageLang));
				Map<String, PreferenceOption> preferencesMap = preferenceListService.getPreferencesMapByTerritory(territory, i18n);
				if(preferencesMap != null)
					preferencesListJSON = new Gson().toJson(preferencesMap);
			}
		}
		
		response.setContentType("text/JSON");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(preferencesListJSON);
	}
}
