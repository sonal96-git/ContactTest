package com.pwc.wcm.taglibs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.json.JSONException;
import org.apache.sling.scripting.jsp.util.TagUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.pwc.wcm.model.Link;
import com.pwc.wcm.utils.BuildLinkFieldGson;

public class LinkListLeftNavTaglib extends BaseTagLib {
	
	private static final long serialVersionUID = 5258593067504127747L;
	private static final Logger log = LoggerFactory.getLogger(LinkListLeftNavTaglib.class);

    private static final String LINKS_PROPERTY = "links";
    private static final String SOURCE_STATIC = com.day.cq.wcm.foundation.List.SOURCE_STATIC;//static
    private static final String SOURCE_CHILDREN = com.day.cq.wcm.foundation.List.SOURCE_CHILDREN;//children
    private static final String SOURCE_PROPERTY_NAME = com.day.cq.wcm.foundation.List.SOURCE_PROPERTY_NAME; //listFrom

    @Override
    protected int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
        Resource resource = request.getResource();
        ValueMap properties = ResourceUtil.getValueMap(resource);

        String source = (String) ((properties != null) ? properties.get(SOURCE_PROPERTY_NAME) : "not_found");
        if (properties != null && SOURCE_STATIC.equals(source)) {
        	doStatic(properties);
        } else if (properties != null && SOURCE_CHILDREN.equals(source)) {
        	doChildren(properties);
        } else {
        	log.warn("This taglib does not support sourc [{}]", source);
        }
        return EVAL_BODY_INCLUDE;
    }
    
	protected void doStatic(ValueMap properties) {
		try {
			List<Link> links = getLinkListFromProperties(properties);
			List<NestingAwareLink> levelAwareLinks = buildNestingAwareLinks(links);
			setComponentContextAttribute(LINKS_PROPERTY, levelAwareLinks);
		} catch (JSONException e) {
			log.error("Failed to build LinkList", e);
		}
	}
	
	protected void doChildren(ValueMap properties) {
		com.day.cq.wcm.foundation.List cqList = new com.day.cq.wcm.foundation.List(request);
		List<Link> links = getLinksFromList(cqList);
		List<NestingAwareLink> levelAwareLinks = buildNestingAwareLinks(links);
		setComponentContextAttribute(LINKS_PROPERTY, levelAwareLinks);
	}
    
    protected List<Link> getLinkListFromProperties(ValueMap properties) throws JSONException {
    	List<Link> links = new ArrayList<Link>();
    	if (properties == null) return links;
    	
    	String[] linkJson;
    	if (properties.get(LINKS_PROPERTY) instanceof String) {
            String[] ra = { (String)properties.get(LINKS_PROPERTY) };
            linkJson = ra;
        } else {
            linkJson = (String[]) properties.get(LINKS_PROPERTY);
        }
    	if (linkJson != null) {
            for (String json : linkJson) {
                if (json != null && !json.isEmpty()) {
                    Link link = BuildLinkFieldGson.buildLink(json);
                    links.add(link);
                }
            }
    	}
    	return links;
    }
    
    protected List<Link> getLinksFromList(com.day.cq.wcm.foundation.List cqList) {
		List<Link> links = new ArrayList<Link>();
		Iterator<Page> pIterator = cqList.getPages();
		while (pIterator.hasNext()) {
			Page page = pIterator.next();
			Link link = new Link(page.getPageTitle(), page.getPath(), false, null, 0);
			links.add(link);
		}
		return links;
	}
    
    /**
     * Nested links on the page end up looking like this:
     * <pre>
     * {@code
     * <ul>
     *   <li>Level 0 item A</li>
     *   <ul>
     *     <li>Level 1 item A1</li>
     *     <li>Level 1 item A2</li>
     *   </ul>
     *   <li>Level 0 item B</li>
     *   <ul>
     *     <li>Level 1 item B1</li>
     *     <ul>
     *       <li>Level 2 item B1-1</li>
     *     </ul>
     *   </ul>
     * </ul>
     * }
     * </pre>
     * 
     * {@link NestingAwareLink} objects store the meta data required by the JSTL to add the nested {@code <ul>} tags.
     * <p>
     * 
     * @param links List of {@link Link} objects to wrap with {@link NestingAwareLink}
     * @return List of {@link NestingAwareLink} objects based on the 
     * @see {@link NestingAwareLink}
     */
    protected List<NestingAwareLink> buildNestingAwareLinks(List<Link> links) {
    	List<NestingAwareLink> nestingAwareLinks = new ArrayList<NestingAwareLink>();
    	for (int i = 0; i < links.size(); i++) {
    		Link curr = links.get(i);
    		int currLevel = curr.getLevel();
    		Link next = (i + 1 < links.size()) ? links.get(i + 1) : null;
    		int nextLevel = (next == null) ? 0 : next.getLevel();
    		
    		// make sure the next level doesn't jump more than one depth in
    		// if this were to happen closing <ul> tags will be off in rendered page
    		if (next != null && nextLevel - currLevel > 1) {
    			throw new IllegalStateException("Level jump is larger than 1 for " 
    					+ curr.getText() + "[" + currLevel + "] to " 
    					+ next.getText() + "[" + nextLevel + "]");
    		}
    		
    		boolean nestedEntry 		= currLevel < nextLevel;
    		boolean nestedExit 			= currLevel > nextLevel;
    		boolean selected			= isLinkCurrentPage(curr);
    		int nestedExitDepthDelta 	= currLevel - nextLevel;
    		NestingAwareLink nestingAwareLink = new NestingAwareLink(curr, nestedEntry, nestedExit, nestedExitDepthDelta, selected);
    		nestingAwareLinks.add(nestingAwareLink);
    	}
    	return nestingAwareLinks;
    }
    
    protected boolean isLinkCurrentPage(Link link) {
    	Page currPage = (Page) pageContext.getAttribute("currentPage");
    	String currentPage = currPage.getPath();
    	return currentPage.equals(link.getUrl()) || (currentPage+".html").equals(link.getUrl());
    }
    
    /**
     * A {@link Link} that is contains metadata related to the links surrounding it in the list of links.
     * 
     * @author joel.epps
     * @see {@link #isNestedEntry()}
     * @see {@link #isNestedExit()}
     * @see {@link #getNestedExitDepthDelta()}
     */
    public static class NestingAwareLink extends Link {

    	private boolean nestedEntry;
    	private boolean nestedExit;
    	private int nestedExitDepthDelta;
    	private boolean selected;
    	
		public NestingAwareLink() {
			super();
		}

		public NestingAwareLink(Link link, boolean nestedEntry, boolean nestedExit, int nestedExitDepthDelta, boolean selected) {
			super(link.getText(), link.getUrl(), link.isOpenInNewWindow(), link.getLinkAddlCSS(), link.getLevel());
			this.nestedEntry = nestedEntry;
			this.nestedExit = nestedExit;
			this.selected = selected;
		}
		
		/**
		 * Signifies that this {@code Link} contains nested links underneath it.
		 * <p>
		 * Essentially this means that the JSTL will have to add a opening {@code <ul>} tag to this rendered link.
		 * 
		 * @return
		 */
		public boolean isNestedEntry() {
			return nestedEntry;
		}

		public void setNestedEntry(boolean nestedEntry) {
			this.nestedEntry = nestedEntry;
		}

		/**
		 * Signifies that this {@code Link} is the last link in a nested list of links.
		 * <p>
		 * Essentially this means that the JSTL will have to add {@link #getNestedExitDepthDelta()} number of closing {@code <ul>} tags.
		 * 
		 * @return
		 */
		public boolean isNestedExit() {
			return nestedExit;
		}

		public void setNestedExit(boolean nestedExit) {
			this.nestedExit = nestedExit;
		}

		/**
		 * The number of closing {@code <ul>} tags that will have to be added directly after this link item.
		 * 
		 * @return This links level depth compared to the next link following it in the list of links
		 */
		public int getNestedExitDepthDelta() {
			return nestedExitDepthDelta;
		}

		public void setNestedExitDepthDelta(int nestedExitDepthDelta) {
			this.nestedExitDepthDelta = nestedExitDepthDelta;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

    }

}
