package com.pwc.model.components.awards;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.jcr.Node;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.dam.api.Asset;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.util.ExceptionLogger;
import com.pwc.util.URLHandlerUtility;

@Component(immediate = true, service = { Servlet.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= Set property of awards-items  and viceversa",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/awards"
        })
public class AwardsServlet extends SlingAllMethodsServlet  {
	private static final long serialVersionUID = 1L;
	private final String LINK_LIST_KEY = "awards_items";
	private final String NAME_KEY = "name";
	private final String ID_KEY = "id";
	private AwardsModel awardsModel;
	private AwardsItemsModel awardsItemsModel;
	
	@Reference
	private OsgiCollectionsLogger logger;
	
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action") != null ? request.getParameter("action") : "";
		String parentPath = request.getParameter("parenPath") != null ? request.getParameter("parenPath") : ""; 
		switch (action) {
		case "1":   // action to update name of Items from item of parent dialog		
			UpdateAwardsItemsToAwards(request , parentPath);
			break;		
		case "2":	// delete node and update list of parent dialog item.	
			UpdateWhenDeleteItems(request ,parentPath, response);
			break;	
		case "3":	// update awardsItem when drag and drop image from cf
			updateAwardsItem(request ,parentPath, response);
			break;	
		case "4":	// create Nodes when item is added
			CreateNodes(request ,parentPath);
			break;	
		case "5":	// update dialog when drag and drop
			updateDialogWhenDragAndDrop(request ,parentPath,response);
			break;
		default:
			break;
		}  
    }      

	private String DecodeString(String text) throws UnsupportedEncodingException {    	 
		String result = URLEncoder.encode(text, "ISO-8859-1" );
		return URLDecoder.decode(result, "UTF-8" );
    }
    
    private void UpdateWhenDeleteItems(SlingHttpServletRequest request, String parentPath,SlingHttpServletResponse response) {  
		ResourceResolver resolver = request.getResourceResolver();
		Resource resource = resolver.getResource(parentPath);
		if(resource == null) return;
		this.awardsModel = resource.adaptTo(AwardsModel.class);
		String[] itemsList = this.awardsModel.getAwards_items();
		itemsList = itemsList != null ? itemsList : null;
		if(itemsList == null) return;		 
		String nodeName = request.getParameter("nodeName");
		nodeName = nodeName != null ? nodeName : "";  
		Node node = resource.adaptTo(Node.class);  
		try{    
			String[] values = new String[itemsList.length];	     		
		 	for(int i = 0; i < itemsList.length; i++) {
				JSONObject jsonLink = new JSONObject(itemsList[i]);	
				if(jsonLink.optString(ID_KEY).equals(nodeName) && i == 0) {	                	
					values = Arrays.copyOfRange(itemsList, i+1, itemsList.length);
					DeleteNodeWhenDeleteItems(resolver.getResource(resource.getPath() + "/" + nodeName));
					break;
				}
				if(jsonLink.optString(ID_KEY).equals(nodeName)) {	 
					values = (String[])ArrayUtils.addAll((Arrays.copyOfRange(itemsList, 0, i)),Arrays.copyOfRange(itemsList, i+1, itemsList.length));	 
					DeleteNodeWhenDeleteItems(resolver.getResource(resource.getPath() + "/" + nodeName));
					break;
				} 
		 	}        
		node.setProperty(LINK_LIST_KEY, values);
		node.getSession().save();
		}catch (Exception e) {
			 logger.logMessage(new Date()+" UpdateWhenDeleteItems error: "+e.toString());
		} 
    } 
        
    private void UpdateAwardsItemsToAwards(SlingHttpServletRequest request, String parentPath) {    	 
		if(parentPath.equals(""))return;
		try{
			String nodeName = request.getParameter("nodeName") != null ? request.getParameter("nodeName") : "";
			String title = request.getParameter("title") != null ? request.getParameter("title") : "";
			String titleResult = DecodeString(title.trim()); 
			ResourceResolver resolver = request.getResourceResolver();
			Resource resource = resolver.getResource(parentPath);			
			Node node = resource.adaptTo(Node.class);        			
			if(node == null) return;		
			this.awardsModel = resource.adaptTo(AwardsModel.class);			
			String[] itemsList = this.awardsModel.getAwards_items() != null ? this.awardsModel.getAwards_items() : null;
			if(itemsList == null) return;			
			String[] values = new String[itemsList.length];	
			for(int i = 0; i < itemsList.length; i++) {
				JSONObject jsonLink = new JSONObject(itemsList[i]);					   
				if(jsonLink.optString(ID_KEY).equals(nodeName) && !titleResult.equals("")) {	                	
					jsonLink.put(NAME_KEY, titleResult);	                	
				}	       
				values[i] = jsonLink.toString();  
			}    
			node.setProperty(LINK_LIST_KEY, values);
			node.getSession().save();   
        }catch (Exception e) {
			logger.logMessage(new Date()+" UpdateWhenDeleteItems error: "+e.toString());
        }
    }
     
    private void updateAwardsItem(SlingHttpServletRequest request, String parentPath,SlingHttpServletResponse response) {
    	 try{    		 
    		 String nodeName = request.getParameter("nodeName");
    		 nodeName = nodeName != null ? nodeName : "";  
    		 ResourceResolver resolver = request.getResourceResolver();
    		 Resource childResource = resolver.getResource(parentPath+"/"+nodeName);    		 
    		 if(childResource == null)
    			 return;  
    		 updateItemsModelWhenDragAndDropImage(request,parentPath);    
    		 updateItemWhenDragImage(childResource);  
    	 }catch(Exception e)
    	 {
    		 logger.logMessage(new Date()+" updateAwardsItem error: "+e.toString());
    	 }
    }
            
    private void updateDialogWhenDragAndDrop(SlingHttpServletRequest request, String parentPath,SlingHttpServletResponse response) {       	
    	try{     		
    		Resource childResource =  getAssetResource(request,parentPath);    		
    		String newImageReference = request.getParameter("imagePath");   
   		    newImageReference = newImageReference != null ? newImageReference : "";
   		    newImageReference = DecodeString(newImageReference.trim());
   		    ResourceResolver resolver = childResource.getResourceResolver();   		    
			Resource imageResource = resolver.getResource(newImageReference);					
			if(imageResource == null) return;			
			Asset asset = imageResource.adaptTo(Asset.class);			
			if(asset == null) return;					
			String assetTitle = asset.getMetadataValue("dc:title");
			assetTitle = assetTitle == null ? "" : assetTitle;			
			String assetDescription = asset.getMetadataValue("dc:description");
			assetDescription = assetDescription == null ? "" : assetDescription;			
			JSONObject finalJsonResponse = new JSONObject();			
			finalJsonResponse.append("title", assetTitle);
			finalJsonResponse.append("summary",assetDescription);			
			response.setHeader("Content-Type", "application/json; charset=UTF-8");
			response.getWriter().println(finalJsonResponse.toString());
			
		}catch(Exception e) {
			ExceptionLogger.logException(e);
		}    	
    }          
    
    private Resource getAssetResource(SlingHttpServletRequest request, String parentPath) {
    	 String nodeName = request.getParameter("nodeName");
    	 nodeName =  nodeName != null ? nodeName : "";   
		 ResourceResolver resolver = request.getResourceResolver();
		 Resource childResource = resolver.getResource(parentPath+"/"+nodeName);
		 this.awardsItemsModel = childResource.adaptTo(AwardsItemsModel.class);   
		 return childResource;
    }
          
    private void updateItemsModelWhenDragAndDropImage(SlingHttpServletRequest request, String parentPath) {    	 
    	 Resource childResource = getAssetResource(request,parentPath);	 
		 if(childResource == null)
			 return;	
		 String newImageReference = request.getParameter("imagePath");
		 newImageReference = newImageReference != null ? newImageReference : ""; 
		 ResourceResolver resolver = childResource.getResourceResolver();		
	 	 this.awardsItemsModel.link_path = this.awardsItemsModel.link_path != null ? 
 			 URLHandlerUtility.handleURLForInternalLinks(this.awardsItemsModel.link_path, resolver) : "";	
		 String oldImageReference = getImage(childResource, resolver);			 
		 newImageReference = newImageReference.equals("") ? oldImageReference : newImageReference;       	 
 		 Resource imageResource = resolver.getResource(newImageReference);
         if(imageResource == null) return;
         Asset asset = imageResource.adaptTo(Asset.class);
         if(asset == null) return;
         String itemTile = this.awardsItemsModel.title; 
         itemTile = StringUtils.isNoneBlank(itemTile) ? itemTile : ""; 
         String assetTile = asset.getMetadataValue("dc:title");
         String itemDescription = this.awardsItemsModel.summary;
         itemDescription = StringUtils.isNoneBlank(itemDescription) ? itemDescription : ""; 
         String assetDescription = asset.getMetadataValue("dc:description");         
         //if was change by drag and drop or by submit the dialog(both use the same listener).
         boolean onSubmit = this.awardsItemsModel.updated;
         this.awardsItemsModel.title = onSubmit ? itemTile : assetTile;         
         this.awardsItemsModel.summary = onSubmit ? itemDescription : assetDescription;               
         this.awardsItemsModel.fileReference = newImageReference;    
    }
     
     
    private void DeleteNodes(Resource resource) {             	 
		 this.awardsModel = resource.adaptTo(AwardsModel.class);
		 String[] itemsList = this.awardsModel.getAwards_items();
		 if(itemsList == null) return; 
		 try{		         	  
			  Iterable<Resource> iterator = resource.getChildren();
		      Iterator<Resource> childItem = iterator.iterator();
		      while(childItem.hasNext()) {
		    	 Resource childResource = childItem.next();	    	 
		    	 StringBuilder itemTitle = new StringBuilder();
		    	 if(!IsNodeInItemsList(childResource.getName(),itemsList,itemTitle)) {        		 
		    		 DeleteNodeWhenDeleteItems(childResource);
		    	 }	    	 
		      }	
		  } catch(Exception e) {
		    	 logger.logMessage("Awards DeleteNodeWhenDeleteItems error : " + e.toString());
		  }
    }    
     
    private void CreateNodes(SlingHttpServletRequest request, String parentPath) {    	     	    	 
    	 ResourceResolver resolver = request.getResourceResolver();  
    	 Resource resource = resolver.getResource(parentPath);    	 
    	 DeleteNodes(resource);    	 
    	 this.awardsModel = resource.adaptTo(AwardsModel.class);
    	 String[] itemsList = this.awardsModel.getAwards_items();
    	 if(itemsList == null) return;
    	 try{
	         for (String items : itemsList) {        	      	 
	             JSONObject jsonLink = new JSONObject(items);
	             String nodeName = jsonLink.optString(ID_KEY);
	             Resource parentResource = resolver.getResource(parentPath);
	             Resource childNode = resolver.getResource(parentPath + "/"+ nodeName);
	
	             if(childNode == null) {	
                     Node parentNode = parentResource.adaptTo(Node.class);	                
                     parentNode.addNode(nodeName);
                     parentNode.getSession().save();                 
	             }	            	             
	         }
    	 } catch (Exception e) {
        	 logger.logMessage("Awards CreateNodes error : " + e.toString());
         }
    }
     
    public boolean IsNodeInItemsList(String nodeName, String[] itemsList, StringBuilder itemTitle) throws JSONException {
		boolean isNodeInItemsList = false;
		for (String item : itemsList) {
			JSONObject jsonLink = new JSONObject(item);
		    if(jsonLink.optString(ID_KEY).equals(nodeName)) {
		        itemTitle.append(jsonLink.optString(NAME_KEY));
		        isNodeInItemsList = true;
		        break;
		    }
		}
		return isNodeInItemsList;
    }
     
    private void DeleteNodeWhenDeleteItems(Resource childResource) {         
		ResourceResolver resolver = childResource.getResourceResolver();     	
		try{	  
			resolver.delete(childResource); // delete node if was deleted the item on parent dialog
			resolver.commit();
		} catch (Exception e) {
		   	 logger.logMessage("Awards DeleteNodeWhenDeleteItems error : " + e.toString());
		}
    }    
 	
 	private void updateItemWhenDragImage(Resource childResource) {	 		
 		try{ 		 			
			Node node = childResource.adaptTo(Node.class);  // update names of items from parent dialog
			node.setProperty("title", this.awardsItemsModel.title);
			node.setProperty("abstract", this.awardsItemsModel.summary);
			node.setProperty("updated", false);
			node.setProperty("link_path", this.awardsItemsModel.link_path);
			node.getSession().save(); 
			 
			if(StringUtils.isNotBlank(this.awardsItemsModel.title)) {
				updateAwards(this.awardsItemsModel.title,childResource);
			}
 		} catch(Exception e) {
 			logger.logMessage(new Date()+" updateItemWhenDragImage error: "+e.toString());
 		}
  	}
 	private void updateAwards(String newItemTitle, Resource childResource) {		
 		Resource parentResource = childResource.getParent();
 		if(ResourceUtil.isNonExistingResource(parentResource)) return;	
 		AwardsModel model = parentResource.adaptTo(AwardsModel.class);
 		 		
        String[] itemsList = model.getAwards_items() != null ? model.getAwards_items() : null;         
 	    String itemId = childResource.getName();
 	    if(itemsList != null) {
 		   boolean updated = false;
 			try{	   
	           for(int i = 0; i < itemsList.length ; i++) {
	                JSONObject jsonItem = new JSONObject(itemsList[i]);
	                if(jsonItem.optString(ID_KEY).equals(itemId) && !jsonItem.optString(NAME_KEY).equals(newItemTitle)) {
	                	jsonItem.put(NAME_KEY, newItemTitle);
	                	itemsList[i] = jsonItem.toString(); 		                		                	
	                	updated = true;
	                	break;
	                }		                	
	           }
	           if(updated){
	        	   Node node = parentResource.adaptTo(Node.class);  
               	   node.setProperty(LINK_LIST_KEY, itemsList);
     	           node.getSession().save();
	           } 		            
 				} catch(Exception e)	{
	 				logger.logMessage(new Date()+" updateAwards error: "+e.toString());
	 			}
 	    }	           
 	}
 	
 	private String getImage(Resource resource, ResourceResolver resolver) {
 		AwardsUtils awardsUtils = new AwardsUtils();
		return awardsUtils.getImage(resource, resolver);
 	}

    
}
