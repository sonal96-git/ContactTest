package com.pwc.util;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.sling.api.SlingHttpServletRequest;

public class JcrHelper {

	public static  QueryResult execute(Session session, String query,long queryLimit ) throws RepositoryException{

		QueryManager queryManager = session.getWorkspace()
				.getQueryManager();
		Query q = queryManager.createQuery(query,
				Query.JCR_SQL2);
		q.setLimit(queryLimit);
		QueryResult result = q.execute();
		return result;
	}

	public static  QueryResult execute(SlingHttpServletRequest request, String query,long queryLimit ) throws RepositoryException{
		Session session = request.getResourceResolver()
				.adaptTo(Session.class);
		QueryManager queryManager = session.getWorkspace()
				.getQueryManager();
		Query q = queryManager.createQuery(query,
				Query.JCR_SQL2);
		q.setLimit(queryLimit);
		QueryResult result = q.execute();
		return result;
	}
}
