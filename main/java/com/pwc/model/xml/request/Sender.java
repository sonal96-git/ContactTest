package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Sender' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Sender")
public class Sender {
    private int id;
    private int credential;
    
    public Sender() {
    }
    
    public Sender(final int id, final int credential) {
	this.id = id;
	this.credential = credential;
    }

    public int getId() {
	return id;
    }
    
    @XmlElement(name = "Id")
    public void setId(final int id) {
	this.id = id;
    }
    
    public int getCredential() {
	return credential;
    }

    @XmlElement(name = "Credential")
    public void setCredential(final int credential) {
	this.credential = credential;
    }
    
}
