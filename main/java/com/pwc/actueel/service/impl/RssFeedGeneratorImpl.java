package com.pwc.actueel.service.impl;

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.result.Hit;
import com.pwc.AdminResourceResolver;
import com.pwc.actueel.service.RssFeedGenerator;
import com.pwc.actueel.utility.SearchUtils;
import com.pwc.actueel.xml.model.Article;
import com.pwc.actueel.xml.model.Articles;
import com.pwc.actueel.xml.model.Channel;
import com.pwc.actueel.xml.model.Error;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Generates an RSS Feed of all NL pages under the configured parent paths of provided languages.
 */
@Component(service = RssFeedGenerator.class, immediate = true, property = {
	Constants.SERVICE_DESCRIPTION + "= Generates an RSS Feed for all pages under configured parent paths of provided languages"	
})
@Designate(ocd = RssFeedGeneratorImpl.Config.class)
public class RssFeedGeneratorImpl implements RssFeedGenerator {
    
    private static final String DEFAULT_DOMAIN = "https://www.pwc.nl";
    private static final String GLOBAL_PAGE_PARENT_PATH = "/content/pwc/nl";
    private static final String GLOBAL_IMAGE_PARENT_PATH = "/content/dam/pwc/nl";
    private static final String PREFIX_ERROR_LOG = "RSS Feed Generator Error: ";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";
    private static final String NO_CONTENT_ERROR_MESSAGE = "No content found";
    private static final String NO_PATHS_CONFIGURED_MESSAGE = "No paths configured for the requestedd language codes.";
    private static final String XML_ENCODING_SCHEME = "utf-8";
    private static final String REGEX_STARTS_WITH_FORWARD_SLASH = "^(?!/)(.*)";
    private static final String REF_DATA_TERRRITORY_PATH_FOR_NL = "/content/pwc/global/referencedata/territories/nl";
    private static final String PWC_DEFAULT_DOMAIN_PID = "PwC Default Domain";
    private static final String DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME = "forward-domain";
    private static final String COLLECTIONS_COMPONENT_PROPERTY_NAME = "sling:resourceType";
    private static final String COLLECTIONS_COMPONENT_PROPERTY_VALUE = "pwc/components/content/collection";
    private static final String PAGE_EXTENSION = ".html";
    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";
    
    @ObjectClassDefinition(name = "PwC Actueel RssFeed Generator", description = "Generates an RSS Feed for all pages under configured parent paths of provided languages")
    @interface Config {
    	@AttributeDefinition(name = "Parent Paths", 
    						description = "Pages under these parent paths will be added to RSS Feeds. "
    			                      + "Enter only the part of the path which starts with the language code, "
    			                      + "'/content/pwc/nl/' gets prepended to these parentPathList. "
    			                      + "Example: For the parent path '/content/pwc/nl/nl/actueel-en-publicaties', enter '/nl/actueel-en-publicaties' as path.",
    						type = AttributeType.STRING,
    						cardinality = 50)
    	public String[] parentPaths();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RssFeedGeneratorImpl.class);
    private String[] parentPaths = null;
    private List<String> parentPathList = null;
    private String domainType = "";
    private String domainPath;
    private ResourceResolver adminResolver = null;
    
    @Reference
    ResourceResolverFactory resolverFactory;
    @Reference
    private AdminResourceResolver adminResourceResolver;
    @Reference
    private ConfigurationAdmin configurationAdmin;
    @Reference
    SlingRepository repository;

    @Activate
    protected void activate(final RssFeedGeneratorImpl.Config context) {
        parentPaths = context.parentPaths();
        parentPathList = Arrays.asList(parentPaths);
        adminResolver = adminResourceResolver.getAdminResourceResolver();
        domainPath = getDomain();
    }
    
    @Override
    public Channel getChannelContainingAllArticlesForTerritory(final ResourceResolver resolver,
            final String[] languageCodes) {
        final LocalTime startTime = LocalTime.now();
        LOGGER.info("Started RSS Feed Processing at : " + startTime);
        if (parentPathList.size() == 0)
            return generateErrorResponse(NO_CONTENT_ERROR_MESSAGE);
        try {
            final List<String> fullParentPathsList = parentPathList.stream().filter(path -> {
                for (final String langCode : languageCodes) {
                    if (path.contains(langCode + "/"))
                        return true;
                }
                return false;
            }).map(path -> {
                // Adds a '/' if it isn't present at the beginning and removes it if it's present in the end.
                path = path.trim().toLowerCase().replaceFirst(REGEX_STARTS_WITH_FORWARD_SLASH, "/$1") //
                        .replaceFirst("/$", "");
                return GLOBAL_PAGE_PARENT_PATH + path;
            }).collect(Collectors.toList());

            if (fullParentPathsList.isEmpty()) {
                LOGGER.error(PREFIX_ERROR_LOG + NO_PATHS_CONFIGURED_MESSAGE);
                return generateErrorResponse(NO_CONTENT_ERROR_MESSAGE);
            }
            return getChannelContainingAllArticlesUnderGivenPaths(resolver, fullParentPathsList);
        } catch (final Exception excep) {
            LOGGER.error(PREFIX_ERROR_LOG + excep.getMessage(), excep);
            return generateErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE);
        } finally {
            final LocalTime endTime = LocalTime.now();
            LOGGER.info("Ending RSS Feed Processing at : " + endTime + ", Difference: "
                    + Duration.between(startTime, endTime));
        }
    }

    @Override
    public void marshalIntoWriter(final Object rootObj, final Writer writer) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(rootObj.getClass());
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, XML_ENCODING_SCHEME);
            jaxbMarshaller.marshal(rootObj, writer);
        } catch (final JAXBException jaxbExp) {
            jaxbExp.printStackTrace();
            LOGGER.error(PREFIX_ERROR_LOG + jaxbExp.getMessage(), jaxbExp.fillInStackTrace());
            marshalIntoWriter(generateErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE), writer);
        }
    }
    
    /**
     * Returns the domain URL present in the Reference Data for NL territory, else the default {@value #DEFAULT_DOMAIN}.
     *
     * @return {@link String} The domain URL found in Reference Data of NL territory, else the default
     *         {@value #DEFAULT_DOMAIN}
     */
    private String getDomain() {
        final Resource territory = adminResolver.getResource(REF_DATA_TERRRITORY_PATH_FOR_NL);
        if (territory != null) {
            final String forwardDomain = ResourceUtil.getValueMap(territory).get(getForwardDomainPropertyname(),
                    String.class);
            return StringUtils.isBlank(forwardDomain) ? DEFAULT_DOMAIN : forwardDomain.trim().replaceFirst("/$", "");
        }
        return DEFAULT_DOMAIN;
    }
    
    /**
     * Returns the name of the property that contains the domain URL for current domainType, else the default value
     * {@value #DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME}.
     *
     * @return {@link String} The domain URL for current domainType, else the default value
     *         {@value #DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME}
     */
    private String getForwardDomainPropertyname() {
        try {
            final Configuration config = configurationAdmin.getConfiguration(PWC_DEFAULT_DOMAIN_PID);
            if (config != null) {
                domainType = (String) config.getProperties().get("domainType");
                return StringUtils.isBlank(domainType) ? DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME
                        : DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME + "-" + domainType;
            }
        } catch (final IOException ioExcep) {
            LOGGER.error(
                    "Configurations can't be read for PID: " + PWC_DEFAULT_DOMAIN_PID + "! Using "
                            + DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME + " as default domain for RSS Article links!",
                    ioExcep);
        }
        return DEFAULT_FORWARD_DOMAIN_PROPERTY_NAME;
    }
    
    /**
     * Returns an Error Object corresponding to an Error XML which wraps the given message.
     *
     * @param errorMsg {@link String} The error message to be displayed in error response XML
     * @return {@link Error} Error object that wraps given error message
     */
    private Error generateErrorResponse(final String errorMsg) {
        return new Error(errorMsg);
    }

    /**
     * Searches for and returns a Channel root object containing all Articles under the given Paths.
     *
     * @param resolver {link ResourceResolver} Resource Resolver to Query the paths
     * @param paths {@link List} Paths under which the Querying is to be performed
     * @return {@link Channel} Object containing all Articles or an Error response object in case an error occurred
     */
    private Channel getChannelContainingAllArticlesUnderGivenPaths(final ResourceResolver resolver,
            final List<String> paths) {
        LOGGER.info("Fetching RSS Feeds under the paths: " + paths);
        final List<Hit> hits = SearchUtils.getPageResults(resolver, paths);
        if (hits.size() == 0) {
            LOGGER.error(PREFIX_ERROR_LOG + NO_CONTENT_ERROR_MESSAGE + " for paths: " + paths);
            return generateErrorResponse(NO_CONTENT_ERROR_MESSAGE);
        }
        
        final List<Article> articles = getArticlesFromSearchResults(hits);
        if (articles.isEmpty()) {
            LOGGER.error(PREFIX_ERROR_LOG + "No valid pages found for paths: " + paths);
            return generateErrorResponse(NO_CONTENT_ERROR_MESSAGE);
        }
        LOGGER.info("Fetching RSS Feeds was successfull. Found {} results.", articles.size());
        final Channel channel = new Articles(articles);
        return channel;
    }

    /**
     * Returns a list of RSS Articles from the provided search results.
     *
     * @param hits {@link List} Search Results a Query
     * @return {@link List} A list of Articles corresponding to the pages found in Search Results
     */
    private List<Article> getArticlesFromSearchResults(final List<Hit> hits) {
        final List<Article> articles = new ArrayList<Article>();
        for (final Hit hit : hits) {
            try {
                final Resource pageRes = hit.getResource();
                if (pageRes == null)
                    throw new NullPointerException("Resource found to be Null at path: " + hit.getPath());
                final Article article = getRssArticle(pageRes);
                if (article != null) {
                    articles.add(article);
                    LOGGER.debug("RSS Feed generated for page at: " + article.getLink());
                }
            } catch (final RepositoryException repoExcep) {
                LOGGER.error(PREFIX_ERROR_LOG + "Skipping RSS feed generation for page result: " + hit, repoExcep);
            } catch (final Exception excep) {
                LOGGER.error(PREFIX_ERROR_LOG + excep.getMessage(), excep);
            }
        }
        return articles;
    }
    
    /**
     * Converts the given Resource into an RSS Article by using ArticleAdaptorFactory if the resource is a Page resource
     * and doesn't contain a collections component.<br />
     *
     * @param pageRes {@link Resource} The resource which is to be transformed to an RSS article
     * @return {@link Article} The transformed Article
     */
    private Article getRssArticle(final Resource pageRes) {
        if (!isCollectionsPage(pageRes)) {
            final Article article = pageRes.adaptTo(Article.class);
            article.setLink(transformPagePathToDomainUrl(article.getLink(), GLOBAL_PAGE_PARENT_PATH));
            article.setImage(transformImagePathToDomainUrl(article.getImage()));
            return article;
        }
        return null;
    }

    /**
     * Transforms the given imagePath to its Domain URL using LinkTransformerService service.
     *
     * @param imagePath {@link String} The image path that needs transformation
     * @return {@link String} The transformed image URL
     */
    private String transformImagePathToDomainUrl(final String imagePath) {
        String transformedImageURL = imagePath;
        if (StringUtils.isNotBlank(imagePath)) {
            try {
                final LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(repository,
                        domainPath, domainType);
                transformedImageURL = linkTransformerService.transformAEMUrl(imagePath, null);
                transformedImageURL = transformedImageURL.replace("http://", "https://");
            } catch (final Exception excep) {
                transformedImageURL = imagePath.replace(GLOBAL_IMAGE_PARENT_PATH, domainPath);
                LOGGER.error(PREFIX_ERROR_LOG + "LinkTransformerService failed to initialize! "
                        + "Static transformation done from " + imagePath + " to " + transformedImageURL, excep);
            }
            transformedImageURL = transformedImageURL.replace(HTTP_PROTOCOL, HTTPS_PROTOCOL);
        }
        return transformedImageURL;
    }
    
    /**
     * Checks if the given page resource is a Collections Page or not, by looking for a Collections component under it.
     *
     * @param pageRes {@link Resource} The page resource that needs to be checked for a collections component
     * @return {@link boolean} Returns true if passed page resource contains a collections component, otherwise false
     */
    private boolean isCollectionsPage(final Resource pageRes) {
        for (final Resource child : pageRes.getChildren()) {
            final String resType = ResourceUtil.getValueMap(child).get(COLLECTIONS_COMPONENT_PROPERTY_NAME,
                    String.class);
            if (resType != null && resType.equals(COLLECTIONS_COMPONENT_PROPERTY_VALUE)
                    || child.hasChildren() && isCollectionsPage(child))
                return true;
        }
        return false;
    }

    /**
     * Replaces the given 'replacingPattern' in the given path with the Domain path.
     *
     * @param path {@link String} The path that needs to be transformed
     * @param replacingPattern {@link String} The replacement pattern which will be replaced with domain
     * @return {@link String} The transformed URL
     */
    private String transformPagePathToDomainUrl(final String path, final String replacingPattern) {
        String transformedPagePath = null;
        if (path != null) {
            transformedPagePath = path.replace(replacingPattern, domainPath);
            transformedPagePath = transformedPagePath.replace(HTTP_PROTOCOL, HTTPS_PROTOCOL);
            transformedPagePath += PAGE_EXTENSION;
        }
        return transformedPagePath;
    }
}
