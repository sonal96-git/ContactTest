package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'OtherInformation' element of the response Xml for Job Results
 * API.
 */
@XmlRootElement(name = "OtherInformation")
public class OtherInformation {
    private int totalRecordsFound;
    private int maxPages;
    private int startDoc;
    private int pageNumber;
    
    public OtherInformation() {
    }

    public OtherInformation(final int totalRecordsFound, final int maxPages, final int startDoc, final int pageNumber) {
	this.totalRecordsFound = totalRecordsFound;
	this.maxPages = maxPages;
	this.startDoc = startDoc;
	this.pageNumber = pageNumber;
    }
    
    public int getTotalRecordsFound() {
	return totalRecordsFound;
    }
    
    @XmlElement(name = "TotalRecordsFound")
    public void setTotalRecordsFound(final int totalRecordsFound) {
	this.totalRecordsFound = totalRecordsFound;
    }
    
    public int getMaxPages() {
	return maxPages;
    }
    
    @XmlElement(name = "MaxPages")
    public void setMaxPages(final int maxPages) {
	this.maxPages = maxPages;
    }
    
    public int getStartDoc() {
	return startDoc;
    }
    
    @XmlElement(name = "StartDoc")
    public void setStartDoc(final int startDoc) {
	this.startDoc = startDoc;
    }
    
    public int getPageNumber() {
	return pageNumber;
    }
    
    @XmlElement(name = "PageNumber")
    public void setPageNumber(final int pageNumber) {
	this.pageNumber = pageNumber;
    }

}
