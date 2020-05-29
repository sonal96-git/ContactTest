package com.pwc.wcm.services;

import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;

public interface PwCSFMCUserReportGenerateService {
    /**
     * Generates a report of users for US users and creates a CSV file at given path.
     *
     * @throws RepositoryException          -
     * @throws UnsupportedEncodingException
     */
    void generateCSVFile() throws RepositoryException, UnsupportedEncodingException;

}
