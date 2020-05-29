package com.pwc.topic.redirect.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.topic.redirect.model.Language;
import com.pwc.topic.redirect.model.Territory;
import com.pwc.topic.redirect.model.Topic;
import com.pwc.topic.redirect.service.TopicTerritoryMapper;
import com.pwc.wcm.utils.CommonUtils;

@Component(immediate = true, service = { TopicTerritoryMapper.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Exposes methods to query the Topic Site Reference data" })
@Designate(ocd = TopicTerritoryMapperImpl.Config.class)
public class TopicTerritoryMapperImpl implements TopicTerritoryMapper {
    	
    private static final String LOGGER_MSG_PREFIX = "Topic Territory Mapper :";
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicTerritoryMapper.class);
    
    private final Map<String, Topic> topics = new HashMap<String, Topic>();
    
    @Reference
    private AdminResourceResolver adminResourceResolver;
    
    @ObjectClassDefinition(name = "PwC Topic Sites to Territory Mapping Service", 
    		description = "Exposes methods to query the Topic Site Reference data")
    @interface Config {
        @AttributeDefinition(name = "Path to Topic Sites' Reference Data", 
                            description = "Path of the root node of the reference data for Topic Sites",
                            type = AttributeType.STRING)
        public String topicSitesRefDataPath() default "/content/pwc/global/topics";
    }
    
    @Activate
    protected void activate(final TopicTerritoryMapperImpl.Config context) {
        final String topicSitesRefDataPath = context.topicSitesRefDataPath();
        topics.clear();
        if (StringUtils.isBlank(topicSitesRefDataPath)) {
            LOGGER.warn(LOGGER_MSG_PREFIX + "Topic Sites Ref Data Root Path Empty! Skipping Topic To Territory mapping!");
        } else {
            LOGGER.trace(LOGGER_MSG_PREFIX + "Updating Topic Sites Reference Data!");
            updateTopicsForPath(topicSitesRefDataPath);
            LOGGER.trace(LOGGER_MSG_PREFIX + "Finished updating Topic Sites Reference Data!");
        }
    }
    
    @Override
    public Map<String, Topic> getAllTopics() {
        return topics;
    }
    
    @Override
    public Map<String, Territory> getAllTerritoriesForTopic(final String topicType) {
        final Topic topic = topics.get(topicType);
        if (topic == null) {
            LOGGER.error("No Topic Found for the topic type: " + topicType);
            return new HashMap<String, Territory>();
        } else
            return topic.getTerritories();
    }
    
    @Override
    public Map<String, Language> getAllLocaleMappingsForTopicAndTerritory(final String topicType, final String territoryCode) {
        final Territory territory = getTerritoryForTopic(topicType, territoryCode);
        return territory == null ? null : territory.getLocaleToLanguageMappings();
    }
    
    @Override
    public String getDefaultHomepageUrlForTopicAndTerritory(final String topicType, final String territoryCode) {
        final Territory territory = getTerritoryForTopic(topicType, territoryCode);
        if (territory != null) {
            final String defaultLocale = territory.getDefaultLocale();
            if (defaultLocale == null) {
                LOGGER.error(LOGGER_MSG_PREFIX + "No default locale property found for the topic: {} and territory: {}", topicType,
                        territory.getCode());
            } else {
                final Map<String, Language> localeMappings = territory.getLocaleToLanguageMappings();
                if (localeMappings == null) {
                    LOGGER.error("No locale mappings found for the topic: {} and territory: {}" + topicType, territory.getCode());
                } else {
                    final Language language = localeMappings.get(defaultLocale);
                    return language == null ? null : language.getTopicHomePageUrl();
                }
            }
        }
        return null;
    }
    
    @Override
    public String getTopicHomepageUrlForTerritoryAndLocale(final String topicType, final String territoryCode, final String locale) {
        final Map<String, Language> localeMappings = getAllLocaleMappingsForTopicAndTerritory(topicType, territoryCode);
        if (localeMappings == null) {
            LOGGER.error("No locale mappings found for the topic: {} and territory: {}", topicType, territoryCode);
        } else {
            final Language language = localeMappings.get(locale);
            return language == null ? null : language.getTopicHomePageUrl();
        }
        return null;
    }
    
    @Override
    public String getTopicSiteOfPage(String pagePath) {
        final String currentPageTerritory = CommonUtils.getCurrentPageTerritory(pagePath);
        if (StringUtils.isNotBlank(currentPageTerritory)) {
            Map<String, Territory> territories;
            for (String topic : topics.keySet()) {
                territories = getAllTerritoriesForTopic(topic);
                Territory territory = territories.get(currentPageTerritory);
                if (territory != null && isPagePartOfTopicTerritory(pagePath, territory)) {
                    LOGGER.debug("Page at path: {} belongs to the Topic site: {}", pagePath, topic);
                    return topic;
                }
            }
            LOGGER.debug("Page at path: {} is not part of any Topic Sites!", pagePath);
        }
        return null;
    }
    
    /**
     * Fetches the Topic Sites info from the Topic Site Reference data and stores it in memory for faster lookup.
     * 
     * @param topicSitesRefDataPath {@link String}
     */
    private void updateTopicsForPath(final String topicSitesRefDataPath) {
        final ResourceResolver adminResolver = adminResourceResolver.getAdminResourceResolver();
        final Resource topicRefDataRoot = adminResolver.getResource(topicSitesRefDataPath);
        if (topicRefDataRoot != null) {
            for (final Resource topicRes : topicRefDataRoot.getChildren()) {
                final Topic topic = topicRes.adaptTo(Topic.class);
                if (topic != null) {
                    topics.put(topicRes.getName().toLowerCase(), topic);
                }
            }
            LOGGER.debug(LOGGER_MSG_PREFIX + "Updated Topic Sites Reference Data:\n {}", topics);
        } else {
            LOGGER.warn(LOGGER_MSG_PREFIX + "No Reference Data found for the path: {}", topicSitesRefDataPath);
        }
        adminResolver.close();
    }
    
    /**
     * Returns true if given page path is part of any of Locale of the given {@link Territory}.
     * 
     * @param pagePath {@link String}
     * @param territory {@link String}
     * @return boolean true if given page path is part of any Topic site in the given territory
     */
    private boolean isPagePartOfTopicTerritory(String pagePath, Territory territory) {
        Map<String, Language> languages = territory.getLocaleToLanguageMappings();
        if (languages != null) {
            for (Language language : languages.values()) {
                if (isPagePartOfTopicPathHierarchy(pagePath, language.getTopicHomePagePath())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if the given page path is a part of the Topic Site, for which the homepage path is provided.
     * 
     * @param pagePath {@link String}
     * @param homepagePath {@link String}
     * @return boolean
     */
    private boolean isPagePartOfTopicPathHierarchy(String pagePath, String homepagePath) {
        return StringUtils.isNotBlank(homepagePath) && pagePath.contains(homepagePath);
    }
    
    /**
     * Returns the {@link Territory} having the given territory code and belongs to given topic type.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @return {@link Territory}
     */
    private Territory getTerritoryForTopic(final String topicType, final String territoryCode) {
        return getAllTerritoriesForTopic(topicType).get(territoryCode);
    }
    
}
