package com.pwc.topic.redirect.service;

import java.util.Map;

import com.pwc.topic.redirect.model.Language;
import com.pwc.topic.redirect.model.Territory;
import com.pwc.topic.redirect.model.Topic;

/**
 * Exposes methods to query the Topic Site Reference data.
 */
public interface TopicTerritoryMapper {
    
    /**
     * Returns a mapping of all the topic names to the respective {@link Topic} object, which is created corresponding to the Topic Sites
     * Reference data. This map gets created/updated only when the service activates or its configuration modifies.
     * 
     * @return {@link Map}
     */
    Map<String, Topic> getAllTopics();
    
    /**
     * For a given Topic type, returns a map of all the {@link Territory} codes to their corresponding properties.
     * 
     * @param topicType {@link String}
     * @return {@link Map} Mapping of territory code to properties if given topic type found, else returns an empty map
     */
    Map<String, Territory> getAllTerritoriesForTopic(String topicType);
    
    /**
     * For a given pair of Topic type and territory code, returns a mapping of Locale to {@link Language}.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @return {@link Map} Mapping of locale to language, returns null if any of the given topic or territory not found.
     */
    Map<String, Language> getAllLocaleMappingsForTopicAndTerritory(String topicType, String territoryCode);
    
    /**
     * For a given pair of Topic type and Territory code, returns the Topic homepage URL of default Locale.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @return {@link String} Returns topic homepage URL of default Locale if found, else returns null if:
     *         <ul>
     *         <li>The given Topic type or Territory code are blank</li>
     *         <li>No default locale info found in territory</li>
     *         <li>No Locale found corresponding to the Default locale value</li>
     *         <li>Homepage URL not found in the default Locale</li>
     *         </ul>
     */
    String getDefaultHomepageUrlForTopicAndTerritory(String topicType, String territoryCode);
    
    /**
     * For a given combination of Topic and Territory, returns the Topic homepage URL of given Locale.
     * 
     * @param topicType {@link String}
     * @param territoryCode {@link String}
     * @param locale {@link String}
     * @return {@link String} Returns topic homepage URL of given Locale if found, else returns null if:
     *         <ul>
     *         <li>The given Topic type, Territory code or Locale are blank</li>
     *         <li>Topic not found</li>
     *         <li>Territory not found corresponding to the Topic</li>
     *         <li>Locale not found corresponding to the Topic and Territory</li>
     *         <li>Homepage URL not found in the Locale</li>
     *         </ul>
     */
    String getTopicHomepageUrlForTerritoryAndLocale(String topicType, String territoryCode, String locale);
    
    /**
     * Returns the name of the Topic Site that the current page belongs to.
     * 
     * @param pagePath {@link String}
     * @return {@link String} Name of Topics Site that current page belongs to, else null if it doesn't belong to any Topic Site.
     */
    String getTopicSiteOfPage(String pagePath);
}
