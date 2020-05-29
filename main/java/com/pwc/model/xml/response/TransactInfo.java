package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Packet' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Packet")
public class TransactInfo {
    private String elementTransactId;
    private String attributeTransactId;
    private String transactType;
    private String timeStamp;
    
    public TransactInfo() {
    }

    public TransactInfo(final String elementTransactId, final String attributeTransactId, final String transactType,
            final String timeStamp) {
	this.elementTransactId = elementTransactId;
	this.attributeTransactId = attributeTransactId;
	this.transactType = transactType;
	this.timeStamp = timeStamp;
    }

    public String getElementTransactId() {
	return elementTransactId;
    }

    @XmlElement(name = "TransactId")
    public void setElementTransactId(final String elementTransactId) {
	this.elementTransactId = elementTransactId;
    }

    public String getAttributeTransactId() {
	return attributeTransactId;
    }

    @XmlAttribute(name = "transactId")
    public void setAttributeTransactId(final String attributeTransactId) {
	this.attributeTransactId = attributeTransactId;
    }

    public String getTransactType() {
	return transactType;
    }

    @XmlAttribute(name = "transactType")
    public void setTransactType(final String transactType) {
	this.transactType = transactType;
    }

    public String getTimeStamp() {
	return timeStamp;
    }

    @XmlElement(name = "TimeStamp")
    public void setTimeStamp(final String timeStamp) {
	this.timeStamp = timeStamp;
    }
    
}
