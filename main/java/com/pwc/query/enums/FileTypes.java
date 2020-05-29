package com.pwc.query.enums;

public enum FileTypes {

	HTML("html"),
	PDF("pdf"),
	VIDEO("video");
	
	private final String type;

    private FileTypes(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
