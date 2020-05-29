package com.pwc.access_control.model;

/**
 * Model class of user's access control group request.
 */
public class GroupRequest {
	private String groupName;
	private String status;
	private String landingPage;

	public GroupRequest(String groupName, String status, String landingPage) {
		this.groupName = groupName;
		this.status = status;
		this.landingPage = landingPage;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}
}
