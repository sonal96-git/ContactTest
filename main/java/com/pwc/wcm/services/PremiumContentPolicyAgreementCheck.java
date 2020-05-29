package com.pwc.wcm.services;

import com.day.cq.wcm.api.Page;

/**
 * Created by rjiang on 2017-04-11.
 */
public interface PremiumContentPolicyAgreementCheck {
     boolean redirectToAgreement(String userID, Page page);
}
