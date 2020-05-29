package com.pwc.model.xml.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Job' element of the response Xml from Job Results API.
 */
@XmlRootElement(name = "Job")
public class Job {
    private List<Question> questions;
    private Map<String, String> questionsMap = new HashMap<String, String>();
    private String hotJob;
    private String lastUpdated;
    private JobDescription jobDescription;
    private String jobDetailLink;
    
    public Job() {
    }

    public Job(final List<Question> questions, final String hotJob, final String lastUpdated,
            final String jobDetailLink, final JobDescription jobDescription) {
	this.questions = questions;
	this.hotJob = hotJob;
	this.lastUpdated = lastUpdated;
	this.jobDetailLink = jobDetailLink;
	this.jobDescription = jobDescription;
    }

    public List<Question> getQuestions() {
	return questions;
    }
    
    @XmlElement(name = "Question")
    public void setQuestions(final List<Question> questions) {
	this.questions = questions;
	getQuestionsMap().clear();
	for (final Question question : questions) {
	    getQuestionsMap().put(question.getId(), question.getValue());
	}
    }
    
    public Map<String, String> getQuestionsMap() {
	return questionsMap;
    }

    public void setQuestionsMap(final Map<String, String> questionsMap) {
	this.questionsMap = questionsMap;
    }

    public String getHotJob() {
	return hotJob;
    }
    
    @XmlElement(name = "HotJob")
    public void setHotJob(final String hotJob) {
	this.hotJob = hotJob;
    }
    
    public String getLastUpdated() {
	return lastUpdated;
    }
    
    @XmlElement(name = "LastUpdated")
    public void setLastUpdated(final String lastUpdated) {
	this.lastUpdated = lastUpdated;
    }
    
    public String getJobDetailLink() {
	return jobDetailLink;
    }
    
    @XmlElement(name = "JobDetailLink")
    public void setJobDetailLink(final String jobDetailLink) {
	this.jobDetailLink = jobDetailLink;
    }
    
    public JobDescription getJobDescription() {
	return jobDescription;
    }
    
    @XmlElement(name = "JobDescription")
    public void setJobDescription(final JobDescription jobDescription) {
	this.jobDescription = jobDescription;
    }
    
}
