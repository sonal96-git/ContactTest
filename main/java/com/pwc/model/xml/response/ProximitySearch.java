package com.pwc.model.xml.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the 'ProximitySearch' element of the response Xml for Job Results API.
 */
@XmlRootElement(name = "ProximitySearch")
public class ProximitySearch {

    private String distance;
    private String measurement;
    private String country;
    private String state;
    private String city;
    private String zipCode;

    public ProximitySearch(final String distance, final String measurement, final String country, final String state,
            final String city, final String zipCode) {
	this.distance = distance;
	this.measurement = measurement;
	this.country = country;
	this.state = state;
	this.city = city;
	this.zipCode = zipCode;
    }

    public ProximitySearch() {
    }

    public String getDistance() {
	return distance;
    }

    @XmlElement(name = "Distance")
    public void setDistance(final String distance) {
	this.distance = distance;
    }

    public String getMeasurement() {
	return measurement;
    }

    @XmlElement(name = "Measurement")
    public void setMeasurement(final String measurement) {
	this.measurement = measurement;
    }

    public String getCountry() {
	return country;
    }

    @XmlElement(name = "Country")
    public void setCountry(final String country) {
	this.country = country;
    }

    public String getState() {
	return state;
    }

    @XmlElement(name = "State")
    public void setState(final String state) {
	this.state = state;
    }

    public String getCity() {
	return city;
    }

    @XmlElement(name = "City")
    public void setCity(final String city) {
	this.city = city;
    }

    public String getZipCode() {
	return zipCode;
    }

    @XmlElement(name = "ZipCode")
    public void setZipCode(final String zipCode) {
	this.zipCode = zipCode;
    }

}
