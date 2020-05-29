package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'InputString' element of the request Xml for Job Results API.
 *
 */
@XmlRootElement(name = "InputString")
public class InputString {

	private int clientId;
	private int siteId;
	private int pageNumber;
	private int outputXMLFormat;
	private String authenticationToken;
	private String hotJobs;
	private String jobMatchCriteriaText;
	private String selectedSearchLocaleId;
	private String returnJobsCount;
	private JobDescription jobDescription;
	private ReturnJobDetailQues returnJobDetailQues;
	private Questions questions;
	private QuestionsForANDCondition questionsForANDCondition;

	public InputString() {
	}

	public InputString(final int clientId, final int siteId, final int pageNumber, final int outputXMLFormat,
			final String authenticationToken, final String hotJobs, final String jobMatchCriteriaText,
			final String selectedSearchLocaleId, final String returnJobsCount,
			final QuestionsForANDCondition questionsForANDCondition, final JobDescription jobDescription,
			final Questions questions,final ReturnJobDetailQues returnJobDetailQues ) {
		this.clientId = clientId;
		this.siteId = siteId;
		this.pageNumber = pageNumber;
		this.outputXMLFormat = outputXMLFormat;
		this.authenticationToken = authenticationToken;
		this.hotJobs = hotJobs;
		this.jobMatchCriteriaText = jobMatchCriteriaText;
		this.selectedSearchLocaleId = selectedSearchLocaleId;
		this.questionsForANDCondition = questionsForANDCondition;
		this.jobDescription = jobDescription;
		this.returnJobDetailQues=returnJobDetailQues;
		this.questions = questions;
		this.returnJobsCount = returnJobsCount;
	}

	public int getClientId() {
		return clientId;
	}

	@XmlElement(name = "ClientId")
	public void setClientId(final int clientId) {
		this.clientId = clientId;
	}

	public int getSiteId() {
		return siteId;
	}

	@XmlElement(name = "SiteId")
	public void setSiteId(final int siteId) {
		this.siteId = siteId;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	@XmlElement(name = "PageNumber")
	public void setPageNumber(final int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getOutputXMLFormat() {
		return outputXMLFormat;
	}

	@XmlElement(name = "OutputXMLFormat")
	public void setOutputXMLFormat(final int outputXMLFormat) {
		this.outputXMLFormat = outputXMLFormat;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	@XmlElement(name = "AuthenticationToken")
	public void setAuthenticationToken(final String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	public String getHotJobs() {
		return hotJobs;
	}

	@XmlElement(name = "HotJobs")
	public void setHotJobs(final String hotJobs) {
		this.hotJobs = hotJobs;
	}

	public String getJobMatchCriteriaText() {
		return jobMatchCriteriaText;
	}

	@XmlElement(name = "JobMatchCriteriaText")
	public void setJobMatchCriteriaText(final String jobMatchCriteriaText) {
		this.jobMatchCriteriaText = jobMatchCriteriaText;
	}

	public String getSelectedSearchLocaleId() {
		return selectedSearchLocaleId;
	}

	@XmlElement(name = "SelectedSearchLocaleId")
	public void setSelectedSearchLocaleId(final String selectedSearchLocaleId) {
		this.selectedSearchLocaleId = selectedSearchLocaleId;
	}

	public String getReturnJobsCount() {
		return returnJobsCount;
	}

	@XmlElement(name = "ReturnJobsCount")
	public void setReturnJobsCount(final String returnJobsCount) {
		this.returnJobsCount = returnJobsCount;
	}

	public JobDescription getJobDescription() {
		return jobDescription;
	}

	@XmlElement(name = "JobDescription")
	public void setJobDescription(final JobDescription jobDescription) {
		this.jobDescription = jobDescription;
	}

	public Questions getQuestions() {
		return questions;
	}

	@XmlElement(name = "Questions")
	public void setQuestions(final Questions questions) {
		this.questions = questions;
	}

	public QuestionsForANDCondition getQuestionsForANDCondition() {
		return questionsForANDCondition;
	}

	@XmlElement(name = "QuestionsForANDCondition")
	public void setQuestionsForANDCondition(final QuestionsForANDCondition questionsForANDCondition) {
		this.questionsForANDCondition = questionsForANDCondition;
	}
	public ReturnJobDetailQues getReturnJobDetailQues() {
		return returnJobDetailQues;
	}

	@XmlElement(name = "ReturnJobDetailQues")
	public void setReturnJobDetailQues(final ReturnJobDetailQues returnJobDetailQues) {
		this.returnJobDetailQues = returnJobDetailQues;
	}

	
}
