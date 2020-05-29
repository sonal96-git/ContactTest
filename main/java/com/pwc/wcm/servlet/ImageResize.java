package com.pwc.wcm.servlet;

import java.awt.Rectangle;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.commons.AbstractImageServlet;
import com.day.cq.wcm.foundation.Image;
import com.day.image.Layer;
import com.day.text.Text;

/**
 * Little Tool for image Rendering for Images from the Profile including
 * Thumbnail generation.
 * To bypass default Rendering, request the Resource with <code>prof</code>
 * selector.
 * Thumbnails are requested via selector <code>{@value #THUMBNAIL}</code>
 * at second selector index.<br>
 * An additional selector can be used for the image size (since 5.4): image.prof.thumbnail.100.png<br>
 * Default size of the thumbnail is 48x48 pixels.
 * In case of normal Image, the style to use can be requested via the
 * request suffix. The suffix is expected to have a format of a path to
 * a {@link com.day.cq.wcm.api.designer.Design Design} and an optional cell-path
 * Both seperated with following sign :
 * <i><b>/-</b></i><br>
 * Example:
 * /etc/designs/geometrixx/-/par/avatar<br>
 * 
 * Re-written the prof.thumbnail service to remove the black stripes on both sides
 * Removed the image aspect ratio calculation and background layer
 */
@Component(service = Servlet.class, immediate = true,
property = {
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.resourceTypes=" + "nt:file",
    "sling.servlet.extensions=" + "jpg",
    "sling.servlet.extensions=" + "png",
    "sling.servlet.extensions=" + "gif",
    "sling.servlet.selectors=" + "pwcimage"
})
public class ImageResize extends AbstractImageServlet {

    private static final int ICON_WIDTH = 48;
    private static final int ICON_HEIGHT = 48;

    @Override
    protected Layer createLayer(ImageContext c) throws RepositoryException, IOException {
        Image image = new Image(c.resource.getResourceResolver().getResource(c.resource, ".."));
        image.setItemName(Image.NN_FILE, Text.getName(c.resource.getPath()));
        if (!image.hasContent()) {
            return null;
        }
        String[] selectors = c.request.getRequestPathInfo().getSelectors();

        int width = 0;
        try {
            width = Integer.parseInt(selectors[1]);
        } catch (Exception e) {
        }
        if (width == 0) {
            // no selectors for width and height defined >> defaults
            width = ICON_WIDTH;
        }
        int height = 0;
        try {
        	height = Integer.parseInt(selectors[2]);
        } catch (Exception e) {
        }
        if (height == 0) {
            // no selectors for width and height defined >> defaults
        	height = ICON_HEIGHT;
        }
        Layer org = image.getLayer(false, false, false);
        //Height would be as per aspect ratio
        /*int w = org.getWidth();
        int h = org.getHeight();
        float ratio;
        ratio = w / (float) width;
        h = (int) Math.floor(h / ratio);
        org.resize(width, h);*/
        int orgWidth = org.getWidth();
        int orgHeight = org.getHeight();

        if(width > 0 && width <= orgWidth || height > 0 && height <= orgHeight ) {

            double scaleRatio = 0.0d;
            double wScaleRatio = (double) width / (double)orgWidth;
            double hScaleRatio =  (double) height / (double) orgHeight;

            scaleRatio = (wScaleRatio > hScaleRatio) ? wScaleRatio : hScaleRatio;

            int newWidth=(int) (orgWidth*scaleRatio);
            int newHeight=(int) (orgHeight*scaleRatio);

            int topX = (newWidth - width) / 2;

            int topY = (newHeight - height) / 2;

            Rectangle rectangle = new Rectangle(topX, topY, width, height);


            org.resize(newWidth , newHeight);
            org.crop(rectangle);

        }
        
        return org;
    }

    @Override
    protected void writeLayer(SlingHttpServletRequest request, SlingHttpServletResponse response, ImageContext context, Layer layer) throws IOException, RepositoryException {
        if (layer == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            super.writeLayer(request, response, context, layer);
        }
    }

    @Override
    protected String getImageType(String ext) {
        if ("res".equals(ext)) {
            // return a dummy image type (return null would result in a 404 in the super class)
            return "res";
        }
        return super.getImageType(ext);
    }
}

