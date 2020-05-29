package com.pwc.topic.redirect.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.topic.redirect.model.Territory;
import com.pwc.topic.redirect.model.Topic;

/**
 * Adapter to map the properties of a {@link Resource} to a {@link Topic}.
 */
public class TopicAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicAdapter.class);
    
    /**
     * Returns a new {@link Topic} object after mapping the required properties of a Resource.
     *
     * @param topicRes {@link Resource} The properties of this resource will be added to the Topic
     * @return {@link Topic}
     */
    public Topic adaptResourceToTopic(final Resource topicRes) {
        Topic topic = null;
        if (topicRes != null) {
            final String name = topicRes.getName();
            final String path = topicRes.getPath();
            final Map<String, Territory> territories = getTerritoriesForTopic(topicRes);
            topic = new Topic(path, name, territories);
            LOGGER.debug("Adapting resource at path {} to Topic: {}", path, topic.toString());
        }
        return topic;
    }
    
    /**
     * Returns a map of {@link Territory} corresponding to the provided {@link Topic}.
     *
     * @param topicRes {@link Resource}
     * @return {@link Map} map of Territory codes to corresponding its properties
     */
    private Map<String, Territory> getTerritoriesForTopic(final Resource topicRes) {
        final Map<String, Territory> territories = new HashMap<String, Territory>();
        for (final Resource territoryRes : topicRes.getChildren()) {
            final Territory territory = territoryRes.adaptTo(Territory.class);
            if (territory != null) {
                territories.put(territoryRes.getName().toLowerCase(), territory);
            }
        }
        return territories;
    }
}
