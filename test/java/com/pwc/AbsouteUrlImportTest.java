package com.pwc;

import com.pwc.wcm.transformer.impl.LinkTransformer;
import static org.junit.Assert.*;

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
//import java.sql.*;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Created by rjiang022 on 8/13/2016.
 */
public class AbsouteUrlImportTest {

    String domainType = "";
    String domain = "https://www.pwc.com";
    String server = "";
    String username="";
    String password="";
    public AbsouteUrlImportTest() throws IOException {
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

    //@Test
   /* public void testImport() throws Exception {

        DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        //enable @Test annotation if you want to import
        String dbURL = "jdbc:sqlserver://<server>;databaseName=<db>";
        String user = "userName";
        String pass = "password";
        Connection conn = DriverManager.getConnection(dbURL,user,pass);
        if (conn != null) {
            //System.out.println("Connected");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT  [Column0] FROM [DPE].[dbo].[scrollingPage]");
            while(rs.next()){
                String url = rs.getString("Column0");
                String query = "Update [DPE].[dbo].[scrollingPage] SET [AbsoluteURL]=? WHERE [Column0]=?";
                PreparedStatement ps = conn.prepareStatement(query);
                Repository repository = JcrUtils.getRepository(this.server);
                SimpleCredentials crds = new SimpleCredentials(this.username, this.password.toCharArray());
                Session session = repository.login(crds, "crx.default");
                LinkTransformerService service = new LinkTransformerServiceImpl(session, domain, domainType);
                String translatedUrl = service.transformAEMUrl(url,"");
                ps.setString(1,translatedUrl);
                ps.setString(2,url);
                ps.executeUpdate();
                ps.close();
                service.logout();
            }
        }

        assertTrue(true);
    }
    */
}
