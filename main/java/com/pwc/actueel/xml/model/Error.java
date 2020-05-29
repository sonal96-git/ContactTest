package com.pwc.actueel.xml.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the Channel object of the RSS Feed Response XML that wraps the error message.
 */
@XmlRootElement(name = "channel")
public class Error implements Channel {
    
    private String errorMsg;

    public Error() {
    }
    
    public Error(final String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }

    @XmlElement(name = "error")
    public void setErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
