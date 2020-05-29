package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Payload' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "Payload")
public class Payload {
    private InputString inputString;
    private ResultSet resultSet;

    public Payload() {
    }

    public Payload(final InputString inputString, final ResultSet resultSet) {
	this.inputString = inputString;
	this.resultSet = resultSet;
    }
    
    public InputString getInputString() {
	return inputString;
    }
    
    @XmlElement(name = "InputString")
    public void setInputString(final InputString inputString) {
	this.inputString = inputString;
    }
    
    public ResultSet getResultSet() {
	return resultSet;
    }
    
    @XmlElement(name = "ResultSet")
    public void setResultSet(final ResultSet resultSet) {
	this.resultSet = resultSet;
    }
}
