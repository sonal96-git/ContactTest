package com.pwc.servlets;

import static com.day.cq.personalization.offerlibrary.usebean.OffercardPropertiesProvider.LOGGER;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.text.Text;
import com.pwc.AdminResourceResolver;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/validateAemCaptcha",
})
public class AemCaptchaValidationServlet extends SlingSafeMethodsServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Reference
    private ResourceResolverFactory resolverFactory;
    @Reference
    private AdminResourceResolver adminResourceResolver;
    private static final String CONTENT_TYPE = "application/json";
    private static final String ENCODING = "UTF-8";

    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String result="valid";
        final String captchatry = request.getParameter("captchatry");
        final String captchakey = request.getParameter("captchakey");

        final String mins = "" + (System.currentTimeMillis() / (60 * 1000));
        final String minsold = "" + (System.currentTimeMillis() / (60 * 1000) - 1);

        final String captchacurrent = (Text.md5("" + (captchakey + mins))).substring(1, 6);
        final String captchaold = (Text.md5("" + (captchakey + minsold))).substring(1, 6);

        if (!captchatry.equals(captchacurrent)  && !captchatry.equals(captchaold)) {
            result= "invalid";
        }

        logger.info("isCaptcha value valid " +result);


        LOGGER.trace("Entered doGet() of ProjectBaseUrlListServlet");
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        response.getWriter().write(result);

    }



}
