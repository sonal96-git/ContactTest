package com.pwc.user.adapter;

import com.pwc.access_control.model.AccessRequest;
import com.pwc.access_control.model.GroupRequest;
import com.pwc.model.Territory;
import com.pwc.user.AccessRequestProps;
import com.pwc.user.Constants;
import com.pwc.user.model.UserProfile;
import com.pwc.user.model.UserProfilePreference;
import com.pwc.user.services.PreferencesListService;
import com.pwc.user.services.UserInformationService;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.wcm.services.CountryTerritoryMapperService;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import java.util.*;

import static com.adobe.aemds.guide.utils.GuideConstants.STATUS;
import static com.pwc.user.Constants.*;

/**
 * Adapter to map {@link Authorizable} or email to {@link UserProfile}.
 */
public class UserProfileAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileAdapter.class);

    private static final Map<String, String> accessControlStatusMap = new HashMap<>();

    static {
        accessControlStatusMap.put(AccessRequestProps.APPROVED.toString(), "Approved");
        accessControlStatusMap.put(AccessRequestProps.REJECTED.toString(), "Request Declined");
        accessControlStatusMap.put(AccessRequestProps.PENDING_APPROVAL.toString(), "Pending Approval");
        accessControlStatusMap.put("", "Status not found");
    }

    /**
     * It adapts the {@link Authorizable} to {@link UserProfile}.
     *
     * @param user {@link Authorizable} object of the user
     * @param resourceResolver the resource resolver
     * @param countryTerritoryMapperService {@link CountryTerritoryMapperService}
     * @param preferencesListService {@link PreferencesListService} the preferences list service
     * @return {@link UserProfile}. Returns null if user is null
     */
    public UserProfile adaptAuthorizableToUserProfile(final Authorizable user, final ResourceResolver resourceResolver,
            final CountryTerritoryMapperService countryTerritoryMapperService, final PreferencesListService preferencesListService) {
        UserProfile userProfile = null;
        if (user != null) {
            Resource userProfileResource;
            try {
                userProfileResource = resourceResolver.getResource(user.getPath() + Constants.USER_PROFILE_NODENAME);
                ValueMap userProfileValueMap = userProfileResource.getValueMap();
                BundleContext bundleContext = FrameworkUtil.getBundle(UserProfileAdapter.class).getBundleContext();
                ServiceReference serviceRef = bundleContext.getServiceReference(UserInformationService.class.getName());
                UserInformationService userInformationService = (UserInformationService) bundleContext.getService(serviceRef);
                final String firstName = userProfileValueMap.get(Constants.USER_FIRST_NAME_PROPERTY, String.class);
                final String lastName = userProfileValueMap.get(Constants.USER_LAST_NAME_PROPERTY, String.class);
                final String email = userProfileValueMap.get(Constants.USER_EMAIL_PROPERTY, String.class);
                final String country = userProfileValueMap.get(Constants.USER_COUNTRY_PROPERTY, String.class);
                final String organization = userProfileValueMap.get(Constants.USER_COMPANY_PROPERTY, String.class);
                final String jobTitle = userProfileValueMap.get(Constants.USER_JOB_TITLE_PROPERTY, String.class);
                final boolean isUserAdvisoryBoard = userProfileValueMap.get(Constants.USER_ADVISORY_BOARD_PROPERTY, false);
                final boolean isInternalUser = userInformationService.isInternalUser(user);
                final String relationshipWithPwC = userProfileValueMap.get(Constants.USER_RELATIONSHIP_WITH_PWC_PROPERTY, String.class);
                String preferredTerritory = userProfileValueMap.get(Constants.USER_TERRITORY_PROPERTY, String.class);
                String preferredLocale = userProfileValueMap.get(Constants.USER_PREFERRED_LOCALE_PROPERTY, String.class);
                if (preferredTerritory == null) {
                    Territory territory = countryTerritoryMapperService.getTerritoryByCountry(country, Constants.DEFAULT_TERRITORY);
                    preferredTerritory = territory.getTerritoryCode();
                    if (preferredLocale == null)
                        preferredLocale = territory.getDefaultLocale();
                }
                final String marketingConsent = userProfileValueMap.get(Constants.USER_REQUEST_MARKETING_CONSENT_PROPERTY,"");
                final boolean showSFMCPage = null != country && country.equalsIgnoreCase("us");
                userProfile = new UserProfile(firstName, lastName, email, country, organization, jobTitle, preferredLocale,
                        preferredTerritory, relationshipWithPwC,
                        getUserPreferences(user, resourceResolver, preferredTerritory, preferencesListService), isUserAdvisoryBoard, marketingConsent, isInternalUser,
                        getUserAccessRequests(user, resourceResolver),showSFMCPage);
            } catch (RepositoryException re) {
                LOGGER.error("UserProfileAdapter : adaptAuthorizableToUserProfile : Exception occured while reading user profile {}", re);
            }
        }
        LOGGER.debug("UserProfileAdapter : adaptAuthorizableToUserProfile : returning User Profile {}", userProfile);
        return userProfile;
    }
    
    /**
     * It adapts the email to {@link UserProfile}.
     * 
     * @param email email-id
     * @param resourceResolver {@link ResourceResolver}
     * @param countryTerritoryMapperService {@link CountryTerritoryMapperService}
     * @param preferencesListService {@link PreferencesListService} the preferences list service
     * @return {@link UserProfile}. Returns null if email is null
     */
    public UserProfile adaptEmailtoUserProfile(final String email, final ResourceResolver resourceResolver,
            final CountryTerritoryMapperService countryTerritoryMapperService, final PreferencesListService preferencesListService) {
        UserProfile userProfile = null;
        if (email != null) {
            Session session = resourceResolver.adaptTo(Session.class);
            Authorizable aemUser = UserRegistrationUtil.getAEMUserByEmail(email, session);
            userProfile = adaptAuthorizableToUserProfile(aemUser, resourceResolver, countryTerritoryMapperService, preferencesListService);
        }
        return userProfile;
    }

    /**
     * It adapts the userId to {@link UserProfile}.
     *
     * @param userId Aem userId {@link String}
     * @param resourceResolver {@link ResourceResolver}
     * @param countryTerritoryMapperService {@link CountryTerritoryMapperService}
     * @param preferencesListService {@link PreferencesListService} the preferences list service
     * @return {@link UserProfile}. Returns null if userId is null
     */
    public UserProfile adaptUserIDtoUserProfile(final String userId, final ResourceResolver resourceResolver,
                                               final CountryTerritoryMapperService countryTerritoryMapperService, final PreferencesListService preferencesListService) {
        UserProfile userProfile = null;
        if (null != userId) {
            JackrabbitSession session = (JackrabbitSession)resourceResolver.adaptTo(Session.class);
            try {
                Authorizable aemUser = session.getUserManager().getAuthorizable(userId);
                userProfile = adaptAuthorizableToUserProfile(aemUser, resourceResolver, countryTerritoryMapperService, preferencesListService);
            } catch (RepositoryException repositoryException) {
                LOGGER.error("UserProfileAdapter.adaptUserIDtoUserProfile : RepositoryException {} occurred while getting user information for id {}", repositoryException, userId);
            }
        }
        return userProfile;
    }
    
    /**
     * Returns the user's saved category to preferences list map for the given territory. If the user's saved preferences for the territory
     * is not present and the territory specific preference list does not exist, it will return the user's saved preferences for the
     * {@value Constants#DEFAULT_TERRITORY} territory.
     * 
     * @param user {@link Authorizable}
     * @param resourceResolver {@link ResourceResolver} must have rights to read the preferences for the user
     * @param territory territory code
     * @param preferencesListService {@link PreferencesListService} the preferences list service
     * @return category to preferences list map or empty map in case of preference list is not present
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     */
    private Map<String, List<UserProfilePreference>> getUserPreferences(final Authorizable user, final ResourceResolver resourceResolver,
            final String territory, final PreferencesListService preferencesListService)
            throws UnsupportedRepositoryOperationException, RepositoryException {
        Map<String, List<UserProfilePreference>> preferencesMap = new HashMap<String, List<UserProfilePreference>>();
        String userPreferencesCategoryPath = user.getPath() + Constants.USER_PREFERENCE_CATEGORY_NODENAME;
        Resource userPreferencesResource = resourceResolver.getResource(userPreferencesCategoryPath + "/" + territory);
        if (userPreferencesResource == null
                && resourceResolver.getResource(preferencesListService.getUserPreferencesPath() + "/" + territory) == null)
            userPreferencesResource = resourceResolver.getResource(userPreferencesCategoryPath + "/" + Constants.DEFAULT_TERRITORY);
        if (userPreferencesResource != null) {
            for (Resource categoryResource : userPreferencesResource.getChildren()) {
                List<UserProfilePreference> preferredOptions = new ArrayList<UserProfilePreference>();
                addPreference(preferredOptions, categoryResource);
                preferencesMap.put(categoryResource.getName().toLowerCase(), preferredOptions);
            }
        }
        LOGGER.debug("UserProfileAdapter : getUserPreferences : returning Preferences {} for territory {}", preferencesMap, territory);
        return preferencesMap;
    }
    
    /**
     * Add user's preference {@link UserProfilePreference} stored in the given resource to the preferredOptions {@link List}. It searches
     * for the {@value Constants#USER_PREFERENCES_PATH_PROPERTY} and {@value Constants#USER_PREFERENCES_STATE_PROPERTY} property on the
     * resource and adds {@link UserProfilePreference} to the given preferredOptions {@link List} and it iterates over the children of the
     * resource and call itself to add child preferences.
     * 
     * @param preferredOptions list to which the preference will be added.
     * @param preferenceOptionResource
     */
    private void addPreference(final List<UserProfilePreference> preferredOptions, final Resource preferenceOptionResource) {
        String pathValue = preferenceOptionResource.getValueMap().get(Constants.USER_PREFERENCES_PATH_PROPERTY, String.class);
        String stateValue = preferenceOptionResource.getValueMap().get(Constants.USER_PREFERENCES_STATE_PROPERTY, String.class);
        if (pathValue != null && stateValue != null) {
            preferredOptions.add(new UserProfilePreference(pathValue, stateValue.equals(Constants.USER_PREFERENCES_STATE_INDETERMINATE)));
        }
        for (Resource childPreferenceOptionResource : preferenceOptionResource.getChildren()) {
            addPreference(preferredOptions, childPreferenceOptionResource);
        }
    }

    /**
     * It returns the list of user's group request {@link List<GroupRequest>}.
     *
     * @param user             {@link Authorizable}
     * @param resourceResolver {@link ResourceResolver} must have rights to read the profile details for the user.
     * @return {@link List<AccessRequest>} empty list, if user {@param user} is null or if no request has been made by the user.
     */
    private List<GroupRequest> getUserAccessRequests(final Authorizable user, final ResourceResolver resourceResolver) {
        List<GroupRequest> userAccessRequests = new ArrayList<>();
        if (user != null) {
            Resource userProfileResource;
            try {
                userProfileResource = resourceResolver.getResource(user.getPath() + Constants.USER_PROFILE_NODENAME);
                if (userProfileResource.hasChildren()) {
                    GroupRequest userGroupRequest = null;
                    for (Resource groupRequest : userProfileResource.getChildren()) {
                        ValueMap groupRequestValueMap = groupRequest.getValueMap();
                        userGroupRequest = new GroupRequest(groupRequestValueMap.get(ACCESS_CONTROL_REQUESTED_GROUP, ""), groupRequestValueMap.get(STATUS, ""),
                                groupRequestValueMap.get(ACCESS_CONTROL_LANDING_PAGE, ""));
                        userAccessRequests.add(userGroupRequest);
                    }
                }
            } catch (RepositoryException repositoryException) {
                LOGGER.error("UserProfileAdapter: getUserAccessRequests : RepositoryException occurred while reading user requests {}", repositoryException);
            }
        }
        LOGGER.debug("UserProfileAdapter: getUserAccessRequests : returning List of user's requests {}", userAccessRequests);
        return userAccessRequests;
    }

    /**
     * It adapts the {@link Authorizable} to {@link List<AccessRequest>}.
     *
     * @param user             {@link Authorizable}
     * @param resourceResolver {@link ResourceResolver} must have rights to read the profile details for the user.
     * @return {@link List<AccessRequest>} empty list, if user {@param user} is null or if no request has been made by the user.
     */
    public List<AccessRequest> adaptAuthorizableToAccessRequest(final Authorizable user, final ResourceResolver resourceResolver) {
        List<AccessRequest> accessRequests = new ArrayList<>();
        if (user != null) {
            Resource userProfileResource;
            try {
                userProfileResource = resourceResolver.getResource(user.getPath() + Constants.USER_PROFILE_NODENAME);
                if (userProfileResource.hasChildren()) {
                    AccessRequest accessRequest;
                    ValueMap userProfileValueMap = userProfileResource.getValueMap();
                    for (Resource groupRequest : userProfileResource.getChildren()) {
                        final ValueMap groupValueMap = groupRequest.getValueMap();
                        for (Resource individualGroupRequest : groupRequest.getChildren()) {
                            final ValueMap requestValueMap = individualGroupRequest.getValueMap();
                            accessRequest = new AccessRequest(individualGroupRequest.getName(),
                                    new Date(Long.valueOf(requestValueMap.get(Constants.ACCESS_CONTROL_REQUESTED_TIME, ""))), requestValueMap.get(STATUS, ""),
                                    requestValueMap.get(ACCESS_CONTROL_DATE_APPROVED_REJECTED, ""),
                                    requestValueMap.get(ACCESS_CONTROL_APPROVED_REJECTED_BY, ""),
                                    groupValueMap.get(ACCESS_CONTROL_REQUESTED_GROUP, ""), requestValueMap.get(Constants.ACCESS_CONTROL_REQUESTED_PAGE, ""),
                                    userProfileValueMap.get(Constants.USER_EMAIL_PROPERTY, ""),
                                    userProfileValueMap.get(Constants.USER_FIRST_NAME_PROPERTY, ""),
                                    userProfileValueMap.get(Constants.USER_LAST_NAME_PROPERTY, ""),
                                    userProfileValueMap.get(Constants.USER_COUNTRY_PROPERTY, ""),
                                    userProfileValueMap.get(Constants.USER_COMPANY_PROPERTY, ""),
                                    userProfileValueMap.get(Constants.USER_JOB_TITLE_PROPERTY, ""),
                                    requestValueMap.get(Constants.USER_REJECTION_REASON, ""),
                                    individualGroupRequest.getPath()
                            );
                            accessRequests.add(accessRequest);
                        }
                    }
                }

            } catch (RepositoryException repositoryException) {
                LOGGER.error("UserProfileAdapter : adaptAuthorizableToAccessRequest : RepositoryException occured while reading user requests {}", repositoryException);
            }
        }
        LOGGER.debug("UserProfileAdapter : adaptAuthorizableToAccessRequest : returning List of user's requests {}", accessRequests);
        return accessRequests;
    }
}
