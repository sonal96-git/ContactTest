package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Unit' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Unit")
public class Unit {
    private Packet packet;
    private String unitProcessor;
    
    public Unit() {
    }
    
    public Unit(final Packet packet, final String unitProcessor) {
	this.packet = packet;
	this.unitProcessor = unitProcessor;
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
}
