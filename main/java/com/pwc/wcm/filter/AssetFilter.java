package com.pwc.wcm.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.EngineConstants;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.user.services.UserInformationService;
import com.pwc.wcm.model.PageProperty;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.TerritoryPrivacyPolicy;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;
import com.pwc.wcm.utils.FileName;
import com.pwc.wcm.utils.PathUtil;
import com.pwc.wcm.utils.UrlSecurity;

/**
 * Created by rjiang on 2016-02-22.
 */
@Component(immediate = true, service = Filter.class,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_FORWARD,
                EngineConstants.SLING_FILTER_PATTERN + "=" + "/content/dam/pwc/\\w{2}/\\w{2}/(.*)",
                Constants.SERVICE_RANKING + "= -703"
        })
public class AssetFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AssetFilter.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private ConfigurationAdmin configAdmin;
    private final String premiumFlagProperty = "premiumFlag";

    @Reference
    SlingSettingsService slingSettingsService;

    @Reference
    AdminResourceResolver resourceResolver;

    @Reference
    TerritoryPrivacyPolicy territoryPrivacyPolicy;

    @Reference
    UserInformationService userInformationService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String requestedFrom = ((HttpServletRequest) request).getRequestURL().toString();
        SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;
        String resourcePath = slingHttpServletRequest.getResource().getPath();
        Set<String> runModes = slingSettingsService.getRunModes();
        boolean isAuthorMode = false;
        boolean triggerLogin = false;
        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;

        //isAuthorMode = runModes.contains("author");

        if (resourcePath.contains("/content/dam/pwc") && !requestedFrom.contains(".json") && !isAuthorMode) {
            ResourceResolver adminResourceResolver = null;
            try {
                //ResourceResolver adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                adminResourceResolver = resourceResolver.getAdminResourceResolver();
                Session session = adminResourceResolver.adaptTo(Session.class);

                Configuration defaultDomainConf = configAdmin.getConfiguration("PwC Default Domain");
                String domain = (String) defaultDomainConf.getProperties().get("domain");
                String domainType = (String) defaultDomainConf.getProperties().get("domainType");

                Configuration enableLinkTransformerConf = configAdmin.getConfiguration("com.pwc.wcm.transformer.LinkTransformerFactory");
                Boolean enableLinkTransformer = (Boolean) enableLinkTransformerConf.getProperties().get("linktransformer.enabled");

                Configuration protectedContentConf = configAdmin.getConfiguration("PwC Protected Content");
                String[] fileExtensionsArray = (String[]) protectedContentConf.getProperties().get("fileExtensions");
                ArrayList<String> fileExtensionsList = new ArrayList<String>(Arrays.asList(fileExtensionsArray));
                FileName assetFile = new FileName(resourcePath, '/', '.');
                String fileExtension = assetFile.extension();
                Resource resource = slingRequest.getResource();
                //Added X-Robots-Tag in the response header of assets
                if (resourcePath.contains(JcrConstants.JCR_CONTENT)) {
                    String valuePath = resourcePath.substring(0, resourcePath.indexOf(JcrConstants.JCR_CONTENT));
                    Resource valRes = adminResourceResolver.getResource(valuePath + "/jcr:content/metadata");
                    if(null != valRes)
                    slingResponse.addHeader("X-Robots-Tag", valRes.getValueMap().get("metaRootHeader", null));
                }
                for (String s : fileExtensionsList) {
                    if (fileExtension.equalsIgnoreCase(s)) {
                        triggerLogin = true;
                        break;
                    }
                }
                triggerLogin=true;

                if (triggerLogin) {
                    String premiumFlag = "off";
                    try {

                        log.info("--------------- " + resource.getPath());
                        if (resourcePath.contains(JcrConstants.JCR_CONTENT)) {
                            String resPath = resourcePath.substring(0, resource.getPath().indexOf(JcrConstants.JCR_CONTENT));
                            Resource refResource = adminResourceResolver.getResource(resPath + "/jcr:content/metadata");
                            ValueMap valueMap = refResource.getValueMap();
                            premiumFlag = valueMap.get("premiumFlag", "off");
                        }
                    } catch (Exception cException) {

                    }
                    Boolean loginToken = false;
                    Cookie pwcIdCookie = null;
                    if (premiumFlag.equalsIgnoreCase("on")) {
                        Cookie[] cookies = req.getCookies();
                        if(cookies !=null) {
                            for (int i = 0; i < cookies.length; i++) {
                                Cookie eachCookie = cookies[i];
                                if (eachCookie.getName().equalsIgnoreCase("pwc-id")) {
                                    loginToken = true;
                                    pwcIdCookie = eachCookie;
                                    break;
                                }
                            }
                        }
                        if (loginToken) {
                            slingResponse.addHeader("Dispatcher", "no-cache");
                            slingResponse.addHeader("Edge-control", "no-store");
                            slingResponse.addHeader("X-testing", "true");
                            slingResponse.setHeader("Pragma", "No-cache");
                            slingResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                            slingResponse.setDateHeader("Expires", -1);
                            String email = UrlSecurity.decode(pwcIdCookie.getValue()).toLowerCase();
                            String currentPagePath = resource.getPath();
                            PageProperty pageProperty1 = PathUtil.getPageProperty(currentPagePath);
                            String micrositeName = null;
                            if (pageProperty1.isMicrosite())
                                micrositeName = pageProperty1.getMicrositeName();
                            boolean isMicrosite = pageProperty1.isMicrosite();
                            ResourceResolver adminResourceResolver1 = resourceResolver.getAdminResourceResolver();
                            Session session2 = adminResourceResolver1.adaptTo(Session.class);
                            Authorizable user = null;
                            boolean agreementRedirect = true;
                            UserManager userManager = ((JackrabbitSession) session2).getUserManager();
                            Iterator<Authorizable> users = userManager.findAuthorizables(new org.apache.jackrabbit.api.security.user.Query() { // 6.2
                                @Override
								public <T> void build(org.apache.jackrabbit.api.security.user.QueryBuilder<T> builder) {
                                    try {
                                        builder.setCondition(builder.eq("profile/email", session2.getValueFactory().createValue(email)));
                                    } catch (RepositoryException e) {
                                        log.error("AssetFilter", e);
                                    }
                                }
                            });
                            if(users.hasNext()){
                                user = users.next();
                            }
                            if (user != null) {
                                //Added check to prevent T&C redirection for internal users
                                final boolean isInternalUser = userInformationService.isInternalUser(user, adminResourceResolver);
                                if (!isInternalUser) {
                                    Node user_node = session2.getNode(user.getPath());
                                    String user_territory = user_node.getPath() + "/preferences/dpe_privacypolicy/" + pageProperty1.getTerritory();
                                    Resource user_territory_res = adminResourceResolver1.getResource(user_territory);
                                    if (user_territory_res != null) {
                                        ValueMap user_map = user_territory_res.adaptTo(ValueMap.class);
                                        String user_version = user_map.get("version-accepted", null);
                                        String version = territoryPrivacyPolicy.getTerritoryPolicyVersion(pageProperty1.getTerritory(), isMicrosite, micrositeName);
                                        agreementRedirect = !user_version.equalsIgnoreCase(version);
                                    }
                                    if (agreementRedirect) {
                                        if (pageProperty1 != null) {
                                            String path = "/content/pwc/" + pageProperty1.getTerritory() + "/" + pageProperty1.getLocale();
                                            try {
                                                String redirectDomain = "";
                                                String url = resourcePath.replaceAll("/jcr:content/.*", "");
                                                try {
                                                    if (!domain.contains("localhost")) {
                                                        LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(session, domain, domainType);
                                                        url = linkTransformerService.transformAEMUrl(url);
                                                        URI redirectURI = new URI(url);
                                                        if (redirectURI != null && redirectURI.getScheme() != null && redirectURI.getHost() != null)
                                                            redirectDomain = redirectURI.getScheme() + "://" + redirectURI.getHost();
                                                        if (pageProperty1.getTerritory().equalsIgnoreCase("uk"))
                                                            pageProperty1.setTerritory("gb");
                                                    }
                                                    String refer = req.getHeader("Referer");
                                                    if (refer != null && !refer.contains("/content/userReg/"))
                                                        refer = req.getHeader("Referer");
                                                    else
                                                        refer = req.getParameter("referrer");
                                                    if (refer == null) refer = UrlSecurity.encode("/");
                                                    String policy_path = redirectDomain + "/content/pwc/userReg/privacy-policy." + pageProperty1.getLocale() + "_" + pageProperty1.getTerritory() + ".html?redirectUrl=" + UrlSecurity.encode(url) + "&parentPagePath=/content/pwc/" + pageProperty1.getTerritory().replace("gb", "uk") + "/" + pageProperty1.getLocale() + "&referrer=" + refer;
                                                    res.sendRedirect(policy_path);

                                                } catch (java.net.URISyntaxException ex) {

                                                }

                                            } catch (Exception ex) {
                                                log.error("AssetFilter", ex);
                                            }
                                        }
                                    } else {
                                        log.info("--------------- " + resource.getPath());
                                        String resPath = resourcePath.substring(0, resource.getPath().indexOf("/jcr:content"));
                                        String fileName = resPath.substring(resPath.lastIndexOf("/"), resPath.length());

                                        res.setHeader("Content-Disposition", "inline; filename=" + fileName);
                                    }
                                }
                            }

                        }else{
                            //go with the full link with user login, such as http://www.pwc.uk/content/pwc/userReg/login.en_uk.html?redirectUrl=http://www.pwc.uk/report-2016.pdf
                            //no microsite asset is handled so far, not sure about the asset file structure of microsite.
                            String assetRegex = "/content/dam/pwc/(\\w{2})/(\\w{2})/(.*)";
                            Pattern assetPattern = Pattern.compile(assetRegex);
                            Matcher assetMatcher = assetPattern.matcher(resourcePath);
                            String forwardDomainProp = (domainType != null && domainType.trim().length() > 0) ? "forward-domain-" + domainType : "forward-domain";
                            if (assetMatcher.find()) {
                                String territory = assetMatcher.group(1);
                                String locale = assetMatcher.group(2);
                                String refDataPath = "/content/pwc/global/referencedata/territories/" + territory;
                                Resource refResource = adminResourceResolver.getResource(refDataPath);
                                ValueMap refValueMap = refResource.getValueMap();
                                Boolean enableUserReg = false;
                                if (refValueMap.containsKey("enableUserReg")) {
                                    enableUserReg = (Boolean) refValueMap.get("enableUserReg");
                                }
                                String selector = locale + "_" + territory;
                                if (refValueMap.containsKey("default-locale")) {
                                    selector = (String) refValueMap.get("default-locale");
                                }

                                String forwardDomain = refValueMap.get(forwardDomainProp, domain);
                                //String selector = locale + "_" + territory;
                                if (enableUserReg) {
                                    if (enableLinkTransformer) {
                                        LinkTransformerService linkTransformerService = new LinkTransformerServiceImpl(session, domain, domainType);
                                        String url = linkTransformerService.transformAEMUrl(resourcePath);
                                        url = url.substring(0,url.indexOf("/jcr:content"));
                                        URL uri = new URL(url);
                                        String refer = req.getHeader("Referer");
                                        if(refer!=null&&!refer.contains("/content/userReg/"))
                                            refer = req.getHeader("Referer");
                                        else
                                            refer = url;
                                        res.sendRedirect(uri.getProtocol() + "://" + uri.getHost() + "/content/pwc/userReg/login." + selector.replace("en_uk","en_gb") + ".html?redirectUrl=" + UrlSecurity.encode(url)+ "&parentPagePath=/content/pwc/" + territory + "/" + locale + "&referrer=" + UrlSecurity.encode(refer));
                                    } else {

                                        String redirectPath = resourcePath.substring(0,resourcePath.indexOf(("/jcr:content")));
                                        String refer = req.getHeader("Referer");
                                        if(refer!=null&&!refer.contains("/content/userReg/"))
                                            refer = req.getHeader("Referer");
                                        else
                                            refer = redirectPath;
                                        res.sendRedirect("/content/pwc/userReg/login." + selector.replace("en_uk","en_gb") + ".html?redirectUrl=" + UrlSecurity.encode(redirectPath) + "&parentPagePath=/content/pwc/" + territory + "/" + locale + "&referrer=" + UrlSecurity.encode(refer));
                                    }
                                }
                            }
                        }
                    }
                    else{

                    }
                }
            } catch (Exception e) {
                String error = "";
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                for (int i = 0; i < elements.length; i++) {
                    error += elements[i].getLineNumber() + " " + elements[i].getMethodName() + "\n";
                }
                log.error("com.pwc.wcm.filter.AssetFilter", e);
            }finally {
                if(adminResourceResolver!=null)
                    adminResourceResolver.close();
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}