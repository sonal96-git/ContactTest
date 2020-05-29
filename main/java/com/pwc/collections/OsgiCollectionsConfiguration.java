package com.pwc.collections;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = OsgiCollectionsConfiguration.class, immediate = true, 
property = {
		Constants.SERVICE_DESCRIPTION + "= Here you can configure the collection components properties"
})
@Designate(ocd = OsgiCollectionsConfiguration.Config.class )
public class OsgiCollectionsConfiguration {
	protected final Logger log = LoggerFactory.getLogger(OsgiCollectionsConfiguration.class);

    public static final int COLLECTIONS_DEFAULT_LIMIT = 100;
	public static final String COLLECTIONS_STRING_DEFAULT_LIMIT = "100";
    public static final Boolean COLLECTION_LOGGER_DEFAULT = false;
    
	private static int collectionLimit;
	private static int contactCollectionLimit;
	private static int eventCollectionLimit;
    private static boolean isCollectionLoggerEnabled;
    
    @ObjectClassDefinition(name = "PwC Collection components configuration ", 
    		description = "Here you can configure the collection components properties")
    @interface Config {
    	@AttributeDefinition(name = "Collection logger", 
				description = "Collection logger",
				type = AttributeType.BOOLEAN)
    	public boolean collection_logger();
    	
    	@AttributeDefinition(name = "Collection limit", 
				description = "Collection limit for retrieving Pages or PDFs",
				type = AttributeType.INTEGER)
    	public int collection_limit() default COLLECTIONS_DEFAULT_LIMIT;
    	
    	@AttributeDefinition(name = "Contact Collection limit", 
				description = "Contact Collection limit",
				type = AttributeType.INTEGER)
    	public int contact_collection_limit() default COLLECTIONS_DEFAULT_LIMIT;
    	
    	@AttributeDefinition(name = "Event Collection limit", 
				description = "Event Collection limit",
				type = AttributeType.INTEGER)
    	public int event_collection_limit() default COLLECTIONS_DEFAULT_LIMIT;
    }


	@Activate
	protected void activate(final OsgiCollectionsConfiguration.Config config) {
		collectionLimit = config.collection_limit();
		contactCollectionLimit = config.contact_collection_limit();
		eventCollectionLimit = config.event_collection_limit();
        isCollectionLoggerEnabled = config.collection_logger();
	}
	
	public int getCollectionLimit() {
		return collectionLimit;
	}

	public int getEventCollectionLimit() {
		return eventCollectionLimit;
	}

	public int getContactCollectionLimit() {
		return contactCollectionLimit;
	}

    public boolean getIsCollectionLoggerEnabled(){ return isCollectionLoggerEnabled; }

}
