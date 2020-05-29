package com.pwc.util;

import java.util.Comparator;

import com.pwc.model.Country;

public class CountryComparator implements Comparator<Country> {

	public int compare(Country o1, Country o2) {
		
		return o1.getCountryName().compareTo(o2.getCountryName());
	}

}
