package com.pwc.wcm.taglibs;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;

import com.day.cq.commons.Console;
import com.pwc.model.SearchResult;
import com.pwc.util.JcrHelper;
import com.pwc.wcm.servlet.ContactSearchServlet;

public class RightRailContactTagLib {
	private static Log logger = LogFactory.getLog(RightRailContactTagLib.class);

	public static String[] displayByDefinedQuery(String criteria,
			SlingHttpServletRequest request) {
		Session session = null;
		String[] userPath = null;
		String firstName = "";
		String lastName = "";
		String country = "";
		String province = "";
		long queryLimit = 500;
		if (criteria != null) {
			JSONObject obj;
			logger.info(criteria);
			try {
				obj = new JSONObject(criteria);
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
						String queryString = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/content/pwc/"
								+ path + "/contacts]) AND  (s.[sling:resourceType]='pwc/components/page/threeColumnContactProfile') ";
						if (!firstName.equals("")) {
							queryString += "and LOWER(s.[firstName]) LIKE '%"
									+ firstName.toLowerCase() + "%'";
						}
						if (!lastName.equals("")) {
							queryString += "and LOWER(s.[lastName]) LIKE '%"
									+ lastName.toLowerCase() + "%'";
						}
                        String appendedCondition = "";
                        if (localLeaderTagList.size() > 0) {

                            for (int i = 0; i < localLeaderTagList.size(); i++) {
                                appendedCondition += "s.[cq:tagslocalleader]='"
                                        + localLeaderTagList.get(i)
                                        + "' OR ";
                            }
                        }

                        if (globalLeaderTagList.size() > 0) {

                            for (int i = 0; i < globalLeaderTagList.size(); i++) {
                                appendedCondition += "s.[cq:tagsgloballeader]='"
                                        + globalLeaderTagList.get(i)
                                        + "' OR ";
                            }
                        }
                        if (!leaderFlag) {
                            if (localMemberTagList.size() > 0) {
                                for (int i = 0; i < localMemberTagList.size(); i++) {
                                    appendedCondition += "s.[cq:tagslocalteammember]='"
                                            + localMemberTagList.get(i)
                                            + "' OR ";
                                }
                            }

                            if (globalMemberTagList.size() > 0) {
                                for (int i = 0; i < globalMemberTagList.size(); i++) {
                                    appendedCondition += "s.[cq:tagsglobalteammember]='"
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
									+ " AND LOWER(s.[country])='"
									+ country.toLowerCase() + "'";
						}
						if (!province.equals("")) {
							queryString = queryString
									+ " AND LOWER(s.[province]) LIKE '%"
									+ province.toLowerCase() + "%'";
						}
						queryString = queryString + " ORDER BY s.[lastName]";
						logger.info("[QUERY]" + queryString);
						QueryResult nodes = JcrHelper.execute(request,
								queryString, queryLimit);
						 session = request.getResourceResolver()
								.adaptTo(Session.class);
						Node root = session.getRootNode();
						List<String> nodePaths = new ArrayList<String>();
						List<String> checkPath = new ArrayList<String>();
						int counter = 0;
						for (NodeIterator nodeResultsIt = nodes.getNodes(); nodeResultsIt
								.hasNext();) {

							Node n = nodeResultsIt.nextNode();
							String nodePath = n.getPath();
							String ancestor = n.getAncestor(7).getPath();
							ancestor = ancestor.substring(1, ancestor.length())
									+ "/jcr:content/contact";

							logger.info("[ANCESTOR]: " + ancestor);

							logger.info("path " + n.getPath());
							String nPath = n.getPath();
							String contactPath = ancestor;
							if (root.hasNode(contactPath)) {
								if (!checkPath.contains(ancestor)) {
									counter++;
									if (counter > 50)
										break;
									checkPath.add(ancestor);
									Node contact = root.getNode(contactPath);
									if (contact.hasProperty("firstName")
											&& contact.hasProperty("lastName")) {
										SearchResult eachResult = new SearchResult();
										eachResult.setPath(n.getAncestor(7)
												.getPath());
										eachResult.setFirstName(contact
												.getProperty("firstName")
												.getString());
										eachResult.setLastName(contact
												.getProperty("lastName")
												.getString());
										if (contact.hasProperty("office"))
											eachResult.setOffice(contact
													.getProperty("office")
													.getString());
										if (contact.hasProperty("province"))
											eachResult.setState(contact
													.getProperty("province")
													.getString());
										resultList.add(eachResult);
									}
								}

							}
						}

					}
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
					logger.info("[RESULT] " + jsonResult.toString());
					logger.info("Call search contact");


					userPath = new String[resultList.size()];
					for (int i = 0; i < resultList.size(); i++) {
						userPath[i] = resultList.get(i).getPath()
								.replace("/jcr:content/centerPar/contact", "");
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				logger.error("[CUSTOM ERROR JSONException] " + e.getMessage());
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				logger.error("[CUSTOM ERROR RepositoryException]"
						+ e.getMessage());
			}finally {
				//do not close the session, this session is coming from the request, nobody finds it guess nobody even use this function.
				//if(session!=null)
				//	session.logout();

			}

		}

		return userPath;
	}

}
