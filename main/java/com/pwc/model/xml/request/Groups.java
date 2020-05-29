package com.pwc.model.xml.request;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Groups' element of the request Xml for Job Results API.
 */
@XmlRootElement(name = "Groups")
public class Groups {
    private List<Group> groupsList;

    public Groups() {
    }

    public Groups(final List<Group> groupsList) {
	this.groupsList = groupsList;
    }
    
    public List<Group> getGroupsList() {
	return groupsList;
    }
    
    @XmlElement(name = "Group")
    public void setGroupsList(final List<Group> groupsList) {
	this.groupsList = groupsList;
    }
}
