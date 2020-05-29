package com.pwc.colors.models;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;

public class ColorsBean {

    private Page currentPage;
    private SlingHttpServletRequest request;

    public ColorsBean(Page currentPage, SlingHttpServletRequest request) {

        this.currentPage = currentPage;
        this.request =request;
    }

    public Page getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }
    public SlingHttpServletRequest getRequest() {
        return request;
    }
    public void setRequest(SlingHttpServletRequest request) {
        this.request = request;
    }
}
