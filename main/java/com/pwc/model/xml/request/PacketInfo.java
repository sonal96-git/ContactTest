package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'PacketInfo' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "PacketInfo")
public class PacketInfo {
    private int packetId;
    private String packetType;

    public PacketInfo() {
    }

    public PacketInfo(final int packetId, final String packetType) {
	this.packetId = packetId;
	this.packetType = packetType;
    }
    
    public int getPacketId() {
	return packetId;
    }
    
    @XmlElement(name = "packetId")
    public void setPacketId(final int packetId) {
	this.packetId = packetId;
    }

    public String getPacketType() {
	return packetType;
    }
    
    @XmlAttribute(name = "packetType")
    public void setPacketType(final String packetType) {
	this.packetType = packetType;
    }

}
