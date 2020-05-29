package com.pwc.wcm.services;

/**
 * Created by jiang on 4/12/2017.
 */
public interface TerritoryPrivacyPolicy {
     boolean hasPolicy(String pagePath);
     String getTerritoryPolicyVersion(String territory, boolean isMicrosite, String micrositeName);
     String getDefaultPrivatePolicyPagePath();
}
