package com.pwc.user.model;

import com.google.gson.Gson;

/**
 * The Class UserProfilePreference provides getter and setters for properties of the user's saved preference in AEM.
 */
public class UserProfilePreference {
    
    private String path;
    private boolean isIndeterminate;
    
    /**
     * Checks if the state is indeterminate.
     *
     * @return true, if the state of the preference is indeterminate.
     */
    public boolean isIndeterminate() {
        return isIndeterminate;
    }
    
    /**
     * Sets whether the state of the preference is indeterminate or not.
     *
     * @param isIndeterminate whether the state of the preference is indeterminate or not
     */
    public void setIndeterminate(boolean isIndeterminate) {
        this.isIndeterminate = isIndeterminate;
    }
    
    /**
     * Gets the content path of the preference.
     *
     * @return {@link String} the content path of the preference
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Sets the content path of the preference.
     *
     * @param path {@link String} the content path of the preference
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Instantiates a new user profile preference.
     *
     * @param path {@link String} the content path of the preference
     * @param isIndeterminate whether the state of the preference is indeterminate or not
     */
    public UserProfilePreference(String path, boolean isIndeterminate) {
        super();
        this.path = path;
        this.isIndeterminate = isIndeterminate;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
    
}
