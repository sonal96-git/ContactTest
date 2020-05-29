package com.pwc.query.enums;

public enum AssetProps {
	
	TAGS("cq:tags"),
	ON_TIME("onTime"),
	TITLE("dc:title"),
	ASSET("dam:Asset"),
	FORMAT("dc:format"),
	OFF_TIME("offTime"),
	VIDEO_WIDTH("width"),
	DAM_SIZE("dam:size"),
	VIDEO_URL("videourl"),
	VIDEO_HEIGHT("height"),
	DESCRIPTION("dc:description"),
    FORMAT_PDF("application/pdf"),
    FORMAT_VIDEO("video/pwcvideo"),
    VIDEO_TYPE("videoType"),
	PRIMARY_TYPE("jcr:primaryType"),
	TRANSCRIP_LINK("transcriptLink"),
	PWC_RELEASE_DATE("pwcReleaseDate"),
    EXPIRATION_DATE("prism:expirationDate");

	private final String prop;

    private AssetProps(String type) {
        this.prop = type;
    }

    @Override
    public String toString() {
        return prop;
    }
}
