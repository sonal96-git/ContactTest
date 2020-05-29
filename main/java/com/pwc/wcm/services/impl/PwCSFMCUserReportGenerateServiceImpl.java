package com.pwc.wcm.services.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.pwc.AdminResourceResolver;
import com.pwc.model.components.userreport.User;
import com.pwc.wcm.services.PwCSFMCUserReportGenerateService;
import org.apache.commons.lang3.StringUtils;
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

import javax.jcr.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generates a user reports to be send to SFMC server and create a CSV file of report at given
 * location. Once CSV file is created, it is reverse-replicated to Author environment by a workflow-launcher.
 */
@Component(immediate = true, service = {PwCSFMCUserReportGenerateService.class},
        property = {
                Constants.SERVICE_DESCRIPTION + "= Generates a report for all external users with preferred territory us"})
@Designate(ocd = PwCSFMCUserReportGenerateServiceImpl.Config.class)
public class PwCSFMCUserReportGenerateServiceImpl implements PwCSFMCUserReportGenerateService {

    @Reference
    private AdminResourceResolver resourceResolver;

    @Reference
    private QueryBuilder queryBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(PwCSFMCUserReportGenerateServiceImpl.class);
    public static final boolean ENABLED_DEFAULT_VALUE = false;
    private static final String EXTERNAL_USER_PATH = "/home/users/pwc-external-users";
    private static final String PROFILE_NODE = "profile";
    private static final String LINE_BREAK = "\n";
    private static final String CSV_HEADER = "\"Email\"|\"First Name\"|\"Last Name\"|\"Company\"|\"Title\"|\"Preferred Language\"|\"Marketing Consent\"|\"Date Of Registration\"|\"Date Of Validation \"|\"Country\"|\"Territory\"|\"Last Modified Date\"|\"MID\"";

    private boolean isEnabled;
    private String userReportPath;
    private String userReportNamePrefix;

    @ObjectClassDefinition(name = "PwC SFMC User Report Generate Service",
            description = "Generates a report for all PwC DPE users with preferred territory 'us'.")
    @interface Config {
        @AttributeDefinition(name = "Enabled",
                description = "Enable/Disable the service",
                type = AttributeType.BOOLEAN) boolean service_enabled() default ENABLED_DEFAULT_VALUE;

        @AttributeDefinition(name = "PwC DPE MA User Report Path",
                description = "Path of DPE MA User Report",
                type = AttributeType.STRING) String user_report_path() default "/content/reports/pwc/dpe-sfmc-us-users-report";

        @AttributeDefinition(name = "PwC External User Report File Name",
                description = "Name of PwC DPE External User Report File",
                type = AttributeType.STRING) String user_report_name_prefix() default "DPE_SFMC_US_Subscribers_";
    }

    @Activate
    protected void activate(final PwCSFMCUserReportGenerateServiceImpl.Config properties) {
        this.isEnabled = properties.service_enabled();
        this.userReportPath = properties.user_report_path();
        this.userReportNamePrefix = properties.user_report_name_prefix();
    }

    @Override
    public void generateCSVFile() throws RepositoryException, UnsupportedEncodingException {
        LOGGER.info("PwCSFMCUserReportGenerateServiceImpl.generateCSVFile() : Inside generateCSVFile method");
        if (isEnabled) {
            LOGGER.info("PwCSFMCUserReportGenerateServiceImpl.generateCSVFile() : PwCSFMCUserReportGenerateService is enabled");
            final ResourceResolver adminResourceResolver = resourceResolver.getAdminResourceResolver();
            final Session session = adminResourceResolver.adaptTo(Session.class);
            try {
                StringBuilder csv = setCSVInfo(getUSUserResult(session, queryBuilder));
                InputStream is = new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
                if (adminResourceResolver.getResource(userReportPath) == null) {
                    JcrUtil.createPath(userReportPath, JcrConstants.NT_FOLDER, JcrConstants.NT_FOLDER, session, true);
                }
                Node userReportNode = session.getNode(userReportPath);
                if (userReportNode.hasNodes()) {
                    userReportNode.getNodes().nextNode().remove();
                }
                createCSVFileReport(userReportNode, session, is, userReportNamePrefix + DateTimeFormatter.ofPattern("MM-dd-yyyy_hh-mm").format(LocalDateTime.now()) + ".csv");
                session.save();
            } finally {
                if (session != null && session.isLive())
                    session.logout();
            }
        } else {
            LOGGER.debug("PwCSFMCUserReportGenerateServiceImpl.generateCSVfile() : PwCSFMCUserReportGenerateService is not enabled.");
        }
    }

    /**
     * Returns results of external user, who are registered through DPE for Global territory.
     *
     * @param session {@link Session} - admin session to get results using {@link QueryBuilder}
     * @return {@link SearchResult}
     */

    private SearchResult getUSUserResult(Session session, QueryBuilder queryBuilder) {
        LOGGER.trace("PwCSFMCUserReportGenerateServiceImpl.getDPEUSUserResult() : Inside getDPEUSUserResult method.");
        final Map<String, String> map = new HashMap<>();

        map.put("type", UserConstants.NT_REP_USER);
        map.put("path", EXTERNAL_USER_PATH);
        map.put("property", "@profile/country");
        map.put("property.value", "US");
        map.put("p.limit", "-1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
        return query.getResult();
    }

    /**
     * Creates external user report in AEM at given location.
     *
     * @param parentNode  {@link Node} - folder under which external user report is created.
     * @param session     {@link Session} - admin session to create nodes.
     * @param inputStream {@link InputStream} - inputStream contains report data in byte form.
     */
    private void
    createCSVFileReport(Node parentNode, Session session, InputStream inputStream, String userReportName)
            throws RepositoryException {
        LOGGER.trace("PwCSFMCUserReportGenerateServiceImpl.createCSVFileReport() method started");
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
     * @param result {@link SearchResult} - results of external user, who are registered through DPE for Global territory.
     */
    private StringBuilder setCSVInfo(SearchResult result) throws RepositoryException {
        LOGGER.info("PwCSFMCUserReportGenerateServiceImpl.setCSVInfo() method started");
        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER);
        csv.append(LINE_BREAK);
        List<Hit> searchResult = result.getHits();
        if (!searchResult.isEmpty()) {
            DateTimeFormatter existingRegDateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXXXX");
            DateTimeFormatter utcDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXXXX");
            for (Hit hit : result.getHits()) {
                Resource resource = hit.getResource();

                Resource profileNode = resource.getChild(PROFILE_NODE);
                if (profileNode != null) {
                    User regUser = profileNode.adaptTo(User.class);
                    regUser.setRegistration(convertToUTC(regUser.getRegistration().replaceAll("&quot;", ""), existingRegDateFormat, utcDateFormat));
                    regUser.setValidation(convertToUTC(regUser.getValidation().replaceAll("&quot;", ""), existingRegDateFormat, utcDateFormat));
                    csv.append(regUser.addSFMCData());
                }
            }
        }
        return csv;
    }

    /**
     * Converts given date to the UTC zone.
     *
     * @param dateString         {@link String} - Date string that needs to be converted.
     * @param existingDateFormat {@link DateTimeFormatter} - Existing date format of the date string.
     * @param desiredDateFormat  {@link DateTimeFormatter}- Desired date format of the date string.
     */
    private String convertToUTC(String dateString, DateTimeFormatter existingDateFormat, DateTimeFormatter desiredDateFormat) {
        LOGGER.debug("PwCSFMCUserReportGenerateServiceImpl.convertToUTC() method started");
        if (StringUtils.isNotEmpty(dateString.replaceAll("\"|\"\"|'", "")) && null != existingDateFormat) {
            OffsetDateTime odtInstanceAtOffset = OffsetDateTime.parse(dateString.replaceAll("\"|\"\"|'", ""), existingDateFormat);
            OffsetDateTime odtInstanceAtUTC = odtInstanceAtOffset.withOffsetSameInstant(ZoneOffset.UTC);
            return odtInstanceAtUTC.format(desiredDateFormat);
        }
        return "\"\"\"\"\"\"";
    }
}
