package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Packet' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Packet")
public class Packet {
    private Payload payload;
    private PacketInfo packetInfo;
    private Status status;

    public Packet() {
    }

    public Payload getPayload() {
	return payload;
    }
    
    public Packet(final Payload payload, final PacketInfo packetInfo, final Status status) {
	this.payload = payload;
	this.packetInfo = packetInfo;
	this.status = status;
    }
    
    @XmlElement(name = "Payload")
    public void setPayload(final Payload payload) {
	this.payload = payload;
    }
    
    public PacketInfo getPacketinfo() {
	return packetInfo;
    }

    @XmlElement(name = "PacketInfo")
    public void setPacketinfo(final PacketInfo packetInfo) {
	this.packetInfo = packetInfo;
    }

    public Status getStatus() {
	return status;
    }

    @XmlElement(name = "Status")
    public void setStatus(final Status status) {
	this.status = status;
    }
    
}
