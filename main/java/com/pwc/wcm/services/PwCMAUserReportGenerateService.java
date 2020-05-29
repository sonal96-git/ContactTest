package com.pwc.wcm.services;

import java.io.UnsupportedEncodingException;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.search.QueryBuilder;

public interface PwCMAUserReportGenerateService {
	/**
	 * Generates a report of users for MA and creates a CSV file at given path.
	 * 
	 * @param adminResourceResolver {@link ResourceResolver} - To get resource Path and admin session.
	 * @param queryBuilder {@link QueryBuilder} - To get results of DPE users.
	 * @param userReportPath {@String} - Path of generated report.
	 * @param userReportName {@String} - Name of generated report.
	 * @throws RepositoryException -
	 * @throws UnsupportedEncodingException
	 */
	void generateCSVfile() throws RepositoryException, UnsupportedEncodingException;
	
}
