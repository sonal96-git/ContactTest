package com.pwc.wcm.services;



public interface TerminateAuthorWorkflow {

    /**
     *@return list of workflows, which can be terminated by author.
     */
    String[] getWorkflowNames();
}
