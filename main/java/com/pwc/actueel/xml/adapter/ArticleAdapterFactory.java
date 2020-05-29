package com.pwc.actueel.xml.adapter;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import com.pwc.actueel.xml.model.Article;

/**
 * Adapter Factory to adapt a Page resource to an Article of an Actueel RSS Feed.
 */
@Component(immediate = true, service = { AdapterFactory.class },
property = { AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
		     AdapterFactory.ADAPTER_CLASSES + "=com.pwc.actueel.xml.model.Article"
})
public class ArticleAdapterFactory implements AdapterFactory {
    @Override
    public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> typeClass) {
        if (adaptable instanceof Resource && typeClass.equals(Article.class))
            return typeClass.cast(new ArticleAdapter().adaptPageResourceToArticle((Resource) adaptable));
        return null;
    }
}