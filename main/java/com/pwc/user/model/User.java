package com.pwc.user.model;

import com.google.gson.Gson;
import com.pwc.user.Constants;

/**
 * User POGO class that provides getter and setters for various properties of the user.
 */
public class User {
    
    private boolean isUserLoggedIn;
    private UserProfile userProfile;
    private boolean isInternalUserLogIn;

    /**
     * Instantiates a new user with the given properties.
     *
     * @param isUserLoggedIn true, if the user is logged in
     * @param userProfile the user profile
     */
    public User(boolean isUserLoggedIn, UserProfile userProfile, boolean isInternalUSer) {
        super();
        this.isUserLoggedIn = isUserLoggedIn;
        this.userProfile = userProfile;
        this.isInternalUserLogIn = isInternalUSer;
    }
    
    /**
     * Checks if the user is logged in.
     *
     * @return true, if the user is logged in
     */
    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }
    
    /**
     * Sets if the user is logged in.
     *
     * @param isUserLoggedIn true, if the user is logged in
     */
    public void setUserLoggedIn(boolean isUserLoggedIn) {
        this.isUserLoggedIn = isUserLoggedIn;
    }
    
    /**
     * Gets the user profile.
     *
     * @return the user profile
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }
    
    /**
     * Sets the user profile.
     *
     * @param userProfile the new user profile
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /**
     * @return true, if user node created under the path {@link Constants#INTERNAL_USER_PATH}
     */
    public boolean isInternalUserLogIn() {
        return isInternalUserLogIn;
    }

    /**
     * Sets isInternalUserLogIn status of user
     *
     * @param internalUserLogIn true, if user node created under the path {@link Constants#INTERNAL_USER_PATH}
     */
    public void setInternalUserLogIn(boolean internalUserLogIn) {
        isInternalUserLogIn = internalUserLogIn;
    }
}
