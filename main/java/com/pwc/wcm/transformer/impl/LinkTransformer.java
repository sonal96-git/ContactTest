package com.pwc.wcm.transformer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;
import com.pwc.wcm.utils.CommonUtils;

import org.apache.cocoon.xml.sax.AbstractSAXPipe;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Created by rui on 6/8/15.
 */

public class LinkTransformer extends AbstractSAXPipe implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(LinkTransformer.class);
    private SlingRepository repository;
    private SlingHttpServletRequest httpRequest;
    private String requestedResourcePath;
    private boolean isPwCRequest;
    /* The element and attribute to act on  */
    //private static final String ATT_NAME = new String("href");
    // private static final String EL_NAME = new String("a");
    private ArrayList<String> transformLinks;
    private boolean enabled;
    private String defaultDomain;
    private String domainType;
    private String pwc365Domain;
    
    @Override
    public void init(ProcessingContext processingContext, ProcessingComponentConfiguration processingComponentConfiguration) throws IOException {
        this.httpRequest = processingContext.getRequest();
        this.requestedResourcePath = this.httpRequest.getResource().getPath();
        isPwCRequest = this.requestedResourcePath.contains("/content/pwc/") || this.requestedResourcePath.contains("/content/dam/pwc/");
    }

    public LinkTransformer() {

    }

    public LinkTransformer(ArrayList<String> links, SlingRepository repository, boolean enabled, String defaultDomain, String domainType, String pwc365Domain) {
        this.transformLinks = links;
        this.repository = repository;
        this.enabled = enabled;
        this.defaultDomain = defaultDomain;
        this.domainType = domainType;
        this.pwc365Domain = pwc365Domain;  
        //log.info("LinkTransformer.contructor = " + this.defaultDomain);

    }

    @Override
    public void dispose() {

    }

    @Override
    public void startElement(String nsUri, String localname, String qname, Attributes atts) throws SAXException {
        AttributesImpl linkAtts = new AttributesImpl(atts);
        //log.info("LinkTransformer.startElement.enabled = " + this.enabled);
        if (enabled && isPwCRequest) {
            //log.info("startElement.enabled " + this.enabled);
            for (String eachLink : transformLinks) {
                //log.info(eachLink + " enabled = " + this.enabled);
                String element_name = eachLink.split(":")[0];
                String element_attr = eachLink.split(":")[1];
                if (element_name.equalsIgnoreCase(localname)) {
                    for (int i = 0; i < linkAtts.getLength(); i++) {
                        if (element_attr.equalsIgnoreCase(linkAtts.getLocalName(i))) {
                            boolean canTranform = false;
                            String path_in_link = linkAtts.getValue(i);
                            //log.info("LinkTransformer.path_in_link = " + path_in_link);
                            //log.info("LinkTransformer.init-requestPath = " + requestedUrl + "\n\n");
                            if (path_in_link.toLowerCase().startsWith("/etc")) { // no translation for /content/pwc/script
                                linkAtts.setValue(i, path_in_link);
                            } else if (path_in_link.toLowerCase().startsWith("/content/pwc/script/")) { // no translation for /content/pwc/script
                                linkAtts.setValue(i, path_in_link);
                            } else if (path_in_link.toLowerCase().startsWith("/content/pwc/userreg/")) { // userReg
                                //String transformedPath = this.defaultDomain + path_in_link.replace("/content/pwc", "");
                                //no link transformation on user reg
                                linkAtts.setValue(i, path_in_link);
                            } else if (path_in_link.toLowerCase().contains("/content/pwc/global/")) { //global
                                //linkAtts.setValue(i, path_in_link.replace("/content/pwc", ""));
                                linkAtts.setValue(i, path_in_link);
                            } else if (path_in_link.startsWith("/content/pwc/") || path_in_link.startsWith("/content/dam/pwc/")) {

                            	//Pattern territoryPattern = Pattern.compile("/content/(?:dam/pwc|pwc)/(\\w{2})/(.*)"); //all the patterns start with /content/pwc/(two alph)/anything is valid
                                Pattern territoryPattern = Pattern.compile("/content/(?:dam/pwc|pwc)/(?:\\d{2}|(\\w{2})/(.*))"); //all the patterns start with /content/pwc/(two alph)/anything is valid
                                Matcher territoryMatch = territoryPattern.matcher(path_in_link);
                                if (territoryMatch.find()) {
                                    try {
                                        if (path_in_link != null && path_in_link.trim().length() > 0) {
                                            LinkTransformerService service = new LinkTransformerServiceImpl(repository, this.defaultDomain, domainType);
                                            String transformedPath = service.transformAEMUrl(path_in_link, this.requestedResourcePath);
                                            //log.info("transformed url " + transformedPath);
                                            linkAtts.setValue(i, transformedPath);
                                        }
                                    } catch (Exception ex) {
                                        //log.info("LinkTransformer.startElement error " + ex);
                                    }
                                } else {
                                    linkAtts.setValue(i, path_in_link.replace("/content/pwc", ""));
                                }

                            } else if(path_in_link.startsWith("/content/pwc365/") || path_in_link.startsWith("/content/dam/pwc365/")){
                                String pwc365TransformedPath = pwc365Domain.replaceFirst("/$", "") + path_in_link;
                                linkAtts.setValue(i, pwc365TransformedPath);
                            } else {
                                linkAtts.setValue(i, path_in_link);
                            }
                        }
                    }
                }
            }
        }
        /* return updated attributes to super and continue with the transformer chain */
        super.startElement(nsUri, localname, qname, linkAtts);
    }
}