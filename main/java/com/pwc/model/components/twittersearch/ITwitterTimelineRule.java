package com.pwc.model.components.twittersearch;

public interface ITwitterTimelineRule {
    Boolean isMatch(TwitterModel model);
    String getUrl(TwitterModel model);
    String getCssClass(TwitterModel model);
    Boolean isValid(TwitterModel model);
}
