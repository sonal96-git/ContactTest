package com.pwc.query.enums;

public enum PlayerOption {

	WIDTH("100%"),
	HEIGHT("100%"),
	ASPECTRATIO("12:5"),
	FLASH_PLAYER("/apps/settings/wcm/designs/pwc/videoplayer/jwplayer.flash.swf");
	
	private final String opt;

    private PlayerOption(String type) {
        this.opt = type;
    }

    @Override
    public String toString() {
        return opt;
    }
    
}
