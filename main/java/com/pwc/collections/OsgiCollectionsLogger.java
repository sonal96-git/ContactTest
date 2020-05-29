package com.pwc.collections;

import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = OsgiCollectionsLogger.class, immediate = true, 
property = {
		Constants.SERVICE_DESCRIPTION + "= Here you have the collection logger calls"
})
public class OsgiCollectionsLogger {
    
	protected final Logger log = LoggerFactory.getLogger(OsgiCollectionsLogger.class);
    
    @Reference
    private OsgiCollectionsConfiguration collectionsConfiguration;

    @Activate
    protected void activate(final Map<String, Object> config) {
    }

    public void logMessage(String message){
        if(isLoggerEnabled()) {
            log.info(message);
        }
    }

    public void logKeyValue(String key, String value) {
        if(isLoggerEnabled()){
            log.info(key + ": " + value);
        }
    }

    public void logListDefinition(String listFrom, String... params){
        if(isLoggerEnabled()) {
            String newLine = System.getProperty("line.separator");
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(newLine).append("------PWC Collection Logger: Configuration------").append(newLine);
            logBuilder.append("Query type: ").append(listFrom).append(newLine);
            for(int i = 0; i < params.length; i++){
                logBuilder.append("Parameter_").append(i).append(": ").append(params[i]).append(newLine);
            }
            logBuilder.append("------PWC Collection Logger: Configuration END-----").append(newLine);
            log.info(logBuilder.toString());
        }
    }

    private boolean isLoggerEnabled() {
        return collectionsConfiguration != null && collectionsConfiguration.getIsCollectionLoggerEnabled();
    }
}
