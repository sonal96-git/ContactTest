package com.pwc.access_control.model;

import java.util.Date;

/**
 * Model class of user's request for access control.
 */
public class AccessRequest {
	private String requestId;
	private Date dateRequested;
	private String status;
	private String dateApprovedOrRejected;
	private String approvedOrRejectedBy;
	private String acg;
	private String pageUrl;
	private String email;
	private String firstName;
	private String lastName;
	private String country;
	private String company;
	private String jobTitle;
	private String rejectionReason;
	private String accessRequestPath;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Date getDateRequested() {
		return dateRequested;
	}

	public void setDateRequested(Date dateRequested) {
		this.dateRequested = dateRequested;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDateApprovedOrRejected() {
		return dateApprovedOrRejected;
	}

	public void setDateApprovedOrRejected(String dateApprovedOrRejected) {
		this.dateApprovedOrRejected = dateApprovedOrRejected;
	}

	public String getApprovedOrRejectedBy() {
		return approvedOrRejectedBy;
	}

	public void setApprovedOrRejectedBy(String approvedOrRejectedBy) {
		this.approvedOrRejectedBy = approvedOrRejectedBy;
	}

	public String getAcg() {
		return acg;
	}

	public void setAcg(String acg) {
		this.acg = acg;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getAccessRequestPath() {
		return accessRequestPath;
	}

	public void setAccessRequestPath(String accessRequestPath) {
		this.accessRequestPath = accessRequestPath;
	}

	public AccessRequest(String requestId, Date dateRequested, String status, String dateApprovedOrRejected,
			String approvedOrRejectedBy, String acg, String pageUrl, String email, String firstName, String lastName,
			String country, String company, String jobTitle, String rejectionReason, String accessRequestPath) {
		this.requestId = requestId;
		this.dateRequested = dateRequested;
		this.status = status;
		this.dateApprovedOrRejected = dateApprovedOrRejected;
		this.approvedOrRejectedBy = approvedOrRejectedBy;
		this.acg = acg;
		this.pageUrl = pageUrl;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.country = country;
		this.company = company;
		this.jobTitle = jobTitle;
		this.rejectionReason = rejectionReason;
		this.accessRequestPath = accessRequestPath;
	}

	@Override
	public String toString() {
		return "AccessRequest [requestId=" + requestId + ", dateRequested=" + dateRequested + ", status=" + status
				+ ", dateApprovedOrRejected=" + dateApprovedOrRejected + ", approvedOrRejectedBy="
				+ approvedOrRejectedBy + ", acg=" + acg + ", pageUrl=" + pageUrl + ", email=" + email + ", firstName="
				+ firstName + ", lastName=" + lastName + ", country=" + country + ", company=" + company + ", jobTitle="
				+ jobTitle + ", rejectionReason=" + rejectionReason + "]";
	}

}
