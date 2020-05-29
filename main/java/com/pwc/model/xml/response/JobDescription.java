package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Models the 'JobDescription' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "JobDescription")
public class JobDescription {
    private String value;
    
    public JobDescription() {
    }
    
    public JobDescription(final String value) {
	this.value = value;
    }
    
    public String getValue() {
	return value;
    }
    
    @XmlValue
    public void setValue(final String value) {
	this.value = value;
    }
    
}
