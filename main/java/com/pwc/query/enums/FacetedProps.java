package com.pwc.query.enums;

public enum FacetedProps {

    FILTER_MENU("filtermenu");

    private final String prop;

    private FacetedProps(String type) {
        this.prop = type;
    }

    @Override
    public String toString() {
        return prop;
    }
}
