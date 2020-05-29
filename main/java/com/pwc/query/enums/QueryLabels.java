package com.pwc.query.enums;

public enum QueryLabels {

	DESC("desc"),
	TRUE("true"),
	PAGE("[cq:Page]"),
	ASSET("[dam:Asset]"),
	PDF("application/pdf"),
	VIDEO("video/pwcvideo"),
	TAGS_BACKLINKS("cq:backlinks"),
	PRIMARY_TYPE("[jcr:primaryType]"),
	HIDE_LEVEL("[jcr:content/hide_level]"),
	PWC_RELEASE_DATE_PAGE("[jcr:content/pwcReleaseDate]"),
	PWC_RELEASE_DATE_ASSETS("[jcr:content/metadata/pwcReleaseDate]"),
	JCR_CONTENT_PAGE_TAGS("[jcr:content/cq:tags]"),
	JCR_CONTENT_ASSET_TAGS("[jcr:content/metadata/cq:tags]"),
	JCR_CONTENT_PAGE_TITLE("LOWER([jcr:content/jcr:title])"),
	JCR_CONTENT_COLLECTION_TITLE("LOWER([jcr:content/pwc_rvp_title])"),
	JCR_CONTENT_ASSET_TITLE("LOWER([jcr:content/metadata/dc:title])"),
	JCR_CONTENT_ASSET_FORMAT("[jcr:content/metadata/dc:format]"),
	JCR_CONTENT_PAGE_DESCRIPTION("LOWER([jcr:content/jcr:description])"),
	JCR_CONTENT_ASSET_DESCRIPTION("LOWER([jcr:content/metadata/dc:description])"),
	JCR_CONTENT_PAGE_TAGSQB("jcr:content/cq:tags"),
	JCR_CONTENT_ASSET_TAGSQB("jcr:content/metadata/cq:tags"),
	JCR_CONTENT_PAGE_TITLEQB("fn:lower-case(jcr:content/jcr:title)"),
	JCR_CONTENT_COLLECTION_TITLEQB("fn:lower-case(jcr:content/pwc_rvp_title)"),
	JCR_CONTENT_ASSET_TITLEQB("fn:lower-case(jcr:content/metadata/dc:title)"),
	JCR_CONTENT_ASSET_FORMATQB("jcr:content/metadata/dc:format"),
	JCR_CONTENT_PAGE_DESCRIPTIONQB("fn:lower-case(jcr:content/jcr:description)"),
	JCR_CONTENT_ASSET_DESCRIPTIONQB("fn:lower-case(jcr:content/metadata/dc:description)"),
	HIDE_LEVEL_PROPERTY("jcr:content/hide_level"),
	UNEQUALS("unequals"),
	NOT("not");

	private final String label;

    private QueryLabels(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
