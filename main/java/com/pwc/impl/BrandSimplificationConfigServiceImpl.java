package com.pwc.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.pwc.ApplicationConstants;
import com.pwc.BrandSimplificationConfigService;

/**
 * A Service class which implements BrandSimplificationConfigService and brand simplification related configurations.
 */
@Component(immediate = true, service = { BrandSimplificationConfigService.class }, enabled = true)
@Designate(ocd = BrandSimplificationConfigServiceImpl.Config.class)
public class BrandSimplificationConfigServiceImpl implements BrandSimplificationConfigService {
        
		private boolean isBrandSimplificationEnabled;
        
        @ObjectClassDefinition(name = "PwC Brand Simplification Configuration", description = "Configure if the brand simplification is to be enabled for the site")
        @interface Config {
        	@AttributeDefinition(name = "Enable Brand Simplification", 
        						description = "Check the box to enable Brand Simplification",
        						type = AttributeType.BOOLEAN)
        	public boolean isBrandSimplificationEnabled() default true;
        }

        @Override public boolean isBrandSimplificationEnabled(SlingHttpServletRequest request) {
                if (null != request) {
                        List<String> selectors = Arrays.asList(request.getRequestPathInfo().getSelectors());
                        return selectors.parallelStream().anyMatch(selector -> selector.contains(ApplicationConstants.REBRAND_URL_SELECTOR))|| isBrandSimplificationEnabled;
                }
                return false;
        }

    @Activate
    @Modified
    protected void activate(final BrandSimplificationConfigServiceImpl.Config config) {
        isBrandSimplificationEnabled = config.isBrandSimplificationEnabled();
    }
}
