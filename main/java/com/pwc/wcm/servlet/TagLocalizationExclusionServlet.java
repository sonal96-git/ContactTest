package com.pwc.wcm.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = Servlet.class, immediate = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Tags Localization Exclusion Servlet that return imformation about whether the tag is to be localized or not",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=" + "/bin/taglocalization"
})
@Designate(ocd = TagLocalizationExclusionServlet.Config.class)
public class TagLocalizationExclusionServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 847157569740350053L;

	public static final String EXCLUSION_LIST_PROPERTY = "tag.exclusion.list";

	private String[] exclusionList;
	
	@ObjectClassDefinition(name = "PwC Tag Localization Exclusion Configuration", 
			description = "Tags Localization Exclusion Servlet that return imformation about whether the tag is to be localized or not")
    @interface Config {
        @AttributeDefinition(name = "Tag Exlusion List", 
                            description = "",
                            type = AttributeType.STRING,
                            cardinality = Integer.MAX_VALUE)
        String[] tag_exclusion_list() default { "pwc-digital", "pwc365.*" };
    }

	@Activate
	@Modified
	protected final void activate(final TagLocalizationExclusionServlet.Config properties) throws Exception {
		exclusionList = properties.tag_exclusion_list();
	}

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/JSON");
		response.setCharacterEncoding("UTF-8");

		String tagNamespace = request.getParameter("namespace");
		boolean isTagNotExcluded = isTagNotExcluded(tagNamespace);
		response.getWriter().write(getResponseString(isTagNotExcluded));
	}

	private String getResponseString(boolean isTagNotExcluded) {
		return "{isTagNotExcluded:" + isTagNotExcluded + "}";
	}

	private boolean isTagNotExcluded(String tagNamespace) {
		boolean isTagNotExcluded = true;
		for (String tagRegex : exclusionList) {
			if (tagNamespace.matches(tagRegex)) {
				isTagNotExcluded = false;
				break;
			}

		}
		return isTagNotExcluded;
	}

}
