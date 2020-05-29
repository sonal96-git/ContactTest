package com.pwc.chart;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.chart.impl.ChartDataHelper;

/** Servlet for validate the correct information on the html table for charts
 * 
 * @author PwC Uy
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/apps/formtest/uy/com/pwc/chart/SlingAllMethodsServlet",
})
public class ChartServlet extends SlingAllMethodsServlet {

	private Logger logger = LoggerFactory.getLogger(ChartServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

		logger.info("... Begin ChartServlet doGet ..");
		response.setContentType("text/html");  
		response.setHeader("Cache-Control", "no-cache");  
		String[][] table = ChartDataHelper.parse(request.getParameter("table"));
		if(!ChartDataHelper.hasEmptyRows(table) && ChartDataHelper.validate(table)){
			response.getWriter().print("ok"); 
		}else{
			response.getWriter().print("error");
			logger.info("... The chart table data is not correct ..");
		};
		logger.info("... End ChartServlet doGet ..");
	}

}
