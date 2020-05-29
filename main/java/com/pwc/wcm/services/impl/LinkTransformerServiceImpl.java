package com.pwc.wcm.services.impl;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.wcm.model.AEMLink;
import com.pwc.wcm.model.Microsite;
import com.pwc.wcm.model.Territory;
import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.utils.CommonUtils;

@Component(immediate = true, service = { LinkTransformerService.class }, enabled = true)
public class LinkTransformerServiceImpl implements LinkTransformerService {
	
    private static final Logger log = LoggerFactory.getLogger(LinkTransformerServiceImpl.class);
    private SlingRepository repository;
    private Session session;
    private Session adminSession;
    private ArrayList<Territory> territories;
    private AEMLink aemLink;
    private String DEFAULT_DOMAIN = "https://www.pwc.com";

    private String domainType = "";
    private String forwardDomain;
    private String micrositeHomePagePattern = "^/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})/website/(([^/]*.html$)|([^/]+)($|/$))";
    private String micrositePagePattern = "^/content/(?:dam/pwc|pwc)/((\\w{2})/(\\w{2})/website/([^/]+))((/.*)|(.*))";
    private String vanityHomePagePattern = "^/content/pwc/(\\w{2})($|/.*)";
    private String vanityRootPagePattern = "^/content/pwc/((\\w{2})($|/$))";
    private String vanityMicrositeRootPagePattern = "^/content/pwc/(\\w{2})/\\w{2}/website/([^/]+)($|/$)";

    public LinkTransformerServiceImpl() {

    }

    public LinkTransformerServiceImpl(SlingRepository repository, String defaultDomain, String domainType) throws Exception {
        territories = new ArrayList<Territory>();
        this.repository = repository;

        this.session = repository.loginService("adminResourceResolver", null);
        this.DEFAULT_DOMAIN = defaultDomain;
        this.domainType = domainType;
        if (domainType != null && domainType.trim().length() > 0) {
            if (domainType.equalsIgnoreCase("staging") || domainType.equalsIgnoreCase("qa"))
                forwardDomain = "forward-domain" + "-" + domainType;
        } else
            forwardDomain = "forward-domain";
        log.info("forward-domain" + forwardDomain);

    }

    //You should NOT use this constructor if you are calling from JSP page, this is only used for testing cases or workflow!!!!!!
    //this may cause session closed on pages which you don't want!!!
    public LinkTransformerServiceImpl(Session session, String defaultDomain, String domainType) throws Exception {
        this.session = session;
        this.DEFAULT_DOMAIN = defaultDomain;
        this.domainType = domainType;
        if (domainType != null && domainType.trim().length() > 0) {
            if (domainType.equalsIgnoreCase("staging") || domainType.equalsIgnoreCase("qa")) {
                forwardDomain = "forward-domain" + "-" + domainType;
            }
        } else
            forwardDomain = "forward-domain";
        log.info("forward-domain" + forwardDomain);
    }


    @Override
	public String transformAEMUrl(String path, String requestedUrl) {

    	if(CommonUtils.isStrategyAndURL(path)) {
    		 
    		return transformStrategyAndURL(path,requestedUrl);
    		
    	}
        String transformedLink = "";

        try {


            if (path.toLowerCase().contains("/content/pwc/script")) {
                transformedLink = path;
            } else if (path.toLowerCase().contains("/content/pwc/userreg/")) {
                //transformedLink = DEFAULT_DOMAIN + path.replace("/content/pwc", "");//no link transformation on user reg
                transformedLink = DEFAULT_DOMAIN + path;
            } else if (path.toLowerCase().contains("/content/pwc/global/")) {
                transformedLink = path;
            } else {
                Pattern h0 = Pattern.compile("^/content/pwc/((\\w{2})(/$|$|.html$))"); // to handle /content/pwc/nl or /content/pwc/nl/ or /content/pwc/nl.html
                Matcher m0 = h0.matcher(path);
                if (m0.find()) {

                    String homePageTerritory = m0.group(2);
                    String homePageContent = m0.group(1);
                    Territory homePageTerritoryObj = findTerritoryByName(m0.group(2));
                    if (homePageTerritoryObj.isTLD())//if home page site is a TLD site like http://www.pwc.co.uk or http://www.pwc.de
                        transformedLink = homePageTerritoryObj.getForwardDomain();
                    else
                        transformedLink = findTerritoryByName(m0.group(2)).getForwardDomain() + "/" + m0.group(1);


                } else {
                    Pattern homePagePattern = Pattern.compile("^/content/pwc/(\\w{2})(/(\\w{2})(.html$|$))");
                    Matcher homePageMatcher = homePagePattern.matcher(path);

                    boolean isHomePage = false;
                    if (homePageMatcher.find()) {

                        aemLink = new AEMLink();
                        aemLink.setTerritory(homePageMatcher.group(1));
                        aemLink.setLocale(homePageMatcher.group(3));
                        aemLink.setContent(homePageMatcher.group(2));
                        aemLink.setIsHomePage(true);
                        isHomePage = true;
                    } else {
                        Pattern r = Pattern.compile("^/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})(/.*)");
                        Matcher m = r.matcher(path);
                        if (m.find()) {
                            aemLink = new AEMLink();
                            aemLink.setTerritory(m.group(1));
                            aemLink.setLocale(m.group(2));
                            aemLink.setContent(m.group(3));
                            aemLink.setIsHomePage(false);
                        }
                    }
                    boolean isMicrosite = isMicroSite(path);
                    if (isMicrosite) {
                        Pattern mh = Pattern.compile(micrositeHomePagePattern);
                        Matcher mm = mh.matcher(path);
                        if (mm.find())
                            isHomePage = true;
                    }

                    if (requestedUrl != null && requestedUrl.length() > 0) {
                        Pattern r1 = Pattern.compile("([^/]+(?:/cf#|/editor.html|))*/content/(?:dam/pwc|pwc)/(\\w{2})/(.*)");
                        Matcher m1 = r1.matcher(requestedUrl);
                        String requestedLink_territory = "";
                        if (m1.find()) {
                            requestedLink_territory = m1.group(2);
                        }
                        boolean requestedUrlisMicrosite = requestUrlisMicrosite(requestedUrl);

                        if (aemLink != null) {
                            Territory territory = findTerritoryByName(aemLink.getTerritory()); //link that needs to be tranformed
                            Territory requestedUrl_Territory = findTerritoryByName(requestedLink_territory);

                            if (!isMicrosite) {
                                if (territory.getForwardDomain().equalsIgnoreCase(DEFAULT_DOMAIN)) { //if the link is http://www.pwc.com
                                    if (territory.isMultiLanguage()) { // for example http://www.pwc.com/us/en/aboutus.html  under http://www.pwc.com

                                        if (requestedLink_territory.equalsIgnoreCase(aemLink.getTerritory())) { //same domain
                                            if (requestedUrlisMicrosite)
                                                transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                                            else
                                                transformedLink = aemLink.getFullLink();
                                        } else if (requestedUrl_Territory.getForwardDomain().equals((DEFAULT_DOMAIN))) {
                                            transformedLink = aemLink.getFullLink();
                                        } else {
                                            transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                                        }
                                    } else //if ame link is a single language
                                    {
                                        if (requestedLink_territory.equalsIgnoreCase(aemLink.getTerritory())) {
                                            if (requestedUrlisMicrosite)
                                                transformedLink = territory.getForwardDomain() + aemLink.getLinkwithTerritory();
                                            else
                                                transformedLink = aemLink.getFullLink();
                                        } else if (requestedUrl_Territory.getForwardDomain().equals((DEFAULT_DOMAIN)))
                                            transformedLink = aemLink.getFullLink();
                                        else {
                                            transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                                        }
                                    }
                                } else { //for non-pwc.com domains such as uk single language or de multi languge
                                    if (territory.isMultiLanguage()) { //if this is pwc.de/de or pwc.de/en

                                        if (requestedLink_territory.equalsIgnoreCase(aemLink.getTerritory())) {
                                            if (requestedUrlisMicrosite)
                                                transformedLink = territory.getForwardDomain() + aemLink.getLinkWithoutTerritory();
                                            else {
                                                if (isHomePage)
                                                    transformedLink = aemLink.getContent();
                                                else
                                                    transformedLink = "/" + aemLink.getLocale() + aemLink.getContent();//for example, http://www.pwc.be so under this page, the link should be /en/service.html
                                            }

                                        } else {
                                            if (isHomePage) // www.pwc.de/de.html or www.pwc.de/en.html
                                                transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                            else
                                                transformedLink = territory.getForwardDomain() + aemLink.getLinkWithoutTerritory();
                                        }
                                    } else { //if the ame link is a NON-pwc.com and it is a single language
                                        if (requestedLink_territory.equalsIgnoreCase(aemLink.getTerritory())) {
                                            if (requestedUrlisMicrosite)
                                                transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                            else {
                                                if (territory.isShowLangugageInUrl()) {
                                                    if (isHomePage)
                                                        transformedLink = aemLink.getContent();
                                                    else
                                                        transformedLink = aemLink.getLinkWithoutTerritory();
                                                } else {
                                                    Pattern sp = Pattern.compile("/\\w{2}(.html|$)");
                                                    Matcher sm = sp.matcher(aemLink.getContent());
                                                    if (sm.find())
                                                        transformedLink = "/";//for for uk homepage link on a uk page, use /
                                                    else
                                                        transformedLink = aemLink.getContent();
                                                }

                                            }
                                        } else {
                                            if (territory.isShowLangugageInUrl()) {
                                                if (isHomePage)
                                                    transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                                else
                                                    transformedLink = territory.getForwardDomain() + aemLink.getLinkWithoutTerritory();

                                            } else {
                                                Pattern sp = Pattern.compile("/\\w{2}.html");
                                                Matcher sm = sp.matcher(aemLink.getContent());
                                                if (sm.find())
                                                    transformedLink = territory.getForwardDomain();
                                                else
                                                    transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                            }
                                        }
                                    }
                                }

                            } else {
                                Microsite microsite = getMicroSite(path);
                                Pattern microsite_pattern = Pattern.compile("/content/pwc/((\\w{2})/(\\w{2})/website/([^/]+))((/.*)|(.*))");
                                Matcher microsite_match = microsite_pattern.matcher(requestedUrl);
                                if (microsite_match.find()) {
                                    String microsite_territory = microsite_match.group(2);
                                    String microsite_locale = microsite_match.group(3);
                                    if (microsite_territory.equalsIgnoreCase(microsite.getTerritory()) && microsite_locale.equalsIgnoreCase(microsite.getLocale())) {
                                        if (microsite.isMultiLanguage()) {
                                            if (isHomePage)
                                                transformedLink = "/" + microsite.getLocale() + "/";
                                            else
                                                transformedLink = microsite.getPath();

                                        } else {
                                            if (isHomePage)
                                                transformedLink = "/";
                                            else
                                                transformedLink = microsite.getPath();
                                        }


                                    } else
                                        transformedLink = microsite.getForwardDomain() + microsite.getPath();
                                } else {
                                    transformedLink = microsite.getForwardDomain() + microsite.getPath();
                                }

                            }
                        } else {
                            transformedLink = path;
                        }
                    } else {//if no requestUrl supplied, it will be translated into external url
                        Territory territory = findTerritoryByName(aemLink.getTerritory());

                        if (!isMicrosite) {

                            if (aemLink.getTerritory().equalsIgnoreCase("gx")) {
                                transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                            } else {
                                if (territory.getForwardDomain().equalsIgnoreCase(DEFAULT_DOMAIN)) {
                                    if (territory.isMultiLanguage())
                                        transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                                    else
                                        transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                                } else { //if it is a NON-pwc.com site
                                    if (territory.isMultiLanguage()) { //multiple lanuage site like http://www.pwc.de
                                        if (isHomePage)
                                            transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                        else
                                            transformedLink = territory.getForwardDomain() + aemLink.getLinkWithoutTerritory();
                                    } //isMultipleLanguage
                                    else { //single language site like http://www.pwc.co.uk
                                        if (territory.isShowLangugageInUrl()) //if a non-pwc.com single language site, and it wants to show the language in the url, for example, http://www.pwc.ba/en
                                            transformedLink = territory.getForwardDomain() + aemLink.getLinkWithoutTerritory();
                                        else {
                                            if (isHomePage)
                                                transformedLink = territory.getForwardDomain();
                                            else
                                                transformedLink = territory.getForwardDomain() + aemLink.getContent();
                                        }
                                    }
                                }
                            }
                        } else {

                            Microsite microsite = getMicroSite(path);
                            transformedLink = microsite.getForwardDomain() + microsite.getPath();
                        }
                    }
                }
            }

        } catch (Exception ex) {
            transformedLink = DEFAULT_DOMAIN + path;
        } finally {
            if (session != null)
                session.logout();
        }
        return transformedLink;
    }

    @Override
    public String transformVanity(String vanityUrl, String currentPage) throws Exception {
        String transformedVanity = "";
        boolean isMicrosite = isMicroSite(vanityUrl);
        vanityUrl = (vanityUrl.startsWith("/") ? vanityUrl : "/" + vanityUrl); //if somehow vanityUrl does not start with /, add / at the front.
        String currentPage_forwardDomain = DEFAULT_DOMAIN;
        Pattern cp = Pattern.compile(vanityHomePagePattern); //check if this is a valid current page /content/pwc/xx/
        Matcher cm = cp.matcher(currentPage);
        if (cm.find()) {//current page is a pwc pattern
            String territory = cm.group(1);
            Territory territoryObj = findTerritoryByName(territory);
            String domain = findcurrentPageForwardDomain(currentPage);
            if (!isMicrosite) { //check if the vanity url is a microsite url
                RootPage rootPageObj = getRootPage(vanityUrl);//this is to get /content/pwc/gx  or /content/pwc/ca or /content/pwc/uk ONLY!!!
                if (rootPageObj.isRootPage()) { //example like /content/pwc/uk or /content/pwc/gx or /content/pwc/ca
                    if (territoryObj.isTLD()) { //such as pwc.co.uk or pwc.de
                        //if(territoryObj.isMultiLanguage()){ //such  or /content/pwc/de
                        //transformedVanity = territoryObj.getForwardDomain();
                        //}else{ // this is for /content/pwc/uk
                        transformedVanity = territoryObj.getForwardDomain();
                        //}
                    } else {
                        transformedVanity = territoryObj.getForwardDomain() + "/" + rootPageObj.getContent();
                    }

                } else { //if it is not a root page

                    if (territoryObj.isTLD()) { //any non pwc.com is a TLD include microsites!!!
                        boolean isCurrentPageMicrosite = isMicroSite(currentPage);
                        if (isCurrentPageMicrosite) {
                            transformedVanity = getMicroSite(currentPage).getForwardDomain() + vanityUrl;
                        } else {
                            if (territoryObj.isMultiLanguage()) {
                                transformedVanity = territoryObj.getForwardDomain() + vanityUrl.replaceAll("/content/pwc/(\\w{2})(/.*)", "$2");
                                //transformedVanity = territoryObj.getForwardDomain() + vanityUrl.replaceAll("/content/pwc/(\\w{2})(/(\\w{2})(($)|(/$)|/(.*)))", "$2");
                            } else {
                                //System.out.println(vanityUrl);
                                transformedVanity = territoryObj.getForwardDomain() + vanityUrl.replaceAll("/content/pwc/(\\w{2})/(\\w{2})((/$)|($)|(/.*))", "$3");
                            }
                        }

                    } else {
                        transformedVanity = territoryObj.getForwardDomain() + vanityUrl.replace("/content/pwc", "");
                    }
                }
            } else { //check if current page is microsite
                String vanityReplacingPattern = "/content/pwc/(\\w{2})/\\w{2}/website/([^/]+)(/.*)";
                RootPage mRootPage = getMirositeRootPage(vanityUrl);
                Microsite microsite = getMicroSite(currentPage);
                if (mRootPage.isRootPage()) {
                    if (microsite.isMultiLanguage())
                        transformedVanity = microsite.getForwardDomain() + "/" + microsite.getLocale();
                    else
                        transformedVanity = microsite.getForwardDomain();
                } else {
                    if (microsite.isMultiLanguage()) {
                        transformedVanity = microsite.getForwardDomain() + "/" + microsite.getLocale() + vanityUrl.replaceAll(vanityReplacingPattern, "$3");
                    } else {
                        transformedVanity = getMicroSite(currentPage).getForwardDomain() + vanityUrl.replaceAll(vanityReplacingPattern, "$3");
                    }
                }

            }
        }

        return transformedVanity;
    }

    @Override
    public String transformAEMUrl(String path) {
        return transformAEMUrl(path, "");
    }

    private RootPage getMirositeRootPage(String path) {
        RootPage rootPage = new RootPage();
        Pattern p = Pattern.compile(vanityMicrositeRootPagePattern);
        Matcher m = p.matcher(path);

        //Pattern r2 = Pattern.compile("/content/pwc/(\\w{2})/\\w{2}/website/([^/]+)($|/$)");
        if (m.find()) {
            rootPage.setContent("");
            rootPage.setIsRootPage(true);
        }
        return rootPage;
    }

    private RootPage getRootPage(String path) {
        Pattern cp = Pattern.compile(vanityRootPagePattern);
        Matcher cm = cp.matcher(path);
        RootPage rootPage = new RootPage();
        if (cm.find()) {
            rootPage.setContent(cm.group(1));
            rootPage.setIsRootPage(true);
        }
        return rootPage;
    }

    private String findcurrentPageForwardDomain(String currentPage) {
        Territory territoryObj = new Territory();
        String currentPage_forwardDomain = DEFAULT_DOMAIN;
        Pattern cp = Pattern.compile(vanityHomePagePattern);
        Matcher cm = cp.matcher(currentPage);
        if (cm.find()) {
            String territory = cm.group(1);
            try {
                Territory currentPageTerritory = findTerritoryByName(territory);
                if (currentPageTerritory.isTLD()) {
                    if (currentPageTerritory.isMultiLanguage()) {
                        String locale = findLocaleByCurrentPagePath(currentPage);
                        currentPage_forwardDomain = currentPageTerritory.getForwardDomain() + locale;

                    } else {
                        currentPage_forwardDomain = currentPageTerritory.getForwardDomain();
                    }
                } else {
                    currentPage_forwardDomain = currentPageTerritory.getForwardDomain();
                }
            } catch (Exception e) {
                currentPage_forwardDomain = DEFAULT_DOMAIN;
            }
        } else {
            currentPage_forwardDomain = DEFAULT_DOMAIN;
        }
        return currentPage_forwardDomain;
    }

    private String findLocaleByCurrentPagePath(String path) {
        Pattern p = Pattern.compile("^/content/pwc/\\w2(/\\w2)(/$|$|/.*)");
        Matcher m = p.matcher(path);
        if (m.find())
            return m.group(1);
        else
            return null;
    }

    // NO need, session is closed when transateAEMUrl(), should be removed!
    @Override
    public void logout() {
        //if(this.session.isLive())
        //if (this.session != null && this.session.isLive())
        //this.session.logout();
    }

    private boolean requestUrlisMicrosite(String requestedUrl) {
        boolean isMicro = false;
        Pattern r = Pattern.compile(micrositeHomePagePattern);
        Matcher m = r.matcher(requestedUrl);
        isMicro = m.find();
        if (!isMicro) {
            Pattern r2 = Pattern.compile("^/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})/website/(.*)");
            Matcher m2 = r2.matcher(requestedUrl);
            isMicro = m2.find();
        }
        return isMicro;
    }

    private boolean isMicroSite(String path) {
        boolean isMicro = false;
        Pattern r = Pattern.compile(micrositeHomePagePattern);
        Matcher m = r.matcher(path);
        isMicro = m.find();
        if (!isMicro) {
            Pattern r2 = Pattern.compile("/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})/website/(.*)");
            Matcher m2 = r2.matcher(path);
            isMicro = m2.find();
        }
        return isMicro;
    }

    private Microsite getMicroSite(String path) throws Exception {
        String territory = "";
        String micrositeName = "";
        String content = "";
        String locale = "";
        boolean isMicrosite = false;
        boolean isHomePage = false;
        Microsite microsite = new Microsite();
        Pattern r2 = Pattern.compile(micrositeHomePagePattern);
        Matcher m2 = r2.matcher(path);
        if (m2.find()) {
            territory = m2.group(1);
            locale = m2.group(2);
            micrositeName = m2.group(3).replace(".html", "");
            content = "";
            isMicrosite = true;
            isHomePage = true;
        } else {
            Pattern r = Pattern.compile(micrositePagePattern);
            Matcher m = r.matcher(path);
            if (m.find()) {
                territory = m.group(2);
                locale = m.group(3);
                micrositeName = m.group(4);
                content = m.group(5);
                isMicrosite = true;
            }
        }

        if (isMicrosite) {
            Node countryNode = session.getNode("/content/pwc/global/referencedata/territories/" + territory);
            NodeIterator itr = countryNode.getNodes();
            int countMicrositeLanugage = 0;
            while (itr.hasNext()) {
                Node languageNode = itr.nextNode();
                String eachPossibleMicrosSiteLink = languageNode.getPath();

                String eachPossibleMicrositeNodePath = eachPossibleMicrosSiteLink + "/website/" + micrositeName;
                try {
                    Node testNode = session.getNode(eachPossibleMicrositeNodePath);
                    countMicrositeLanugage++;
                } catch (Exception ex) {
                }
            }
            //String micrositePath = "/content/pwc/global/referencedata/territories/" + territory + "/" + locale + "/website/" + micrositeName;
            Node territoryNode = session.getNode("/content/pwc/global/referencedata/territories/" + territory + "/" + locale + "/website/" + micrositeName);

            if (territoryNode.hasProperty(forwardDomain))
                microsite.setForwardDomain(territoryNode.getProperty(forwardDomain).getString());
            else
                microsite.setForwardDomain(territoryNode.getProperty("forward-domain").getString());


            String micrositeLocale = locale;
            microsite.setTerritory(territory);
            microsite.setLocale(locale);
            microsite.setIsMultiLanguage(countMicrositeLanugage > 1);
            if (microsite.isMultiLanguage()) {
                if (isHomePage)
                    microsite.setPath("/" + locale + "/");
                else
                    microsite.setPath("/" + micrositeLocale + content);
            } else {
                //String relPath = content.substring(1, content.length());
                if (isHomePage)
                    microsite.setPath("");
                else
                    microsite.setPath(content);
            }

        }

        return microsite;

    }

    private Microsite findMicrositeByName(String micrositePath) throws Exception {
        Node territoryNode = session.getNode("/content/pwc/global/referencedata/territories/" + micrositePath);
        Microsite microsite = new Microsite();
        microsite.setForwardDomain(territoryNode.getProperty(forwardDomain).getString());
        return microsite;
    }

    private Territory findTerritoryByName(String territoryName) throws Exception {
        Node territoryNode = session.getNode("/content/pwc/global/referencedata/territories/" + territoryName);
        Territory territory = new Territory();
        territory.setTerritoryName(territoryNode.getProperty("countryCode").getString().toLowerCase());
        territory.setTerritoryCodeProperty(territoryNode.hasProperty("territoryCode") ? territoryNode.getProperty("territoryCode").getString().toLowerCase() : territoryName);
       	territory.setForwardDomain(territoryNode.hasProperty(forwardDomain) ? territoryNode.getProperty(forwardDomain).getString() : DEFAULT_DOMAIN);
        territory.setShowLangugageInUrl(territoryNode.hasProperty("showLanguageInUrl") ? territoryNode.getProperty("showLanguageInUrl").getBoolean() : false);
        if (territoryNode.hasNodes() && territoryNode.getNodes().getSize() >= 2)
            territory.setIsMultiLanguage(true);
        else
            territory.setIsMultiLanguage(false);
        territory.setIsTLD(!territory.getForwardDomain().equalsIgnoreCase(DEFAULT_DOMAIN));//if current forward domain is not pwc.com, then it is a TLD site
        return territory;
    }

    private class RootPage {
        private boolean isRootPage;
        private String content;
        private String micrositeName;

        public boolean isRootPage() {
            return isRootPage;
        }

        public void setIsRootPage(boolean isRootPage) {
            this.isRootPage = isRootPage;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getMicrositeName() {
            return micrositeName;
        }

        public void setMicrositeName(String micrositeName) {
            this.micrositeName = micrositeName;
        }
    }
    
    public String transformStrategyAndURL(String path, String requestedUrl) {

        String transformedLink = "";
        try {

                Pattern h0 = Pattern.compile("^/content/pwc/((\\w{2})(/$|$|.html$))"); // to handle /content/pwc/nl or /content/pwc/nl/ or /content/pwc/nl.html
                Matcher m0 = h0.matcher(path);
                if (m0.find()) {

                    Territory homePageTerritoryObj = findTerritoryByName(m0.group(2));
                    transformedLink = homePageTerritoryObj.getForwardDomain() + "/" + homePageTerritoryObj.getTerritoryCodeProperty();
                    
                } else {
                    Pattern homePagePattern = Pattern.compile("^/content/pwc/(\\w{2})(/(\\w{2})(.html$|$))");
                    Matcher homePageMatcher = homePagePattern.matcher(path);
                    
                    boolean isHomePage = false;
                    if (homePageMatcher.find()) {

                        aemLink = new AEMLink();
                        aemLink.setTerritory(homePageMatcher.group(1));
                        aemLink.setLocale(homePageMatcher.group(3));
                        aemLink.setContent(homePageMatcher.group(2));
                        isHomePage = true;
                    } else {
                        Pattern r = Pattern.compile("^/content/(?:dam/pwc|pwc)/(\\w{2})/(\\w{2})(/.*)");
                        Matcher m = r.matcher(path);
                        if (m.find()) {
                            aemLink = new AEMLink();
                            aemLink.setTerritory(m.group(1));
                            aemLink.setLocale(m.group(2));
                            aemLink.setContent(m.group(3));
                        }
                    }

                    if (requestedUrl != null && requestedUrl.length() > 0) {
                        Pattern r1 = Pattern.compile("([^/]+(?:/cf#|/editor.html|))*/content/(?:dam/pwc|pwc)/(\\w{2})/(.*)");
                        Matcher m1 = r1.matcher(requestedUrl);
                        String requestedLink_territory = "";
                        if (m1.find()) {
                            requestedLink_territory = m1.group(2);
                        }

                        if (aemLink != null) {
                        	
                            Territory territory = findTerritoryByName(aemLink.getTerritory()); //link that needs to be tranformed
                            aemLink.setTerritory(territory.getTerritoryCodeProperty());
                            
                            Territory requestedUrl_Territory = findTerritoryByName(requestedLink_territory);

                            if (requestedUrl_Territory.getForwardDomain().equalsIgnoreCase(territory.getForwardDomain())) {
                                if(isHomePage)
                                	transformedLink = aemLink.getLinkwithTerritory();
                                else
                                	transformedLink = aemLink.getFullLink();
                            }else {
                            	
                            	if(isHomePage)
                                	transformedLink = territory.getForwardDomain() + aemLink.getLinkwithTerritory();
                                else
                                	transformedLink = territory.getForwardDomain() + aemLink.getFullLink();
                            }
                                
                                
                        } else {
                            transformedLink = path;
                        }
                    } else {//if no requestUrl supplied, it will be translated into external url
                        Territory territory = findTerritoryByName(aemLink.getTerritory());
                    	aemLink.setTerritory(territory.getTerritoryCodeProperty());
                    	
                    	if(isHomePage)
                        	transformedLink = territory.getForwardDomain() + aemLink.getLinkwithTerritory();
                        else
                        	transformedLink = territory.getForwardDomain() + aemLink.getFullLink(); 
                    }
                }

        } catch (Exception ex) {
            transformedLink = DEFAULT_DOMAIN + path;
        } finally {
            if (session != null)
                session.logout();
        }
        return transformedLink;
    }

    public String transformVanityforStrategyAnd(String vanityUrl, String currentPage) throws Exception {
        String transformedVanity = "";
        vanityUrl = (vanityUrl.startsWith("/") ? vanityUrl : "/" + vanityUrl); //if somehow vanityUrl does not start with /, add / at the front.
        Pattern cp = Pattern.compile(vanityHomePagePattern); //check if this is a valid current page /content/pwc/xx/
        Matcher cm = cp.matcher(currentPage);
        if (cm.find()) {
            String territory = cm.group(1);
            Territory territoryObj = findTerritoryByName(territory);
                RootPage rootPageObj = getRootPage(vanityUrl);
                if (rootPageObj.isRootPage()) {
                        transformedVanity = territoryObj.getForwardDomain() + "/" + rootPageObj.getContent();
                } else {
                        transformedVanity = territoryObj.getForwardDomain() + vanityUrl.replace("/content/pwc", "");
                }
        }

        return transformedVanity;
    }

}