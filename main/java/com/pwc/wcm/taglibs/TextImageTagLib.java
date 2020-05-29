package com.pwc.wcm.taglibs;

import com.day.cq.widget.Doctype;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.DropTarget;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.foundation.Image;
import com.day.cq.wcm.foundation.Placeholder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.scripting.jsp.util.TagUtil;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ken.mitsumoto
 * Date: 10/9/13
 * Time: 3:12 PM
 */
public class TextImageTagLib  extends BaseTagLib {

    private final static String IMAGE_ALIGNMENT_PROPERTY = "imageAlignment";

    private boolean isAuthoringUIModeTouch;
    private Image image;
    private String ddClassName;

    @Override
    public int startTag() {
        SlingHttpServletRequest request = TagUtil.getRequest(pageContext);
        Resource resource = request.getResource();
        ValueMap properties = ResourceUtil.getValueMap(resource);
        Style currentStyle = WCMUtils.getStyle(request);

        isAuthoringUIModeTouch = Placeholder.isAuthoringUIModeTouch(request);
        request.setAttribute("isAuthoringUIModeTouch", isAuthoringUIModeTouch);
        image = new Image(resource, "image");

        // don't draw the placeholder in case UI mode touch it will be handled afterwards
        if (isAuthoringUIModeTouch) {
            image.setNoPlaceholder(true);
        }

        //drop target css class = dd prefix + name of the drop target in the edit config
        ddClassName = DropTarget.CSS_CLASS_PREFIX + "image";

        if (image.hasContent() || WCMMode.fromRequest(request) == WCMMode.EDIT) {
            image.loadStyleData(currentStyle);
            // add design information if not default (i.e. for reference paras)

            Design currentDesign  = (Design)pageContext.getAttribute("currentDesign");
            Design resourceDesign = (Design)pageContext.getAttribute("resourceDesign");

            if (!currentDesign.equals(resourceDesign)) {
                image.setSuffix(currentDesign.getId());
            }
            image.addCssClass(ddClassName);
            image.setSelector(".img");
            image.setImageDoctype(Doctype.fromRequest(request));

            if(properties.containsKey(IMAGE_ALIGNMENT_PROPERTY)) {
                image.addCssClass(properties.get(IMAGE_ALIGNMENT_PROPERTY).toString());
            }

            request.setAttribute("divId", "cq-textimage-jsp-" + resource.getPath());
            request.setAttribute("imageHeight", image.get(image.getItemName(Image.PN_HEIGHT)));
            request.setAttribute("image", image);
            request.setAttribute("bgImageSrc", image.getSrc());
        }

        request.setAttribute("placeholder",
                (isAuthoringUIModeTouch && !image.hasContent())
                ? Placeholder.getDefaultPlaceholder(request, (Component)pageContext.getAttribute("component"), "", ddClassName)
                : "");

        return EVAL_BODY_INCLUDE;
    }

}
