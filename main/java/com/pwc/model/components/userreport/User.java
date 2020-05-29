package com.pwc.model.components.userreport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import com.pwc.user.Constants;

@Model(adaptables = Resource.class)
public class User{

	private static final String EOL = "\n";
	private static final String QUOTE = "\"";
	private final String CSV_SEPARATOR = ",";
	private final String CSV_PIPE_SEPARATOR = "|";

	@Inject @Optional @Named("givenName") @Default(values="\"\"\"\"")
	private String fname;
	 
	@Inject @Optional @Named("familyName")  @Default(values="\"\"\"\"")
	private String lname;
	 
	@Inject @Optional @Default(values="\"\"\"\"")
	private String email;
	 
	@Inject @Optional @Default(values="\"\"\"\"")
	private String company;
	
	@Inject @Optional @Default(values="\"\"\"\"")
	private String country;
	
	@Inject @Optional @Default(values="\"\"\"\"")
	private String jobTitle;
	
	@Inject @Optional @Named("dateRegistered") @Default(values= "\"\"\"\"")
	private String registration;
	
	@Inject @Optional @Named("accountActivationDateTime") @Default(values="\"\"\"\"")
	private String validation;
	
	@Inject @Optional @Named("registrationSource") @Default(values="\"\"\"\"")
	private String entryPoint;
	
	@Inject @Optional @Named("registrationUrl") @Default(values="\"\"\"\"")
	private String url;
	
	@Inject @Optional @Named("parentPagePath") @Default(values="\"\"\"\"")
	private String parentPath;
	
	@Inject @Named("preferredLanguage") @Default(values="\"\"\"\"\"\"")
	private String preferredLanguage;
	
	@Inject @Named("preferredLocale") @Default(values="\"\"\"\"")
	private String preferredLocale;
	 
	
	@Inject @Optional @Named("isUserAdvisoryBoard") @Default(values="false")
	private Boolean isUserAdvisoryBoard;
	
	@Inject @Optional @Named("relationshipWithPwC") @Default(values="\"\"\"\"")
	private String relationshipPwc;		
	
	@Inject @Optional @Named("isDeleted") @Default(values="false")
	private Boolean deleted;	
	
	@Inject @Optional @Named("marketingConsent") @Default(values=Constants.MARKETING_CONSENT_UNKNOWN)
        private String marketingConsent;

	@Inject
	@Optional
	@Default(values = "\"\"\"\"\"\"")
	private String lastModifiedTimeStamp;
	
	@Inject
	@Optional
	@Default(values = "\"\"\"\"\"\"")
	private String userMarketingID;

	private String territory;
	private List<String> terrArray;	
	
	private List<List<String>> PreferredCountryArray;
	
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public List<String> getTerrArray() {
		return terrArray;
	}
	public void setTerrArray(List<String> terrArray) {
		this.terrArray = terrArray;
	}
	public String getRegistration() {
		return registration;
	}
	public void setRegistration(String registration) {
		this.registration = registration;
	}
	public String getValidation() {
		return validation;
	}
	public void setValidation(String validation) {
		this.validation = validation;
	}
	public String getEntryPoint() {
		return entryPoint;
	}
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	public String getTerritory() {
		return territory;
	}
	public void setTerritory(String territory) {
		this.territory = territory;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}
	public Boolean getUserAdvisoryBoard() {
		return isUserAdvisoryBoard;
	}
	public void setUserAdvisoryBoard(Boolean isUserAdvisoryBoard) {
		this.isUserAdvisoryBoard = isUserAdvisoryBoard;
	}
	public String getRelationshipPwc() {
		return relationshipPwc;
	}
	public void setRelationshipPwc(String relationshipPwc) {
		this.relationshipPwc = relationshipPwc;
	}	
	
	public String getLastModifiedTimeStamp() {
		return lastModifiedTimeStamp;
	}
	
	public void setLastModifiedTimeStamp(String lastModifiedTimeStamp) {
		this.lastModifiedTimeStamp = lastModifiedTimeStamp;
	}
	
	public String getUserMarketingID() {
		return userMarketingID;
	}
	
	public void setUserMarketingID(String userMarketingID) {
		this.userMarketingID = userMarketingID;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public String getMarketingConsent() {
            return marketingConsent;
        }
        
        public void setMarketingConsent(String marketingConsent) {
            this.marketingConsent = marketingConsent;
        }
	
	public List<List<String>> getPreferredCountryArray() {
		return PreferredCountryArray;
	}
	public void setPreferredCountryArray(List<List<String>> preferredCountryArray) {
		PreferredCountryArray = preferredCountryArray;
	}
	
	@PostConstruct
    protected void validateInput() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX");

		
		fname = QUOTE + (fname.contains(EOL) ? fname.replace(EOL, "").replace(QUOTE, "'") : fname) + QUOTE;
		lname = QUOTE + (lname.contains(EOL) ? lname.replace(EOL, "").replace(QUOTE, "'") : lname) + QUOTE;
		email = QUOTE + (email.contains(EOL) ? email.replace(EOL, "").replace(QUOTE, "'") : email) + QUOTE;
		company = QUOTE + (company.contains(EOL) ? company.replace(EOL, "").replace(QUOTE, "'") : company) + QUOTE;
		country = QUOTE + (country.contains(EOL) ? country.replace(EOL, "").replace(QUOTE, "'") : country) + QUOTE;
		jobTitle = QUOTE + (jobTitle.contains(EOL) ? jobTitle.replace(EOL, "").replace(QUOTE, "'") : jobTitle) + QUOTE;
		registration = QUOTE + (registration.contains(EOL) ? registration.replace(EOL, "").replace(QUOTE, "'") : registration) + QUOTE;
		validation = QUOTE + (validation.contains(EOL) ? validation.replace(EOL, "").replace(QUOTE, "'") : validation) + QUOTE;
		marketingConsent = QUOTE + marketingConsent.replace(EOL, "").replace(QUOTE, "'") + QUOTE;
		entryPoint = QUOTE + (entryPoint.contains(EOL) ?  entryPoint.replace(EOL, "").replace(QUOTE, "'") : entryPoint) + QUOTE;
		url = QUOTE + (url.contains(EOL) ? url.replace(EOL, "").replace(QUOTE, "'") : url) + QUOTE;
		parentPath = QUOTE + (parentPath.contains(EOL) ? parentPath.replace(EOL, "").replace(QUOTE, "'") : parentPath) + QUOTE;
		relationshipPwc = QUOTE + (relationshipPwc.contains(EOL) ? relationshipPwc.replace(EOL, "").replace(QUOTE, "'") : relationshipPwc) + QUOTE;
		
    }
	
	public String toString(){
			
		String csvString = CSV_SEPARATOR+fname+CSV_SEPARATOR+lname+CSV_SEPARATOR+
				email+CSV_SEPARATOR+company+CSV_SEPARATOR+country+CSV_SEPARATOR+jobTitle+CSV_SEPARATOR+
				(terrArray != null ? QUOTE+terrArray.toString()+QUOTE : "[]")+CSV_SEPARATOR+registration +CSV_SEPARATOR+validation+CSV_SEPARATOR+
				marketingConsent+CSV_SEPARATOR+entryPoint+CSV_SEPARATOR+territory+CSV_SEPARATOR+url+CSV_SEPARATOR+parentPath
				+CSV_SEPARATOR+ (preferredLanguage.equals("") ? QUOTE + preferredLocale + QUOTE : preferredLanguage) 
				+CSV_SEPARATOR+isUserAdvisoryBoard+CSV_SEPARATOR+relationshipPwc+CSV_SEPARATOR+Boolean.toString(deleted);
		
		
		StringBuffer csvLine = new StringBuffer(territory+csvString+EOL);
		
		if(terrArray != null){
			for (String terrTermsNCond : terrArray) {				
				if(!terrTermsNCond.equals(territory)){
					csvLine.append(terrTermsNCond+csvString+EOL);
				}				
			}
		}

		if (PreferredCountryArray != null) {
			for (List<String> PreferredCountries : PreferredCountryArray) {
				csvLine.append(createPreferredCategory(PreferredCountries, csvString) + EOL);
			}
		}
		return csvLine.toString();
	}

	/**
	 * Creates an entry for a pwc365 user, in 365 User report.
	 *
	 * @return {@link String} row to be added for a user, in 365 User report.
	 */
	public String csv365String() {
		if (terrArray != null && !terrArray.isEmpty()) {
			territory = terrArray.get(0);
			return territory + CSV_SEPARATOR + fname + CSV_SEPARATOR + lname + CSV_SEPARATOR +
					email + CSV_SEPARATOR + company + CSV_SEPARATOR + country + CSV_SEPARATOR + jobTitle + CSV_SEPARATOR +
					(terrArray != null ? QUOTE + terrArray.toString() + QUOTE : "[]") + CSV_SEPARATOR + registration + CSV_SEPARATOR + validation + CSV_SEPARATOR +
					marketingConsent + CSV_SEPARATOR + entryPoint + CSV_SEPARATOR + territory + CSV_SEPARATOR
					+ (preferredLanguage.equals("") ? QUOTE + preferredLocale + QUOTE : preferredLanguage) + EOL;
		}
		return "";
	}

	/**
	 * Creates an array of user's preferred categories for given list of preferred countries
	 *
	 * @param PreferredCountries {@link List<String>} list of preferred countries.
	 * @param csvString          {@link String} user report's row without territory filter data.
	 * @return {@link StringBuilder} rows with preferred categories data.
	 */
	private StringBuilder createPreferredCategory(List<String> PreferredCountries, String csvString) {
		final StringBuilder categories = new StringBuilder();
		int i = 0;
		for (String preferredCategory : PreferredCountries) {
			if (i == 0) {
				categories.append(preferredCategory + csvString);
			} else {
				categories.append(CSV_SEPARATOR
						+ (preferredCategory != null ? QUOTE + preferredCategory.toString().replace(",", EOL) + QUOTE
						: "[]"));
			}
			i++;
		}
		return categories;
	}
	
	public String getUserData() {
		if(validation.length() > 6) {
			validation = validation.replace(QUOTE,"");
			if(validation.contains("Z")){
				validation = validation.replace("Z","+00:00");
			}
		}
		if(lastModifiedTimeStamp.length() > 6){
			lastModifiedTimeStamp = lastModifiedTimeStamp.replace(QUOTE,"");
			lastModifiedTimeStamp = lastModifiedTimeStamp.replace(" ","T") + "+00:00";
		}
		String csvString = CSV_SEPARATOR + registration + CSV_SEPARATOR + validation + CSV_SEPARATOR + entryPoint + CSV_SEPARATOR + url
				+ CSV_SEPARATOR + Boolean.toString(deleted) + CSV_SEPARATOR
				+ (terrArray != null ? QUOTE + terrArray.toString() + QUOTE : "[]") + CSV_SEPARATOR + email + CSV_SEPARATOR + fname
				+ CSV_SEPARATOR + lname
				+ CSV_SEPARATOR + company + CSV_SEPARATOR + country + CSV_SEPARATOR + jobTitle + CSV_SEPARATOR
				+ (preferredLanguage.equals("") ? QUOTE + preferredLocale + QUOTE : preferredLanguage) + CSV_SEPARATOR + isUserAdvisoryBoard
				+ CSV_SEPARATOR + relationshipPwc + CSV_SEPARATOR + lastModifiedTimeStamp + CSV_SEPARATOR + userMarketingID + CSV_SEPARATOR
				+ marketingConsent;
		
		StringBuilder csvLine = new StringBuilder(territory + csvString);
		
		if (PreferredCountryArray != null) {
			csvLine.append(CSV_SEPARATOR + QUOTE);
			for (List<String> PreferredCountries : PreferredCountryArray) {
				StringBuilder categories = new StringBuilder();
				for (String preferredCategory : PreferredCountries) {
					categories.append(
							preferredCategory != null ? preferredCategory.replace(CSV_SEPARATOR, EOL) : "[]");
				}
				csvLine.append(categories);
				if (PreferredCountryArray.indexOf(PreferredCountries) != (PreferredCountryArray.size() - 1)) {
					csvLine.append(EOL);
				}
			}
			csvLine.append(QUOTE + EOL);
		}
		else {
			csvLine.append(CSV_SEPARATOR + QUOTE + QUOTE + QUOTE + QUOTE + QUOTE + QUOTE + EOL);
		}
		
		return csvLine.toString();
	}

	/**
	 * Creates an entry for a user with preferred territory 'US', in SFMC user report.
	 *
	 * @return {@link String} row to be added for a user, in SFMC user report.
	 */
	public String addSFMCData(){
		return email+CSV_PIPE_SEPARATOR+fname+CSV_PIPE_SEPARATOR+lname+CSV_PIPE_SEPARATOR+company+CSV_PIPE_SEPARATOR+jobTitle+CSV_PIPE_SEPARATOR+preferredLanguage
				+CSV_PIPE_SEPARATOR+marketingConsent+CSV_PIPE_SEPARATOR+registration +CSV_PIPE_SEPARATOR+validation+CSV_PIPE_SEPARATOR+
				country+CSV_PIPE_SEPARATOR+"US"+CSV_PIPE_SEPARATOR+lastModifiedTimeStamp+CSV_PIPE_SEPARATOR+userMarketingID+EOL;
	}

}
