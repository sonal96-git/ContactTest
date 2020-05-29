package com.pwc.model.components.userreport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.pwc.AdminResourceResolver;
import com.pwc.user.util.UserRegistrationUtil;
import com.pwc.util.ExceptionLogger;

@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "sling/servlet/default",
    "sling.servlet.selectors=" + "user-report"
})
public class ExternalUserReports extends SlingAllMethodsServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -8176732399470951203L;

	private Resource userNode;
    
    @Reference
    private AdminResourceResolver resourceResolver;
    private ResourceResolver adminResourceResolver;
    
    private StringBuffer csv = new StringBuffer();
    private StringBuffer csv365 = new StringBuffer();
    private final int  MATCH_GROUP = 2;
    private final String DEFAULT_VALUE = "-";
    private final String PROFILE_NODE = "profile";
    private final String PREFERENCES_NODE = "preferences";
    private final String USER_NODE_PRIM_TYPE = "rep:User";
    private final String DPE_FOLLOWED_CATEGORY = "dpe_followed_categories";
    private final String ACCEPTED_TNC_NODE =".*accepted_tnc";
    private final String TERMS_CONDITIONS_NODE =".*privacypolicy";
    private final String EXTERNAL_USER_PATH = "/home/users/pwc-external-users";
    // Territory, if found, will be saved in gruop 2
    private final String TERRITORY_REGEX ="\\A\"\\/content\\/pwc(365)?\\/([a-zA-Z]{2})";
    private final String CSV_HEADERS = "Territory filter,First Name,Last Name,Email,Company/Organization,Country,Job Title,"
    		+ "Accepted T&C,Date of Registration,Date of Validation,Marketing Consent,Entry Point,Territory,URL,Parent Page Path,Preferred Language,"
    		+ "User Advisory Board, Relationship with PwC, Deleted, Preference Category \n";
	private final String CSV_HEADERS_365 = "Territory filter,First Name,Last Name,Email,Company/Organization,Country,Job Title,"
			+ "Accepted T&C,Date of Registration,Date of Validation,Marketing Consent,Entry Point,Territory Alignment,Preferred Language,"
			+ "Preference Category \n";

	private final String CSV_FILE = "external-users-report.csv";
	private final String CSV_FILE_365 = "pwc-365-users-report.csv";
	private final String CONTENT_FOLDER = "/content/reports/pwc/external-users-report";
	private final String ENTRY_365 = "\"pwc365\"";


    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException {

    	response.setContentType("text/html");
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	response.setHeader("Access-Control-Allow-Credentials", "true");
    	
    	adminResourceResolver = resourceResolver.getAdminResourceResolver();
    	
    	csv.setLength(0);
    	csv.append(CSV_HEADERS);
		csv365.setLength(0);
		csv365.append(CSV_HEADERS_365);

    	try {
    		
    		//Setting info to "csv"
			setCSVInfo(EXTERNAL_USER_PATH);

			final InputStream is = new ByteArrayInputStream(csv.toString().getBytes("UTF-8"));
			final InputStream is365 = new ByteArrayInputStream(csv365.toString().getBytes("UTF-8"));

			Session session = adminResourceResolver.adaptTo(Session.class);

			Node node = session.getNode(CONTENT_FOLDER);

			UserRegistrationUtil.createCSVFileReport(node, session, is, CSV_FILE);
			UserRegistrationUtil.createCSVFileReport(node, session, is365, CSV_FILE_365);

			session.save();
			session.logout();

		} catch (RepositoryException | ParseException | UnsupportedEncodingException exception) {
			ExceptionLogger.logExceptionMessage("UserReportCSVServlet:doGet. Error while getting csv report", exception);
		} 
    }

	private void setCSVInfo(String path) throws ParseException {
    	
    	User regUser;
    	
    	String primType;
    	String territory;
    	String parentPath; 
    	
    	ValueMap properties;
    	
    	userNode = adminResourceResolver.getResource(path);
    	Iterable<Resource> itChildren = userNode.getChildren();
    	
    	for (Resource resource : itChildren) {
    		
    		properties = resource.getValueMap();
        	
        	primType = properties.containsKey("jcr:primaryType") ? 
        			properties.get("jcr:primaryType").toString():"";

        	if(primType.equals(USER_NODE_PRIM_TYPE)) {
        		
        		Resource profileNode = resource.getChild(PROFILE_NODE);
        		
        		if(profileNode != null) {
        			
        			Resource preferenceNode = resource.getChild(PREFERENCES_NODE);
            		
            		regUser = profileNode.adaptTo(User.class);
        		
            		parentPath = regUser.getParentPath();
            		
            		Pattern pattern = Pattern.compile(TERRITORY_REGEX);
                    Matcher matcher = pattern.matcher(parentPath);
                    
            		territory  = matcher.find() ? matcher.group(MATCH_GROUP):DEFAULT_VALUE;
            		
            		List<String> termsCond = setTermsAndCondCountries(preferenceNode);
        			
        			regUser.setTerritory(territory);
        			regUser.setTerrArray(termsCond);        	
        			if(preferenceNode != null){
        				setPreferredCategory(preferenceNode,regUser);
					}
					csv.append(regUser.toString());
					final String entryPoint = regUser.getEntryPoint();
					if (null != entryPoint && entryPoint.equals(ENTRY_365))
						csv365.append(regUser.csv365String());
				}
			} else {
        		setCSVInfo(resource.getPath());
        	}
    	}
	}
    
    private List<String>  setTermsAndCondCountries(Resource preferenceNode) {
    	
    	String countryName;
    	List<String> termsCond = new ArrayList<>();
    	
    	if(preferenceNode != null) {
    		
        	Iterable<Resource> prefChildren = preferenceNode.getChildren();
        	
        	for (Resource prefChild : prefChildren) {
        		
        		if(prefChild.getName().matches(TERMS_CONDITIONS_NODE) || prefChild.getName().matches(ACCEPTED_TNC_NODE)) {
        			
        			Iterable<Resource> termsCondIt = prefChild.getChildren();
        			
        			for (Resource country : termsCondIt) {
        				countryName = country.getName().equals("gb") ? "uk": country.getName();
        				termsCond.add(countryName);
        			}
        		}
        	}
    	}
    	
    	return termsCond;
    }   
            
    private void setPreferredCategory(Resource preferenceNode,User regUser){ 
    	List<String> preferedCategoryArray = new ArrayList<>(); 
    	List<String> preferredCountry = new ArrayList<>();
    	List<List<String>> PreferredCountryArray = new ArrayList<>(); 
		if(preferenceNode != null) {		    		
        	Iterable<Resource> prefChildren = preferenceNode.getChildren();        	
        	for (Resource prefChild : prefChildren) {
        		if(prefChild.getName().matches(DPE_FOLLOWED_CATEGORY)){
        			// countries
					Iterable<Resource> countries = prefChild.getChildren();	
	    			for (Resource country : countries) {	    				
	    				//  preferred category	    				
	    				Iterable<Resource> categories = country.getChildren();
	    				for (Resource category : categories) {	    					
	    					// category first level	    					
	    					if(!category.hasChildren()){
	    						preferedCategoryArray.add(category.getName());
	    						continue;
	    					}
	    					Iterable<Resource> firstLevelCategories = category.getChildren();	    					
		    				for (Resource firstLevelCategory : firstLevelCategories) {		    					
		    					// category second level
		    					if(!firstLevelCategory.hasChildren()){
		    						preferedCategoryArray.add(category.getName()+ "->"+ firstLevelCategory.getName());
		    						continue;
		    					}
		    					Iterable<Resource> secondLevelCategories = firstLevelCategory.getChildren();
			    				for (Resource secondLevelCategory : secondLevelCategories) {
			    					preferedCategoryArray.add(category.getName() + "->"+ firstLevelCategory.getName() + "->"+secondLevelCategory.getName());			    					
			    				}
		    				}		    				
	    				}	
	    				preferredCountry.add(country.getName());		    				
	    				preferredCountry.add(preferedCategoryArray.toString());
	    				PreferredCountryArray.add(preferredCountry);		    				
	    				preferedCategoryArray = new ArrayList<>(); 
	    				preferredCountry = new ArrayList<>();  
	    			}	
	    			regUser.setPreferredCountryArray(PreferredCountryArray);
	    			break;
        		}        		
        	}        		
    	} 
    }
}




  
