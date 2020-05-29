package com.pwc.wcm.taglibs;

import java.util.Set;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.scripting.jsp.util.TagUtil;

/**
 * Helper tag library which exposes all component/node level properties into 
 * the JSP page context. This allows us to use JSP EL (e.g. ${prop3}) to access
 * the property values.
 * 
 * Uses:
 * 	Assuming component instance node has prop2 and prop3 properties defined.
 * 
 * 	<acrobat:defineNodeProperties propertyNames="prop2" />
 * 		Will place only prop2 in the page context.
 * 
 * 	<acrobat:defineNodeProperties/>
 * 		Will place all properties in the page context. i.e. prop2 as well as prop3.
 * 
 * @author narayan.krishnan darren.hoffman
 */
public class DefineNodePropertiesTagLib extends SimpleTagSupport {
	private String propertyNames = "";

	@Override
	public void setJspContext(JspContext pc) {
		super.setJspContext(pc);
	}

	@Override
	public void doTag() {
		SlingHttpServletRequest request = (SlingHttpServletRequest) getJspContext().getAttribute("slingRequest");

        // request is fine here.  whole page is in edit more or not
        boolean authormode = ((WCMMode.fromRequest(request) == WCMMode.EDIT) || (WCMMode.fromRequest(request) == WCMMode.DESIGN)) && !(WCMMode.fromRequest(request) == WCMMode.PREVIEW);
        request.setAttribute("isEditOrDesignMode", authormode);

        ValueMap valueMap = (ValueMap) getJspContext().getAttribute("properties");
		String[] keys;

		if (StringUtils.isBlank(propertyNames)) {
			Set<String> valueMapKeys = valueMap.keySet();
			keys = valueMapKeys.toArray(new String[valueMapKeys.size()]);
		} else {
			keys = propertyNames.split(",");
		}

		for (String key : keys) {
			//request.setAttribute(key, valueMap.get(key, ""));       // don't use the request scope
            setComponentContextAttribute(key, valueMap.get(key, "")); // store in component context for those that use it
            getJspContext().setAttribute(key, valueMap.get(key, "")); // default is to store in page context
		}
	}

	public String getPropertyNames() {
		return propertyNames;
	}

	public void setPropertyNames(String propertyNames) {
		this.propertyNames = propertyNames;
	}

    protected final void setComponentContextAttribute(String key, Object value) {
        setComponentContextAttribute(key, value, false);
    }
    protected final void setComponentContextAttribute(String key, Object value, boolean overwrite) {
        ComponentContext componentContext = WCMUtils.getComponentContext(TagUtil.getRequest((PageContext)getJspContext()));
        if(componentContext.getAttribute(key) == null || overwrite) {
            componentContext.setAttribute(key, value);
        }
    }
}


//<tag>
//<name>defineNodeProperties</name>
//<tag-class>com.acrobat.wcm.platform.taglibs.DefineNodePropertiesTagLib
//</tag-class>
//<attribute>
//	<name>propertyNames</name>
//	<required>false</required>
//	<rtexprvalue>true</rtexprvalue>
//</attribute>
//<body-content>scriptless</body-content>
//</tag>
