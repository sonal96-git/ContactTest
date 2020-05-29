package com.pwc.wcm.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.AdminResourceResolver;
import com.pwc.wcm.services.LocaleService;

@Component(immediate = true, service = { LocaleService.class }, enabled = true)
public class LocaleServiceImpl implements LocaleService {

	@Reference
	AdminResourceResolver adminResourceResolver;
	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleService.class);

	public static final String LOCALE_SEPARATOR = "_";
	public static final String LANG_CODE_SEPERATOR= "-";
	public static final String LANGUAGE_ORIGINAL_PATH ="/apps/wcm/core/resources/languages/";
	public static final String LOCALE_LANGUAGE_ORIGINAL ="languageOriginal";

	@Override
	public  String getLanguageOriginalFromLanguageCode(String languageCode){
		String languageOriginal=null;
		LOGGER.info("fetching languageOriginal value for languageCode: "+languageCode);
		if(languageCode!=null && languageCode.contains(LANG_CODE_SEPERATOR)){
			//get locale from the lang code by replacing "-" with "_"
			String locale=languageCode.replace(LANG_CODE_SEPERATOR,LOCALE_SEPARATOR);
			languageOriginal = getLanguageOriginalFromLocale(locale);
		}
		return languageOriginal;
	}

	@Override
	public String getLanguageOriginalFromLocale(String locale) {
		String languageOriginal = StringUtils.EMPTY;
		LOGGER.info("fetching languageOriginal value for locale: " + locale);
		final ResourceResolver resourceResolver = adminResourceResolver.getAdminResourceResolver();
		try {
			final Resource resource = resourceResolver.getResource(LANGUAGE_ORIGINAL_PATH + locale);
			ValueMap properties = ResourceUtil.getValueMap(resource);
			// set locale as languageOriginal if languageOriginal property is not present in
			// the node.
			languageOriginal = properties.get(LOCALE_LANGUAGE_ORIGINAL, locale);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive())
				resourceResolver.close();
		}
		return languageOriginal;
	}

}
