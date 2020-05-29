package com.pwc.wcm.services.impl;

import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.wcm.services.UserregRedirectCheck;

/**
 * Created by rjiang022 on 1/20/2016.
 */
/*@Component(immediate = true, metatype = true, label = "Check if the redirect link in user reg login is valid", name = "UserregRedirectCheck")
@Service*/	// TODO: Do we need to override the Name Property of the Service. 
@Component(immediate = true, service = { UserregRedirectCheck.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Check if the redirect link in user reg login is valid" })
public class UserregRedirectCheckImpl implements UserregRedirectCheck {
    
	@Reference
    private SlingRepository repository;

    @Override
    public boolean urlIsValid(String path) {
        boolean isValid = false;
        Session session = null;
        try {
             session = repository.login();
            path = "https://" + path;
            String queryStr = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/content/pwc/global/referencedata/territories]) AND (s.[forward-domain]='" + path + "' OR s.[forward-domain-qa]='" + path +"' OR s.[forward-domain-staging]='" + path +"')";
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(queryStr, Query.JCR_SQL2);
            QueryResult result = query.execute();
            return result.getNodes().getSize()>0;
        }catch(Exception ex){

        }finally {
            if(session!=null)
                session.logout();
        }
        return isValid;
    }
}
