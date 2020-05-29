package com.pwc.model.xml.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'Question' element of the request Xml for Job Results API.
 */
@XmlRootElement
public class Question {
    private String id;
    private String value;
    private String sortorder;
    private String sort;
    
    public Question() {
    }
    
    public Question(final String id, final String value, final String sortorder, final String sort) {
	this.id = id;
	this.value = value;
	this.sortorder = sortorder;
	this.sort = sort;
    }
    
    public String getId() {
	return id;
    }

    @XmlElement(name = "Id")
    public void setId(final String id) {
	this.id = id;
    }

    public String getValue() {
	return value;
    }

    @XmlElement(name = "Value")
    public void setValue(final String value) {
	this.value = value;
    }

    public String getSortorder() {
	return sortorder;
    }

    @XmlAttribute(name = "Sortorder")
    public void setSortorder(final String sortorder) {
	this.sortorder = sortorder;
    }

    public String getSort() {
	return sort;
    }

    @XmlAttribute(name = "Sort")
    public void setSort(final String sort) {
	this.sort = sort;
    }
    
}
