package com.pwc.model.components.userreport;


import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

@Model(adaptables = Resource.class)
public class DeletedUserRequest{

	private final String EOL = "\n";
	private final String CSV_SEPARATOR = ",";
	
		
	@Inject @Optional @Default(values="-")
	private String deletionComment;
	 
	@Inject @Optional @Default(values="-")
	private String deletionOption;
	 
	@Inject @Optional @Default(values="-")
	private String deletionOptionTitle;
	 
	@Inject @Optional @Named("deletionRequestDate") @Default(values="-")
	private String deletionTime;
	
	@Inject @Optional @Default(values="-")
	private String phoneNumber;		
	
	@PostConstruct
    protected void validateInput() {
		deletionComment="\""+deletionComment.replace("\n","").replace("\"","'")+"\"";
		deletionOption="\""+deletionOption.replace("\n","").replace("\"","'")+"\"";
		deletionOptionTitle="\""+deletionOptionTitle.replace("\n","").replace("\"","'")+"\"";
		phoneNumber="\""+phoneNumber.replace("\n","").replace("\"","'")+"\"";
		deletionTime="\""+deletionTime.replace("\n","").replace("\"","'")+"\"";	
    }
	
	public String toString(){		
				
		String csvLine = deletionComment+CSV_SEPARATOR+
				deletionOption+CSV_SEPARATOR+deletionOptionTitle+CSV_SEPARATOR+phoneNumber+CSV_SEPARATOR+deletionTime+EOL;			
		return csvLine;				
	}
		
}
