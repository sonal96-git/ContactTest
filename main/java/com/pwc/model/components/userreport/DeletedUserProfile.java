package com.pwc.model.components.userreport;


import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

@Model(adaptables = Resource.class)
public class DeletedUserProfile{
	
	private final String CSV_SEPARATOR = ",";
	
	@Inject @Optional @Named("deletedUserEmail") @Default(values="-")
	private String email;	
	
	@Inject @Optional @Default(values="-")
	private Boolean isProflleDeleted;
	 
	@Inject @Optional @Default(values="-")
	private String profileDeleteDate;	 	
	
	@PostConstruct
    protected void validateInput() {		
		email="\""+email.replace("\n","").replace("\"","'")+"\"";
		profileDeleteDate="\""+profileDeleteDate.replace("\n","").replace("\"","'")+"\"";		
    }
	
	public String toString(){		
				
		String csvLine = email+CSV_SEPARATOR+isProflleDeleted+CSV_SEPARATOR+profileDeleteDate+CSV_SEPARATOR;			
		return csvLine;				
	}
		
}
