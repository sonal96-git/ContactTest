package com.pwc.schedulers;

import com.day.cq.commons.jcr.JcrConstants;
import com.jcraft.jsch.*;
import com.pwc.AdminResourceResolver;
import com.pwc.wcm.utils.UrlSecurity;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.settings.SlingSettingsService;
import org.joda.time.DateTime;
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

import javax.annotation.Nonnull;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * This class listens 'added' event under /content/reports/pwc/dpe-sfmc-us-users-report path for any
 * resource.
 */
@Component(immediate = true, service = {ResourceChangeListener.class}, enabled = true,
        property = {Constants.SERVICE_DESCRIPTION + "= PwC SFMC User Report Add Event"})
@Designate(ocd = PwCSFMCUserReportAddEvent.Config.class)
public class PwCSFMCUserReportAddEvent implements ResourceChangeListener {

    @Reference
    private AdminResourceResolver resourceResolver;

    @Reference
    private SlingSettingsService slingSettingsService;

    private String reportFolderPath;
    private String reportFieldOne;
    private int reportFieldTwo;
    private String reportFieldThree;
    private String reportFieldFour;
    private String reportFieldFive;
    private String reportFieldSix;
    private String reportFieldSeven;

    private static final Logger LOGGER = LoggerFactory.getLogger(PwCSFMCUserReportAddEvent.class);

    @ObjectClassDefinition(name = "PwC SFMC User Report ADD Event", description = "PwC SFMC User Report Update Event")
    @interface Config {

        @AttributeDefinition(name = "Listener Paths",
                description = "Resource Change Listener Paths",
                type = AttributeType.STRING) String resource_paths() default "/content/reports/pwc/dpe-sfmc-us-users-report";

        @AttributeDefinition(name = "Listener Changes",
                description = "Resource Listener Changes",
                type = AttributeType.STRING) String[] resource_change_types() default "";

        @AttributeDefinition(name = "Report Field One",
                type = AttributeType.STRING) String report_field_one() default "";

        @AttributeDefinition(name = "Report Field Two",
                type = AttributeType.INTEGER) int report_field_two();

        @AttributeDefinition(name = "Report Field Three",
                type = AttributeType.STRING) String report_field_three() default "";

        @AttributeDefinition(name = "Report Field Four",
                type = AttributeType.STRING) String report_field_four() default "";

        @AttributeDefinition(name = "Report Field Five",
                type = AttributeType.STRING) String report_field_five() default "";

        @AttributeDefinition(name = "Report Field Six",
                type = AttributeType.STRING) String report_field_six() default "";

        @AttributeDefinition(name = "Report Field Seven",
                type = AttributeType.STRING) String report_field_seven() default "";
    }

    @Override
    public void onChange(@Nonnull final List<ResourceChange> changes) {
        LOGGER.debug("PwCSFMCUserReportAddEvent.onChange() :: Inside the method");
        Set<String> runModes = slingSettingsService.getRunModes();
        if (runModes.contains("author")) {
            ResourceResolver adminResourceResolver = resourceResolver.getAdminResourceResolver();
            try {
                for (final ResourceChange change : changes) {
                    final Resource resource = adminResourceResolver.getResource(change.getPath());
                    removePreviousReport(adminResourceResolver);
                    copyFileToSFTP(resource);
                }
            } finally {
                if (adminResourceResolver.isLive())
                    adminResourceResolver.close();
            }
        }
    }

    /**
     * Transfer the given Resource to external location using SFTP.
     *
     * @param fileResource {@link Resource} The resource which is to be transfer using SFTP.
     */

    private void copyFileToSFTP(Resource fileResource) {

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        Resource file;
        String fileName;
        final String filePath = fileResource.getPath();
        LOGGER.info("PwCSFMCUserReportAddEvent.copyFileToSFTP() : method started for resource {}", filePath);

        try {
            if (filePath.contains(JcrConstants.JCR_CONTENT)) {
                file = fileResource;
                fileName = fileResource.getParent().getName();
            } else {
                file = fileResource.getChild(JcrConstants.JCR_CONTENT);
                fileName = fileResource.getName();
            }
            if (null != file) {
                JSch jsch = new JSch();
                jsch.addIdentity(UrlSecurity.decode(reportFieldSix), UrlSecurity.decode(reportFieldSeven));
                session = jsch.getSession(UrlSecurity.decode(reportFieldThree), UrlSecurity.decode(reportFieldOne), reportFieldTwo);
                session.setPassword(UrlSecurity.decode(reportFieldFour));
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                LOGGER.debug("Host connected.");
                channel = session.openChannel("sftp");
                channel.connect();
                LOGGER.debug("sftp channel opened and connected.");
                channelSftp = (ChannelSftp) channel;
                channelSftp.cd(UrlSecurity.decode(reportFieldFive));
                final ValueMap contentVM = file.getValueMap();
                InputStream inputStream = (InputStream) contentVM.get(JcrConstants.JCR_DATA);
                channelSftp.put(inputStream, fileName);
                LOGGER.info("PwCSFMCUserReportAddEvent.copyFileToSFTP() :: File transfered successfully to host.");
            }
        } catch (JSchException | SftpException excep) {
            LOGGER.error("PwCSFMCUserReportAddEvent.copyFileToSFTP() :: Exception found while transfer the response.", excep);
        } finally {
            if (channelSftp != null) {
                channelSftp.exit();
                LOGGER.trace("PwCSFMCUserReportAddEvent.copyFileToSFTP() :: sftp Channel exited.");
            }
            if (channel != null) {
                channel.disconnect();
                LOGGER.trace("PwCSFMCUserReportAddEvent.copyFileToSFTP() :: Channel disconnected.");
            }
            if (session != null) {
                session.disconnect();
                LOGGER.trace("PwCSFMCUserReportAddEvent.copyFileToSFTP() :: Host Session disconnected.");
            }
        }

    }

    /**
     * Removes the previous reports generated on author instance.
     *
     * @param resourceResolver {@link ResourceResolver} dpe adminResourceResolver.
     */
    private void removePreviousReport(ResourceResolver resourceResolver) {
        LOGGER.info("PwCSFMCUserReportAddEvent.removeResource() :: Inside the method");
        try {
            final String queryString = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" + reportFolderPath + "]) and s.[jcr:created] < CAST('" + new DateTime().withTimeAtStartOfDay() + "' AS DATE)";
            javax.jcr.Session session = resourceResolver.adaptTo(javax.jcr.Session.class);
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
            NodeIterator nodes = query.execute().getNodes();
            while (nodes.hasNext()) {
                nodes.nextNode().remove();
            }
            session.save();
        } catch (InvalidQueryException invalidQueryException) {
            LOGGER.error("PwCSFMCUserReportAddEvent.removeResource() :: error occurred while removing older the sfmc user report", invalidQueryException);
        } catch (RepositoryException repositoryException) {
            LOGGER.error("PwCSFMCUserReportAddEvent.removeResource() :: error occurred while removing older the sfmc user report", repositoryException);
        }
    }

    @Activate
    protected void activate(final Config properties) {
        this.reportFolderPath = properties.resource_paths();
        this.reportFieldOne = properties.report_field_one();
        this.reportFieldTwo = properties.report_field_two();
        this.reportFieldThree = properties.report_field_three();
        this.reportFieldFour = properties.report_field_four();
        this.reportFieldFive = properties.report_field_five();
        this.reportFieldSix = properties.report_field_six();
        this.reportFieldSeven = properties.report_field_seven();

    }

}
