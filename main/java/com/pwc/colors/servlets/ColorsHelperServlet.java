package com.pwc.colors.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.colors.models.Color;
import com.pwc.colors.models.ColorsBean;
import com.pwc.colors.utils.ColorsHelper;
import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "colorsHelper"
})
public class ColorsHelperServlet extends SlingAllMethodsServlet {

    private final String BASE_COLORS    = "/etc/designs/pwc/fallbackcomponentstyles/longform-base/cq:styles/colorsutils/basecolors";
    private final String PROPERTY   = "data";

    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        try {

            Color color;

            String operation = request.getParameter("operation");

            Resource resource = request.getResource();
            PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
            Page page = pageManager.getContainingPage(resource);

            ColorsBean colorsBean = new ColorsBean(page,request);

            ColorsHelper colorHelper = new ColorsHelper();
            colorHelper.setColorsBean(colorsBean);

            JSONObject jsonColor = new JSONObject();
            JSONObject jsonColors = new JSONObject();

            switch (operation) {
                case "isEnabled":
                    jsonColor.put("isEnabled", colorHelper.getIsColorEnabled());
                    break;
                case "primary":
                    color = colorHelper.getPrimaryColor();
                    jsonColor = getJsonColor(color);
                    break;
                case "secondary":
                    color = colorHelper.getSecondaryColor();
                    jsonColor = getJsonColor(color);
                    break;
                case "tertiary":
                    color = colorHelper.getTertiaryColor();
                    jsonColor = getJsonColor(color);
                    break;
                case "all":

                    color = colorHelper.getPrimaryColor();
                    jsonColors.put("primary", getJsonColor(color));

                    color = colorHelper.getSecondaryColor();
                    jsonColors.put("secondary", getJsonColor(color));

                    color = colorHelper.getTertiaryColor();
                    jsonColors.put("tertiary", getJsonColor(color));

					color = colorHelper.getColor("white");
                    jsonColors.put("white", getJsonColor(color));

                    color = colorHelper.getColor("grey");
                    jsonColors.put("grey", getJsonColor(color));

                    color = colorHelper.getColor("lightgrey");
                    jsonColors.put("lightgrey", getJsonColor(color));

                    Resource res = request.getResourceResolver().getResource(BASE_COLORS);
                    String[] data = (String[])res.getValueMap().get(PROPERTY);

                    for(int i =0; i< data.length;i++){
                        String baseColor = data[i];
                        color = colorHelper.getColor(baseColor);
                        jsonColors.put(baseColor, getJsonColor(color));
                    }
                    break;
                default:
                    color = colorHelper.getColor(operation);
                    if(color != null) {
                        color = colorHelper.getColor(operation);
                        jsonColor = getJsonColor(color);
                    } else {
                        throw new IllegalArgumentException("Invalid operation: " + operation);
                    }
            }
            String jsonColorString = jsonColor != null && jsonColor.length() > 0 ? jsonColor.toString() : jsonColors.toString();
           
            response.getWriter().write(jsonColorString);
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
        } catch (JSONException e) {
            ExceptionLogger.logException(e);
        }

    }

    private JSONObject getJsonColor(Color color) throws JSONException {

        JSONObject jsonColor = new JSONObject();
        jsonColor.put("name", color.getName());
        jsonColor.put("value", color.getValue());
        jsonColor.put("hexColor", color.getHexColor());
        jsonColor.put("baseColor", color.getBaseColor());

        return jsonColor;
    }
}
