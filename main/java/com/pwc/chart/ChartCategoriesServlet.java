package com.pwc.chart;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.chart.impl.ChartDataHelper;
///apps/formtest/uy/com/pwc/chart/ChartCategoriesServlet.json?params=spacer
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/apps/formtest/uy/com/pwc/chart/ChartCategoriesServlet",
})
public class ChartCategoriesServlet extends SlingAllMethodsServlet  {

	private Logger logger = LoggerFactory.getLogger(ChartServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		logger.info("... Begin ChartCategoriesServlet doGet ..");
		JSONWriter jw = new JSONWriter(response.getWriter());
		try {
			jw.object();
			jw.key("data").array();
			if(!request.getParameter("table").isEmpty()){
				ArrayList<String> categories = ChartDataHelper.getCategoriesArrayList(ChartDataHelper.parse(request.getParameter("table")));
				for (String cat : categories) {
					jw.object();
					jw.key("text").value(cat);
					jw.endObject();
				}
			}
			jw.endArray();
			jw.endObject();
		} catch (JSONException e) {
			logger.error("Json exception",e);
			e.printStackTrace();
		}
		logger.info("... End ChartCategoriesServlet doGet ..");



	}
}
