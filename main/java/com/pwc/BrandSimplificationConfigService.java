package com.pwc;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * An interface for brand simplification related configurations.
 */
public interface BrandSimplificationConfigService {
        /**
         * Returns true if brand simplification is enabled OR request's url has "rebrand" or "rebrand-dynamic" selector.
         *
         * @param request {@link SlingHttpServletRequest} SlingHttpServletRequest object, based on it's path info, method determines if brand simplification is to be enabled.
         * @return {@link boolean}
         */
        public boolean isBrandSimplificationEnabled(SlingHttpServletRequest request);
}
