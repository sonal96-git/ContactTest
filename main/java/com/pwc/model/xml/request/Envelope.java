package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Envelope' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Envelope")
public class Envelope {
    private Sender sender;
    private Unit unit;
    private String version;

    public Envelope() {
    }
    
    public Envelope(final Sender sender, final Unit unit, final String version) {
	this.sender = sender;
	this.unit = unit;
	this.version = version;
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
}
