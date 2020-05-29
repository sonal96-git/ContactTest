package com.pwc.query.controllers.models.contents;

public class FilterTag {

    private String filterText;
    private String filterValue;

    public FilterTag(String text, String value) {
    	filterText = text;
    	filterValue = value;
    }
    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }
    
    @Override
    public boolean equals(Object obj) {
    	FilterTag fTag = (FilterTag) obj;
    	return fTag.filterText.equals(filterText) && fTag.filterValue.equals(filterValue);
    }
}
