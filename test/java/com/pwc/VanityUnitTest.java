package com.pwc;

import com.pwc.wcm.services.LinkTransformerService;
import com.pwc.wcm.services.impl.LinkTransformerServiceImpl;
import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Created by rjiang022 on 9/24/2015.
 */
public class VanityUnitTest {
    Session session = null;
    String domainType = "";
    String domain = "https://www.pwc.com";
    String server = "";
    String username="";
    String password="";

    public VanityUnitTest() throws IOException {
        Properties prop = new Properties();
        String propFileName = "server.properties";
        InputStream inputStream;
        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        this.server = prop.getProperty("host");
        this.username = prop.getProperty("user");
        this.password = prop.getProperty("password");
    }

    private void test(String fileName){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("testCase");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String currentPage = eElement.getElementsByTagName("currentPage").item(0).getTextContent().trim();
                    String link = eElement.getElementsByTagName("vanity").item(0).getTextContent().trim();
                    String expectedResult = eElement.getElementsByTagName("expectedResult").item(0).getTextContent().trim();
                    testEachLink(currentPage, link, expectedResult, fileName, temp);
                    //System.out.println(currentPage);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
            assertEquals("1", "2");
        }
    }
    @Test
    public void testVanities(){
        LinkedList<String> territories = new LinkedList<>();
        territories.add("TestVanity_gx.xml");
        territories.add("TestVanity_uk.xml");
        territories.add("TestVanity_nl.xml");
        territories.add("TestVanity_de.xml");
        territories.add("TestVanity_jp.xml");
        territories.add("TestVanity_www_pwclegal_co_uk.xml");
        territories.add("TestVanity_www_pwclegal_de.xml");

        int counter = 0;
        for(String eachTerritory: territories) {
            test(eachTerritory);
        }
    }

    @Test
    public void testSinglVanity() throws Exception{
        Repository repository = JcrUtils.getRepository(this.server);
        SimpleCredentials crds = new SimpleCredentials(this.username, this.password.toCharArray());
        session = repository.login(crds, "crx.default");
        LinkTransformerService service = new LinkTransformerServiceImpl(session, domain, domainType);
        String link = "/content/pwc/gx/";
        String currentPage= "/content/pwc/gx/en";
        String expectedResult = "https://www.pwc.com/gx/";
        String translatedPath = service.transformVanity(link,currentPage);
        service.logout();
        if(!translatedPath.equalsIgnoreCase(expectedResult)){
            System.out.println("ERROR " + currentPage +  " Vanity=" + link +" T=" + translatedPath +  " E="  + expectedResult);

        }
        assertEquals(translatedPath, expectedResult);
    }

    private void testEachLink(String currentPage, String link, String expectedResult, String fileName, int index) throws Exception {
        index++;
        Repository repository = JcrUtils.getRepository(this.server);
        SimpleCredentials crds = new SimpleCredentials(this.username, this.password.toCharArray());
        session = repository.login(crds, "crx.default");
        LinkTransformerService service = new LinkTransformerServiceImpl(session, domain, domainType);

        String translatedPath = service.transformVanity(link, currentPage);
        if(!translatedPath.equalsIgnoreCase(expectedResult)){
            System.out.println("ERROR @Line #" + index + " fileName=" + fileName + " C=" + currentPage +  " vanity=" + link +" T=" + translatedPath +  " E="  + expectedResult);
        }
        assertEquals(translatedPath, expectedResult);
    }

}
