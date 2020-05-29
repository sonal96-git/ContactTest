package com.pwc.user.services.impl;

import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.Cookie;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.group.api.GroupUtil;
import com.pwc.AdminResourceResolver;
import com.pwc.access_control.model.AccessRequest;
import com.pwc.user.Constants;
import com.pwc.user.adapter.UserProfileAdapter;
import com.pwc.user.model.User;
import com.pwc.user.model.UserProfile;
import com.pwc.user.services.PreferencesListService;
import com.pwc.user.services.UserInformationService;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import com.pwc.wcm.utils.UrlSecurity;

@Component(immediate = true, service = UserInformationService.class)
public class UserInformationServiceImpl implements UserInformationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserInformationService.class);

	@Reference
	private AdminResourceResolver adminResourceResolver;

	@Reference
	private CountryTerritoryMapperService countryTerritoryMapperService;

	@Reference
	private PreferencesListService preferencesListService;

	@Override
	public User getUser(SlingHttpServletRequest slingHttpServletRequest) {
		return slingHttpServletRequest.adaptTo(User.class);
	}

	@Override
	public UserProfile getUserProfileInformation(Authorizable aemUser) {
		ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
		UserProfile userProfile = new UserProfileAdapter().adaptAuthorizableToUserProfile(aemUser, resourceResolver,
			countryTerritoryMapperService, preferencesListService);
		resourceResolver.close();
		return userProfile;
	}

	@Override
	public UserProfile getUserProfileInformation(String email) {
		ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
		UserProfile userProfile = new UserProfileAdapter().adaptEmailtoUserProfile(email, resourceResolver,
			countryTerritoryMapperService, preferencesListService);
		resourceResolver.close();
		return userProfile;
	}

	@Override
	public boolean isUserLoggedIn(SlingHttpServletRequest slingHttpServletRequest) {
		String emailId = getUserEmailId(slingHttpServletRequest);
		return emailId != null;
	}

	@Override
	public String getUserEmailId(SlingHttpServletRequest slingHttpServletRequest) {
		Cookie pwcCookie = UserRegistrationUtil.getPwCCookie(slingHttpServletRequest, Constants.COOKIE_AUTH_PWC_ID);
		return pwcCookie == null ? null : UrlSecurity.decode(pwcCookie.getValue());
	}

	@Override
	public Authorizable getLoggedInAuthorizable(SlingHttpServletRequest slingHttpServletRequest, Session adminSession) {
		String emailId = getUserEmailId(slingHttpServletRequest);
		return emailId == null ? null : UserRegistrationUtil.getAEMUserByEmail(emailId, adminSession);
	}

	@Override
	public boolean isInternalUser(Authorizable authorizable) {
		boolean isInternalUser = false;
		if (null == authorizable)
			LOGGER.info("UserRegistrationUtil.isInternalUser: Authorizable is null");
		else {
			ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
			isInternalUser = isInternalUser(authorizable, resourceResolver);
			resourceResolver.close();
		}
		return isInternalUser;
	}

	@Override
	public boolean isInternalUser(Authorizable authorizable, ResourceResolver resourceResolver) {
		try {
			if (null == authorizable)
				LOGGER.info("UserRegistrationUtil.isInternalUser: Authorizable is null");
			else if (null == resourceResolver)
				LOGGER.info("UserRegistrationUtil.isInternalUser: ResourceResolver is null");
			else
				return authorizable.getPath().contains(Constants.INTERNAL_USER_PATH) || GroupUtil.isMember(
					(resourceResolver.adaptTo(UserManager.class)), Constants.INTERNAL_USER_GROUP, authorizable.getID());
		} catch (RepositoryException repositoryException) {
			LOGGER.error(
				"UserRegistrationUtil.isInternalUser: RepositoryException occurred while fetching the authorizable details",
				repositoryException);
		}
		return false;
	}

	@Override
	public List<AccessRequest> getUserAccessControlInformation(Authorizable aemUser) {
		ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
		List<AccessRequest> accessRequests = new UserProfileAdapter().adaptAuthorizableToAccessRequest(aemUser,
			resourceResolver);
		resourceResolver.close();
		return accessRequests;
	}

	@Override
	public boolean isMemberOf(Authorizable authorizable, String groupId) throws RepositoryException {
		Iterator<Group> groupIterator = authorizable.memberOf();
		while (groupIterator.hasNext()) {
			if (groupId.equals(groupIterator.next().getID())) {
				return true;
			}
		}
		return false;
	}

}
