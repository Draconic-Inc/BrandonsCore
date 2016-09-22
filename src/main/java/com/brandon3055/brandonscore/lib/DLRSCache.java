package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.FileHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 13/09/2016.
 * Downloadable Resource Cache
 */
public class DLRSCache {

    public static final DLResourceLocation DOWNLOADING_TEXTURE = new DLResourceLocation(BrandonsCore.MODID.toLowerCase(), "textures/loading_texture.png");
    static {
        DOWNLOADING_TEXTURE.height = 16;
        DOWNLOADING_TEXTURE.width = 16;
        DOWNLOADING_TEXTURE.sizeSet = true;
    }
    private static Map<String, DLResourceLocation> resourceCache = new HashMap<String, DLResourceLocation>();
    private static File cacheFolder;

    public static void initialize() {
        cacheFolder = new File(FileHandler.brandon3055Folder, "ResourceCache");
        cacheFolder.mkdirs();
    }


    public static DLResourceLocation getResource(String url) {
        if (!resourceCache.containsKey(url)) {

            DLResourceLocation resourceLocation = new DLResourceLocation(BrandonsCore.MODID.toLowerCase(), url);
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();

            File cache = new File(cacheFolder, "Cache#" + url.hashCode() + ".png");

            ThreadedImageDownloader downloader = new ThreadedImageDownloader(cache, url, DOWNLOADING_TEXTURE, null);
            downloader.setDlLocation(resourceLocation);
            texturemanager.loadTexture(resourceLocation, downloader);

            resourceCache.put(url, resourceLocation);
        }

        return resourceCache.get(url);
    }

    public static void clearResourceCache() {
        resourceCache.clear();
    }

    public static void clearFileCache() {
        clearResourceCache();
        File[] files = cacheFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("Cache#")) {
                    file.delete();
                }
            }
        }
    }

    public static void clearResourceCache(String url) {
        resourceCache.remove(url);
    }

    public static void clearFileCache(String url) {
        clearResourceCache(url);
        File[] files = cacheFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("Cache#" + url.hashCode() + ".png")) {
                    file.delete();
                    return;
                }
            }
        }
    }
}
