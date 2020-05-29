package com.pwc.wcm.servlet;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.commons.AbstractImageServlet;
import com.day.cq.wcm.foundation.AdaptiveImageHelper;
import com.day.cq.wcm.foundation.Image;
import com.day.image.Layer;

/**
* Servlet to render images associated with pages in a variety of supported dimensions and qualities.
* Responds to the "image" selector on a page resource called with the .jpg extension and two additional
* selectors specifying the desired width and height, respectively.
* The quality of the generated image can also be specified in an optional third selector (low, medium,
* or high).
*
* For example:
* en.image.770.360.jpg
* or,
* en.image.770.360.high.jpg
*/
/*@Component(metatype = true, label = "Adobe CQ Image Reference Modification Servlet",
       description = "Render the image associated with a page in a variety of dimensions and qualities")
*/
@Component(service = CustomThumbnailServlet.class,
property = {
		Constants.SERVICE_DESCRIPTION + "= Render the image associated with a page in a variety of dimensions and qualities",
		"sling.servlet.resourceTypes=" + NameConstants.NT_PAGE,
		"sling.servlet.selectors=" + "image",
		"sling.servlet.extensions=" + "jpg",
		"sling.servlet.extensions=" + "jpeg",
		"sling.servlet.extensions=" + "png",
		"sling.servlet.extensions=" + "gif"
})
@Designate(ocd = CustomThumbnailServlet.Config.class)
public class CustomThumbnailServlet extends AbstractImageServlet {

   private static final long serialVersionUID = 3184796694131928742L;
   private static final Logger log = LoggerFactory.getLogger(CustomThumbnailServlet.class);
   
   private List<Dimension> supportedDimensions;
   private double imageQualityFromOsgiConfig;
   
   @ObjectClassDefinition(name = "Adobe CQ Image Reference Modification Servlet", 
		   description = "Render the image associated with a page in a variety of dimensions and qualities")
   @interface Config {
       @AttributeDefinition(name = "Image Quality", 
                           description = "Quality must be a double between 0.0 and 1.0",
                           type = AttributeType.STRING)
       public String imageQuality() default "0.82";		// Default Value of Image Quality
       
       @AttributeDefinition(name = "Supported Resolutions", 
               description = "List of resolutions this component is permitted to generate.",
               type = AttributeType.STRING)
       public String[] imageResolutions() default {
           "256x192", // Category page article list images
           "370x150", // "Most popular" desktop & iPad & carousel min-width: 1px
           "480x200", // "Most popular" phone
           "127x127", // article summary phone square images
           "770x360", // article summary, desktop
           "620x290", // article summary, tablet
           "480x225", // article summary, phone (landscape)
           "320x150", // article summary, phone (portrait) and fallback
           "375x175", // 2-column article summary, desktop
           "303x142", // 2-column article summary, tablet
           "1170x400", // carousel, full
           "940x340",  // carousel min-width: 980px
           "770x300",  // carousel min-width: 768px
           "480x190"   // carousel min-width: 480px
       };
   }

   public CustomThumbnailServlet() {
       super();
       supportedDimensions = new ArrayList<Dimension>();
   }

   protected void activate(CustomThumbnailServlet.Config properties) {
       //Dictionary<String, Object> properties = componentContext.getProperties();

       // Set the fallback quality
       setImageQualityFromOsgiConfig(properties.imageQuality());

       // Set the permitted resolutions
       String[] supportedResolutionsArray = properties.imageResolutions();
       if (supportedResolutionsArray != null && supportedResolutionsArray.length > 0) {
           for (String resolution : supportedResolutionsArray) {
               String[] widthAndHeight = resolution.split("x");
               if (widthAndHeight.length == 2) {
                   try {
                       int width = Integer.parseInt(widthAndHeight[0]);
                       int height = Integer.parseInt(widthAndHeight[1]);
                       supportedDimensions.add(new Dimension(width, height));
                   } catch (NumberFormatException ex) {
                       // No-op
                   }
               }
           }
       }
   }

   /**
    * Query if this servlet has been configured to render images of the given dimensions.
    * This method could be overridden to always return true in the case where any dimension
    * combination is permitted.
    * @param width     Width of the image to render
    * @param height    Height of the image to render
    * @return          true if the given dimensions are supported, false otherwise
    */
   protected boolean isDimensionSupported(int width, int height) {
       Iterator<Dimension> iterator = getSupportedDimensionsIterator();
       Dimension dimension = new Dimension(width, height);
       while (iterator.hasNext()) {
           if (dimension.equals(iterator.next())) {
               return true;
           }
       }

       return false;
   }

   /**
    * An iterator to the collection of dimensions this servlet is configured to render.
    * @return  Iterator
    */
   protected Iterator<Dimension> getSupportedDimensionsIterator() {
       return supportedDimensions.iterator();
   }

   @Override
   protected Layer createLayer(ImageContext imageContext) throws RepositoryException, IOException {
       SlingHttpServletRequest request = imageContext.request;
       String selectors[] = request.getRequestPathInfo().getSelectors();
       // We expect exactly 3 or 4 selectors. If more or less: return null
       if ((selectors.length < 3 || selectors.length > 4) ||
               !isInteger(selectors[1]) || !isInteger(selectors[2])) {
           log.error("Expected a width and height selector.");
           return null;
       }

       // [1] width x [2] height
       int width = Integer.parseInt(selectors[1]);
       int height = Integer.parseInt(selectors[2]);
       // Ensure this is one of our supported dimension combinations
       if (isDimensionSupported(width, height) == false) {
           log.error("Unsupported dimensions requested: {} x {}.", width, height);
           return null;
       }

       Page page = imageContext.currentPage;
       Resource imageResource = null;
       // If the page does not have an image associated with it: draw a placeholder
       if (page == null || page.getContentResource() == null ||
               (imageResource = page.getContentResource().getChild("image")) == null) {
           log.error("This page does not have an image associated with it; drawing a placeholder.");
           return AdaptiveImageHelper.renderScaledPlaceholderImage(width, height);
       }

       Image image = new Image(imageResource);
       // If this image does not have a valid file reference: draw a placeholder
       if (image.getFileReference() == null || image.getFileReference().length() == 0) {
           // Pages that had an image at one point will still have the property, but no file reference
           log.error("The image associated with this page does not have a valid file reference; drawing a placeholder.");
           return AdaptiveImageHelper.renderScaledPlaceholderImage(width, height);
       }

       AdaptiveImageHelper adaptiveAdaptiveImageComponent = new AdaptiveImageHelper();

       return adaptiveAdaptiveImageComponent.scaleThisImage(image, width, height, imageContext.style);
   }

   @Override
   protected void writeLayer(SlingHttpServletRequest request, SlingHttpServletResponse response, ImageContext context, Layer layer) throws IOException, RepositoryException {
       double quality;
       // If the quality selector exists, use it
       String selectors[] = request.getRequestPathInfo().getSelectors();
       if (selectors.length == 4) {
           String imageQualitySelector = selectors[3];
           quality = getRequestedImageQuality(imageQualitySelector);
       } else {
           // If the quality selector does not exist, fall back to the default
           quality = getImageQualityFromOsgiConfig();
       }

       writeLayer(request, response, context, layer, quality);
   }

   private double getRequestedImageQuality(String imageQualitySelector) {
       // If imageQualitySelector is not a valid Quality, fall back to imageQuality from the OSGi config
       AdaptiveImageHelper.Quality newQuality = AdaptiveImageHelper.getQualityFromString(imageQualitySelector);
       if (newQuality != null ) {
           return newQuality.getQualityValue();
       }
       // Fall back to the OSGi configuration
       return getImageQualityFromOsgiConfig();
   }

   private void setImageQualityFromOsgiConfig(String imageQuality) {
       double newQuality;
       try {
           newQuality = Double.parseDouble(imageQuality);
           if (newQuality > 1 || newQuality < 0) {
               newQuality = AdaptiveImageHelper.Quality.MEDIUM.getQualityValue();
           }
       } catch (NumberFormatException ex) {
           log.error("Could not set imageQuality to: [" + imageQuality + "] because it is not a double.");
           newQuality = AdaptiveImageHelper.Quality.MEDIUM.getQualityValue();
       }
       this.imageQualityFromOsgiConfig = newQuality;
   }

   private double getImageQualityFromOsgiConfig() {
       return imageQualityFromOsgiConfig;
   }

   @Override
   protected String getImageType() {
       return "image/jpeg";
   }

   private boolean isInteger(String string) {
       try {
           Integer.parseInt(string);
       } catch(NumberFormatException e) {
           return false;
       }
       return true;
   }
}
