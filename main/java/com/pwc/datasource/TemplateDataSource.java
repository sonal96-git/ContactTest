/**
 * 
 */
package com.pwc.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.adobe.cq.sightly.WCMUsePojo;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

/**
 * @author sevenkat
 *
 */
public class TemplateDataSource extends WCMUsePojo {
	
	private String[] allowedPaths= {"/content(/.*)?","/content/pwc(/.*)?"};
 
@Override
 public void activate() throws Exception {
 final ResourceResolver resolver = getResource().getResourceResolver();
 
//Creating the Map instance to insert the templates
 final TreeMap<String,Integer> templates = new TreeMap<>();
 Map<String, String> sortedMap = new LinkedHashMap<>();
 
Resource res=resolver.getResource("/apps/pwc/templates");
if(res.hasChildren()) {
	Iterator<Resource> resIt=res.listChildren();
	while(resIt.hasNext()) {
		Resource templateRes=resIt.next();
		ValueMap map=templateRes.getValueMap();
		String[] providedPaths=map.get("allowedPaths", String[].class);
		Integer rank=map.get("ranking", Integer.class);
		for(String path:providedPaths) {
			if(ArrayUtils.contains(allowedPaths,path)) {
				String title=map.get("jcr:title", String.class);
				String templatePath=templateRes.getPath();
				templates.put(templatePath+"|"+title,rank);
			}
		}
	}
	
		List<Map.Entry<String, Integer>> entries = new ArrayList<>(templates.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
			  public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b){
			    return a.getValue().compareTo(b.getValue());
			  }
			});
			
			for (Map.Entry<String, Integer> entry : entries) {
				String[] values=entry.getKey().split("\\|");
				sortedMap.put(values[0], values[1]);
			}
	
}
 @SuppressWarnings("unchecked")
 
//Creating the Datasource Object for populating the drop-down control.
 DataSource ds = new SimpleDataSource(new TransformIterator(sortedMap.keySet().iterator(), new Transformer() {
 
 @Override
 
//Transforms the input object into output object
 public Object transform(Object o) {
 String key = (String) o;

 
//Allocating memory to Map
 ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
//Populate the Map
 vm.put("value", key);
 vm.put("text", sortedMap.get(key));
 
 return new ValueMapResource(resolver, new ResourceMetadata(), "nt:unstructured", vm);
 }
 }));
 
 this.getRequest().setAttribute(DataSource.class.getName(), ds);
 }
}