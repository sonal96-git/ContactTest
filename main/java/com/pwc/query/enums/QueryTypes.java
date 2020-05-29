package com.pwc.query.enums;

public enum QueryTypes {

	CHILDREN("children"),
	DESCENDANTS("descendants");
	
	private final String type;

    private QueryTypes(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
