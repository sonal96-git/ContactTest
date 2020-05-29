package com.pwc.adapter.factory;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import com.pwc.adapter.LanguageAdapter;
import com.pwc.adapter.MicrositeAdapter;
import com.pwc.model.Language;
import com.pwc.model.Microsite;

/**
 * Adapter Factory to adapt a Resource to a {@link Language}.
 */
@Component(immediate = true, service = { AdapterFactory.class },
			property = { AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
					AdapterFactory.ADAPTER_CLASSES + "=com.pwc.model.Microsite",
					AdapterFactory.ADAPTER_CLASSES + "=com.pwc.model.Language"
})
public class TerritoryAdapterFactory implements AdapterFactory {
    @Override
    public <AdapterType> AdapterType getAdapter(final Object adaptable, final Class<AdapterType> typeClass) {
        if (adaptable instanceof Resource) {
            if (typeClass.equals(Language.class))
                return typeClass.cast(new LanguageAdapter().adaptResourceToLanguage((Resource) adaptable));
            else if (typeClass.equals(Microsite.class)) {
                return typeClass.cast(new MicrositeAdapter().adaptResourceToMicrosite((Resource) adaptable));
            }
        }
        return null;
    }
}
