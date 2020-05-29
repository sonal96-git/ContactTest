package com.pwc.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.TidyJSONWriter;
import com.day.cq.commons.TidyJsonItemWriter;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.pwc.util.ExceptionLogger;

//http://experiencedelivers.adobe.com/cemblog/en/experiencedelivers/2014/02/the-styles-tab-part2.html
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "fallbackstyle",
    "sling.servlet.extensions=" + "json"
})
public class FallbackComponentStyle extends SlingSafeMethodsServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(FallbackComponentStyle.class);
    public static final String TIDY = "tidy";

    @Override
	protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp)	throws ServletException, IOException{
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        boolean tidy = "true".equals(req.getParameter("tidy"));


        ResourceResolver resolver = req.getResourceResolver();
        Resource res = req.getResource();
        Designer designer = resolver.adaptTo(Designer.class);

        if (designer == null) {
            log.error("No Designer available.");
            resp.sendError(500);
            return;
        }

        Page page = res.adaptTo(Page.class);
        if (page != null)
        {
            Design design = designer.getDesign(page);
            exportJSON(resp, design.getContentResource(), tidy);
        } else {
            String name = req.getRequestPathInfo().getSuffix();
            if ((name != null) && (name.startsWith("/"))) {
                name = name.substring(1);
            }
            if ("cq:styles".equals(name)) {
                PageManager pMgr = resolver.adaptTo(PageManager.class);
                page = pMgr.getContainingPage(res);

                String componentType = req.getResource().getResourceType();

                try {

                    Node node = res.getResourceResolver().getResource(componentType).adaptTo(Node.class);
                    String superType = node.getProperty("sling:resourceSuperType").getValue().toString();

                    componentType = superType.contains("longform") ? "longform-base" : componentType;

                } catch (RepositoryException e) {
                    ExceptionLogger.logException(e);
                }



                componentType= componentType.substring(componentType.lastIndexOf("/")+1);
                String defaultStyle = "/etc/designs/pwc/fallbackcomponentstyles/"+componentType+"/cq:styles/";
                Resource defaultStyleResource = req.getResourceResolver().getResource(defaultStyle);
                exportParaStyles(resp, designer.getDesign(page), res, tidy,defaultStyleResource);
                return;
            }

            Style style = designer.getStyle(res);
            if (style == null) {
                log.warn("Unable to resolve style of given resource");
                writeEmpty(resp);
                return;
            }
            if (name == null)
            {
                Resource r = style.getDefiningResource("");
                exportJSON(resp, r, tidy);
            } else if (name.length() == 0)
            {
                exportJSON(resp, style, null, tidy);
            } else if (style.containsKey(name)) {
                exportJSON(resp, style, name, tidy);
            }
            else {
                exportJSON(resp, style.getSubStyle(name), null, tidy);
            }
        }
    }

    private void exportParaStyles(SlingHttpServletResponse resp, Design design, Resource res, boolean tidy,Resource defaultStyleResource) throws IOException
    {
        try
        {

            Style style = design.getStyle(res);
            Cell cell = style == null ? null : style.getCell();
            Map<String,ComponentStyle> styles = design.getComponentStyles(cell);
            if(styles.size()==0){
                defaultStyleJSON(resp, res, defaultStyleResource);
            }else{
                TidyJSONWriter writer = new TidyJSONWriter(resp.getWriter());
                writer.setTidy(tidy);
                writer.object();
                for (ComponentStyle s : styles.values()) {
                    writer.key(s.getName());
                    s.write(writer);
                }
                writer.endObject();
            }

        } catch (Exception e) {
            log.error("Error while dumping component styles.", e);
            resp.sendError(500);
        }
    }



    private void exportJSON(SlingHttpServletResponse resp, Resource res, boolean tidy) throws IOException{
        if (res == null) {
            log.warn("Provided page or style has no content.");
            writeEmpty(resp);
        } else {
            Node node = res.adaptTo(Node.class);
            if (node == null) {
                log.error("Provided page or style content is not a node.");
                resp.sendError(500);
                return;
            }
            try {
                TidyJsonItemWriter writer = new TidyJsonItemWriter(null);
                writer.setTidy(tidy);
                writer.dump(node, resp.getWriter(), -1);
            } catch (Exception e) {
                log.error("Error while dumping design.", e);
                resp.sendError(500);
            }
        }
    }

    private void exportJSON(SlingHttpServletResponse resp, Style style, String name, boolean tidy)throws IOException	{
        try
        {
            TidyJSONWriter writer = new TidyJSONWriter(resp.getWriter());
            writer.setTidy(tidy);
            writer.object();
            if (name == null) {
                for (String key : style.keySet())
                {
                    if (!key.startsWith("jcr:"))
                        writeProperty(writer, style, key);
                }
            }
            else {
                writeProperty(writer, style, name);
            }
            writer.endObject();
        } catch (Exception e) {
            log.error("Error while dumping style.", e);
            resp.sendError(500);
        }
    }

    private void writeProperty(TidyJSONWriter writer, Style style, String name) throws JSONException, Exception	{
        String value = style.get(name, String.class);
        String label = name;
        int idx = label.lastIndexOf('/');
        if (idx > 0) {
            label = label.substring(idx + 1);
        }
        if (value != null)
        {
            writer.key(label).value(value);
        } else {
            String[] values = style.get(name, String[].class);
            if (values != null) {
                writer.key(label).array();
                for (String v : values) {
                    writer.value(v);
                }
                writer.endArray();
            }
        }
    }

    private void writeEmpty(ServletResponse resp)	throws IOException	{
        resp.getWriter().print("{}");
    }
    /* Fallback Operations */
    private void defaultStyleJSON(SlingHttpServletResponse resp, Resource res,	Resource fallbackResource) throws IOException, JSONException, Exception {
        TidyJSONWriter json = new TidyJSONWriter(resp.getWriter());
        json.object();
        Iterator<Resource> children = fallbackResource.listChildren();
        while (children.hasNext()) {
            Resource child = children.next();
            ValueMap jcrProp2=child.adaptTo(ValueMap.class);
            Iterator<Resource> comboProperties = child.listChildren();
            ArrayList<String> comboValues = new ArrayList<String>();
            while (comboProperties.hasNext()) {
                Resource comboValue = comboProperties.next();
                ValueMap comboPropertyValues=comboValue.adaptTo(ValueMap.class);
                comboValues.add(comboPropertyValues.get("value")==null? comboValue.getName():comboPropertyValues.get("value",String.class));
                comboValues.add(comboPropertyValues.get("text",String.class));
            }
            addingItem(json, child.getName(), jcrProp2.get("title",String.class), comboValues.toArray(new String[comboValues.size()]),res.getPath());
        }
        json.endObject();
    }

    private void addingItem(TidyJSONWriter json, String name, String title, String[] options,String path) throws JSONException, Exception {
        json.key(name);
        json.object().key("path").value(path).key("name").value(name).key("title").value(title).key("description").value("");
        json.key("options");
        json.array();
        for( int i=0; i<options.length;i++) {
            String optionValue = options[i];
            i++;
            String optionText = options[i];
            addingOption(json, optionValue, optionText);
        }
        json.endArray();
        json.endObject();
    }

    private void addingOption(TidyJSONWriter json, String value, String text) throws JSONException, Exception {
        json.object().key("value").value(value).key("text").value(text).key("description").value("").key("icon").value("").endObject();

    }
}
