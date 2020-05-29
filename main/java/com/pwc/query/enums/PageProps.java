package com.pwc.query.enums;

public enum PageProps {

	PRIMARY_TYPE("jcr:primaryType"),
	PWC_RELEASE_DATE("pwcReleaseDate"),
	TITLE("jcr:title"),
	DESCRIPTION("jcr:description"),
	HIDE_LEVEL("hide_level"),
	PWC_RVP_TITLE("pwc_rvp_title"),
	TAGS("cq:tags"),
	IMAGE("image"),
	PAGE("cq:Page");
	
	private final String prop;

    private PageProps(String type) {
        this.prop = type;
    }

    @Override
    public String toString() {
        return prop;
    }
}
