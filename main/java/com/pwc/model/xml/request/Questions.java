package com.pwc.model.xml.request;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Questions' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Questions")
public class Questions {
    private List<Question> questions;

    public Questions() {
    }

    public Questions(final List<Question> questions) {
	this.questions = questions;
    }
    
    public List<Question> getQuestions() {
	return questions;
    }
    
    @XmlElement(name = "Question")
    public void setQuestions(final List<Question> questions) {
	this.questions = questions;
    }
    
}
