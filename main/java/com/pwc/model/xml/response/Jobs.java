package com.pwc.model.xml.response;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Jobs' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Jobs")
public class Jobs {
    private List<Job> jobs;

    public Jobs() {
    }

    public Jobs(final List<Job> jobs) {
	this.jobs = jobs;
    }
    
    public List<Job> getJobs() {
	return jobs;
    }
    
    @XmlElement(name = "Job")
    public void setJobs(final List<Job> jobs) {
	this.jobs = jobs;
    }
    
}
