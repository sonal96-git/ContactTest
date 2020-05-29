package com.pwc.model.components.assetsharing;

public interface ShareCalculator {
    String getShareUrl(SocialItem socialItem) throws Exception;
    String getBitlyAccess() throws Exception;
    String getShareUrl(String socialChannel, boolean returnEncodedUrl) throws Exception;
    String getImageUrlResizeSuffix(String socialLabel);
    String getDefaultWidth(String socialLabel);
    String getDefaultHeight(String socialLabel);
}
