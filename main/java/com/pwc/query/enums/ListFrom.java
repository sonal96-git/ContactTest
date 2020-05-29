package com.pwc.query.enums;

public enum ListFrom {
	
	CHILDREN("children"),
	TREE("tree"),
	STATIC("static"),
	QUERYBUILDER("querybuilder"),
	ADVANCEDPATHS("advancedpaths"),
	ADVANCEDTAGS("advancedtags"),
	TAGS("tags");
	
	private final String from;

    private ListFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return from;
    }
}
