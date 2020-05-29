package com.pwc.chart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONWriter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/** 
 * 
 * @author PwC Uy
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.extensions=" + "json",
    "sling.servlet.selectors=" + "modals",
})
public class ModalPickerServlet extends SlingAllMethodsServlet {

	private Logger logger = LoggerFactory.getLogger(ModalPickerServlet.class);

	private static final long serialVersionUID = 1L;

	@Reference
	private QueryBuilder builder;	
	@Reference
	private ResourceResolverFactory resolverFactory;
	private Session session;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

		logger.info("... Begin ModalPickerServlet doGet ..");
		ResourceResolver resolver = request.getResourceResolver();
		PageManager pMgr = resolver.adaptTo(PageManager.class);
		Resource res = request.getResource();
		Page page = pMgr.getContainingPage(res);
		searchCQForContent(page.getPath(),response.getWriter(),resolver);
		logger.info("... End ModalPickerServlet doGet ..");
	}


	public void searchCQForContent(String pagePath,PrintWriter pwcWriter,ResourceResolver resourceResolver) {
		try { 

			//Invoke the adaptTo method to create a Session 						
			session = resourceResolver.adaptTo(Session.class);

			// create query description as hash map (simplest way, same as form post)
			Map<String, String> map = new HashMap<String, String>();

			// create query description as hash map (simplest way, same as form post)	
			map.put("path", pagePath);
			map.put("1_property","sling:resourceType");
			map.put("1_property.value","pwc/components/content/modal-pwc");
			
			Query query = builder.createQuery(PredicateGroup.create(map), session);

			query.setStart(0);
			query.setHitsPerPage(20);

			SearchResult result = query.getResult();


			JSONWriter jw = new JSONWriter(pwcWriter);
			jw.object();
			jw.key("data").array();
			// iterating over the results
			for (Hit hit : result.getHits()) {
				String path = hit.getPath();				
				jw.object();
				jw.key("path").value(path);
				jw.key("title").value(hit.getNode().getName());
				jw.endObject();
			}
			jw.endArray();
			jw.endObject();
			

		}
		catch(Exception e){
			logger.info(e.getMessage());
		}finally{
			session.logout();       
		}
	}    
}