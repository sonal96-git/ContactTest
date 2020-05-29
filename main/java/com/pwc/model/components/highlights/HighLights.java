package com.pwc.model.components.highlights;

import com.adobe.cq.sightly.WCMUsePojo;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.I18nPwC;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.JSONObject;


public class HighLights extends WCMUsePojo {

    private final String BACK_LABEL_KEY = "Highlights_Back";
    private final String READ_MORE_LABEL_KEY = "Highlights_ReadMore";    
    private final String ID_KEY = "id";  
    private final String TITLE_KEY = "title";
    private final String NAME_KEY = "name";    
    private HighLightsModel model;
    private Resource resource;
    private SlingHttpServletRequest request;  
    private ResourceResolver resolver;
      
    @Override
    public void activate() throws Exception {    	
    	this.resource = getResource();
    	this.request = getRequest();
    	this.resolver = getResourceResolver(); 
    	if(this.resource == null) 
    		return;
    	else
		    this.model = this.resource.adaptTo(HighLightsModel.class);
    }    
    
    private void DeleteNodeWhenDeleteItems(String[] itemsList) { 
		if(itemsList == null) return; 
		try{  
			CreateNodes(itemsList); // create nodes when is added an item of parent dialog			         	  
			Iterable<Resource> iterator = resource.getChildren();
			Iterator<Resource> childItem = iterator.iterator();
			while(childItem.hasNext()) {
				Resource childResource = childItem.next();	    	 
				StringBuilder itemTitle = new StringBuilder();
				if(!IsNodeInItemsList(childResource.getName(),itemsList,itemTitle))	{        		 
					resolver.delete(childResource); // delete node if was deleted the item on parent dialog
					resolver.commit();
				}else {	 
					Node node = childResource.adaptTo(Node.class);  // update names of items from parent dialog
					node.setProperty(TITLE_KEY, itemTitle.toString());
					node.getSession().save();   
				}
			}	
		}catch(Exception e) {
			ExceptionLogger.logExceptionMessage("Highlights DeleteNodeWhenDeleteItems error :",e);	
		}
    }
    
    public List<List<String>> getHighlightsItemsNested() {        
		String[] listHighlightsItems = this.model.getHighlight_items();		
    	if(listHighlightsItems == null) return null;
    	List<String> listHighlightsItemsNames = new ArrayList<>();
    	List<List<String>> listHighlightsItemsNamesNested = new ArrayList<>();    	
		try{
			int iterator = 0;
			int endPosition = listHighlightsItems.length;
			for(int i = 0; i < endPosition; i++) {	           
				JSONObject jsonLink = new JSONObject(listHighlightsItems[i]); 
				listHighlightsItemsNames.add(jsonLink.optString(ID_KEY));     
				iterator++;
				if (iterator == 3 || i+1 == endPosition) {
					listHighlightsItemsNamesNested.add(listHighlightsItemsNames);
					listHighlightsItemsNames = new ArrayList<>();
					iterator = 0;
				}
			}	    	
		 } catch(Exception e) {
			 ExceptionLogger.logExceptionMessage("Highlights getHighlightsItemsNested error :",e);			
		 }		 
	     return listHighlightsItemsNamesNested;
    }
    
    private void CreateNodes(String[] itemsList) {
    	for(String items : itemsList) {
    		try {
				JSONObject jsonLink = new JSONObject(items);	
				String nodeName = jsonLink.optString(ID_KEY);
				Resource childNode = resolver.getResource(this.resource.getPath() + "/"+ nodeName);			  
				if(childNode == null){
					Node parentNode = resource.adaptTo(Node.class);
					parentNode.addNode(nodeName);
					parentNode.getSession().save();		
				}			  
    		} catch (Exception e) {					
				 ExceptionLogger.logExceptionMessage("Highlights CreateNodes error :",e);	
			} 
		}
    }
    
    private boolean IsNodeInItemsList(String nodeName, String[] itemsList, StringBuilder itemTitle) {
    	try{
		   	for(String item : itemsList) {
		   		JSONObject jsonLink = new JSONObject(item);
				if(jsonLink.optString(ID_KEY).equals(nodeName)) {
					itemTitle.append(jsonLink.optString(NAME_KEY));					
					return true;
				}					
			}
    	}catch (Exception e) {					
			 ExceptionLogger.logExceptionMessage("Highlights IsNodeInItemsList error :",e);	
		} 
	   	return false;
    }
    
    public List<String> getHighLightsItemsName() {    	
    	String[] listHighLightsItems = this.model.getHighlight_items() != null ? this.model.getHighlight_items() : null;
    	if(listHighLightsItems == null) return null;
    	List<String> listHighLightsItemsNames = new ArrayList<>();    	
		 try{ 
	    	for(int i = 0; i < listHighLightsItems.length; i++) {
	            JSONObject jsonLink = new JSONObject(listHighLightsItems[i]);  
	            listHighLightsItemsNames.add(jsonLink.optString(ID_KEY));	            
	    	}
	    	DeleteNodeWhenDeleteItems(listHighLightsItems);
		 }
		 catch(Exception e)
		 {
			 ExceptionLogger.logExceptionMessage("Highlights getHighLightsItemsName error :",e);	
		 }		 
	     return listHighLightsItemsNames;
    }
    
    public Boolean getHasList(){
    	return this.model.getHighlight_items() != null;    	
    }

    public Boolean getIsSimple() {
    	return this.model.getStyle().equals("simple");
    }
    
    public String getReadMoreLabel() {
    	I18nPwC i18nPwC = new I18nPwC(this.request, resource);
    	return i18nPwC.getPwC(READ_MORE_LABEL_KEY);
    }
    
    public String getBackLabel() {
    	I18nPwC i18nPwC = new I18nPwC(this.request, resource);
    	return i18nPwC.getPwC(BACK_LABEL_KEY);
    }
    
    public HighLightsModel getModel() {
    	return this.model;
    }
   
}
