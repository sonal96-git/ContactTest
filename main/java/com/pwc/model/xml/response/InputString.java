package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'InputString' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "InputString")
public class InputString {
    
    private int clientId;
    private int siteId;
    private int pageNumber;
    private int outputXMLFormat;
    private String authenticationToken;
    private String hotJobs;
    private ProximitySearch proximitySearch;
    private String jobMatchCriteriaText;
    private String selectedSearchLocaleId;
    private Questions questions;

    public InputString() {
    }

    public InputString(final int clientId, final int siteId, final int pageNumber, final int outputXMLFormat,
            final String authenticationToken, final String hotJobs, final ProximitySearch proximitySearch,
            final String jobMatchCriteriaText, final String selectedSearchLocaleId, final Questions questions) {
	this.clientId = clientId;
	this.siteId = siteId;
	this.pageNumber = pageNumber;
	this.outputXMLFormat = outputXMLFormat;
	this.authenticationToken = authenticationToken;
	this.hotJobs = hotJobs;
	this.proximitySearch = proximitySearch;
	this.jobMatchCriteriaText = jobMatchCriteriaText;
	this.selectedSearchLocaleId = selectedSearchLocaleId;
	this.questions = questions;
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
    
    public ProximitySearch getProximitySearch() {
	return proximitySearch;
    }
    
    @XmlElement(name = "ProximitySearch")
    public void setProximitySearch(final ProximitySearch proximitySearch) {
	this.proximitySearch = proximitySearch;
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
    
    public Questions getQuestions() {
	return questions;
    }
    
    @XmlElement(name = "Questions")
    public void setQuestions(final Questions questions) {
	this.questions = questions;
    }
    
}
