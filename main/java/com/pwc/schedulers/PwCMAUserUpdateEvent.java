package com.pwc.schedulers;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.pwc.AdminResourceResolver;

/**
 * This class listens 'changed/added' event under /content/reports/pwc/dpe-gx-ma-external-users-report path for any
 * resource.
 */
@Component(immediate = true, service = { ResourceChangeListener.class }, enabled = true,
property = {Constants.SERVICE_DESCRIPTION + "= PwC MA User Report Update Event" })
@Designate(ocd = PwCMAUserUpdateEvent.Config.class)
public class PwCMAUserUpdateEvent implements ResourceChangeListener {

	@Reference
	private AdminResourceResolver resourceResolver;

	private String hostName;
	private int portNumber;
	private String userName;
	private String userPassword;
	private String directoryPath;

	private static final Logger LOGGER = LoggerFactory.getLogger(PwCMAUserUpdateEvent.class);

	@ObjectClassDefinition(name = "PwC MA User Report Update Event", description = "PwC MA User Report Update Event")
	@interface Config {
		
		@AttributeDefinition(name = "Listener Paths", 
				description = "Resource Change Listener Paths",
				type = AttributeType.STRING)
		public String resource_paths() default "/content/reports/pwc/dpe-gx-ma-external-users-report";
		
		@AttributeDefinition(name = "Listener Changes", 
				description = "Resource Listener Changes",
				type = AttributeType.STRING)
		public String[] resource_change_types() default "";
		
		@AttributeDefinition(name = "SFTP Host Name", 
				description = "Host Name of SFTP Server",
				type = AttributeType.STRING)
		public String sftp_host_name() default "";

		@AttributeDefinition(name = "SFTP Port Number", 
				description = "Port Number of SFTP Server",
				type = AttributeType.INTEGER)
		public int sftp_host_port();

		@AttributeDefinition(name = "SFTP User Name", 
				description = "User Name of SFTP Server",
				type = AttributeType.STRING)
		public String sftp_user_name() default "";

		@AttributeDefinition(name = "SFTP Password", 
				description = "Password of SFTP Server",
				type = AttributeType.STRING)
		public String sftp_user_password() default "";

		@AttributeDefinition(name = "SFTP Directory Path", 
				description = "Directory Path to Store file",
				type = AttributeType.STRING)
		public String sftp_directory_path() default "";
	}

	@Override
	public void onChange(@Nonnull final List<ResourceChange> changes) {
		LOGGER.info("PwCMAUserUpdateEvent :: onChange started");
		ResourceResolver adminResourceResolver = resourceResolver.getAdminResourceResolver();
		for (final ResourceChange change : changes) {
			final Resource resource = adminResourceResolver.getResource(change.getPath());
			copyFileToSFTP(resource);
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
		Resource file = null;
		String fileName = StringUtils.EMPTY;
		LOGGER.info("PwCMAUserUpdateEvent.copyFileToSFTP() method started for resource {}", fileResource.getPath());

		try {
			if(fileResource.getPath().contains(JcrConstants.JCR_CONTENT)) {
				file = fileResource;
				fileName = fileResource.getParent().getName();
			} else {
			 file = fileResource.getChild(JcrConstants.JCR_CONTENT);
				fileName = fileResource.getName();
			}
			if (null != file) {
				JSch jsch = new JSch();
				session = jsch.getSession(userName, hostName, portNumber);
				session.setPassword(userPassword);
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
				session.connect();
				LOGGER.trace("Host connected.");
				channel = session.openChannel("sftp");
				channel.connect();
				LOGGER.trace("sftp channel opened and connected.");
				channelSftp = (ChannelSftp) channel;
				channelSftp.cd(directoryPath);
				final ValueMap contentVM = file.getValueMap();
				InputStream inputStream = (InputStream) contentVM.get(JcrConstants.JCR_DATA);
				channelSftp.put(inputStream, fileName);
				LOGGER.info("PwCMAUserUpdateEvent.copyFileToSFTP() :: File transfered successfully to host.");
			}
		} catch (JSchException | SftpException excep) {
			LOGGER.error("PwCMAUserUpdateEvent.copyFileToSFTP() :: Exception found while transfer the response.", excep);
		} finally {
			if (channelSftp != null) {
				channelSftp.exit();
				LOGGER.trace("sftp Channel exited.");
			}
			if (channel != null) {
				channel.disconnect();
				LOGGER.trace("Channel disconnected.");
			}
			if (session != null) {
				session.disconnect();
				LOGGER.trace("Host Session disconnected.");
			}
		}

	}

	@Activate
	protected void activate(final Config properties) {
		this.hostName = properties.sftp_host_name();
		this.portNumber = properties.sftp_host_port();
		this.userName = properties.sftp_user_name();
		this.userPassword = properties.sftp_user_password();
		this.directoryPath = properties.sftp_directory_path();

	}

}
