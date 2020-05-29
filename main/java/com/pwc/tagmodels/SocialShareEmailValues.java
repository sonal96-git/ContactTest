package com.pwc.tagmodels;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
* Created with IntelliJ IDEA.
* User: intelligrape
* Date: 9/1/14
* Time: 2:15 PM
* To change this template use File | Settings | File Templates.
*/
public class SocialShareEmailValues extends SimpleTagSupport {


String basepath ="/etc/config/socialshare/jcr:content/socialshare/email";
public void doTag() throws JspException
{
Resource resource = (Resource)getJspContext().getAttribute("resource");
  ValueMap valueMap = resource.getResourceResolver().getResource(basepath).adaptTo(ValueMap.class);
String mailpath = (String)valueMap.get("emailpath");
String emailid = (String)valueMap.get("emailid");
String mailsubject = (String)valueMap.get("emailsubject");
    PageContext pageContext = (PageContext)getJspContext();
    pageContext.setAttribute("mailpath",mailpath);
    pageContext.setAttribute("emailid",emailid);
    pageContext.setAttribute("mailsubject",mailsubject);

}     }
