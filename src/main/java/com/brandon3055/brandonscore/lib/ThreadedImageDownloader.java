package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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


//    private static final AtomicInteger TEXTURE_DOWNLOADER_THREAD_ID = new AtomicInteger(0);
//    @Nullable
//    private final File cacheFile;
//    private final String imageUrl;
//    @Nullable
//    private final IImageBuffer imageBuffer;
//    @Nullable
//    private Thread imageThread;
//    private volatile boolean textureUploaded;

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
                        httpurlconnection = (HttpURLConnection) (new URL(this.imageUrl)).openConnection(Minecraft.getInstance().getProxy());
                        httpurlconnection.setDoInput(true);
                        httpurlconnection.setDoOutput(false);
                        httpurlconnection.connect();
                        if (httpurlconnection.getResponseCode() / 100 == 2) {
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
                        }
                    }
                    catch (Exception exception) {
                        LOGGER.error("Couldn't download http texture", (Throwable) exception);
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

//        if (this.future == null) {
//            NativeImage nativeimage = null;
//            if (this.cacheFile != null && this.cacheFile.isFile()) {
//                LogHelperBC.debug("Loading http texture from local cache ({})", (Object) this.cacheFile);
//
//                try {
//                    nativeimage = NativeImage.read(new FileInputStream(this.cacheFile));
//                    this.setImage(nativeimage);
//                    dlLocation.width = nativeimage.getWidth();
//                    dlLocation.height = nativeimage.getHeight();
//                    dlLocation.sizeSet = true;
//                    dlLocation.dlFailed = false;
//                    dlLocation.dlFinished = true;
//
//                }
//                catch (IOException ioexception) {
//                    LOGGER.error("Couldn't load skin {}", this.cacheFile, ioexception);
//                    this.loadTextureFromServer();
//                }
//                finally {
//                    if (nativeimage != null) {
//                        nativeimage.close();
//                    }
//                }
//            } else {
//                this.loadTextureFromServer();
//            }
//        }
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

//    protected void loadTextureFromServer() {
//        this.imageThread = new Thread("Texture Downloader #" + TEXTURE_DOWNLOADER_THREAD_ID.incrementAndGet()) {
//            public void run() {
//                HttpURLConnection connection = null;
//                LogHelperBC.debug("Downloading http texture from %s to %s", ThreadedImageDownloader.this.imageUrl, ThreadedImageDownloader.this.cacheFile);
//
//                try {
//                    connection = FileHandler.openConnection(ThreadedImageDownloader.this.imageUrl, Minecraft.getInstance().getProxy());
//                    int response = connection.getResponseCode();
//
//                    if (response / 100 == 2) {
//                        InputStream inputstream;
//                        if (ThreadedImageDownloader.this.cacheFile != null) {
//                            FileUtils.copyInputStreamToFile(connection.getInputStream(), ThreadedImageDownloader.this.cacheFile);
//                            inputstream = new FileInputStream(ThreadedImageDownloader.this.cacheFile);
//                        } else {
//                            inputstream = connection.getInputStream();
//                        }
//
//                        Minecraft.getInstance().execute(() -> {
//                            NativeImage nativeimage = null;
//
//                            try {
//                                nativeimage = NativeImage.read(inputstream);
//                                if (ThreadedImageDownloader.this.imageBuffer != null) {
//                                    nativeimage = ThreadedImageDownloader.this.imageBuffer.parseUserSkin(nativeimage);
//                                }
//
//                                ThreadedImageDownloader.this.setImage(nativeimage);
//                                dlLocation.width = nativeimage.getWidth();
//                                dlLocation.height = nativeimage.getHeight();
//                                dlLocation.sizeSet = true;
//                                dlLocation.dlFailed = false;
//                                dlLocation.dlFinished = true;
//                            }
//                            catch (IOException ioexception) {
//                                LogHelperBC.warn("Error while loading texture", (Throwable) ioexception);
//                            }
//                            finally {
//                                if (nativeimage != null) {
//                                    nativeimage.close();
//                                }
//
//                                IOUtils.closeQuietly(inputstream);
//                            }
//
//                        });
//                        return;
//                    }
//                }
//                catch (Exception exception) {
//                    LogHelperBC.error("Couldn\'t download http texture " + exception);
//                    if (ThreadedImageDownloader.this.dlLocation != null) {
//                        ThreadedImageDownloader.this.dlLocation.dlFailed = ThreadedImageDownloader.this.dlLocation.dlFinished = true;
//                    }
//                    return;
//                }
//                finally {
//                    if (connection != null) {
//                        connection.disconnect();
//                    }
//                }
//            }
//        };
//        this.imageThread.setDaemon(true);
//        this.imageThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
//        this.imageThread.start();
//    }

//    @Override
//    protected SimpleTexture.TextureData getTextureImage(IResourceManager resourceManager) {
//        return SimpleTexture.TextureData.load(resourceManager, this.textureLocation);
//    }
}
