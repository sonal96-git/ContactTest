package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Status' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Status")
public class Status {
    
    private String Code;
    private String ShortDescription;
    private String LongDescription;
    
    public Status(final String code, final String shortDescription, final String longDescription) {
	this.Code = code;
	this.ShortDescription = shortDescription;
	this.LongDescription = longDescription;
    }
    
    public Status() {
    }
    
    public String getCode() {
	return Code;
    }
    
    @XmlElement(name = "Code")
    public void setCode(final String code) {
	this.Code = code;
    }
    
    public String getShortDescription() {
	return ShortDescription;
    }
    
    @XmlElement(name = "ShortDescription")
    public void setShortDescription(final String shortDescription) {
	this.ShortDescription = shortDescription;
    }
    
    public String getLongDescription() {
	return LongDescription;
    }
    
    @XmlElement(name = "LongDescription")
    public void setLongDescription(final String longDescription) {
	this.LongDescription = longDescription;
    }
}
