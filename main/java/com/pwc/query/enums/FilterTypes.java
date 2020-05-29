package com.pwc.query.enums;

public enum FilterTypes {
	
	FACETED("Faceted"),
	COLLECTION("Collection"),
	CONTACT("Contact");
	
	private final String type;

    private FilterTypes(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
