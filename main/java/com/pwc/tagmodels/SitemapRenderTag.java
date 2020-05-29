/*
 * 
 */
package com.pwc.tagmodels;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SitemapRenderTag extends SimpleTagSupport {
	private static final Logger logger = LoggerFactory.getLogger(SitemapRenderTag.class);
	String locale;

	public void setLocale(final String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return this.locale;
	}

	@Override
	public void doTag() throws JspException {
		logger.info(":: Inside SitemapRenderTag ::");
		logger.info(" ===== " + this.locale);
		try {
			final Resource resource = (Resource) getJspContext().getAttribute("resource");

			final Session session = resource.getResourceResolver().adaptTo(Session.class);

			final PageContext pageContext = (PageContext) getJspContext();

			final String path = "/content/geometrixx/en/sitemap.xml/jcr:content";
			if (session.nodeExists(path)) {
				final Node node = session.getNode(path);
				final InputStream inputStream = node.getProperty("jcr:data").getBinary().getStream();
				final BufferedInputStream bis = new BufferedInputStream(inputStream);
				final DataInputStream dis = new DataInputStream(bis);
				String final_str = "";
				while (dis.available() != 0) {
					final String currentLine = dis.readLine();
					final_str = final_str + currentLine + System.getProperty("line.separator");
				}
				logger.info(final_str);
				pageContext.setAttribute("sitemap", final_str);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}

