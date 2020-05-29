package com.pwc.model.components.twittersearch.impl;

import com.pwc.model.components.twittersearch.ITwitterTimelineRule;
import com.pwc.model.components.twittersearch.ITwitterTimelineUrlCalculator;
import com.pwc.model.components.twittersearch.TwitterModel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class TwitterTimelineUrlCalculator implements ITwitterTimelineUrlCalculator {
    private ArrayList<ITwitterTimelineRule> timelineRules;
    private TwitterModel model;

    public TwitterTimelineUrlCalculator(TwitterModel model){
        this.model = model;
        //INFO: Remove '@' character from username
        if(!StringUtils.isBlank(this.model.twitterUsername)) {
            this.model.twitterUsername = this.model.twitterUsername.replaceAll("@", StringUtils.EMPTY);
        }
        timelineRules = new ArrayList<>();
        timelineRules.add(new TwitterLikesRule());
        timelineRules.add(new TwitterProfileRule());
        timelineRules.add(new TwitterCollectionRule());
        timelineRules.add(new TwitterListRule());
        timelineRules.add(new TwitterWidgetId());
    }

    @Override
    public String getUrl() throws Exception {
        String url = StringUtils.EMPTY;
        for (ITwitterTimelineRule rule : timelineRules) {
            if (!rule.isMatch(this.model)) continue;
            url = rule.getUrl(this.model);
            break;
        }
        return url;
    }

    @Override
    public String getCssClass() {
        String cssClass = StringUtils.EMPTY;
        for (ITwitterTimelineRule rule : timelineRules) {
            if (!rule.isMatch(this.model)) continue;
            cssClass = rule.getCssClass(this.model);
            break;
        }
        return cssClass;
    }

    @Override
    public Boolean isValid() {
        Boolean isValid = true;
        for (ITwitterTimelineRule rule : timelineRules) {
            if (!rule.isMatch(this.model)) continue;
            isValid = rule.isValid(this.model);
            break;
        }
        return isValid;
    }
}
