package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Payload' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Payload")
public class Payload {
    private InputString inputString;
    
    public Payload() {
    }
    
    public Payload(final InputString inputString) {
	this.inputString = inputString;
    }

    public InputString getInputString() {
	return inputString;
    }

    @XmlElement(name = "InputString")
    public void setInputString(final InputString inputString) {
	this.inputString = inputString;
    }
}
