package com.pwc.topic.redirect.adapter.factory;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import com.pwc.topic.redirect.adapter.LanguageAdapter;
import com.pwc.topic.redirect.adapter.TerritoryAdapter;
import com.pwc.topic.redirect.adapter.TopicAdapter;
import com.pwc.topic.redirect.model.Language;
import com.pwc.topic.redirect.model.Territory;
import com.pwc.topic.redirect.model.Topic;

/**
 * Adapter Factory to adapt a Resource to a {@link Topic}, a {@link Territory} or a {@link Language}.
 */
@Component(immediate = true, service = { AdapterFactory.class },
			property = { AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
					AdapterFactory.ADAPTER_CLASSES + "=com.pwc.topic.redirect.model.Topic",
					AdapterFactory.ADAPTER_CLASSES + "=com.pwc.topic.redirect.model.Territory",
					AdapterFactory.ADAPTER_CLASSES + "=com.pwc.topic.redirect.model.Language"
})
public class TopicAdapterFactory implements AdapterFactory {
    @Override
    public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> typeClass) {
        if (adaptable instanceof Resource) {
            if (typeClass.equals(Topic.class))
                return typeClass.cast(new TopicAdapter().adaptResourceToTopic((Resource) adaptable));
            else if (typeClass.equals(Territory.class))
                return typeClass.cast(new TerritoryAdapter().adaptResourceToTerritoryTerritory((Resource) adaptable));
            else if (typeClass.equals(Language.class))
                return typeClass.cast(new LanguageAdapter().adaptResourceToLanguage((Resource) adaptable));
        }
        return null;
    }
}
