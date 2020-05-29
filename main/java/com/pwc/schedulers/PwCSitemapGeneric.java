package com.pwc.schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.AdminResourceResolver;
import com.pwc.ApplicationConstants;
import com.pwc.impl.filters.PwCPageFilter;
import com.pwc.wcm.services.LinkTransformerServiceFactory;
import com.pwc.wcm.services.ListAllParentPages;
import com.pwc.wcm.utils.CommonUtils;

/**
 * Created by intelligrape on 18/5/15.
 */
@Component(immediate = true, service = { PwCSitemapGeneric.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Produce the SiteMap " })
@Designate(ocd = PwCSitemapGeneric.Config.class)
public class PwCSitemapGeneric {

    @Reference
    private Scheduler scheduler;

    @Reference
    ListAllParentPages getParentPages;

    @Reference
    private Replicator replicator;

    @Reference
    private AdminResourceResolver adminResourceResolver;

    @Reference
    private LinkTransformerServiceFactory linkTransformerServiceFactory;

    @Reference
    SlingRepository repository;
       
    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";
    private static final String GENERIC_SITE_MAP_CRON_EXPRESSION = "genericSitemapCronExpression";
    private static final String SITE_MAP_GENERIC_SCHEDULER = "SiteMapGenericScheduler";
    public static final String GENERIC_SITE_MAP_FILE = "sitemap.xml";
    private static final String TEMPLATES_PATH = "/apps/pwc/templates";
    private String rootPath;
    private String sitemapDomain;
    private static Logger logger = LoggerFactory
            .getLogger(PwCSitemapGeneric.class);
    private PageManager pageManager = null;
    private Page page = null;
    private boolean isValidRoot;
    private HashMap<String,String> templatesTitle;

    SimpleDateFormat dateFormat = null;
    
    @ObjectClassDefinition(name = "PwC Generic Sitemap", description = "Produce the SiteMap")
    @interface Config {
        @AttributeDefinition(name = "Generic Sitemap Cron Expression", 
                            description = "Cron expression to generate sitemap.",
                            type = AttributeType.STRING)
        public String genericSitemapCronExpression() default "0 0 0/24 1/1 * ? *";
    }
    
    @Activate
    protected void activate(Config properties) {

        // Dictionary<?, ?> properties = componentContext.getProperties();
        final String schedulingExpression = properties.genericSitemapCronExpression().replace("\\", "");
        final String jobName = SITE_MAP_GENERIC_SCHEDULER;
        dateFormat = new SimpleDateFormat(ApplicationConstants.SITE_MAP_DATE_FORMAT);
        Map<String, Serializable> config1 = new HashMap<String, Serializable>();
        templatesTitle = new HashMap<>();
        boolean canRunConcurrently = true;
        final Runnable SiteMapGenericSchedulerThread = new Runnable() {
            @Override
			public void run() {
                ResourceResolver resourceResolver = null;
                try {
                    resourceResolver = adminResourceResolver.getAdminResourceResolver();
                    final PwCPageFilter pwCPageFilter = new PwCPageFilter();
                    if (resourceResolver != null) {
                        Resource templates = resourceResolver.getResource(TEMPLATES_PATH);
                        for (Iterator<Resource> it = templates.listChildren(); it.hasNext(); ) {
                            Resource template = it.next();
                            String templateName =  template.adaptTo(ValueMap.class).get("jcr:title",String.class);
                            templateName = templateName == null ? StringUtils.substringAfterLast(templateName,"/") : templateName.toString();
                            templatesTitle.put(template.getPath().toString(),templateName);
                        }
                        pageManager = resourceResolver.adaptTo(PageManager.class);
                        Set<String> parentPagePathList = getParentPages
                                .getPathOfAllParentPage();
                        for (String path : parentPagePathList) {
                            rootPath = path;
                            page = pageManager.getPage(path);
                            if (page != null) {
                                sitemapDomain = page.getProperties().get(ApplicationConstants.SITE_MAP_DOMAIN_PROPERTY, "");
                                isValidRoot = pwCPageFilter.includes(page);
                            }
                            if ("true".equals(sitemapDomain) && isValidRoot) {
                                XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
                                try {
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    Session session = resourceResolver.adaptTo(Session.class);
                                    XMLStreamWriter stream = outputFactory.createXMLStreamWriter(bos, "UTF-8");
                                    stream.writeStartDocument("1.0");
                                    stream.writeStartElement("", "urlset", NS);
                                    stream.writeNamespace("", NS);
                                    boolean isMicroSiteRoot = CommonUtils.isMicroSite(page.getPath());
                                    write(page, stream, isMicroSiteRoot);
                                    for (Iterator<Page> children = page.listChildren(new PwCPageFilter(), true); children.hasNext();) {
                                        write(children.next(), stream, isMicroSiteRoot);
                                    }
                                    stream.writeEndElement();
                                    stream.writeEndDocument();
                                    ValueFactory vf = session.getValueFactory();
                                    Binary binary = vf
                                            .createBinary(new ByteArrayInputStream(
                                                    bos.toByteArray()));
                                    Node rootNode = resourceResolver.resolve(
                                            rootPath).adaptTo(Node.class);
                                    if (!rootNode.hasNode(GENERIC_SITE_MAP_FILE)) {
                                        Node sitemapNode = rootNode.addNode(
                                                GENERIC_SITE_MAP_FILE, JcrConstants.NT_FILE);
                                        Node resNode = sitemapNode.addNode(
                                                JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                                        resNode.setProperty(JcrConstants.JCR_DATA, binary);
                                    } else {
                                        Node resNode = rootNode.getNode(GENERIC_SITE_MAP_FILE).getNode(JcrConstants.JCR_CONTENT);
                                        resNode.setProperty(JcrConstants.JCR_DATA, binary);
                                    }
                                    session.save();
                                    activatePage(session, rootPath + "/" + GENERIC_SITE_MAP_FILE);
                                } catch (XMLStreamException XMLStreamException) {
                                    logger.error("PwCSitemapGeneric.siteMapGenericSchedulerThread: XMLStreamException occurred while creating generic sitemap.xml : {} for rootpath {}", XMLStreamException, path);
                                } catch (Exception exception) {
                                    logger.error("PwCSitemapGeneric.siteMapGenericSchedulerThread: Exception occurred while creating generic sitemap.xml : {} for rootpath {}", exception, path);
                                }
                            }
                        }
                    } else {
                        logger.debug("PwCSitemapGeneric.siteMapGenericSchedulerThread: AdminResourceResolver is null");
                    }
                } catch (Exception exception) {
                    logger.error("PwCSitemapGeneric.siteMapGenericSchedulerThread: Exception occurred while creating generic sitemap.xml : {} for rootpath {}", exception, rootPath);
                } finally {
                    if (resourceResolver != null) {
                        resourceResolver.close();
                    }
                }
            }
        };
        try {
            CommonUtils.scheduleSiteMapGeneration(this.scheduler, schedulingExpression, canRunConcurrently, jobName, config1, SiteMapGenericSchedulerThread);
        } catch (Exception e) {
            SiteMapGenericSchedulerThread.run();
        }
    }

    private void write(final Page page, final XMLStreamWriter stream, final boolean isMicroSiteRoot) throws XMLStreamException {
            ValueMap valueMap = page.getProperties();
            final String canonicalOverride = valueMap.get(ApplicationConstants.CANONICAL_OVERRIDE, "");
            final String redirectTarget = valueMap.get(ApplicationConstants.REDIRECT_TARGET, "");
            final String pagePath = page.getPath();
            final Calendar lastModified = page.getLastModified();
            final String templateType = templatesTitle.get(valueMap.get("cq:template"));
            if (StringUtils.isEmpty(canonicalOverride) && StringUtils.isEmpty(redirectTarget) && (isMicroSiteRoot ?
                    CommonUtils.isMicroSite(pagePath) :
                    !CommonUtils.isMicroSite(pagePath)))
                    writeLOC(stream, CommonUtils.getTransformedUrl(pagePath + ".html", linkTransformerServiceFactory, repository)
                                                .replaceAll("http://", "https://"), lastModified,templateType);
            else if (StringUtils.isNotEmpty(canonicalOverride))
                    writeLOC(stream, canonicalOverride, lastModified,templateType);
    }

    private void writeLOC(final XMLStreamWriter streamWriter, final String loc, final Calendar lastModified,String templateType) throws XMLStreamException {
            if (loc != null) {
                    streamWriter.writeStartElement(NS, "url");
                    streamWriter.writeStartElement(NS, "loc");
                    streamWriter.writeCharacters(loc);
                    streamWriter.writeEndElement();
                    Calendar lastModifiedCalender = lastModified;
                    String lastModifiedDate = dateFormat.format(lastModifiedCalender.getTime());
                    streamWriter.writeStartElement(NS, "lastmod");
                    streamWriter.writeCharacters(lastModifiedDate);
                    streamWriter.writeEndElement();
                    streamWriter.writeStartElement(NS,"template-type");
                    streamWriter.writeCharacters(templateType);
                    streamWriter.writeEndElement();
                    streamWriter.writeEndElement();
            }
    }

    private void activatePage(Session session, String pathToPage) {
        try {
            replicator.replicate(session, ReplicationActionType.ACTIVATE,
                    pathToPage);
        } catch (ReplicationException e) {
            logger.error("Error in replication", e);
        }
    }
}
