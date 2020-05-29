package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'PacketInfo' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "PacketInfo")
public class PacketInfo {
    private int packetId;
    private String packetType;
    private String encrypted;
    
    public PacketInfo() {
    }

    public PacketInfo(final int packetId, final String packetType, final String encrypted) {
	this.packetId = packetId;
	this.packetType = packetType;
	this.encrypted = encrypted;
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
    
    public String getEncrypted() {
	return encrypted;
    }
    
    @XmlAttribute(name = "encrypted")
    public void setEncrypted(final String encrypted) {
	this.encrypted = encrypted;
    }

}
