package com.pwc.model.components.download;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Model(adaptables = Resource.class)
public class DownloadModel {
    @Inject @Optional
    public String file;

    @Inject @Optional
    public String linktext;

    @Inject @Optional
    public String title;

    @Inject @Optional @Named("jcr:description")
    public String description;

    @Inject @Optional
    public Boolean showThumbnail;

    @Inject @Optional
    public String display;

    @Inject @Optional
    public String closeText;

    @Inject @Optional
    public String openText;

    @Inject @Optional
    public String furtherLanguages;

    @Inject @Optional
    public String otherDocuments;

    @Inject @Optional @Named("additionalpdfsfiles")
    public String[] additionalPdfFilesJSON;

    @Inject @Optional @Named("additionalfiles")
    public String[] additionalFilesJSON;

    public List<AdditionalFileModel> additionalPdfFiles;

    public List<AdditionalFileModel> additionalFiles;

    public List<AdditionalFileModel> getAdditionalPdfFiles() {
        return additionalPdfFiles;
    }

    public List<AdditionalFileModel> getAdditionalFiles() {
        return additionalFiles;
    }
}
