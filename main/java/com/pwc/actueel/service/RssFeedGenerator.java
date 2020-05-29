package com.pwc.actueel.service;

import java.io.Writer;

import org.apache.sling.api.resource.ResourceResolver;

import com.pwc.actueel.xml.model.Channel;

public interface RssFeedGenerator {
    /**
     * Get the root object of type Channel containing all the Articles corresponding to each page present under the 'nl'
     * territory under the provided language hierarchy.
     *
     * @param resolver {@link ResourceResolver}
     * @param languageCodes {@link String[]} The pages under these languages of 'nl' territory will be returned.
     *            Example: nl, en, etc.
     * @return {@link Channel} The root object containing all the Articles. An error XML is returned in case any error
     *         is received.
     */
    Channel getChannelContainingAllArticlesForTerritory(ResourceResolver resolver, String[] languageCodes);
    
    /**
     * Marshals the given root object into the given Writer. Error Respon
     *
     * @param channel {@link Channel} The root object which will be marshaled into XML
     * @param writer {@link Writer} The marshaled XML will be written to <b>writer</b>.
     */
    void marshalIntoWriter(Object rootObject, Writer writer);
}
