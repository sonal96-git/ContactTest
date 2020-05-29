package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Envelope' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Envelope")
public class Envelope {
    private Sender sender;
    private Unit unit;
    private String version;
    private Status status;

    public Envelope() {
    }
    
    public Envelope(final Sender sender, final Unit unit, final String version, final Status status) {
	this.sender = sender;
	this.unit = unit;
	this.version = version;
	this.status = status;
    }
    
    public String getVersion() {
	return version;
    }

    @XmlAttribute(name = "Version")
    public void setVersion(final String version) {
	this.version = version;
    }

    public Sender getSender() {
	return sender;
    }
    
    @XmlElement(name = "Sender")
    public void setSender(final Sender sender) {
	this.sender = sender;
    }
    
    public Unit getUnit() {
	return unit;
    }
    
    @XmlElement(name = "Unit")
    public void setUnit(final Unit unit) {
	this.unit = unit;
    }
    
    public Status getStatus() {
	return status;
    }
    
    @XmlElement(name = "Status")
    public void setStatus(final Status status) {
	this.status = status;
    }
}
