package com.pwc.query.enums;

public enum ControllerType {

    COLLECTION("collection"),
    FACETED("faceted");

    private final String type;

    private ControllerType(String type) {
    this.type = type;
    }

    @Override
    public String toString() {
return type;
}
}
