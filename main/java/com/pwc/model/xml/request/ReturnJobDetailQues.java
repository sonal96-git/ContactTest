package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Models the 'ReturnJobDetailQues' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "ReturnJobDetailQues")
public class ReturnJobDetailQues {
	private String value;

	public ReturnJobDetailQues() {
	}

	public ReturnJobDetailQues(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@XmlValue
	public void setValue(final String value) {
		this.value = value;
	}

}
