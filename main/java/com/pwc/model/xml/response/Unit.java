package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Unit' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Unit")
public class Unit {
    private Packet packet;
    private String unitProcessor;
    private Status status;

    public Unit() {
    }

    public Unit(final Packet packet, final String unitProcessor, final Status status) {
	this.packet = packet;
	this.unitProcessor = unitProcessor;
	this.status = status;
    }
    
    public String getUnitProcessor() {
	return unitProcessor;
    }

    @XmlAttribute(name = "UnitProcessor")
    public void setUnitProcessor(final String unitProcessor) {
	this.unitProcessor = unitProcessor;
    }

    public Packet getPacket() {
	return packet;
	
    }
    
    @XmlElement(name = "Packet")
    public void setPacket(final Packet packet) {
	this.packet = packet;
    }
    
    public Status getStatus() {
	return status;
    }

    @XmlElement(name = "Status")
    public void setStatus(final Status status) {
	this.status = status;
    }
}
