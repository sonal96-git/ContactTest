package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.model.SearchResult;
import com.pwc.util.JcrHelper;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Search Contacts",
    "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    "sling.servlet.paths=" + "/bin/contactSearch",
})
public class ContactSearchServlet extends SlingAllMethodsServlet {
	
	private Log logger = LogFactory.getLog(ContactSearchServlet.class);
	
	@Reference
	private ResourceResolverFactory resolverFactory;
	@Reference
	SlingRepository slingRepository;
	@Override
	protected void doPost(SlingHttpServletRequest request,
						  SlingHttpServletResponse response) throws ServletException,
			IOException {
		String param = request.getParameter("criteria");
		String searchType = request.getParameter("type");
		String firstName = "";
		String lastName = "";
		String country = "";
		String province = "";
		long queryLimit = 500;
		//logger.info(searchType);
		if (param != null) {
			JSONObject obj;
			//logger.info(param);
			try {
				obj = new JSONObject(param);
				String locale = obj.getString("locale");
				JSONArray globalLeaderTags = obj
						.getJSONArray("globalLeaderTags");
				List<String> globalLeaderTagList = new ArrayList<String>();
				for (int i = 0; i < globalLeaderTags.length(); i++) {
					globalLeaderTagList.add(globalLeaderTags.getString(i));
				}

				JSONArray globalMembersTags = obj
						.getJSONArray("globalMemberTags");
				List<String> globalMemberTagList = new ArrayList<String>();
				for (int i = 0; i < globalMembersTags.length(); i++) {
					globalMemberTagList.add(globalMembersTags.getString(i));
				}

				JSONArray localLeaderTags = obj.getJSONArray("localLeaderTags");
				List<String> localLeaderTagList = new ArrayList<String>();
				for (int i = 0; i < localLeaderTags.length(); i++) {
					localLeaderTagList.add(localLeaderTags.getString(i));
				}

				JSONArray localMemberTags = obj.getJSONArray("localMemberTags");
				List<String> localMemberTagList = new ArrayList<String>();
				for (int i = 0; i < localMemberTags.length(); i++) {
					localMemberTagList.add(localMemberTags.getString(i));
				}
				String includeLeadersOnly = obj.getString("includeLeadersOnly");
				boolean leaderFlag = false;
				if (includeLeadersOnly != null
						&& includeLeadersOnly.toLowerCase().equals("yes")) {
					leaderFlag = true;
				}
				if (obj.has("firstName")) {
					firstName = obj.getString("firstName");
				}
				if (obj.has("lastName")) {
					lastName = obj.getString("lastName");
				}
				if (obj.has("country")) {
					country = obj.getString("country");
				}
				if (obj.has("province")) {
					province = obj.getString("province");
				}

				if (locale != null && locale.trim().length() > 0) {
					String[] locales = locale.toLowerCase().split(",");
					List<String> pathList = new ArrayList<String>();
					for (int i = 0; i < locales.length; i++) {
						String path = locales[i].split("_")[1] + "/"
								+ locales[i].split("_")[0];
						pathList.add(path);
					}
					List<SearchResult> resultList = new ArrayList<SearchResult>();
					for (String path : pathList) {
						String queryString = "SELECT * FROM [cq:PageContent] as parent INNER JOIN [nt:unstructured] as child ON ISCHILDNODE(child, parent) WHERE ISDESCENDANTNODE(parent, [/content/pwc/"
								+ path + "/contacts])  ";
						if (!firstName.equals("")) {
							queryString += "and LOWER(child.[firstName]) LIKE '%"
									+ firstName.toLowerCase() + "%' ";
						}
						if (!lastName.equals("")) {
							queryString += "and LOWER(child.[lastName]) LIKE '%"
									+ lastName.toLowerCase() + "%'";
						}
						String appendedCondition = "";
						if (localLeaderTagList.size() > 0) {

							for (int i = 0; i < localLeaderTagList.size(); i++) {
								appendedCondition += "parent.[cq:tagslocalleader]='"
										+ localLeaderTagList.get(i)
										+ "' OR ";
							}
						}

						if (globalLeaderTagList.size() > 0) {

							for (int i = 0; i < globalLeaderTagList.size(); i++) {
								appendedCondition += "parent.[cq:tagsgloballeader]='"
										+ globalLeaderTagList.get(i)
										+ "' OR ";
							}
						}
						if (!leaderFlag) {
							if (localMemberTagList.size() > 0) {
								for (int i = 0; i < localMemberTagList.size(); i++) {
									appendedCondition += "parent.[cq:tagslocalteammember]='"
											+ localMemberTagList.get(i)
											+ "' OR ";
								}
							}

							if (globalMemberTagList.size() > 0) {
								for (int i = 0; i < globalMemberTagList.size(); i++) {
									appendedCondition += "parent.[cq:tagsglobalteammember]='"
											+ globalMemberTagList.get(i)
											+ "' OR ";
								}

							}
						}
						if(!appendedCondition.equals(""))
							appendedCondition = "AND (" + appendedCondition + ")";
						appendedCondition=appendedCondition.replace("OR )",")");
						queryString = queryString + appendedCondition;

						if (!country.equals("")) {
							queryString = queryString
									+ " AND LOWER(child.[country])='"
									+ country.toLowerCase() + "'";
						}
						if (!province.equals("")) {
							queryString = queryString
									+ " AND LOWER(child.[province]) LIKE '%"
									+ province.toLowerCase() + "%'";
						}
						queryString = queryString + " ORDER BY child.[lastName]";
						//logger.info("[QUERY]" + queryString);
						ResourceResolver resourceResolver = request.getResourceResolver();
						Session session = resourceResolver.adaptTo(Session.class);
						QueryResult nodes = JcrHelper.execute(session,
								queryString, queryLimit);
						for(RowIterator rowIterator = nodes.getRows(); rowIterator
								.hasNext();){
							Row row = rowIterator.nextRow();
							Node contactNode = row.getNode("child");
							String slingType = "pwc/components/content/contact";
							if (contactNode.hasProperty("sling:resourceType")&&contactNode.getProperty("sling:resourceType").getString().toLowerCase().equals(slingType.toLowerCase())) {
								if (contactNode.hasProperty("firstName")
										&& contactNode.hasProperty("lastName")) {
									SearchResult eachResult = new SearchResult();
									eachResult.setPath(contactNode.getAncestor(7)
											.getPath());
									eachResult.setFirstName(contactNode
											.getProperty("firstName")
											.getString());
									eachResult.setLastName(contactNode
											.getProperty("lastName")
											.getString());
									if (contactNode.hasProperty("office"))
										eachResult.setOffice(contactNode
												.getProperty("office")
												.getString());
									if (contactNode.hasProperty("province"))
										eachResult.setState(contactNode
												.getProperty("province")
												.getString());
									Node contactJcrNode = contactNode.getParent();
									if(contactJcrNode!=null && (!contactJcrNode.hasProperty("cq:lastReplicationAction") || contactJcrNode.getProperty("cq:lastReplicationAction").getString().equalsIgnoreCase("Activate")))
										resultList.add(eachResult);
								}

							}
						}


					}
					Collections.sort(resultList, new Comparator<SearchResult>() {
						@Override
						public int compare(SearchResult o1, SearchResult o2) {
							return o1.getFirstName().compareTo(o2.getFirstName());
						}
					});
					JSONObject jsonResult = new JSONObject();
					JSONArray userList = new JSONArray();
					for (SearchResult result : resultList) {
						JSONObject eachUser = new JSONObject();
						eachUser.put("firstname", result.getFirstName());
						eachUser.put("lastname", result.getLastName());
						eachUser.put("office", result.getOffice());
						eachUser.put("state", result.getState());
						eachUser.put("path", result.getPath());
						userList.put(eachUser);
					}
					jsonResult.put("data", userList);
					//logger.info("[RESULT] " + jsonResult.toString());
					//logger.info("Call search contact");
					response.setContentType("application/json; charset=UTF-8");
					response.getWriter().write(jsonResult.toString());

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("[CUSTOM ERROR JSONException] " + e.getMessage());
			}

		}

	}

}
