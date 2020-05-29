package com.pwc.inject.url.enums;

import com.pwc.inject.url.service.TerritorySpecificUrlProvider;

/**
 * Defines the type of the territory specific URL. Mostly defined to represent the type of the service implementation for {@link
 * {@link TerritorySpecificUrlProvider}.
 */
public enum TerritorySpecificUrlType {
    
    COOKIE_CONSENT(Constants.COOKIE_CONSENT_VALUE), DTM(Constants.DTM_VALUE);
    
    private String type;
    
    TerritorySpecificUrlType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public static class Constants {
        public static final String COOKIE_CONSENT_VALUE = "COOKIE_CONSENT";
        public static final String DTM_VALUE = "DTM";
    }
    
}
