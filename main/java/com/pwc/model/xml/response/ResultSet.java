package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'ResultSet' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "ResultSet")
public class ResultSet {

    private OtherInformation otherInformation;
    private Jobs jobs;

    public ResultSet() {
    }
    
    public ResultSet(final OtherInformation otherInformation, final Jobs jobs) {
	this.otherInformation = otherInformation;
	this.jobs = jobs;
    }

    public OtherInformation getOtherInformation() {
	return otherInformation;
    }
    
    @XmlElement(name = "OtherInformation")
    public void setOtherInformation(final OtherInformation otherInformation) {
	this.otherInformation = otherInformation;
    }

    public Jobs getJobs() {
	return jobs;
    }
    
    @XmlElement(name = "Jobs")
    public void setJobs(final Jobs jobs) {
	this.jobs = jobs;
    }

}
