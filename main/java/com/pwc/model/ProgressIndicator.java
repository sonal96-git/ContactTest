package com.pwc.model;

import java.util.Iterator;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.pwc.user.Constants;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.PageService;

@Model(adaptables = { SlingHttpServletRequest.class })
public class ProgressIndicator {

	@Self
	private SlingHttpServletRequest request;

	@ChildResource(injectionStrategy = InjectionStrategy.OPTIONAL)
	private Resource progressIndicatorLinks;

	@ValueMapValue
	@Default(values = StringUtils.EMPTY)
	private String progressIndicatorTitle;
	
	private static String PROGRESS_IND_TITLE = "Progress_Indicator_Title";

	private String jsonItems;

	@PostConstruct
	protected void init() throws JSONException {
		if (progressIndicatorTitle.isEmpty()) {
			String locale = null;
			RequestPathInfo pathinfo = request.getRequestPathInfo();
			String resourcePath = pathinfo.getResourcePath();
			String path = resourcePath.substring(0, resourcePath.indexOf("/" + JcrConstants.JCR_CONTENT));
			PageManager pageManager = request.getResource().getResourceResolver().adaptTo(PageManager.class);
			if (pageManager != null) {
				Page currentPage = pageManager.getContainingPage(path);
				if (currentPage.getPath().contains(Constants.USER_REG_PAGE_PREFIX)
						|| currentPage.getPath().contains("/content/pwc/global/forms/")) {
					locale = CommonUtils.getPathLocale(path);
				} else {
					PageService pageService = new PageService();
					locale = pageService.getLocale(request, currentPage);
				}
				Locale pageLang = new Locale(locale);
				I18nPwC i18nPwC = new I18nPwC(request, request.getResourceBundle(pageLang));
				progressIndicatorTitle = i18nPwC.getPwC(PROGRESS_IND_TITLE);
			}
		}
		if (progressIndicatorLinks != null) {
			Iterator<Resource> iterator = progressIndicatorLinks.listChildren();
			JSONArray jsonArray = new JSONArray();
			while (iterator.hasNext()) {
				Resource value = iterator.next();
				ValueMap valueMap = value.adaptTo(ValueMap.class);
				if (valueMap != null) {
					JSONObject formDetailsJson = new JSONObject();
					formDetailsJson.put("id", valueMap.get("id"));
					formDetailsJson.put("title", valueMap.get("title"));
					jsonArray.put(formDetailsJson);
				}
			}
			jsonItems = jsonArray.toString();
		}

	}

	public String getProgressIndicatorTitle() {
		return progressIndicatorTitle;
	}

	public String getJsonItems() {
		return jsonItems;
	}

}
