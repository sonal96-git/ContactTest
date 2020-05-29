package com.pwc.query.controllers.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.wcm.utils.I18nPwC;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.cm.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ControllerBean {

	private List<List<String>> filters = new ArrayList<List<String>>();
	private ValueMap properties;
	private  Resource resource;
	private ResourceResolver resourceResolver;
	private PageManager pageManager;
	private Page parentPage;
	private Page currentPage;
	private SlingHttpServletRequest request;
	private String compName;
	private String offSet;
	private String searchText;
	private OsgiCollectionsLogger pwcLogger ;
	private boolean isDeepLinkSearch;
	private boolean isEnabledTransformer;
	private Configuration defaultDomainConf;
	private SlingRepository repository;
	private I18nPwC i18nPwC;
	private boolean isBrandSimplificationEnabled;
	private boolean isCollectionV2;
	private String defaultImagePath;



	public ControllerBean() { }

	public ControllerBean(List<List<String>> filters, ValueMap properties, Resource resource,
						  ResourceResolver resourceResolver, PageManager pageManager, Page parentPage, Page currentPage,
						  SlingHttpServletRequest request, String compName, String offSet, String searchText,
						  OsgiCollectionsLogger pwcLogger, boolean isEnabledTransformer,boolean isDeepLinkSearch,
						  Configuration defaultDomainConf, SlingRepository repository,I18nPwC i18nPwC, boolean isBrandSimplificationEnabled, boolean isCollectionV2, String defaultImagePath) {

		this.filters = filters;
		this.properties = properties;
		this.resource = resource;
		this.resourceResolver = resourceResolver;
		this.pageManager = pageManager;
		this.parentPage = parentPage;
		this.currentPage = currentPage;
		this.request = request;
		this.compName = compName;
		this.offSet = offSet;
		this.searchText = searchText;
		this.pwcLogger = pwcLogger;
		this.isEnabledTransformer = isEnabledTransformer;
		this.defaultDomainConf =defaultDomainConf;
		this.repository = repository;
        this.i18nPwC = i18nPwC;
        this.isDeepLinkSearch =  isDeepLinkSearch;
		this.isBrandSimplificationEnabled = isBrandSimplificationEnabled;
		this.isCollectionV2 = isCollectionV2;
		this.defaultImagePath = defaultImagePath;
	}

	public List<List<String>> getFilters() {
		return filters;
	}
	public void setFilters(List<List<String>> filters) {
		this.filters = filters;
	}
	public ValueMap getProperties() {
		return properties;
	}
	public void setProperties(ValueMap properties) {
		this.properties = properties;
	}
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public ResourceResolver getResourceResolver() {
		return resourceResolver;
	}
	public void setResourceResolver(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}
	public PageManager getPageManager() {
		return pageManager;
	}
	public void setPageManager(PageManager pageManager) {
		this.pageManager = pageManager;
	}
	public Page getParentPage() {
		return parentPage;
	}
	public void setParentPage(Page parentPage) {
		this.parentPage = parentPage;
	}
	public Page getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(Page currentPage) {
		this.currentPage = currentPage;
	}
	public SlingHttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(SlingHttpServletRequest request) {
		this.request = request;
	}
	public String getCompName() {
		return compName;
	}
	public void setCompName(String compName) {
		this.compName = compName;
	}
	public String getOffSet() {
		return offSet;
	}
	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	public OsgiCollectionsLogger getPwcLogger() {
		return pwcLogger;
	}
	public void setPwcLogger(OsgiCollectionsLogger pwcLogger) {
		this.pwcLogger = pwcLogger;
	}
	public boolean isEnabledTransformer() {
		return isEnabledTransformer;
	}
	public void setEnabledTransformer(boolean isEnabledTransformer) {
		this.isEnabledTransformer = isEnabledTransformer;
	}
	public Configuration getDefaultDomainConf() {
		return defaultDomainConf;
	}
	public void setDefaultDomainConf(Configuration defaultDomainConf) {
		this.defaultDomainConf = defaultDomainConf;
	}
	public SlingRepository getRepository() {
		return repository;
	}
	public void setRepository(SlingRepository repository) {
		this.repository = repository;
	}
    public I18nPwC getI18nPwC() {
        return i18nPwC;
    }
    public void setI18nPwC(I18nPwC i18nPwC) {
        this.i18nPwC = i18nPwC;
    }
    public boolean isDeepLinkSearch() {
        return isDeepLinkSearch;
    }
    public void setDeepLinkSearch(boolean deepLinkSearch) {
        isDeepLinkSearch = deepLinkSearch;
    }

	public boolean isBrandSimplificationEnabled() {
		return isBrandSimplificationEnabled;
	}

	public void setBrandSimplificationEnabled(boolean brandSimplificationEnabled) {
		isBrandSimplificationEnabled = brandSimplificationEnabled;
	}

	public boolean isCollectionV2() {
		return isCollectionV2;
	}

	public void setCollectionV2(boolean collectionV2) {
		isCollectionV2 = collectionV2;
	}

	public String getDefaultImagePath() {
		return defaultImagePath;
	}

	public void setDefaultImagePath(String defaultImagePath) {
		this.defaultImagePath = defaultImagePath;
	}
}
