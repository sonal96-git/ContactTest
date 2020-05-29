package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Packet' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Packet")
public class Packet {
    private Payload payload;
    private PacketInfo packetInfo;

    public Packet() {
    }

    public Payload getPayload() {
	return payload;
    }
    
    public Packet(final Payload payload, final PacketInfo packetInfo) {
	this.payload = payload;
	this.packetInfo = packetInfo;
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
    
}
