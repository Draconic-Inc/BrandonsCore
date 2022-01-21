package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Created by brandon3055 on 13/09/2016.
 */
public class ThreadedImageDownloader extends SimpleTexture {

    private static final Logger LOGGER = LogHelperBC.logger;
    private DLResourceLocation dlLocation = null;

    @Nullable
    private final File cacheFile;
    private final String imageUrl;
    @Nullable
    private CompletableFuture<?> future;
    private boolean textureUploaded;

    /**
     * @param cacheFile       The location to cache the downloaded image. If this file already exists it will not be re-downloaded.
     * @param imageUrl        The image URL.
     * @param textureLocation This should be a new resource location linked to some "Loading" texture. Once the image is downloaded this location will be rebound to the downloaded image automatically.
     */
    public ThreadedImageDownloader(File cacheFile, String imageUrl, DLResourceLocation textureLocation) {
        super(textureLocation);
        this.cacheFile = cacheFile;
        this.imageUrl = imageUrl;
    }

    /**
     * If sized location is set its width and height will be updated when the texture download is complete
     */
    public void setDlLocation(DLResourceLocation dlLocation) {
        this.dlLocation = dlLocation;
    }

    private void upload(NativeImage imageIn) {
        TextureUtil.prepareImage(this.getId(), imageIn.getWidth(), imageIn.getHeight());
        imageIn.upload(0, 0, 0, true);
    }

    private void setImage(NativeImage nativeImageIn) {
        Minecraft.getInstance().execute(() -> {
            this.textureUploaded = true;
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> {
                    this.upload(nativeImageIn);
                });
            } else {
                this.upload(nativeImageIn);
            }

        });

        dlLocation.width = nativeImageIn.getWidth();
        dlLocation.height = nativeImageIn.getHeight();
        dlLocation.sizeSet = true;
        dlLocation.dlFailed = false;
        dlLocation.dlFinished = true;
    }


    @Override
    public void load(IResourceManager manager) throws IOException {
        if (!this.textureUploaded) {
            synchronized (this) {
                super.load(manager);
                this.textureUploaded = true;
            }
        }

        if (this.future == null) {
            NativeImage nativeimage;
            if (this.cacheFile != null && this.cacheFile.isFile()) {
                LOGGER.debug("Loading http texture from local cache ({})", (Object) this.cacheFile);
                FileInputStream fileinputstream = new FileInputStream(this.cacheFile);
                nativeimage = this.loadTexture(fileinputstream);
            } else {
                nativeimage = null;
            }

            if (nativeimage != null) {
                this.setImage(nativeimage);
            } else {
                this.future = CompletableFuture.runAsync(() -> {
                    HttpURLConnection httpurlconnection = null;
                    LOGGER.debug("Downloading http texture from {} to {}", this.imageUrl, this.cacheFile);
                    try {
                        httpurlconnection = FileHandler.openConnection(this.imageUrl, Minecraft.getInstance().getProxy());
//                        httpurlconnection = (HttpURLConnection) (new URL(this.imageUrl)).openConnection(Minecraft.getInstance().getProxy());
                        httpurlconnection.connect();
                        int responseCode = httpurlconnection.getResponseCode();
                        if (responseCode / 100 == 2) {
                            InputStream inputstream;
                            if (this.cacheFile != null) {
                                FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.cacheFile);
                                inputstream = new FileInputStream(this.cacheFile);
                            } else {
                                inputstream = httpurlconnection.getInputStream();
                            }

                            Minecraft.getInstance().execute(() -> {
                                NativeImage nativeimage1 = this.loadTexture(inputstream);
                                if (nativeimage1 != null) {
                                    this.setImage(nativeimage1);
                                }
                            });
                            return;
                        } else {
                            dlLocation.dlFailed = dlLocation.dlFinished = true;
                        }
                    }
                    catch (Throwable exception) {
                        LOGGER.error("Couldn't download http texture", (Throwable) exception);
                        dlLocation.dlFailed = dlLocation.dlFinished = true;
                        return;
                    }
                    finally {
                        if (httpurlconnection != null) {
                            httpurlconnection.disconnect();
                        }

                    }

                }, Util.backgroundExecutor());
            }
        }
    }

    @Nullable
    private NativeImage loadTexture(InputStream inputStreamIn) {
        NativeImage nativeimage = null;

        try {
            nativeimage = NativeImage.read(inputStreamIn);
        }
        catch (IOException ioexception) {
            LOGGER.warn("Error while loading the skin texture", (Throwable) ioexception);
        }

        return nativeimage;
    }
}
