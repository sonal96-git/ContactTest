package com.pwc.spellchecker.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.spellchecker.SpellCheckException;
import com.day.cq.spellchecker.SpellCheckService;
import com.day.cq.spellchecker.TextCheckResult;
import com.day.cq.spellchecker.WordCheckResult;
import com.day.cq.wcm.api.LanguageManager;

@Component(service = Servlet.class,
			property = {
				"sling.servlet.methods=" + HttpConstants.METHOD_GET,
				"sling.servlet.methods=" + HttpConstants.METHOD_POST,
				"sling.servlet.resourceTypes=" + "sling/servlet/default",
				"sling.servlet.selectors=" + "pwcspellcheck",
				"sling.servlet.extensions=" + "json"
			})
public class PwCSpellChekerServlet extends SlingAllMethodsServlet {

	public static final String MODE = "mode";
	public static final String WORD = "word";
	public static final String TEXT = "text";
	public static final String HTML = "html";
	public static final String LANGUAGE = "language";
	public static final String CONTENT_PATH = "cp";
	public static final String DEFAULT_LANGUAGE = "en_us";
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Reference
	private SpellCheckService spellchecker;
	
	@Reference
	private LanguageManager languageManager;

	private JSONObject getLanguage(SlingHttpServletRequest req) throws JSONException {
		JSONObject jsonLocale = new JSONObject();
		String language = req.getParameter("language");
		if (language == null) {
			String contentPath = req.getParameter("cp");
			if (contentPath != null) {
				this.log.debug(	"Trying to retrieve language for resource '{}'.",contentPath);
				ResourceResolver resolver = req.getResourceResolver();
				Resource contentResource = resolver.getResource(contentPath);
				if (contentResource == null) {
					contentResource = new NonExistingResource(resolver,	contentPath);
				}
				Locale locale = this.languageManager.getLanguage(contentResource, true);
				if (locale != null) {
					String rscLanguage = locale.getLanguage();
					String country = this.languageManager.getIsoCountry(locale);
					
					if ((country == null) || (country.length() < 2)) {
						country = rscLanguage;
					}
					if (this.log.isDebugEnabled()) {
						this.log.debug(	"Language settings for resource '{}': {}; country: {}",	new Object[] { contentPath, rscLanguage,country });
					}
					language = rscLanguage.toLowerCase() + "_"	+ country.toLowerCase();
					jsonLocale.put("country",locale.getDisplayCountry());
					jsonLocale.put("language",locale.getDisplayLanguage());
					jsonLocale.put("locale",language);
					
				} else {
					this.log.warn("Unable to determine language for content at {}. using default",contentPath);
				}
			}
		}
		if (language == null) {
			jsonLocale.put("country","Global");
			jsonLocale.put("language","English");
			jsonLocale.put("locale",DEFAULT_LANGUAGE);
		}
		this.log.debug("Spellchecking language is: '{}'", language);
		return jsonLocale;
	}

	@Override
	protected void doGet(SlingHttpServletRequest req,	SlingHttpServletResponse resp) throws ServletException, IOException {
		try {
			check(req, resp, false, !isDeprecatedRequest(req));
		} catch (JSONException e) {			
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(SlingHttpServletRequest req, 	SlingHttpServletResponse resp) throws ServletException, IOException {
		boolean isDeprecatedReq = isDeprecatedRequest(req);
		try {
			check(req, resp, !isDeprecatedReq, !isDeprecatedReq);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private boolean isDeprecatedRequest(SlingHttpServletRequest req) {
		return "/bin/spellcheck".equals(req.getPathInfo());
	}

	private void check(SlingHttpServletRequest req,SlingHttpServletResponse resp, boolean createHtmlWrapper,	boolean sparse) throws ServletException, IOException, JSONException {
		String mode = req.getParameter("mode");
		if (mode == null) {
			throw new ServletException("Missing 'mode' parameter.");
		}
		if (mode.equals("word")) {
			checkWord(req, resp, createHtmlWrapper);
		} else if (mode.equals("text")) {
			checkText(req, resp, createHtmlWrapper, sparse);
		} else {
			throw new ServletException("Invalid 'mode' parameter: " + mode);
		}
	}

	private void checkWord(SlingHttpServletRequest req, SlingHttpServletResponse resp, boolean createHTMLWrapper)	throws IOException, JSONException {
		resp.setContentType(createHTMLWrapper ? "text/html"	: "application/json");
		resp.setCharacterEncoding("utf-8");
		try {
			String word = req.getParameter("word");
			if (word == null) {
				throw new SpellCheckException("No word specified.");
			}
			WordCheckResult wordResult = this.spellchecker.checkWord(word,getLanguage(req).getString("language"), null);

			String json = wordResult.toJsonString();
			PrintWriter out = resp.getWriter();
			if (createHTMLWrapper) {
				out.print("<div id=\"json\">");
				out.print(StringEscapeUtils.escapeHtml(json));
				out.println("</div>");
			} else {
				out.print(json);
			}
		} catch (SpellCheckException se) {
			resp.sendError(500, se.getMessage());
		}
	}

	private void checkText(SlingHttpServletRequest req, SlingHttpServletResponse resp, boolean createHTMLWrapper,	boolean sparse) throws IOException, JSONException {
		resp.setContentType(createHTMLWrapper ? "text/html": "application/json");
		resp.setCharacterEncoding("utf-8");
		StringBuffer language = new StringBuffer();
		PrintWriter out = resp.getWriter();
		try {			
			JSONObject jsonLocale = getLanguage(req);
			if(!jsonLocale.getString("country").equals("")){ language.append(jsonLocale.getString("country")).append("-");};
			language.append(jsonLocale.getString("language"));
			language.append(" (").append(jsonLocale.getString("locale")).append(")");
			
			String text = req.getParameter("text");
			if (text == null) {
				throw new SpellCheckException("No text specified.");
			}
			boolean isHtml = "true".equals(req.getParameter("html"));
			TextCheckResult textResult = this.spellchecker.checkText(text,jsonLocale.getString("language"), isHtml, null);

			String json = textResult.toJsonString(sparse);
			if (createHTMLWrapper) {
				out.print("<div id=\"json\">");
				out.print(StringEscapeUtils.escapeHtml(json));
				out.println("</div>");
			} else {
				out.print(json);
			}
		} catch (SpellCheckException se) {
			JSONStringer json = new JSONStringer();
			out.print("<div id=\"json\">");
			json.object();
			json.key("error");
			json.value(true);
			json.key("language");
			json.value(language);
			json.endObject();
			out.print(json.toString());
			out.println("</div>");
		} catch (Exception e) {
			resp.sendError(500, e.getMessage());
		}
	}
}
