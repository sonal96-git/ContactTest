package com.pwc.workflow;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.ParticipantStepChooser;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.metadata.MetaDataMap;

/**
 * Created by rjiang on 2017-01-03.
 */
@Component(service = ParticipantStepChooser.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Implementation of PwC dynamic participant chooser",    
    ParticipantStepChooser.SERVICE_PROPERTY_LABEL + "= PwC Form Participant Chooser"    
})
public class PwCParticipantSelfStepChooserImpl implements ParticipantStepChooser{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        String currentUserId = workflowSession.getSession().getUserID();
        return currentUserId;
    }
}
