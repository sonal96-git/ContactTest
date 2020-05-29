package com.pwc.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceBundleBase extends ResourceBundle {
	
	public Object handleGetObject(String key) {
        
        return key;
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }
    

    // Overrides handleKeySet() so that the getKeys() implementation
    // can rely on the keySet() value.
    protected Set<String> handleKeySet() {
        return new HashSet<String>(Arrays.asList("ContactUsOthers_PwCContactUsCountry", "Country"));
    }

}

