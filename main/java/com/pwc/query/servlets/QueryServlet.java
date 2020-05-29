package com.pwc.query.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.rewriter.TransformerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.ApplicationConstants;
import com.pwc.BrandSimplificationConfigService;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.query.controllers.QueryController;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.contents.ControllerContent;
import com.pwc.query.controllers.models.factories.ControllerFactory;
import com.pwc.wcm.utils.I18nPwC;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "filter-dynamic",
    "sling.servlet.selectors=" + "rebrand-filter-dynamic"
})
public class QueryServlet extends SlingAllMethodsServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String COMPONENT_NAME = "faceted";
    private final String TAGS_LIST = "list";
    private final String PAGE = "page";
    private final String SEARCH_TEXT = "searchText";
    private final String DEFAULT_IMAGE_PATH = "defaultImagePath";

    private ControllerFactory controllerFactory = new ControllerFactory();

    @Reference
    private OsgiCollectionsLogger logger;

    @Reference
    private ConfigurationAdmin confAdmin;

    @Reference
    private SlingRepository repository;

    @Reference
    private TransformerFactory transformerFactory;

    @Reference
    private BrandSimplificationConfigService brandSimplificationConfigService;


    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        try {
            Configuration defaultDomainConf = confAdmin.getConfiguration("PwC Default Domain");
            boolean isEnabledLink = (boolean)confAdmin.getConfiguration("com.pwc.wcm.transformer.LinkTransformerFactory").getProperties().get("linktransformer.enabled");

            boolean isBrandSimplificationEnabled = brandSimplificationConfigService.isBrandSimplificationEnabled(request);

            String pageMax = request.getParameter(PAGE);
            String tags = request.getParameter(TAGS_LIST);           
            String searchText = request.getParameter(SEARCH_TEXT) != null ? new String(request.getParameter(SEARCH_TEXT).getBytes("iso-8859-1"),"UTF-8"):null;
                               

            JSONObject jsonObj = new JSONObject(tags);

            Resource resource = request.getResource();
            PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
            Page page  = pageManager.getContainingPage(resource);

            //TODO: getController's parameter should be dynamic. To use a different controller,we should get the component's name
            QueryController queryController = controllerFactory.getController("faceted");

            ValueMap properties = resource.adaptTo(ValueMap.class);

            I18nPwC i18nPwC  = new I18nPwC(request,resource);
            boolean isCollectionV2 = resource.getResourceType().equals(ApplicationConstants.COLLECTIONV2_RESOURCE);
            String defaultImagePath = request.getParameter(DEFAULT_IMAGE_PATH);

            ControllerBean controllerBean = new ControllerBean(getTagsList(jsonObj), properties, resource, resource.getResourceResolver(),
                    pageManager, page.getParent(), page, request,COMPONENT_NAME,pageMax,searchText,
                    logger,isEnabledLink,false,defaultDomainConf,repository,i18nPwC, isBrandSimplificationEnabled, isCollectionV2 ,defaultImagePath != null? defaultImagePath:StringUtils.EMPTY);

            queryController.setControllerBean(controllerBean);
            queryController.setServletCall(true);
            ControllerContent content = queryController.getContent();


            //List<List<String>> menuList = getTagsList(jsonObj);
            String collectionParsed = content.getCollectionJSONParsed();
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.getWriter().write(collectionParsed);

        } catch (JSONException e) {
            logger.logMessage(new Date()+" QueryServlet doGet error: "+e);
        }
    }

    private List<List<String>> getTagsList(JSONObject jsonObj) throws JSONException {

        JSONArray menuArray;
        List<String> tagsList =  new ArrayList<String>();
        List<List<String>> menuList = new ArrayList<List<String>>();

        if( jsonObj == null || jsonObj.length() == 0 ) return menuList;
        for (int i=0; i < jsonObj.names().length(); i++) {

            tagsList = new ArrayList<String>();
            menuArray = ((JSONArray)jsonObj.get(jsonObj.names().getString(i)));

            for (int j=0; j <menuArray.length(); j++) {
                String tag = menuArray.getJSONObject(j).getString("tag");
                tagsList.add(tag);
            }
            menuList.add(tagsList);
        }


        return menuList;
    }
}
