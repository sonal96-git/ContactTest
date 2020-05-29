package com.pwc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;

/**
 * Created by rjiang022 on 9/24/2015.
 */
public class LinkTransformerUnitTest {
    Session session = null;
    String domainType = "";
    String domain = "https://www.pwc.com";
    String server = "";
    String username = "";
    String password = "";
    
    public LinkTransformerUnitTest() throws IOException {
        final Properties prop = new Properties();
        final String propFileName = "server.properties";
        InputStream inputStream;
        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        this.server = prop.getProperty("host");
        this.username = prop.getProperty("user");
        this.password = prop.getProperty("password");
    }
    
    @Test
    public void testSinglLink() throws Exception {
        final Repository repository = JcrUtils.getRepository(this.server);
        final SimpleCredentials crds = new SimpleCredentials(this.username, this.password.toCharArray());
        session = repository.login(crds, "crx.default");
        final LinkTransformerService service = new LinkTransformerServiceImpl(session, domain, domainType);
        final String link = "/etc/designs/pwc/canvas/content/pwc/ca/en/3cc3/1388/jcr:content/contentPar/importer/headindex.htmlclientlibs.css ";
        final String currentPage = "/content/pwc/ca/en/x";
        final String expectedResult = "/etc/designs/pwc/canvas/content/pwc/ca/en/3cc3/1388/jcr:content/contentPar/importer/headindex.htmlclientlibs.css ";
        final String translatedPath = service.transformAEMUrl(link, currentPage);
        service.logout();
        if (!translatedPath.equalsIgnoreCase(expectedResult)) {
            System.out.println("ERROR " + currentPage + " " + link + " T=" + translatedPath + " E=" + expectedResult);
            
        }
        assertEquals(translatedPath, expectedResult);
    }
    
    @Test
    public void testLinks() {
        final LinkedList<String> territories = new LinkedList<>();
        territories.add("TestLinkTransformers_gx.xml");
        territories.add("TestLinkTransformers_uk.xml");
        territories.add("TestLinkTransformers_za.xml");
        territories.add("TestLinkTransformers_nl.xml");
        territories.add("TestLinkTransformers_ca.xml");
        territories.add("TestLink_www_pwclegal_co_uk.xml");
        territories.add("TestLink_www_pwclegal_de.xml");
        for (final String eachTerritory : territories) {
            test(eachTerritory);
        }
        
    }
    
    @Test
    public void testExternalLinks() {
        final LinkedList<String> territories = new LinkedList<>();
        territories.add("TestExternal.xml");
        for (final String eachTerritory : territories) {
            test(eachTerritory);
        }
    }
    
    private void test(final String fileName) {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File file = new File(classLoader.getResource(fileName).getFile());
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            final NodeList nList = doc.getElementsByTagName("testCase");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                final Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element eElement = (Element) nNode;
                    final String currentPage = eElement.getElementsByTagName("currentPage").item(0).getTextContent()
                            .trim();
                    final String link = eElement.getElementsByTagName("link").item(0).getTextContent().trim();
                    final String expectedResult = eElement.getElementsByTagName("expectedResult").item(0)
                            .getTextContent().trim();
                    testEachLink(currentPage, link, expectedResult, fileName);
                    //System.out.println(currentPage);
                }
            }
        } catch (final Exception ex) {
            System.out.println(ex);
            assertEquals("1", "2");
        }
    }
    
    private void testEachLink(final String currentPage, final String link, final String expectedResult,
            final String fileName) throws Exception {
        final Repository repository = JcrUtils.getRepository(this.server);
        final SimpleCredentials crds = new SimpleCredentials(this.username, this.password.toCharArray());
        session = repository.login(crds, "crx.default");
        final LinkTransformerService service = new LinkTransformerServiceImpl(session, domain, domainType);
        final String translatedPath = service.transformAEMUrl(link, currentPage);
        service.logout();
        if (!translatedPath.equalsIgnoreCase(expectedResult)) {
            System.out.println("ERROR fileName=" + fileName + " C=" + currentPage + " " + link + " T=" + translatedPath
                    + " E=" + expectedResult);
            
        }
        assertEquals(translatedPath, expectedResult);
    }
}
