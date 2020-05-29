package com.pwc.topic.redirect.service;

import org.apache.sling.api.SlingHttpServletRequest;

import com.pwc.topic.redirect.model.RedirectInfo;

/**
 * Provides information related to Topic Site Redirection for the provided page data.
 */
public interface TopicSiteRedirection {
    /**
     * Applies the Topic Site Redirection logic to the given pagePath, topicType, topicHomepage and topicSiteTitle and returns a response
     * containing the redirection details depending on the user's location(for anonymous user) or preference(for logged in user).
     * 
     * @param request {@link SlingHttpServletRequest}
     * @param currentPagePath {@link String}
     * @param topicType {@link String}
     * @param topicHomePagePath {@link String}
     * @param topicSiteTitle {@link String}
     * @return {@link RedirectInfo}
     */
    public RedirectInfo getTopicSiteRedirectionInfo(SlingHttpServletRequest request, String currentPagePath, String topicType,
            String topicHomePagePath, String topicSiteTitle);
}
