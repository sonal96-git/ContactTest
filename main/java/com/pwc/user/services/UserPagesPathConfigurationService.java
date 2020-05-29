package com.pwc.user.services;

/**
 * The Interface UserPagesPathConfigurationService.
 */
public interface UserPagesPathConfigurationService {
	
    /**
     * Gets the user details page path.
     *
     * @return {@link String}
     */
	public String getUserDetailsPagePath();
    
    /**
     * Gets the user edit page path.
     *
     * @return {@link String}
     */
	public String getUserEditPagePath();
    
    /**
     * Gets the user's edit preferences page path.
     *
     * @return {@link String}
     */
	public String getUserPreferencesPagePath();
    
    /**
     * Gets the user delete page path.
     *
     * @return {@link String}
     */
	public String getUserDeletePagePath();
	
     /**
      * Gets the user reading list page path.
      *
      * @return {@link String}
      */
	public String getMyReadingListPagePath();
	
      /**
       * Gets the user shared reading list page path.
       * 
       * @return {@link String}
       */
	public String getSharedReadingListPagePath();

        
	/**
	 * Returns the Resend Activation page path.
	 */
	public String getResendActivationPagePath();

	/**
	 * Returns the Approver's Console page path.
	 */
	public String getApproverConsolePagePath();

}
