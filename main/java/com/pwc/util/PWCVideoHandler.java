package com.pwc.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.osgi.service.component.annotations.Component;

import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.handler.StandardImageHandler;
import com.day.image.Layer;

@Component(immediate = true, service = PWCVideoHandler.class)
public class PWCVideoHandler extends StandardImageHandler {
    public static final String CONTENT_MIMETYPE_1 = "video/pwcvideo";
    
   /* @Property(
            boolValue = {false}
    )
    private static final String ENABLE_BINARY_META_EXTRACTION = "cq.dam.enable.ext.meta.extraction";*/

    public PWCVideoHandler() {
    }

    @Override
	public String[] getMimeTypes() {

        return new String[]{"video/pwcvideo"};
    }

    @Override
	public BufferedImage getImage(Rendition rendition) throws IOException {
        return this.getImage(rendition, (Dimension)null);
    }

    @Override
	public BufferedImage getImage(Rendition rendition, Dimension maxDimension) throws IOException {
        try {
            BufferedImage image = (new Layer(rendition.getStream(), maxDimension)).getImage();
            if(1 != image.getType()) {
                BufferedImage writeableImage = new BufferedImage(image.getWidth(), image.getHeight(), 1);
                Graphics2D nag2 = writeableImage.createGraphics();
                nag2.drawRenderedImage(image, new AffineTransform());
                return writeableImage;
            } else {
                return image;
            }
        } catch (Exception var6) {
            this.log.error("failed to extract image using Layer will try the fallback", var6);
            return super.getImage(rendition, maxDimension);
        }
    }
}