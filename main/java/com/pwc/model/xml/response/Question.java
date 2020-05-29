package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Models the 'Question' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Question")
public class Question {
    private String id;
    private String value;
    
    public Question() {
    }
    
    public Question(final String id, final String value) {
	this.id = id;
	this.value = value;
    }
    
    public String getId() {
	return id;
    }

    @XmlAttribute(name = "Id")
    public void setId(final String id) {
	this.id = id;
    }

    public String getValue() {
	return value;
    }

    @XmlValue
    public void setValue(final String value) {
	this.value = value;
    }
}
