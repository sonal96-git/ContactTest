package com.pwc.wcm.model;

import java.util.List;

/**
 * Created by rjiang022 on 3/25/2015.
 */
public class AkamaiConfig {
    private boolean enableCachePurge;
    private boolean purgeSinglePage;
    private boolean enablePurgeAsset;


    public boolean isEnableCachePurge() {
        return enableCachePurge;
    }

    public void setEnableCachePurge(boolean enableCachePurge) {
        this.enableCachePurge = enableCachePurge;
    }


    public boolean isEnablePurgeAsset() {
        return enablePurgeAsset;
    }

    public void setEnablePurgeAsset(boolean enablePurgeAsset) {
        this.enablePurgeAsset = enablePurgeAsset;
    }

    public boolean isPurgeSinglePage() {
        return purgeSinglePage;
    }

    public void setPurgeSinglePage(boolean purgeSinglePage) {
        this.purgeSinglePage = purgeSinglePage;
    }
}
