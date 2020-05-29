package com.pwc.user.services.impl;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.pwc.user.services.UserPagesPathConfigurationService;

@Component(immediate = true, service = { UserPagesPathConfigurationService.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Provides methods to access the User Account Pages Content Path" })
@Designate(ocd = UserPagesPathConfigurationServiceImpl.Config.class)
public class UserPagesPathConfigurationServiceImpl implements UserPagesPathConfigurationService {
    
    public static final String USER_DETAILS_PAGE_PATH_PROPERTY = "user.details.path";
    public static final String USER_DETAILS_PAGE_PATH_VALUE = "/content/pwc/userReg/user-details.html";
    public static final String USER_EDIT_PAGE_PATH_PROPERTY = "user.edit.path";
    public static final String USER_EDIT_PAGE_PATH_VALUE = "/content/pwc/userReg/edit-profile.html";
    public static final String USER_PREFERENCES_PAGE_PATH_PROPERTY = "user.preference.path";
    public static final String USER_PREFERENCES_PAGE_PATH_VALUE = "/content/pwc/userReg/edit-preference-list.html";
    public static final String USER_DELETE_PAGE_PATH_PROPERTY = "user.delete.path";
    public static final String USER_READING_LIST_PAGE_PATH_VALUE = "/content/pwc/userReg/user-readinglists.html";
    public static final String USER_READING_LIST_PAGE_PATH_PROPERTY = "user.readinglist.path";
    public static final String USER_SHARED_READING_LIST_PAGE_PATH_VALUE = "/content/pwc/userReg/user-sharedreadinglist.html";
    public static final String USER_SHARED_READING_LIST_PAGE_PATH_PROPERTY = "user.shared.readinglist.path";
    public static final String USER_DELETE_PAGE_PATH_VALUE = "/content/pwc/userReg/delete-profile.html";
    public static final String USER_RESEND_ACTIVATION_PAGE_PATH_VALUE = "/content/pwc/userReg/resend-activation.html";
    public static final String USER_RESEND_ACTIVATION_PAGE_PATH_PROPERTY = "user.resend.activation.path";
    public static final String APPROVER_CONSOLE_PAGE_PATH_VALUE = "/content/pwc/global/access-control/approver-console.html";
    public static final String APPROVER_PAGE_PATH_PROPERTY = "approver.console.path";
    
    
    private String userDetailsPagePath;
    private String userEditPagePath;
    private String userPreferencesPagePath;
    private String userDeletePagePath;
    private String userReadingListPagePath;
    private String userSharedReadingListPagePath;
    private String resendActivationPagePath;
    private String approverConsolePagePath;
    
    @ObjectClassDefinition(name = "PwC User Registration Pages Paths Configuration Service", 
    		description = "Provides methods to access the User Account Pages Content Path")
    @interface Config {
        @AttributeDefinition(name = "User Details Page Path", 
                            description = "Content page path for user details page",
                            type = AttributeType.STRING)
        public String user_details_path() default USER_DETAILS_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Edit Page Path", 
                description = "Content page path for user edit details page",
                type = AttributeType.STRING)
        public String user_edit_path() default USER_EDIT_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Preferences Page Path", 
                description = "Content page path for user edit preferences page",
                type = AttributeType.STRING)
        public String user_preference_path() default USER_PREFERENCES_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Delete Page Path", 
                description = "Content page path for user delete form page",
                type = AttributeType.STRING)
        public String user_delete_path() default USER_DELETE_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Reading List Page Path", 
                description = "Content page path for user reading list page",
                type = AttributeType.STRING)
        public String user_readinglist_path() default USER_READING_LIST_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Shared Reading List Page Path", 
                description = "Content page path for user shared reading list page",
                type = AttributeType.STRING)
        public String user_shared_readinglist_path() default USER_SHARED_READING_LIST_PAGE_PATH_VALUE;
        
        @AttributeDefinition(name = "User Resend Activation Page Path", 
                description = "Content page path for allowing user to resend the activation link",
                type = AttributeType.STRING)
        public String user_resend_activation_path() default USER_RESEND_ACTIVATION_PAGE_PATH_VALUE;

        @AttributeDefinition(name = "Approver's Console Page Path", 
                description = "Content page path for approver's console to see all the requests",
                type = AttributeType.STRING)
        public String approver_console_path() default APPROVER_CONSOLE_PAGE_PATH_VALUE;
    }
    
    @Activate
    @Modified
    protected final void activate(final UserPagesPathConfigurationServiceImpl.Config properties) throws Exception {
        userDetailsPagePath = properties.user_details_path();
        userEditPagePath = properties.user_edit_path();
        userPreferencesPagePath = properties.user_preference_path();
        userDeletePagePath = properties.user_delete_path();
        userReadingListPagePath = properties.user_readinglist_path();
        userSharedReadingListPagePath = properties.user_shared_readinglist_path();
        resendActivationPagePath = properties.user_resend_activation_path();
        approverConsolePagePath = properties.approver_console_path();
    }
    
    @Override
    public String getUserDetailsPagePath() {
        return userDetailsPagePath;
    }
    
    @Override
    public String getUserEditPagePath() {
        return userEditPagePath;
    }
    
    @Override
    public String getUserPreferencesPagePath() {
        return userPreferencesPagePath;
    }
    
    @Override
    public String getUserDeletePagePath() {
        return userDeletePagePath;
    }
    
    @Override
    public String getMyReadingListPagePath() {
        return userReadingListPagePath;
    }

    @Override
    public String getSharedReadingListPagePath() {
        return userSharedReadingListPagePath;
    }
    
    @Override
    public String getResendActivationPagePath() {
        return resendActivationPagePath;
    }

    @Override
    public String getApproverConsolePagePath() {
        return approverConsolePagePath;
    }

}
