package com.pwc.wcm.services;

import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;

/**
 * A factory for creating LinkTransformerService objects and also provides methods to access the "PwC Default Domain Configuration".
 */
public interface LinkTransformerServiceFactory {
    
    /**
     * Gets the link transformer service.
     *
     * @param transformerType {@link Object}, expects {@link SlingRepository} / {@link Session} / null object, transformerType defines
     *            whether to initialize the {@link SlingRepository} based or {@link Session} based {@link LinkTransformerService},
     *            {@link LinkTransformerService} object is created with no parameters in case the type is null
     * @return {@link LinkTransformerService} the link transformer service, null in case the transformer type is not instance of the above
     *         mentioned transformer type or an exception occurred while getting Link Transformer Service
     */
    public LinkTransformerService getLinkTransformerService(Object transformerType);
    
    /**
     * Gets the link transformer service if PwC Link Transformer is enabled.
     *
     * @param transformerType {@link Object}, expects {@link SlingRepository} / {@link Session} / null object, transformerType defines
     *            whether to initialize the {@link SlingRepository} based or {@link Session} based {@link LinkTransformerService},
     *            {@link LinkTransformerService} object is created with no parameters in case the type is null
     * @return {@link LinkTransformerService} the link transformer service, null in case the transformer type is not instance of the above
     *         mentioned transformer type or an exception occurred while getting Link Transformer Service or the PwC Link transformer is disabled
     */
    public LinkTransformerService getLinkTransformerServiceIfTransformerEnabled(Object transformerType);

    
    /**
     * Gets the default domain set in "PwC Default Domain Configuration".
     *
     * @return {@link String} the default domain
     */
    public String getDefaultDomain();
    
    /**
     * Gets the domain type set in "PwC Default Domain Configuration".
     *
     * @return {@link String} the domain type
     */
    public String getDomainType();
    
}
