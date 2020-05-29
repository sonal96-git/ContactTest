package com.pwc.wcm.services;

/**
 * Created by rjiang022 on 6/15/2015.
 */
public interface LinkTransformerService {
    public String transformAEMUrl(String path, String requestedUrl);
    public String transformVanity(String vanityUrl, String currentPage) throws Exception;
    public String transformAEMUrl(String path);
    public void logout();
}