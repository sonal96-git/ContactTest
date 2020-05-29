package com.pwc.util;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.foundation.forms.FieldDescription;
import com.day.cq.wcm.foundation.forms.FormsConstants;
import com.day.cq.wcm.foundation.forms.FormsHelper;
import com.pwc.wcm.utils.CommonUtils;
import com.pwc.wcm.utils.I18nPwC;
import com.pwc.wcm.utils.PageService;




public class FormFieldHelper  {
	public static final String ELEMENT_PROPERTY_OPTIONS_LOAD_PATH = "optionsLoadPath";
	public static final String ELEMENT_PROPERTY_OPTIONS = "options";
	public static final String COUNTRY_PATH="/content/pwc/global/referencedata/countries";
	public static final String COUNTRY_PROPERTY="reference-data";
	private static final Logger log = Logger.getLogger(FormFieldHelper.class);

	public static Map<String, String> getOptions(final SlingHttpServletRequest request, final Resource elementResource,String loadPath) {
		final ValueMap properties = ResourceUtil.getValueMap(elementResource);

		String[] options = null;

		if(loadPath==null)
		{
			loadPath = properties.get(ELEMENT_PROPERTY_OPTIONS_LOAD_PATH, "");

		}

		if ( loadPath.length() > 0 ) {
			final Resource rsrc = request.getResourceResolver().getResource(loadPath);

			if(rsrc!=null && rsrc.getPath().contains("country"))
			{

				options =rsrc.adaptTo(String.class).split(",");
			}
			else if ( rsrc != null ) {

				options = rsrc.adaptTo(String[].class);
			}
		}



		// if we don't have values yet, get default values
		if ( options == null ) {
			options = properties.get(ELEMENT_PROPERTY_OPTIONS, String[].class);
		}
		else{
			if(properties.get(ELEMENT_PROPERTY_OPTIONS, String[].class)!=null){
				options=combine(options,properties.get(ELEMENT_PROPERTY_OPTIONS, String[].class));
			}
		}

		if ( options == null ) {
			return null;
		}
		// now split into key value
		final Map<String, String> splitValues = new java.util.LinkedHashMap<String, String>();
		for(int i=0; i<options.length; i++) {
			final String value = options[i].trim();
			if ( value.length() > 0 ) {
				boolean endLoop = true;
				int pos = -1;
				int start = 0;
				do {
					pos = value.indexOf('=', start);
					// check for escaping
					if ( pos > 0 && value.charAt(pos-1) == '\\' ) {
						start = pos +1;
						endLoop = false;
					} else {
						endLoop = true;
					}
				} while ( !endLoop);
				String v, t;
				if ( pos == -1 ) {
					v = value;
					t = value;
				} else {
					v = value.substring(0, pos);
					t = value.substring(pos+1);
				}
				v = v.replace("\\=", "=");
				t = t.replace("\\=", "=");
				splitValues.put(v, t);
			} else {
				splitValues.put("", "");
			}
		}
		if ( splitValues.size() == 0 ) {
			return null;
		}
		return splitValues;
	}
	public static String[] combine(String[] a, String[] b){
		int length = a.length + b.length;
		String[] result = new String[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static Map<String, String> getCountryOptions(final SlingHttpServletRequest request)
	{
		String jcrPath =COUNTRY_PATH;
		ResourceResolver resourceResolver = request.getResourceResolver();
		Map<String, String> list = null;
		Resource res = resourceResolver.getResource(jcrPath);
		if(res!=null){
			ValueMap properties = res.adaptTo(ValueMap.class);
			String[] territoryList = (String[])properties.get("country-code-name");
			list = new LinkedHashMap<String, String>();
			//list=new TreeMap<String, String>();
			for(String t: territoryList){
				String[] country= t.split(":");
				if(country.length>1)
					list.put(country[1], country[0]);
			}
		}
		return list;
	}

	public static Map<String, String> getProvinceOptions(final SlingHttpServletRequest request,String provincePath)
	{
		ResourceResolver resourceResolver = request.getResourceResolver();
		Resource res = resourceResolver.getResource(provincePath);
		ValueMap properties = res.adaptTo(ValueMap.class);
		String[] territoryList = (String[])properties.get(COUNTRY_PROPERTY);
		Map<String, String> list = new TreeMap<String, String>();
		for(String t: territoryList){
			//String[] country= t.split(":");
			//if(country.length>1)
			list.put(t, t);
		}


		return list;
	}

	public static Map<String, String> getTerritoriesWithContacts(SlingHttpServletRequest request)
	{
		
		ResourceResolver resourceResolver = request.getResourceResolver();
		
		String jcrPath=GlobalParam.getContactUsParams("territoryContactPath", resourceResolver);
		boolean isStrategyDomain = CommonUtils.isStrategyDomain(request);
		Map<String, String> territoryMap = null;
		Resource res = resourceResolver.getResource(jcrPath);
		if(res!=null){
			
			territoryMap=new TreeMap<String, String>();
			Iterator<Resource> territories=res.listChildren();
			while(territories.hasNext()){
				Resource territory=territories.next();
				String territoryName = territory.getName();
				if ((isStrategyDomain && StringUtils.isNumeric(territoryName)
						|| (!isStrategyDomain && !StringUtils.isNumeric(territoryName)))) {
					addTerritoryToMap(territoryMap, territory);
				}
			}
			
		}
		return territoryMap;
	}
	
	/**
	 * @param territoryMap
	 * @param territory
	 */
	private static void addTerritoryToMap(Map<String, String> territoryMap, Resource territory) {
		ValueMap prop = territory.adaptTo(ValueMap.class);
		String hcconttacts = prop.get("hcContacts", "");
		String defaultContacts = prop.get("defaultContacts", "");
		String rfpContacts = prop.get("rfpContacts", "");
		
		if (!"".equals(hcconttacts) || !"".equals(defaultContacts) || !"".equals(rfpContacts)) {
			//territoryMap.put(prop.get("territoryName", prop.get("countryName", "")), territory.getName());
			String territoryi18Key = "";
			if(prop.get("countryCode")!=null) {
				territoryi18Key = "TerritoriesandLanguagesOthers_PwCCountry" + prop.get("countryCode").toString().toUpperCase();
			}else{
				territoryi18Key = prop.get("countryName","");
			}
			territoryMap.put(territoryi18Key, prop.get("countryCode").toString().toLowerCase());
		}
	}
	
	
	
	public static Map<String, String> getTerritories(SlingHttpServletRequest request)
	{

		ResourceResolver resourceResolver = request.getResourceResolver();
		boolean isStrategyDomain = CommonUtils.isStrategyDomain(request);
		String jcrPath=GlobalParam.getContactUsParams("territoryContactPath", resourceResolver);
		Map<String, String> list = null;
		Resource res = resourceResolver.getResource(jcrPath);
		if(res!=null){

			list=new TreeMap<String, String>();
			Iterator<Resource> territories=res.listChildren();
			while(territories.hasNext()){
				Resource territory=territories.next();
				String territoryName = territory.getName();
				if ((isStrategyDomain && StringUtils.isNumeric(territoryName)
						|| (!isStrategyDomain && !StringUtils.isNumeric(territoryName)))) {
				ValueMap prop=territory.adaptTo(ValueMap.class);
				list.put(prop.get("territoryName",prop.get("countryName","")),territory.getName());
				}
			}

		}
		return list;
	}

	public static String getClientFieldQualifier(SlingHttpServletRequest request, FieldDescription desc, String suffix) {
		final String formId = FormsHelper.getFormId(request);
		return "document.forms[\"" + StringEscapeUtils.escapeEcmaScript(formId) + "\"]"
				+ ".elements[\"" + desc.getName() + suffix + "\"]";
	}
	public static void writeClientRequiredCheck(final SlingHttpServletRequest request,
												final SlingHttpServletResponse response,
												final FieldDescription desc,Page currentPage)
			throws IOException {
		final String formId = FormsHelper.getFormId(request);
		if ( desc.isRequired() ) {
			final PrintWriter out = response.getWriter();
			final String qualifier = getClientFieldQualifier(request, desc,"");
			String requiredMsg = desc.getRequiredMessage();
			// localize required message
			requiredMsg = getLocalizedMessage(requiredMsg, request,currentPage);
			out.write("if (cq5forms_isEmpty(");
			out.write(qualifier);
			out.write(")) {cq5forms_showMsg('");
			out.write(StringEscapeUtils.escapeEcmaScript(formId));
			out.write("','");
			out.write(StringEscapeUtils.escapeEcmaScript(desc.getName()));
			out.write("','");
			out.write(StringEscapeUtils.escapeEcmaScript(requiredMsg));
			out.write("'); return false; }\n");
		}
	}
	public static String getLocalizedMessage(String msg, SlingHttpServletRequest request,Page currentPage) {
		PageService ps = new PageService();
		String locale = ps.getLocale(request,currentPage);
		
		log.debug(request.getRequestURL() + ">>>>");
		
		String pageName = request.getRequestURL().toString().substring(request.getRequestURL().toString().lastIndexOf("/"));
		log.debug(pageName+ ">>>>");
		String[] selectors = pageName.split("\\.");
		if(selectors.length>=3)
			locale= selectors[1];

		log.debug(locale + ">>>>");
		Locale pageLang = new Locale(locale);
		final I18nPwC i18n = new I18nPwC(request, request.getResourceBundle(pageLang));

		return i18n.getPwC(msg);
	}

	public static void writeClientRegexpText(final SlingHttpServletRequest request,
											 final SlingHttpServletResponse response,
											 final FieldDescription desc,
											 final String regexp,Page currentPage)
			throws IOException {
		final PrintWriter out = response.getWriter();
		final String id = getClientFieldQualifier(request, desc,"");
		out.write("{var obj =");
		out.write(id);
		out.write(";" +
				"if ( cq5forms_isArray(obj)) {" +
				"for(i=0;i<obj.length;i++) {" +
				"if (!cq5forms_regcheck(obj[i].value, ");
		out.write(regexp);
		out.write(")) {" +
				"cq5forms_showMsg('");
		out.write(FormsHelper.getFormId(request));
		out.write("','");
		out.write(desc.getName());
		out.write("','");
		out.write(getConstraintMessage(desc, request,currentPage));
		out.write("', i); return false;}}} else {" +
				"if (!cq5forms_regcheck(obj.value, ");
		out.write(regexp);
		out.write(")) {" +
				"cq5forms_showMsg('");
		out.write(StringEscapeUtils.escapeEcmaScript(FormsHelper.getFormId(request)));
		out.write("','");
		out.write(StringEscapeUtils.escapeEcmaScript(desc.getName()));
		out.write("','");
		out.write(StringEscapeUtils.escapeEcmaScript(getConstraintMessage(desc, request,currentPage)));
		out.write("'); return false;}}}");
	}

	public static String getConstraintMessage(final FieldDescription desc,
											  final SlingHttpServletRequest request,Page currentPage) {
		String msg = desc.getConstraintMessage();
		if ( msg == null ) {
			final ResourceResolver resourceResolver = request.getResourceResolver();
			int index = 0;
			final String[] paths = resourceResolver.getSearchPath();
			while ( index < paths.length && msg == null ) {
				final String scriptPath = paths[index] + request.getResource().getResourceType();
				try {
					final Resource scriptResource = resourceResolver.getResource(scriptPath);
					if ( scriptResource != null ) {
						// check for a default message from constraint
						final ValueMap props = ResourceUtil.getValueMap(scriptResource);
						msg = props.get(FormsConstants.COMPONENT_PROPERTY_CONSTRAINT_MSG, String.class);
					}
				} catch (SlingException se) {
					// we ignore this!
				}
				index++;
			}
			if ( msg == null ) {
				msg = "Field is not valid.";
			}
		}
		// localize msg
		msg = getLocalizedMessage(msg, request,currentPage);
		return msg;
	}
	public static String getLocale(Page currentPage)
	{
		int depth=currentPage.getDepth();
		Page pageWithLocale=currentPage;

		for(int i=0;i<depth-2;i++)
		{
			if(pageWithLocale.getProperties().get("jcr:language",null)!=null)
			{

				break;
			}
			else
			{
				pageWithLocale=pageWithLocale.getParent();

			}

		}
		//Modified By Rui Jiang @ 07/10/2015, To fixed the contact us link problem.
		return(pageWithLocale.getProperties().get("jcr:language","en_gx"));
	}


}