package com.pwc.model.components.download;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;
import com.pwc.util.ExceptionLogger;
import com.pwc.wcm.utils.I18nPwC;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.sling.models.factory.MissingElementsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Download extends WCMUsePojo {
    
    private SlingHttpServletRequest request;
    private Resource resource;
    private ResourceResolver resourceResolver;
    private static String LINK_TEXT_I18N_KEY = "Download_LinkText";
    private static String ICON_PATH_PROPERTY = "iconRootPathColors";
    private String iconPath;
    private String cssClass;
    private DownloadModel model;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Download.class);

    @Override
    public void activate() throws Exception {
        this.request = getRequest();
        this.resource = this.request.getResource();
        if(resource == null) return;
        this.resourceResolver = this.resource.getResourceResolver();
        if(this.resourceResolver == null) return;
        Style currentStyle = WCMUtils.getStyle(request);
        this.iconPath = currentStyle.get(ICON_PATH_PROPERTY, "");
        this.cssClass = com.day.cq.wcm.api.components.DropTarget.CSS_CLASS_PREFIX + "file";
        try {
            this.model = this.resource.adaptTo(DownloadModel.class);
            updateModel();
        } catch(MissingElementsException missingElementsExcep) {
            LOGGER.warn("One or more required properties are missing!! Skipping processing for Download component!! Error: {}", missingElementsExcep.getMessage());
        } catch(Exception e) {
            ExceptionLogger.logException(e);
        }
    }

    private void updateModel() throws JSONException {
        I18nPwC i18nPwC = new I18nPwC(request, resource);
        this.model.linktext = StringUtils.isBlank(this.model.linktext) ? i18nPwC.getPwC(LINK_TEXT_I18N_KEY) : this.model.linktext;
        if(this.model.additionalPdfFilesJSON != null && this.model.additionalPdfFilesJSON.length > 0) {
            this.model.additionalPdfFiles = new ArrayList<>();
            for(String pdfFile : this.model.additionalPdfFilesJSON) {
                JSONObject pdf = new JSONObject(pdfFile);
                if(!AdditionalFileModel.isValid(pdf)) continue;
                this.model.additionalPdfFiles.add(new AdditionalFileModel(pdf));
            }
        }
        if(this.model.additionalFilesJSON != null && this.model.additionalFilesJSON.length > 0) {
            this.model.additionalFiles = new ArrayList<>();
            for(String pdfFile : this.model.additionalFilesJSON) {
                JSONObject pdf = new JSONObject(pdfFile);
                if(!AdditionalFileModel.isValid(pdf)) continue;
                this.model.additionalFiles.add(new AdditionalFileModel(pdf));
            }
        }
        Resource fileResource = resourceResolver.getResource(this.model.file);
        if(fileResource == null) return;
        Asset asset = fileResource.adaptTo(Asset.class);
        if(asset == null) return;
        String assetTitle = asset.getMetadataValue("dc:title");
        assetTitle = assetTitle == null ? "" : assetTitle;
        this.model.title = StringUtils.isBlank(this.model.title) ? assetTitle : this.model.title;
        if (StringUtils.isBlank(this.model.description))
        {
            String assetDescription = asset.getMetadataValue("dc:description");
            assetDescription = assetDescription == null ? "" : assetDescription;
            this.model.description = StringUtils.abbreviate(assetDescription, 247);
        }
    }

    public DownloadModel getModel() {
        return model;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getCssClass() {
        return cssClass;
    }
}
