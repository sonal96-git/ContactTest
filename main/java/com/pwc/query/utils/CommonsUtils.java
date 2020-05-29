package com.pwc.query.utils;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pwc.collections.OsgiCollectionsConfiguration;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.enums.AssetProps;
import com.pwc.query.enums.CollectionProps;
import com.pwc.query.enums.FileTypes;
import com.pwc.query.enums.QueryLabels;
import com.pwc.wcm.utils.I18nPwC;

public class CommonsUtils {

    private static final String EMPTY_TAGS = "{}";

    private static final String PATTERN = "[^/a-zA-Z0-9]|/";
    private static final String DEFAULT_DATE_FORMAT = "short";


    public static String[] getTagBackLinks(ControllerBean controllerBean,String tagID) {

        TagManager tagManager = controllerBean.getResourceResolver().adaptTo(TagManager.class);
        Tag tag = tagManager.resolve(tagID);

        if(tag == null) return new String[0];

        String backLinks = tag.getGQLSearchExpression(QueryLabels.TAGS_BACKLINKS.toString());

        if(StringUtils.isBlank(backLinks)) return new String[0];

        String[] backLinksArray  = backLinks.replaceAll("\"","").replaceAll("cq:backlinks:","").split("OR");

        return backLinksArray;
    }

    public static  boolean isEquivalentMatch(String s1, String s2){

        String string1 = removeDiacriticalMarks(s1).toLowerCase().replaceAll("'","").replaceAll("\\n", " ");
        String string2 = removeDiacriticalMarks(s2).toLowerCase().replaceAll("'","").replaceAll("\\n", " ");

        return string2.matches(".*"+string1+".*");
    }

    public static void setFiltersControllerBean(List<String>  filterMenu, ControllerBean contrBean) throws JSONException ,IOException {

        ValueMap componentProperties = contrBean.getProperties();

        GregorianCalendar createdDate = componentProperties.get(CollectionProps.CREATED_DATE) != null ?
                (GregorianCalendar) componentProperties.get(CollectionProps.CREATED_DATE) : null;

        String seconds = String.valueOf(createdDate.get(Calendar.SECOND));
        String milliSec = String.valueOf(createdDate.get(Calendar.MILLISECOND));

        String deepLinkId =  seconds+milliSec;

        SlingHttpServletRequest request = contrBean.getRequest();

        String tags = StringUtils.isNotBlank(request.getParameter("tags"+deepLinkId)) ? request.getParameter("tags"+deepLinkId) : EMPTY_TAGS;
        String search = StringUtils.isNotBlank(request.getParameter("search"+deepLinkId)) ?
                (StringUtils.isNotBlank(request.getParameter("_charset_")) && request.getParameter("_charset_").equals("UTF-8") ?
                request.getParameter("search"+deepLinkId) : new String(request.getParameter("search"+deepLinkId).getBytes("iso-8859-1"),"UTF-8")) :null;

        if(tags.equals(EMPTY_TAGS)) {
            contrBean.setSearchText(search);
        } else {
            contrBean.setFilters(getTagsList(tags,filterMenu,contrBean));
        }

    }

    public static List<List<String>> getTagsList(String filters,List<String>  filterMenu,ControllerBean contrBean) throws JSONException {

        JSONObject jsonObj = new JSONObject(filters);

        List<String> tagsList =  new ArrayList<String>();
        List<List<String>> menuList = new ArrayList<List<String>>();

        if( jsonObj == null || jsonObj.length() == 0 ) return menuList;
        for (int i=0; i < jsonObj.getJSONArray(jsonObj.names().getString(0)).length(); i++) {

            tagsList = new ArrayList<String>();

            JSONArray array = ((JSONArray)jsonObj.getJSONArray(jsonObj.names().getString(0)).get(i));
            for (int j=0; j < array.length() ; j++) {
                String tag = array.get(j).toString();
                if(isValidTagFaceted(tag,filterMenu,contrBean)) tagsList.add(tag);
            }
            menuList.add(tagsList);
        }


        return menuList;
    }
    public static List<List<String>> getTagsList(String filters) throws JSONException {

        JSONObject jsonObj = new JSONObject(filters);

        List<String> tagsList =  new ArrayList<String>();
        List<List<String>> menuList = new ArrayList<List<String>>();

        if( jsonObj == null || jsonObj.length() == 0 ) return menuList;
        for (int i=0; i < jsonObj.getJSONArray(jsonObj.names().getString(0)).length(); i++) {

            tagsList = new ArrayList<String>();

            JSONArray array = ((JSONArray)jsonObj.getJSONArray(jsonObj.names().getString(0)).get(i));
            for (int j=0; j < array.length() ; j++) {
                String tag = array.get(j).toString();
                tagsList.add(tag);
            }
            menuList.add(tagsList);
        }

        return menuList;
    }

    public static boolean isValidTagFaceted(String tagString, List<String>   filterMenu,ControllerBean contrBean) throws JSONException{

        boolean isValid = false;
        TagManager tagManager = contrBean.getResourceResolver().adaptTo(TagManager.class);

        List<String> elementBL = Arrays.asList(CommonsUtils.getTagBackLinks(contrBean,tagString));
        elementBL = elementBL.stream().map(String :: trim).collect(Collectors.toList());
        
		if(elementBL.isEmpty()) return false;
		
        for(String menu : filterMenu){

            JSONObject jsonObj = new JSONObject(menu);
            JSONArray jsonArray =  jsonObj.getJSONArray("tags");
            boolean includeSubTags =  jsonObj.getJSONArray("includeSubTags").length()> 0 ?
                    jsonObj.getJSONArray("includeSubTags").getBoolean(0) : false;

            for (int i =0;i < jsonArray.length();i++) {

                String filterTag = jsonArray.getString(i);
                boolean isMatch = elementBL.stream().anyMatch(elem -> filterTag.equals(elem));
                
                if(isMatch) {
                    isValid = true;
                    break;

                } else {

                    if(!isValid && includeSubTags ) {

                        Tag tag = tagManager.resolve(filterTag);
                        
                        if(tag == null) continue;
                        
                        Iterator<Tag> tagIte =  tag.listAllSubTags();
                        
                        while( tagIte.hasNext() ) {
                            String tagId =  tagIte.next().getTagID();
                            boolean isMatchSub = elementBL.stream().anyMatch(elem -> tagId.equals(elem));
                            if(isMatchSub) {
                                isValid = true;
                                break;
                            }
                        }

                    }
                }
            }

        }
        return isValid;
    }
    public static boolean isValidTagCollection(String tagString, List<String>  tags,ControllerBean contrBean) throws JSONException{

        boolean isValid = false;
        ValueMap componentProperties = contrBean.getProperties();
        TagManager tagManager = contrBean.getResourceResolver().adaptTo(TagManager.class);

        boolean includeSubTags = componentProperties.get(CollectionProps.INCLUDE_SUB_TAGS) != null ?
                componentProperties.get(CollectionProps.INCLUDE_SUB_TAGS).equals("true") : false;
                
        List<String> elementBL = Arrays.asList(CommonsUtils.getTagBackLinks(contrBean,tagString));
        elementBL = elementBL.stream().map(String :: trim).collect(Collectors.toList());
        
        
		if(elementBL.isEmpty()) return false;

        for (String tagS:tags) {

        	boolean isMatch = elementBL.stream().anyMatch(elem -> tagS.equals(elem));
            if(isMatch) {
                isValid = true;
                break;

            } else {

                if(!isValid && includeSubTags ) {

                    Tag tag = tagManager.resolve(tagS);
                    Iterator<Tag> tagIte =  tag.listAllSubTags();
                    while( tagIte.hasNext() ) {
                    	
                        String tagId =  tagIte.next().getTagID();
                        boolean isMatchSub = elementBL.stream().anyMatch(elem -> tagId.equals(elem));
                        if(isMatchSub) {
                            isValid = true;
                            break;
                        }
                    }

                }
            }
        }
        return isValid;
    }

    public static String getFirstTag(List<List<String>> tags){
        return tags == null ? "" : (tags.size() > 0 ? tags.get(0).get(0) : "");
    }

    //return true if types is one of the fileTypes selected
    public static boolean isFileTypeSelected (Object fileTypes, String type) {

        boolean isSelected=false;
        String[] fileArray ;

        if(fileTypes instanceof String) {
            fileArray = new String[1];
            fileArray[0]= (String) fileTypes;
        } else {
            fileArray = (String[]) fileTypes;
        }
        for(int i=0; i<fileArray.length;i++){
            if(fileArray[i].equals(type)){
                isSelected = true;
                break;
            }
        }

        return isSelected;
    }

    public static int getLimit(ControllerBean contrBean){

        ValueMap compProp = contrBean.getProperties();
        OsgiCollectionsConfiguration pwcOsgiConf = new OsgiCollectionsConfiguration();

        String limit_default = compProp.get(CollectionProps.LIMIT_BYPASS)!=null ?
                compProp.get(CollectionProps.LIMIT_BYPASS).toString():
                String.valueOf(pwcOsgiConf.getCollectionLimit());

        String limit = compProp.get(CollectionProps.LIMIT)!=null ?
                compProp.get(CollectionProps.LIMIT).toString():limit_default;

        return Integer.parseInt(limit);

    }

    public static boolean isValidPage(Resource resource, ControllerBean contrBean) throws RepositoryException{

        Page page = contrBean.getPageManager().getContainingPage(resource.getPath());
        return page.isValid() && isFileTypeSelected(contrBean.getProperties().get(CollectionProps.FILE_TYPE), FileTypes.HTML.toString());
    }

    public static boolean isValidAsset(Node node, ControllerBean contrBean) throws RepositoryException{

        boolean isValid = false;

        ValueMap prop = contrBean.getProperties();

        String dcFormat = node.getProperty(AssetProps.FORMAT.toString()).getValue().toString();


        contrBean.getPwcLogger().logMessage(new Date()+" START isValidAsset dc:format "+dcFormat);
        if(isFileTypeSelected(prop.get(CollectionProps.FILE_TYPE), FileTypes.PDF.toString()) &&
                dcFormat.equals(AssetProps.FORMAT_PDF.toString())) {

            isValid = compareDates(node);

        } else if(isFileTypeSelected(prop.get(CollectionProps.FILE_TYPE), FileTypes.VIDEO.toString()) &&
                dcFormat.equals(AssetProps.FORMAT_VIDEO.toString())) {

            isValid = compareDates(node);
        }
        contrBean.getPwcLogger().logMessage(new Date()+" END isValidAsset . Result= "+isValid);
        return isValid;
    }

    private static boolean compareDates(Node node) throws RepositoryException{

        boolean isValid = false;

        Date expire = node.hasProperty(AssetProps.EXPIRATION_DATE.toString()) ?
                node.getProperty(AssetProps.EXPIRATION_DATE.toString()).getValue().getDate().getTime() : null;

        Date onTime = node.getParent().hasProperty(AssetProps.ON_TIME.toString()) ?
                node.getParent().getProperty(AssetProps.ON_TIME.toString()).getValue().getDate().getTime() : null;

        Date offTime = node.getParent().hasProperty(AssetProps.OFF_TIME.toString()) ?
                node.getParent().getProperty(AssetProps.OFF_TIME.toString()).getValue().getDate().getTime(): null;

        Date now = new Date();

        if (expire != null && now.after(expire)) {

            isValid = false;

        } else {

            boolean isOnTime  = onTime  != null ? now.after(onTime) : true;
            isValid = isOnTime ? (offTime != null ? now.before(offTime) : true) : false;
        }

        return isValid;
    }

    public static String toJSON(List<?> collection, long numberHits, String selectedTags,String filterTags) throws JSONException {

        Gson parser = new GsonBuilder().create();
        String elements = parser.toJson(collection);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("elements",elements);
        jsonObject.put("numberHits",numberHits);
        jsonObject.put("filterTags",filterTags);
        jsonObject.put("selectedTags",selectedTags);

        return jsonObject.toString();
    }

    public static String getComponentId(Node currentNode) throws RepositoryException {

        String currentNodePath = currentNode.getPath();
        String componentId  = currentNodePath.replaceAll(PATTERN, "-").toLowerCase();

        return componentId;
    }

    // Returns the date of the current object formatted with the format passed as parameter
    public static String formattedDate(ControllerBean controllerBean ,String unformattedDate) throws ParseException {

        if (StringUtils.isBlank(unformattedDate)) return "";

        I18nPwC i18nPwC = controllerBean.getI18nPwC();

        boolean displayAsPage = controllerBean.getCompName().equals("Faceted") ? true :
                ( controllerBean.getProperties().get(CollectionProps.DISPLAY_AS) != null ?
                controllerBean.getProperties().get(CollectionProps.DISPLAY_AS).equals("page") : false) ;

        String dateFormat = ObjectUtils.firstNonNull( controllerBean.getProperties().get(CollectionProps.DATE_FORMAT),DEFAULT_DATE_FORMAT).toString();
        String groupBy = ObjectUtils.firstNonNull( controllerBean.getProperties().get(CollectionProps.GROUP_BY),"monthyear").toString();

        String format = displayAsPage ? dateFormat : groupBy ;

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date uDate = df.parse(unformattedDate);
        GregorianCalendar date = new GregorianCalendar();
        date.setTime(uDate);

        Integer day_int = date.get(Calendar.DAY_OF_MONTH);
        String day 		= day_int < 10 ? "0" + day_int.toString() : day_int.toString();

        Locale dateLocale 			= new Locale("en");
        String monthName 			= new DateFormatSymbols(dateLocale).getMonths()[date.get(Calendar.MONTH)];
        String i18nMonthName 		= i18nPwC.getPwC("GeneralTranslationsOthers_PwCMonths" + monthName);
        String i18nShortMonthName 	= i18nPwC.getPwC("GeneralTranslationsOthers_PwCMonthShort" + monthName);
        Integer fullYear 			= date.get(Calendar.YEAR);
        Integer month;
        SimpleDateFormat sdf;

        switch (format) {
            case "medium" :
                return day + " " + i18nShortMonthName + " " + fullYear;
            case "mediumusa" :
                return i18nShortMonthName + " " + day + " " + fullYear;
            case "mediumusacomma" :
                return i18nShortMonthName + " " + day + ", " + fullYear;
            case "long" :
                return i18nMonthName + " " + day + ", " + fullYear;
            case "full" :
                String dayName 		= new DateFormatSymbols(dateLocale).getWeekdays()[date.get(Calendar.DAY_OF_WEEK)];
                String i18nDayName 	= (i18nPwC.getPwC("GeneralTranslationsOthers_PwC"+dayName));
                return i18nDayName + ", " + i18nMonthName + " " + day + ", " + fullYear;
            case "yyyymm":
                month = date.get(Calendar.MONTH) + 1;
                return fullYear + (month < 10 ? "0" + month.toString() : month.toString());
            case "yyyymmdd" :
                month = date.get(Calendar.MONTH) + 1;
                return fullYear + (month < 10 ? "0" + month.toString() : month.toString()) + day;
            case "yyyy" :
                return fullYear.toString();
            case "shortusa" :
                sdf = new SimpleDateFormat("MM/dd/yy");
                return sdf.format(date.getTime());
            case "iso":
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.format(date.getTime());
            case "monthyear":
                return i18nMonthName + " " + fullYear;
            case "dateTime":
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                return sdf.format(date.getTime());
            case "short" :
            default :
                sdf = new SimpleDateFormat("dd/MM/yy");
                return sdf.format(date.getTime());
        }
    }
    
    public static boolean isInArray(JSONArray jTags, String tagID) throws JSONException {
		
		boolean isIn = false;
		int tagsL =  jTags.length();
		
		for (int i=0; i<tagsL; i++) {
			JSONObject tag = (JSONObject) jTags.get(i);
			if(tag.getString("tagID").equals(tagID)) {
				isIn=true;
				break;
			}
		}
		
		return isIn;
		
	}
    
	public static boolean isMatch(String elementTag, String filterTag,ControllerBean controllerBean) {
		
		List<String> elementBL = Arrays.asList(CommonsUtils.getTagBackLinks(controllerBean,elementTag));
		elementBL = elementBL.stream().map(String :: trim).collect(Collectors.toList());
		
		if(elementBL.isEmpty()) return false;
		
		String backLinkTag = elementBL.get(0);
		
		//Is the same tag?
		if(backLinkTag.equals(filterTag)) return true;
		
		//Is it elementTag a parent tag?
		if(filterTag.matches(backLinkTag+".*")) return false;
				
		//Is it filterTag a parent tag?
		if(backLinkTag.matches(filterTag+".*")) return true;
		
		//the name was change/move/merge ?
		return elementBL.stream().anyMatch(elem -> elem.matches(filterTag));
	}

    private static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}