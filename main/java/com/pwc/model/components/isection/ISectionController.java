package com.pwc.model.components.isection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.pwc.collections.OsgiCollectionsLogger;
import com.pwc.util.ExceptionLogger;

public class ISectionController extends WCMUsePojo {

	Page currentPage;
	ValueMap pageProp;
	boolean wcmModeEdit;
	
	private final String EMPTY_STRING ="";
	private final String FULL_WIDTH ="fullwidth";
	private final String INHERITANCE ="inheritance";
	private final String FIXED_HEIGHT ="fixedheight";
	private final String COLORS_ENABLED ="colorsEnabled";
	private final String COLORS_BGCOLOUR ="colorsBgColour";
	private final String SECTION_BGCOLOUR ="sectionBgColour";
	private final String RESOURCE_TYPE ="sling:resourceType";
	private final String RELEATED_LINK_PAR ="related-links-par";
	private final String IPARSYS_PAR ="foundation/components/iparsys/par";
	private final String LONGFORM_SECTION ="pwc/components/content/longform-section";
	private final String LONGFORM_ISECTION ="pwc/components/content/longform-isection";
	
	private final String NO ="No";
	private final String ZERO ="0";
	private final String YES ="Yes";
	private final String TRUE ="true";
	private final String CANCEL ="cancel";
	private final String COLORS ="colors-";
	private final String DEFAULT_SBGCOLOUR ="white";
	private final String DEFAULT_CBGCOLOUR ="colors-white";
	private final String NEW_NODE_NAME ="iparsys_fake_par";
	
	
	@Override
	public void activate() throws Exception {

		pageProp = getPageProperties();
		currentPage = getCurrentPage();
		wcmModeEdit = getWcmMode().isEdit();

		Resource relatedResource = getResource();
		
		if(wcmModeEdit && relatedResource.getName().equals(RELEATED_LINK_PAR)) getUpdate(relatedResource);
	}

	private void getUpdate(Resource relatedResource) {

		boolean isColorEnabled = pageProp.containsKey(COLORS_ENABLED) ? pageProp.get(COLORS_ENABLED).toString().equals(TRUE) : false;
		
		ValueMap relatedProps = relatedResource.getValueMap();
		String resourceType = relatedProps.containsKey(RESOURCE_TYPE) ? relatedProps.get(RESOURCE_TYPE).toString() : EMPTY_STRING;

		if(resourceType.equals(LONGFORM_SECTION)) {

			try {
				
				ModifiableValueMap map = relatedResource.adaptTo(ModifiableValueMap.class);
				
				String fullwidth = EMPTY_STRING;
				String fixedheight = relatedProps.containsKey(FIXED_HEIGHT) ? relatedProps.get(FIXED_HEIGHT).toString() : ZERO;

				if(isColorEnabled) {
					
					fullwidth = relatedProps.containsKey(FULL_WIDTH) ? relatedProps.get(FULL_WIDTH).toString() : YES;
					String colorsBgColour = relatedProps.containsKey(COLORS_BGCOLOUR) ? relatedProps.get(COLORS_BGCOLOUR).toString() : DEFAULT_CBGCOLOUR;
					
					if(colorsBgColour.indexOf(COLORS) == -1) colorsBgColour = COLORS+colorsBgColour.toLowerCase();
					
					map.put(COLORS_BGCOLOUR, colorsBgColour);
					
				} else {
					
					fullwidth = relatedProps.containsKey(FULL_WIDTH) ? relatedProps.get(FULL_WIDTH).toString() : NO;
					String sectionBgColour = relatedProps.containsKey(SECTION_BGCOLOUR) ? relatedProps.get(SECTION_BGCOLOUR).toString() : DEFAULT_SBGCOLOUR;
				
					map.put(SECTION_BGCOLOUR, sectionBgColour);
				}
				
				//map.put(INHERITANCE, CANCEL);
				map.put(FULL_WIDTH, fullwidth);
				map.put(FIXED_HEIGHT, fixedheight);
				map.put(RESOURCE_TYPE, LONGFORM_ISECTION);
				
				Node releatedNode = relatedResource.adaptTo(Node.class);
				Node fakeParNode = releatedNode.addNode(NEW_NODE_NAME);
				
				fakeParNode.setProperty(INHERITANCE, CANCEL);
				fakeParNode.setProperty(RESOURCE_TYPE, IPARSYS_PAR);
				
				relatedResource.getResourceResolver().commit();

			} catch (PersistenceException | RepositoryException e) {
				ExceptionLogger.logException(e);
			} 
			
		}

	}
}
