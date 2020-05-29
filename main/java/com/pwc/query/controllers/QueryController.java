package com.pwc.query.controllers;

import com.adobe.cq.sightly.WCMUsePojo;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.pwc.ApplicationConstants;
import com.pwc.BrandSimplificationConfigService;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.contentlist.ContentList;
import com.pwc.query.controllers.models.contents.ControllerContent;
import com.pwc.query.controllers.models.factories.ContentListFactory;

import com.pwc.query.enums.CollectionProps;
import com.pwc.query.search.factories.SearchQueryFactory;
import com.pwc.query.search.queries.SearchQuery;
import com.pwc.query.utils.CommonsUtils;
import com.pwc.wcm.utils.I18nPwC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.cm.ConfigurationAdmin;

public abstract class QueryController extends WCMUsePojo {


    protected ControllerBean controllerBean;
    protected String rendition;

    private int hits;
    private boolean isServletCall = false;

    private List<Content> contentList = new ArrayList<Content>();
    private List<Content> fullContentList = new ArrayList<Content>();
    private SearchQueryFactory searchQueryFactory = new SearchQueryFactory();
    private ContentListFactory clFactory = new ContentListFactory();

    private final String RENDITION = "rendition";
    private final String CLASSIC = "classic";

    private final String ZERO = "0";
    private final String EMPTY = "";



    public abstract ControllerContent getContent() ;
    @Override
    public void activate() throws Exception {

        I18nPwC i18nPwC  = new I18nPwC(getRequest(), getResource());

        ConfigurationAdmin configAdmin = getSlingScriptHelper().getService(org.osgi.service.cm.ConfigurationAdmin.class);
        boolean isEnabled = (boolean)configAdmin.getConfiguration("com.pwc.wcm.transformer.LinkTransformerFactory").getProperties().get("linktransformer.enabled");
        BrandSimplificationConfigService brandSimplificationConfigService = getSlingScriptHelper()
                .getService(BrandSimplificationConfigService.class);
        boolean isBrandSimplificationEnabled = brandSimplificationConfigService.isBrandSimplificationEnabled(getRequest());
        boolean isCollectionV2 = getResource().getResourceType().equals(ApplicationConstants.COLLECTIONV2_RESOURCE);
        Pattern pattern = Pattern.compile("\\/content\\/pwc\\/(?:\\d{2}\\/(.*))");
        Boolean isStrategyPage = pattern.matcher(getCurrentPage().getPath()).matches();
        String defaultImagePath = isStrategyPage ? getCurrentStyle().get(ApplicationConstants.STRATEGY_ICON_ROOT_PATH,StringUtils.EMPTY) : getCurrentStyle().get(ApplicationConstants.ICON_ROOT_PATH, StringUtils.EMPTY);
        isServletCall = false;
        rendition = ObjectUtils.firstNonNull(getCurrentStyle().get(RENDITION), CLASSIC).toString();
        controllerBean = new ControllerBean(new ArrayList<List<String>>(), getProperties(), getResource(), getResourceResolver(),
                                            getPageManager(), getResourcePage().getParent(), getCurrentPage(), getRequest(),
                                            EMPTY, ZERO, EMPTY, getSlingScriptHelper().getService(OsgiCollectionsLogger.class), isEnabled, false, null, null, i18nPwC,
                                            isBrandSimplificationEnabled, isCollectionV2, defaultImagePath);

    }

    protected List<Content> getContentList() throws Exception {

        Resource resource = controllerBean.getResource();
        ValueMap componentProperties = controllerBean.getProperties();

        controllerBean.getPwcLogger().logKeyValue(controllerBean.getCompName(),resource.getPath());

        //getting the "Build List Using" value. e.g Child, Descendants, etc.
        String listFrom = componentProperties.get(CollectionProps.BUILD_LIST_USING) !=null ?
                componentProperties.get(CollectionProps.BUILD_LIST_USING).toString():EMPTY;

        //getting query from the factory
        SearchQuery searchQ = searchQueryFactory.getSearchQuery(listFrom);

        controllerBean.getPwcLogger().logListDefinition(listFrom, searchQ.getPaths(controllerBean));
        controllerBean.getPwcLogger().logMessage("start search: "+new Date());
        //Execute query, and get the result.
        List<Resource> resources = searchQ.getResults(controllerBean);
        controllerBean.getPwcLogger().logMessage("end search  : "+new Date());

        contentList = addItems(searchQ,resources,controllerBean);
        controllerBean.getPwcLogger().logMessage(controllerBean.getCompName()+" element results: " + contentList.size() + " results");

        return contentList;
    }

    private List<Content> addItems(SearchQuery searchQ, List<Resource> resources, ControllerBean controllerBean) throws Exception {

        ContentList contentListController = clFactory.getContentListType(controllerBean.getCompName());

        //getting the set of default images
        List<String> randomImagePaths = new ArrayList<>();

        if (controllerBean.isBrandSimplificationEnabled() && controllerBean.isCollectionV2()) {
            Resource defaultImageFolder = controllerBean.getResourceResolver().getResource(controllerBean.getDefaultImagePath());
            if (null != defaultImageFolder) {
                Iterable<Resource> defaultImageSet = defaultImageFolder.getChildren();
                for (Resource currentResource : defaultImageSet) {
                    Asset asset = currentResource.adaptTo(Asset.class);
                    if (null != asset && DamUtil.isValid(asset) && DamUtil.isImage(asset))
                        randomImagePaths.add(currentResource.getPath());
                }
            }
        }

        contentList = contentListController.getContentList(resources,controllerBean, randomImagePaths);
        fullContentList = contentListController.getFullContentList();
        int numberHits = contentListController.getHits();

        hits = numberHits < CommonsUtils.getLimit(controllerBean) ? numberHits: CommonsUtils.getLimit(controllerBean);

        return contentList;
    }

    public ControllerBean getControllerBean() {
        return controllerBean;
    }
    public void setControllerBean(ControllerBean controllerBean) {
        this.controllerBean = controllerBean;
    }
    public int getHits() {
        return hits;
    }
    public void setHits(int hits) {
        this.hits = hits;
    }
    public SearchQueryFactory getSearchQueryFactory() {
        return searchQueryFactory;
    }
    public void setSearchQueryFactory(SearchQueryFactory searchQueryFactory) {
        this.searchQueryFactory = searchQueryFactory;
    }
    public String getRendition() {
        return rendition;
    }
    public void setRendition(String rendition) {
        this.rendition = rendition;
    }
    public List<Content> getFullContentList() {
        return fullContentList;
    }
    public void setFullContentList(List<Content> fullContentList) {
        this.fullContentList = fullContentList;
    }
    public boolean isServletCall() {
        return isServletCall;
    }
    public void setServletCall(boolean servletCall) {
        isServletCall = servletCall;
    }
}
