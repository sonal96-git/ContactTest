package com.pwc.user.services;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

import com.pwc.access_control.model.AccessRequest;
import com.pwc.user.Constants;
import com.pwc.user.model.User;
import com.pwc.user.model.UserProfile;

/**
 * The Interface UserInformationService is used to get the user information like whether user is logged in or not or user's profile
 * information.
 */
public interface UserInformationService {
    
    /**
     * Gets the {@link User} from the given {@link SlingHttpServletRequest}
     *
     * @param slingHttpServletRequest {@link SlingHttpServletRequest}
     * @return {@link User}
     */
    public User getUser(SlingHttpServletRequest slingHttpServletRequest);
    
    /**
     * Gets the {@link UserProfile} for the given AEM {@link Authorizable}.
     *
     * @param aemUser {@link Authorizable}
     * @return {@link UserProfile} returns null if {@link Authorizable} is null
     */
    public UserProfile getUserProfileInformation(Authorizable aemUser);
    
    /**
     * Gets the {@link UserProfile} for the given Email ID.
     *
     * @param email {@link String} User's Email ID
     * @return {@link UserProfile} returns null if the email is null
     */
    public UserProfile getUserProfileInformation(String email);
    
    /**
     * Checks if is user is logged in or not.
     *
     * @param slingHttpServletRequest {@link SlingHttpServletRequest}
     * @return true, if is user logged-in
     */
    public boolean isUserLoggedIn(SlingHttpServletRequest slingHttpServletRequest);

    /**
     * Checks if user node created under the path {@link Constants#INTERNAL_USER_PATH} OR if user belongs to {@link Constants#INTERNAL_USER_GROUP}.
     *
     * @param authorizable     {@link Authorizable}
     * @return {@link Boolean}
     */
    public boolean isInternalUser(Authorizable authorizable);

    /**
     * Checks if user node created under the path {@link Constants#INTERNAL_USER_PATH} OR if user belongs to {@link Constants#INTERNAL_USER_GROUP}.
     *
     * @param authorizable     {@link Authorizable}
     * @param resourceResolver {@link ResourceResolver}
     * @return {@link Boolean}
     */
    public boolean isInternalUser(Authorizable authorizable, ResourceResolver resourceResolver);

    /**
     * Gets the List of user's request for the given AEM {@link Authorizable}.
     *
     * @param aemUser {@link Authorizable}
     * @return {@link List<AccessRequest>} empty list, if user has not made any request for access control.
     */
    public List<AccessRequest> getUserAccessControlInformation(Authorizable aemUser);

	/**
	 * Returns the current logged in user's email address stored in his/her
	 * cookies.
	 * 
	 * @param slingHttpServletRequest {@link SlingHttpServletRequest}
	 * @return Current user's email Id
	 */
	String getUserEmailId(SlingHttpServletRequest slingHttpServletRequest);

	/**
	 * Returns the {@link Authorizable} object for the logged-in user.
	 * @param slingHttpServletRequest {@link SlingHttpServletRequest} of the logged in user
	 * @param adminSession {@link Session} of the admin or a user with permissions having read access to user account paths
	 * @return {@link Authorizable}
	 */
	Authorizable getLoggedInAuthorizable(SlingHttpServletRequest slingHttpServletRequest, Session adminSession);

	boolean isMemberOf(Authorizable authorizable, String groupId) throws RepositoryException;

}
