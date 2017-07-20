package com.brandon3055.brandonscore.lib;

import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 13/09/2016.
 *
 */
public class DLResourceLocation extends ResourceLocation {

    private final String url;
    public volatile int width = 0;
    public volatile int height = 0;
    public volatile boolean sizeSet = false;
    public volatile boolean dlFailed = false;
    public volatile boolean dlFinished = false;
    public boolean lastCheckStatus = false;
//
//    protected DLResourceLocation(int unused, String... resourceName) {
//        super(unused, resourceName);
//    }

    public DLResourceLocation(String resourceDomainIn, String url) {
        super(resourceDomainIn, url);
        this.url = url;
    }

    /**
     * @return true once when the download completes
     */
    public boolean dlStateChanged() {
        if (dlFinished && !lastCheckStatus) {
            lastCheckStatus = true;
            return true;
        }
        return false;
    }
}
