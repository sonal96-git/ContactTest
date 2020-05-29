package com.pwc.model.components.twittersearch;

public interface ITwitterTimelineUrlCalculator {
    String getUrl() throws Exception;
    String getCssClass();
    Boolean isValid();
}
