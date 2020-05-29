package com.pwc.tagmodels;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FetchFeedData extends SimpleTagSupport  {

    String options[];
    String path;
    String limit;
    String urls[];


    Map feedValues = new HashMap();

    private static final Logger logger = LoggerFactory
            .getLogger(FetchFeedData.class);

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String[] getOptions() {
        return options;
    }



    public void setOptions(String[] options) {
        this.options = options;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Reference
    private ResourceResolverFactory resourceFactory;

    @Override
	public void doTag() throws JspException {
        logger.info(":: Inside FetchFeedData ::");
        logger.info("Urls are :: "+urls.length);
        logger.info("Options are :: "+options.length);

        ResourceResolver resourceResolver;
        List pages = new ArrayList();
        LinkedHashMap<String, List> map = new LinkedHashMap<String, List>();
        Resource resource = (Resource) getJspContext().getAttribute("resource");
        PageContext pageContext = (PageContext) getJspContext();
        Session sess = resource.getResourceResolver().adaptTo(
                Session.class);
        //   logger.info("path is :: " + path);
        //   logger.info("limit is :: " + limit);
        URL u = null;
        InputStream is = null;
        Integer items = Integer.parseInt(limit);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            u = new URL(path);
            is = getStream(u);
            // logger.info("Stream is "+is.available());
            if(is.available() < 1)
            {
                //   logger.info("Not Avilable Stream");
                is.close();
                Thread.sleep(10);
                is = getStream(u);
            }
            Document doc = builder.parse(is);
            //  logger.info("doc is "+doc.getElementsByTagName("item"));
            String title;
            NodeList nodes = doc.getElementsByTagName("item");
            // logger.info("First Name is "+nodes.item(0).getNodeName());

            for (int i = 0; i < items; i++) {
                Element element = (Element) nodes.item(i);
                List values = new ArrayList();
                for (int j = 0; j < options.length; j++) {
                    String option = options[j];
                    String url = urls[j];
                    logger.info(" ========================== "+option+" ================URL==============="+url);
                    /*if (option.contains("#apply url#")) {
                        String optionValTest[] = option.split("#apply url#");
                        logger.info("Property whose value will add in list going to getElementValue ::::: "+optionValTest[0]);
                        String optionVal = getElementValue(element,optionValTest[0]);
                        logger.info("Returned value going to store in list "+optionVal);
                        if (optionVal != null) {
                          //  values.add("<a href = "+ optionValTest[1]+">"+ optionVal + "</a>");
                            values.add(optionVal);
                        }
                    } */
                    String optionVal = getElementValue(element, option);
                    String linkVal = getElementValue(element, url);
                    logger.info("linkVal.equals('') comparison ::: "+linkVal.equals(""));
                    if (optionVal != null) {
                        if (linkVal.equals("")) {
                            logger.info("Value in else going to add in list " + optionVal + " And link is " + linkVal);
                            values.add(optionVal);

                        } else {
                            logger.info("Value in if going to add in list " + optionVal + " And link is " + linkVal);
                            values.add("<a href = " + linkVal + ">" + optionVal + "</a>");
                        }
                    }

                }
                logger.info("key is " + getElementValue(element, "link"));
                map.put(getElementValue(element, "link"), values);

            }// for
            logger.info("Map is " + map);
            pageContext.setAttribute("feeds", map);
            is.close();
        }// try


        catch (Exception ex) {
            try {
                is.close();
                logger.error("Error is "+ex.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ex.printStackTrace();

        }
        finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public String getElementValue(Element parent, String label) {
        return getCharacterDataFromElement((Element) parent
                .getElementsByTagName(label).item(0));
    }

    public String getCharacterDataFromElement(Element e) {
        try {
            Node child = e.getFirstChild();
            if (child instanceof CharacterData) {
                CharacterData cd = (CharacterData) child;
                return cd.getData();
            }
        } catch (Exception ex) {

        }
        return "";
    }
    public InputStream getStream(URL url) throws IOException
    {
        InputStream istream = url.openStream();
        return istream;
    }
}
