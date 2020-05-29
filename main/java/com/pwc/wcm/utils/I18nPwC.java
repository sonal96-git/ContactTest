package com.pwc.wcm.utils;

import java.util.Locale;

/*************************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
*  Copyright 2011 Adobe Systems Incorporated
*  All Rights Reserved.
*
* NOTICE:  All information contained herein is, and remains
* the property of Adobe Systems Incorporated and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Adobe Systems Incorporated and its
* suppliers and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Adobe Systems Incorporated.
**************************************************************************/

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
* PwC variation of I18n class. In addition to OOTB i18n functionality, additional method getPwC() is added
* Following should be noted:
*       Added new constructor that must be called before getPwc() is called
*       OOTB constuctors are made protected - OOTB i18n should be used as needed 
*       Default language is set to en_GX 
* 
* Externalizes strings using {@link ResourceBundle} dictionaries and simulatenously
* acts as marker for the string extraction process. In addition to plain string
* translation, it allows for translation comments and placeholders
* (<code>{0}</code>, <code>{1}</code>, etc.). ResourceBundles will be taken from
* a {@link SlingHttpServletRequest} and it's default locale (standard case) or
* can be passed explicitly.
*
* <p>
* There are two kinds of methods, which are identical in their implementation,
* but have a different signature to indicate the proper intention to the
* string extraction tool:
* <ul>
*     <li>
*         <code>get()</code>: for use with literal strings, also acts as marker for the string extraction
*         <p>
*             Example: <pre>
*i18n.get("Label");
   *         </pre>
*         </p>
*         <p>
*             Note that this below is incorrect - while it works programmatically, it prevents
*             the string from being extracted if the literal is not part of the method call directly:
*             <pre>
*String title = "Label";
*i18n.get(title);
*         </pre>
*         </p>
*     </li>
*     <li>
*         <code>getVar()</code>: for use with string variables where the actual string is read
*         and extracted from another location (e.g. the JCR)
*         <p>
*         Example:
*         <pre>
*String var = properties.get("jcr:title", String.class);
*i18n.getVar(var);
*         </pre>
*         When using this method, make sure the string gets extracted properly from the other
*         location (i18n engineers will know).
*         </p>
*     </li>
* </ul>
*
* </p>
*
* <p>
* The way to invoke this class from JSP is as follows:
* <ol>
* <li>
* Get locale associated with page using following lines:
*               Locale pageLang = currentPage.getLanguage(false);
*               final I18nPwC i18nPwC = new I18nPwC(slingRequest, slingRequest.getResourceBundle(pageLang));    
* </li>
* <li>
* Invoke method to translate 
*               String myProfileString = "My Profile";
*               String pwcTranslationNewString = i18nPwC.getPwC(myProfileString);
* </li>
* </ol>
*/
@Component(immediate = true, service = { I18nPwC.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= I18nPwC Service" })
@Designate(ocd = I18nPwC.Config.class)
public class I18nPwC {

   private final HttpServletRequest request;
   private final ResourceBundle resourceBundle;
   private SlingHttpServletRequest slingRequest;
  
   /*@Property(name = "defaultLanguage", label = "Default language", value = "en_gx",
           description = "Default Language tranlations used is original translation is not found")
   private static final String  DEFAULTLANGUAGE     = "defaultLanguage";
   private String               defaultLanguage   = "";*/
   
   @ObjectClassDefinition(name = "I18nPwC Service", description = "")
   @interface Config {
       @AttributeDefinition(name = "Default language", 
                           description = "Default Language tranlations used is original translation is not found",
                           type = AttributeType.STRING)
       public String defaultLanguage() default "en_gx";
   }

    public I18nPwC(){
        this.request = null;
        this.resourceBundle = null;
        this.slingRequest = null;
    }

   // Hide constructor - use OOTB i18n when needed
   public I18nPwC(final HttpServletRequest request) {
       this.request = request;
       this.resourceBundle = null;
       this.slingRequest = null;
   }

   // Hide constructor - use OOTB i18n when needed  
   public I18nPwC(final ResourceBundle resourceBundle) {
       this.resourceBundle = resourceBundle;
       this.request = null;
       this.slingRequest = null;
   }

   /**
    * Constructor to be called if getPwc() is invoked  
    * @param slingRequest       slingRequest used to instantiate "default" resource bundle
    * @param resourceBundle     resource bundle associated with language already set
    */
   public I18nPwC(SlingHttpServletRequest slingRequest, final ResourceBundle resourceBundle) {
       this.resourceBundle = resourceBundle;
       this.request = null;
       this.slingRequest = slingRequest;
       
   }

    /**
     * Constructor to be called if getPwc() is invoked
     * @param slingRequest      slingRequest used to instantiate "default" resource bundle
     * @param resource  requested resource from the page
     */
   public I18nPwC(SlingHttpServletRequest slingRequest, Resource resource) {
       this.request = null;
       this.slingRequest = slingRequest;
       PageService ps = new PageService();
       PageManager pageManager = this.slingRequest.getResourceResolver().adaptTo(PageManager.class);
       Page page  = pageManager.getContainingPage(resource);
       String locale = ps.getLocale(this.slingRequest, page);
       Locale pageLang = new Locale(locale);
       this.resourceBundle = this.slingRequest.getResourceBundle(pageLang);
   }

     
   
   private ResourceBundle getResourceBundle() {
       if ( this.request != null ) {
           return getResourceBundle(this.request);
       }
       return this.resourceBundle;
   }

   /** Request attribute holding the resource bundle. */
   private static String BUNDLE_REQ_ATTR = "org.apache.sling.i18n.resourcebundle";

   private static ResourceBundle getResourceBundle(final HttpServletRequest req) {
       if ( req instanceof SlingHttpServletRequest ) {
           return ((SlingHttpServletRequest)req).getResourceBundle(null);
       }
       return (ResourceBundle) req.getAttribute(BUNDLE_REQ_ATTR);
   }

   // ----------------------------------------< request-based >

   /**
    * Translates the given text. Will return the original text if no
    * translation was found.
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    */
   public String get(final String text) {
       return get(this.getResourceBundle(), text, null, (Object[]) null);
   }

   
   
   /**
    * PwC variation of get() method - note that class must be instantiated using constructor that 
    * accepts slingRequest and resourceBundle parameters 
    * @param text       text to be translated
    * @return           string representing translated text 
    */
    public String getPwC(final String text) {
        Page currentPage = slingRequest.adaptTo(Page.class);
           // Get initial translation using resource bundle being passed in
           String tempTranslation = get(this.getResourceBundle(), text, null, (Object[]) null);
          
           // Return empty if no translation is found for locale sent in 
           if (tempTranslation.isEmpty()) {
                   // Need to set locale for "default language" and invoke translation
                   ResourceBundle bundle = null;
                   Locale myLocale = new Locale("en_gx");
                   // Get resource bundle for default language and invoke translation call again 
                   try {
                           bundle = slingRequest.getResourceBundle(myLocale);
                           tempTranslation = get(bundle, text, null, (Object[]) null);
                           return tempTranslation;
                   }
                   // In case of exception, return back an original text 
                   catch (Exception e ){
                           e.printStackTrace();
                           return text;
                   }   
           }
           else {
                   return tempTranslation;
           }
           
   }
   
   
   
   /**
    * Translates the given text considering a special comment for translators.
    * Will return the original text if no translation was found.
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    */
   public String get(final String text, final String comment) {
       return get(this.getResourceBundle(), text, comment, (Object[]) null);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    *            (can be <code>null</code>).
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public String get(final String text, final String comment, final Object... args) {
       return get(this.getResourceBundle(), text, comment, args);
   }

   /**
    * Translates the given text. Will return the original text if no
    * translation was found.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate
    */
   public String getVar(final String text) {
       return getVar(this.getResourceBundle(), text, null);
   }

   /**
    * <b>Note: this variant is only for rare cases.</b>
    *
    * <p>
    * Translates the given text considering a special comment for translators.
    * Will return the original text if no translation was found.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>).
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    */
   public String getVar(final String text, final String comment) {
       return getVar(this.getResourceBundle(), text, comment);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>):
    *
    * <pre>i18n.getVar(text, null, variable1, variable2);</pre>
    *
    * <p>
    * If this object was created via {@link #I18n(HttpServletRequest)},
    * the default language resource bundle of the sling request will be used,
    * otherwise it uses the underlying {@link ResourceBundle} provided in
    * {@link #I18n(ResourceBundle)}.
    * </p>
    *
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public String getVar(final String text, final String comment, Object... args) {
       return getVar(this.getResourceBundle(), text, comment, args);
   }

   // ----------------------------------------< static, request-based >

   /**
    * Translates the given text. Will return the original text if no
    * translation was found.
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingRequest.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    */
   public static String get(final HttpServletRequest request, final String text) {
       return get(getResourceBundle(request), text, null, (Object[]) null);
   }

   /**
    * Translates the given text considering a special comment for translators.
    * Will return the original text if no translation was found.
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingRequest.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    */
   public static String get(final HttpServletRequest request, final String text, final String comment) {
       return get(getResourceBundle(request), text, comment, (Object[]) null);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingRequest.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    *            (can be <code>null</code>).
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public static String get(final HttpServletRequest request, final String text, final String comment, Object... args) {
       return get(getResourceBundle(request), text, comment, args);
   }

   /**
    * Translates the specified text into the current language.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingRequest.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate
    */
   public static String getVar(final HttpServletRequest request, final String text) {
       return getVar(getResourceBundle(request), text, null);
   }

   /**
    * <b>Note: this variant is only for rare cases.</b>
    *
    * <p>
    * Translates the specified text considering a special comment for translators
    * into the current language.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>).
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingReque/content/pwc/ee/en/home/demo/jcr:content/footerPar/secondary_navigation_1721018207st.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    */
   public static String getVar(final HttpServletRequest request, final String text, final String comment) {
       return getVar(getResourceBundle(request), text, comment);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>):
    *
    * <pre>I18n.getVar(request, text, null, variable1, variable2);</pre>
    *
    * <p>
    * Uses the default language resource bundle of the sling request, ie. using
    * {@link SlingHttpServletRequest#getResourceBundle(java.util.Locale)
    * slingRequest.getResourceBundle(null)}.
    * </p>
    *
    * @param request
    *            The request object of which the default language resource
    *            bundle will be taken from as translation source
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public static String getVar(final HttpServletRequest request, final String text, final String comment, Object... args) {
       return getVar(getResourceBundle(request), text, comment, args);
   }

   // ----------------------------------------< static, resource-bundle-based >

   /**
    * Translates the given text. Will return the original text if no
    * translation was found.
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    */
   public static String get(final ResourceBundle resourceBundle, final String text) {
       return get(resourceBundle, text, null, (Object[]) null);
   }

   /**
    * Translates the given text considering a special comment for translators.
    * Will return the original text if no translation was found.
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    */
   public static String get(final ResourceBundle resourceBundle, final String text, final String comment) {
       return get(resourceBundle, text, comment, (Object[]) null);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate - as string literal <code>"My Label"</code>
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used - as string literal <code>"Action button label"</code>
    *            (can be <code>null</code>).
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public static String get(final ResourceBundle resourceBundle, final String text, final String comment, final Object... args) {
       if (text == null) {      
           return text;
       }
       if (resourceBundle == null) {
           // if the text comes with args and the resource bundle is null,
           // the text with the placeholders replaced by the args should be returned
           return patchText(text, args);

       }

       String msg;
       // whether the translation is equal to the original
       boolean equals = false;
       if (comment != null && comment.length() > 0) {
           // if the original text in the source code comes with a comment,
           // it will be included in the key for the sling messages because
           // different comments indicate different meanings hence a need for
           // possibly different translations of the same text depending on
           // its context
           final String key = text + " ((" + comment + "))";

           msg = resourceBundle.getString(key);

           // if the translation is the same as the key, it means it wasn't
           // translated by the Sling JCR resource bundle and just returned
           // as it is; in this case we have to make sure to strip the comment
           if (key.equals(msg)) {
               equals = true;
               msg = text;
           }
       } else {
           // Translation call 
           msg = resourceBundle.getString(text);           
           if (text.equals(msg)) {
               equals = true;
           }
       }

       // replace placeholders ala "{0} {1}" with the given arguments
       try {
           return patchText(msg, args);
       } catch (IllegalArgumentException e) {
           // invalid format for translation
           if (equals) {
               // original string => throw for developer to fix
               throw e;
           } else {
               // translation => avoid exception, use unformatted string
               return msg;
           }
       }
   }

   /**
    * Translates the specified text into the current language.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate
    */
   public static String getVar(final ResourceBundle resourceBundle, final String text) {
       return get(resourceBundle, text, null);
   }

   /**
    * <b>Note: this variant is only for rare cases.</b>
    *
    * <p>
    * Translates the specified text considering a special comment for translators
    * into the current language.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>).
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    */
   public static String getVar(final ResourceBundle resourceBundle, final String text, final String comment) {
       return get(resourceBundle, text, comment);
   }

   /**
    * Translates the given text considering a special comment for translators
    * and replaces placeholders (<code>{0}</code>, <code>{1}</code>, etc.) with
    * the given arguments. Will return the original text if no translation was
    * found.
    *
    * <p>
    * Use this variant to translate <b>string variables</b> which have a value read from
    * somewhere else, such as the JCR repository. In this case the getVar() signature
    * marks it clearly that the extraction tool should not expect a literal string
    * here. When adding this, make sure the string gets extracted properly from
    * the JCR repository for example (i18n engineers will know).
    *
    * <p>
    * <b>Warning:</b> It is very important that the translation <b>comment</b> passed
    * as second argument is present in the dictionary (unless it's <code>null</code>).
    * Since in the <code>getVar()</code> case the strings are read or extracted from a
    * different location, this comment must also be present wherever the strings
    * are extracted from, and read from there! Because of this, there will usually be
    * no comment with getVar() (<code>null</code>):
    *
    * <pre>I18n.getVar(bundle, text, null, variable1, variable2);</pre>
    *
    * <p>
    * Uses the given {@link ResourceBundle} as translation source.
    * </p>
    *
    * @param resourceBundle
    *            The resourceBundle used as translation source
    * @param text
    *            The text to translate
    * @param comment
    *            A comment for translators to specify the context in which the
    *            text is used (can be <code>null</code>). <b>Warning:</b> must also be
    *            present in the exact same form where the actual string is read from, which is
    *            usually impractical, so by default skip with <code>null</code>.
    * @param args
    *            A varargs list that (as Strings) will be used to replace
    *            numbered placeholders in the text (eg. (<code>{0}</code>,
    *            <code>{1}</code>, etc.)
    */
   public static String getVar(final ResourceBundle resourceBundle, final String text, final String comment, Object... args) {
       return get(resourceBundle, text, comment, args);
   }

   /**
    * Simple variant of MessageFormat that replaces "{n}" with the
    * n-th var arg object (converted to string).
    *
    * Same implementation as for javascript in Granite.Util.patchText().
    */
   public static String patchText(String text, Object... args) {
       if (text == null || args == null) {
           return text;
       }

       for (int i = 0; i < args.length; i++) {
           Object o = args[i];
           if (o != null) {
               text = text.replace("{" + i + "}", o.toString());
           }
       }
       return text;
   }
}
