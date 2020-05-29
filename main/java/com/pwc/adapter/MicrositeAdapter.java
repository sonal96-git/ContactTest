package com.pwc.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.Microsite;

/**
 * Adapter to map the properties of a {@link Resource} to a {@link Microsite}.
 */
public class MicrositeAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicrositeAdapter.class);
    private static final String PROP_COOKIE_CONSENT_URL = "cookieConsentUrl";
	private static final String PROP_COOKIE_STAGE_CONSENT_URL = "cookieConsentStageUrl";
    private static final String PROP_FORWARD_DOMAIN = "forward-domain";
    
    /**
     * Returns a new {@link Microsite} object after mapping the required properties of a Resource.
     *
     * @param micrositeRes {@link Resource} The properties of this resource will be added to the Microsite
     * @return {@link Microsite}
     */
    public Microsite adaptResourceToMicrosite(final Resource micrositeRes) {
        Microsite Microsite = null;
        if (micrositeRes != null) {
            final ValueMap properties = micrositeRes.getValueMap();
            final String name = micrositeRes.getName();
            final String path = micrositeRes.getPath();
            final String cookieConsentUrl = properties.get(PROP_COOKIE_CONSENT_URL, "");
			final String cookieConsentStageUrl = properties.get(PROP_COOKIE_STAGE_CONSENT_URL, "");
			Microsite = new Microsite(path, name, cookieConsentUrl, cookieConsentStageUrl, getTerritoryForwardDomains(properties));
            LOGGER.debug("Adapting resource at path {} to Microsite: {}", path, Microsite.toString());
        }
        return Microsite;
    }
    
    /**
     * Gets the Microsite forward domains {@link Map} where key is the forward domain property and value is the properties's value.
     *
     * @param micrositeValueMap {@link ValueMap} the territory value map
     * @return {@link Map} the territory forward domains
     */
    private Map<String, String> getTerritoryForwardDomains(final ValueMap micrositeValueMap) {
        Map<String, String> forwardDomains = new HashMap<String, String>();
        for (String property : micrositeValueMap.keySet()) {
            if (property.startsWith(PROP_FORWARD_DOMAIN)) {
                forwardDomains.put(property, micrositeValueMap.get(property, String.class));
            }
        }
        return forwardDomains;
    }
}
