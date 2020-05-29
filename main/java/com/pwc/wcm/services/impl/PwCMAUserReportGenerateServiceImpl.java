package com.pwc.wcm.services.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.pwc.AdminResourceResolver;
import com.pwc.model.components.userreport.User;
import com.pwc.wcm.services.PwCMAUserReportGenerateService;

/**
 * This class generates an external user reports specific to PwC DPE MA and create a CSV file of report at given
 * location. Once CSV file is created, it is reverse-replicated to Author environment by a workflow-launcher.
 */
@Component(immediate = true, service = { PwCMAUserReportGenerateService.class }, enabled = true,
property = {
		Constants.SERVICE_DESCRIPTION + "= Generates a report for all PwC DPE users for territory 'gx'" })
@Designate(ocd = PwCMAUserReportGenerateServiceImpl.Config.class)
public class PwCMAUserReportGenerateServiceImpl implements PwCMAUserReportGenerateService {

	@Reference
	private AdminResourceResolver resourceResolver;

	@Reference
	private QueryBuilder queryBuilder;

	private static final Logger LOGGER = LoggerFactory.getLogger(PwCMAUserReportGenerateServiceImpl.class);
	public static final boolean ENABLED_DEFAULT_VALUE = false;
	private static final String EXTERNAL_USER_PATH = "/home/users/pwc-external-users";
	private static final String PROFILE_NODE = "profile";
	private static final String PREFERENCES_NODE = "preferences";
	private static final String DPE_FOLLOWED_CATEGORY = "dpe_followed_categories";
	private static final String ACCEPTED_TNC_NODE = ".*accepted_tnc";
	private static final String TERMS_CONDITIONS_NODE = ".*privacypolicy";
	private static final String LINE_BREAK = "\n";
	private static final String CSV_HEADER = "\"Registered Territory\",\"Date of Registration\",\"Date of Validation\",\"Registration entry point\",\"Site registration referral URL\",\"Request to deactivate profile\",\"Site terms accepted\",\"Email-ID\",\"First Name \",\"Last Name\",\"Organization\",\"Country / Territory\",\"Job Title\",\"Preferred language\",\"User advisory board\",\"Relationship with PwC\",\"Last Modified Date\",\"Marketing ID\",\"Marketing consent snapshot\",\"Preferences\"";

	private boolean isEnabled;
	private String userReportPath;
	private String userReportName;

	@ObjectClassDefinition(name = "PwC MA User Report Generate Service", 
			description = "Generates a report for all PwC DPE users for territory 'gx'")
	@interface Config {
		@AttributeDefinition(name = "Enabled", 
				description = "Enable/Disable the service",
				type = AttributeType.BOOLEAN)
		public boolean service_enabled() default ENABLED_DEFAULT_VALUE;

		@AttributeDefinition(name = "PwC DPE MA User Report Path", 
				description = "Path of DPE MA User Report",
				type = AttributeType.STRING)
		public String user_report_path() default "/content/reports/pwc/dpe-gx-ma-external-users-report";

		@AttributeDefinition(name = "PwC External User Report File Name", 
				description = "Name of PwC DPE External User Report File",
				type = AttributeType.STRING)
		public String user_report_name() default "dpe-gx-ma-external-users-report.csv";
	}

	@Activate
	protected void activate(final PwCMAUserReportGenerateServiceImpl.Config properties) {
		this.isEnabled = properties.service_enabled();
		this.userReportPath = properties.user_report_path();
		this.userReportName = properties.user_report_name();
	}

	@Override
	public void generateCSVfile() throws RepositoryException, UnsupportedEncodingException {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.generateCSVfile() method started");
		if (isEnabled) {
			final ResourceResolver adminResourceResolver = resourceResolver.getAdminResourceResolver();
			final Session session = adminResourceResolver.adaptTo(Session.class);
			try {
				StringBuilder csv = setCSVInfo(getDPEExternalUserResult(session, queryBuilder));
				InputStream is = new ByteArrayInputStream(csv.toString().getBytes("UTF-8"));
				if (adminResourceResolver.getResource(userReportPath) == null) {
					JcrUtil.createPath(userReportPath, JcrConstants.NT_FOLDER, JcrConstants.NT_FOLDER, session, true);
				}
				Node node = session.getNode(userReportPath);
				createCSVFileReport(node, session, is, userReportName);
				session.save();
			} finally {
				if (session != null && session.isLive())
					session.logout();
			}
		} else {
			LOGGER.trace("PwCMAUserReportGenerateServiceImpl.generateCSVfile() is not enabled");
		}
	}

	/**
	 * Returns results of external user, who are registered through DPE for Global territory.
	 *
	 * @param session {@link Session} - admin session to get results using {@link QueryBuilder}
	 * @return {@link SearchResult}
	 */

	private SearchResult getDPEExternalUserResult(Session session, QueryBuilder queryBuilder) {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.getDPEExternalUserResult() method started");
		final Map<String, String> map = new HashMap<>();

		map.put("type", UserConstants.NT_REP_USER);
		map.put("path", EXTERNAL_USER_PATH);
		map.put("property", "@profile/registrationSource");
		map.put("property.value", "dpe");
		map.put("1_property", "@profile/parentPagePath");
		map.put("1_property.value", "/content/pwc/gx%");
		map.put("1_property.operation", "like");
		map.put("p.limit", "-1");

		Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
		return query.getResult();
	}

	/**
	 * Creates external user report in AEM at given location.
	 * 
	 * @param parentNode {@link Node} - folder under which external user report is created.
	 * @param session {@link Session} - admin session to create nodes.
	 * @param inputStream {@link InputStream} - inputStream contains report data in byte form.
	 */
	private void createCSVFileReport(Node parentNode, Session session, InputStream inputStream, String userReportName)
			throws RepositoryException {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.createCSVFileReport() method started");
		if (parentNode.hasNode(userReportName)) {
			Node fileNode = parentNode.getNode(userReportName);
			fileNode.remove();
		}
		ValueFactory valueFactory = session.getValueFactory();
		Binary contentValue = valueFactory.createBinary(inputStream);
		Node fileNode = parentNode.addNode(userReportName, JcrConstants.NT_FILE);
		fileNode.addMixin(JcrConstants.MIX_REFERENCEABLE);
		Node resNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
		resNode.setProperty(JcrConstants.JCR_MIMETYPE, "text/csv");
		resNode.setProperty(JcrConstants.JCR_DATA, contentValue);
		Calendar lastModified = Calendar.getInstance();
		lastModified.setTimeInMillis(lastModified.getTimeInMillis());
		resNode.setProperty("jcr:lastModified", lastModified);
	}

	/**
	 * Creates external user report in AEM at given location.
	 * 
	 * @param {@link SearchResult} - results of external user, who are registered through DPE for Global territory.
	 */
	private StringBuilder setCSVInfo(SearchResult result) throws RepositoryException {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.setCSVInfo() method started");
		StringBuilder csv = new StringBuilder();
		csv.append(CSV_HEADER);
		csv.append(LINE_BREAK);
		for (Hit hit : result.getHits()) {
			Resource resource = hit.getResource();

			Resource profileNode = resource.getChild(PROFILE_NODE);
			if (profileNode != null) {
				Resource preferenceNode = resource.getChild(PREFERENCES_NODE);
				User regUser = profileNode.adaptTo(User.class);
				List<String> termsCond = preferenceNode != null ? setTermsAndCondCountries(preferenceNode) : new ArrayList<>();
				regUser.setTerritory("gx");
				regUser.setTerrArray(termsCond);
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX");
				Calendar calendar = (Calendar) resource.getValueMap().get(JcrConstants.JCR_CREATED);
				String formattedCreateDate = dateFormat.format((calendar.getTime()));
				formattedCreateDate = formattedCreateDate.contains("Z") ? formattedCreateDate.replace("Z","+00:00") : formattedCreateDate;
				regUser.setRegistration(formattedCreateDate);
				if (preferenceNode != null) {
					setPreferredCategory(preferenceNode, regUser);
				}
				csv.append(regUser.getUserData());
			}
		}
		return csv;
	}

	private List<String> setTermsAndCondCountries(Resource preferenceNode) {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.setTermsAndCondCountries() method started");

		List<String> termsCond = new ArrayList<>();
		for (Resource prefChild : preferenceNode.getChildren()) {
			if (prefChild.getName().matches(TERMS_CONDITIONS_NODE) || prefChild.getName().matches(ACCEPTED_TNC_NODE)) {
				for (Resource country : prefChild.getChildren()) {
					String countryName = country.getName().equals("gb") ? "uk" : country.getName();
					termsCond.add(countryName);
				}
			}
		}
		return termsCond;
	}

	private void setPreferredCategory(Resource preferenceNode, User regUser) {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.setPreferredCategory() method started");

		for (Resource prefChild : preferenceNode.getChildren()) {
			if (prefChild.getName().matches(DPE_FOLLOWED_CATEGORY)) {
				List<List<String>> preferredCountryArray = new ArrayList<>();
				for (Resource country : prefChild.getChildren()) {
					List<String> preferedCategoryArray = new ArrayList<>();
					List<String> preferredCountry = new ArrayList<>();
					for (Resource category : country.getChildren()) {
						addPreferedCategory(preferedCategoryArray, category);
					}
					preferredCountry.add(country.getName());
					preferredCountry.add(preferedCategoryArray.toString());
					preferredCountryArray.add(preferredCountry);
				}
				regUser.setPreferredCountryArray(preferredCountryArray);
				break;
			}
		}
	}

	private void addPreferedCategory(List<String> preferedCategoryArray, Resource category) {
		LOGGER.trace("PwCMAUserReportGenerateServiceImpl.addPreferedCategory() method started");

		if (!category.hasChildren()) {
			preferedCategoryArray.add(category.getName());
			return;
		}
		for (Resource firstLevelCategory : category.getChildren()) {
			if (!firstLevelCategory.hasChildren()) {
				preferedCategoryArray.add(category.getName() + "->" + firstLevelCategory.getName());
				continue;
			}
			for (Resource secondLevelCategory : firstLevelCategory.getChildren()) {
				preferedCategoryArray.add(category.getName() + "->" + firstLevelCategory.getName() + "->" + secondLevelCategory.getName());
			}
		}
	}
}
