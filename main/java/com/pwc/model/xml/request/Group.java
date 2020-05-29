package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Models the 'Group' element of the request Xml for Job Results API.
 */
@XmlRootElement
public class Group {
    private String value;

    public Group() {
    }
    
    public Group(final String value) {
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
