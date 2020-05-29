package com.pwc.model.components.twittersearch;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;
import com.pwc.model.components.twittersearch.impl.TwitterTimelineUrlCalculator;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.I18nPwC;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import java.util.Arrays;

public class TwitterSearch extends WCMUsePojo{
    
	private SlingHttpServletRequest request;
    private Resource resource;
	private static String LOADING_I18N_KEY = "TwitterSearch_Loading";
	private static String UNAVAILABLE_I18N_KEY = "TwitterSearch_Unavailable";
	
    private String embedHref;

    private String embedHtml;

    private String cssClass;

    private Boolean isAnchor;

    private Boolean isValid;

    private String height;  

    private String limit;

    private Boolean isLimitEnabled;

    private String chrome;

    private String title;
    
    private String widgetId;

    private String divId;

    private Boolean advancedConfiguration;
    
    private String loadingLabel;
    
    private String unavailableLabel;

	@Override
	public void activate() throws Exception {
        
		this.request = getRequest();
        if(request == null) return;
        
        this.resource = this.request.getResource();			
        if(resource == null) return;
        
        TwitterModel model = resource.adaptTo(TwitterModel.class);

        if(model == null) return;
        model.chrome = model.chrome == null ? new String[]{} : model.chrome;
        this.advancedConfiguration = model.advancedConfiguration != null && model.advancedConfiguration.equals("true");
        Style currentStyle = WCMUtils.getStyle(request);
        this.isLimitEnabled = !StringUtils.isBlank(model.limit);
        this.limit = this.isLimitEnabled ? model.limit : currentStyle.get("limit", "5");
        this.height = StringUtils.isBlank(model.height) ? currentStyle.get("height", "350") : model.height;
        String[] chromeSelection = this.advancedConfiguration ? model.chrome : currentStyle.get("chrome", String[].class);
        chromeSelection = (chromeSelection == null) ? new String[0] : chromeSelection;

        final String[] CHROMEOPTS = {"header", "footer", "borders", "scrollbar"};
        for (String opt : CHROMEOPTS) {
            if (!Arrays.asList(chromeSelection).contains(opt)) {
                this.chrome += " no" + opt;
            }
        }

        this.title = model.twittertitle;  
        this.widgetId = model.widgetType.equals("widgetid") ? model.widgetId : "";
        String uniqSuffix = resource.getPath().replaceAll("/","-").replaceAll(":","-");
        this.divId = "twitter-search" + uniqSuffix;
        ITwitterTimelineUrlCalculator urlCalculator = new TwitterTimelineUrlCalculator(model);
        this.isValid = false;
        if(model.widgetType.equals("singletweet")) {
            this.isAnchor = false;
            try {
                PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
                Page currentPage = pageManager.getContainingPage(resource);
                this.embedHtml = TwitterEmbedHtmlCalculator.getSingleTweet(model, currentPage,request);
                this.isValid = !StringUtils.isBlank(this.embedHtml);
            } catch (Exception e) {
                ExceptionLogger.logException(e);
            }
        } else {
            this.isAnchor = true;
            try {
                this.embedHref = urlCalculator.getUrl();
                this.cssClass = urlCalculator.getCssClass();
                this.isValid = urlCalculator.isValid();
            } catch (Exception e) {
                ExceptionLogger.logException(e);
            }
        }
        
        //I18n Labels
        I18nPwC i18nPwC = new I18nPwC(request, resource);
        this.loadingLabel = i18nPwC.getPwC(LOADING_I18N_KEY);
        this.unavailableLabel = i18nPwC.getPwC(UNAVAILABLE_I18N_KEY);
    }

    public String getEmbedHref() {
        return embedHref;
    }
    
    public String getWidgetId() {
        return widgetId;
    }

    public String getEmbedHtml() {
        return embedHtml;
    }   

    public Boolean getIsAnchor() {
        return isAnchor;
    }

    public Boolean getIsValid() { return isValid; }

    public String getCssClass() {
        return cssClass;
    }

    public String getHeight() {
        return height;
    }

    public String getLimit() {
        return limit;
    }

    public String getChrome() {
        return chrome;
    }

    public String getTitle() {
        return title;
    }

    public String getDivId() {
        return divId;
    }

    public Boolean getAdvancedConfiguration() {
        return advancedConfiguration;
    }

    public Boolean getLimitEnabled() {
        return isLimitEnabled;
    }
    
    public String getLoadingLabel() {
    	return loadingLabel;
    }
    
    public String getUnavailableLabel() {
    	return unavailableLabel;
    }

}
