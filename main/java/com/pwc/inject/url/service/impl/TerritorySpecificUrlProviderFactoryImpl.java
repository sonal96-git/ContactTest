package com.pwc.inject.url.service.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.inject.url.enums.TerritorySpecificUrlType;
import com.pwc.inject.url.service.TerritorySpecificUrlProvider;
import com.pwc.inject.url.service.TerritorySpecificUrlProviderFactory;

@Component(service = TerritorySpecificUrlProviderFactory.class, immediate = true)
public class TerritorySpecificUrlProviderFactoryImpl implements TerritorySpecificUrlProviderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TerritorySpecificUrlProviderFactory.class);
    
    private BundleContext bundleContext = null;
    
    @Activate
    @Modified
    protected final void activate(final BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
    }
    
    @Override
    public TerritorySpecificUrlProvider getTerritorySpecificServiceProvider(TerritorySpecificUrlType territorySpecificUrlType) {
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(TerritorySpecificUrlProvider.class.getName(),
                    "(" + TerritorySpecificUrlProviderFactory.TERRITORY_SPECIFIC_URL_PROVIDER_PROPERTY + "="
                            + territorySpecificUrlType.getType() + ")");
            if (refs != null)
                return (TerritorySpecificUrlProvider) bundleContext.getService(refs[0]);
            else
                LOGGER.debug(
                        "TerritorySpecificUrlProviderFactoryImpl.getTerritorySpecificServiceProvider : No Service Reference found for type {}",
                        territorySpecificUrlType);
        } catch (InvalidSyntaxException invalidSyntaxException) {
            LOGGER.error(
                    "TerritorySpecificUrlProviderFactoryImpl.getTerritorySpecificServiceProvider : InvalidSyntaxException occured while getting service reference of type {} : {}",
                    territorySpecificUrlType, invalidSyntaxException);
        }
        return null;
    }
    
}
