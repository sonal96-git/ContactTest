package com.pwc.query.enums;

public enum I18nCollection {
    PREVIOUS_LABEL("Collection_Previous_Label"),
    CURRENT_LABEL("Collection_Current_Label"),
    FILTERALL_LABEL("Collection_Filter_All_Label"),
    NAVIGATIONNEXT_LABEL("Navigation_Next"),
    NAVIGATIONPREVIOUS_LABEL("Navigation_Previous"),
    LOADMORE_LABEL("Collection_Load_More"),
    VIEWALL_LABEL("Collection_View_All"),
    DOWNLOAD_PDF("Collection_Download_PDF"),
    SHOW_ALL_LABEL("Collection_Show_All"),
    OR_LABEL("Collection_Or_Label"),
    FILTER_BY_LABEL("Collection_Filter_By_Label");

    private final String i18n;

    private I18nCollection(String i18n) {
        this.i18n = i18n;
    }

    @Override
    public String toString() {
        return i18n;
    }
}
