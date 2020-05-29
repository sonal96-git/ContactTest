package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'QuestionsForANDCondition' element of the request Xml for Job
 * Results API.
 */
@XmlRootElement(name = "QuestionsForANDCondition")
public class QuestionsForANDCondition {
    private Groups groups;
    
    public QuestionsForANDCondition() {
    }

    public QuestionsForANDCondition(final Groups groups) {
	this.groups = groups;

    }

    public Groups getGroups() {
	return groups;
    }

    @XmlElement(name = "Groups")
    public void setGroups(final Groups groups) {
	this.groups = groups;
    }
}
