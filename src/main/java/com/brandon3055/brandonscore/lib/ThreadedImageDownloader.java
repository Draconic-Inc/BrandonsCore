package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by brandon3055 on 13/09/2016.
 */
public class ThreadedImageDownloader extends SimpleTexture {

    private static final Logger LOGGER = LogHelperBC.logger;
    private DLResourceLocation dlLocation = null;
    private static final AtomicInteger TEXTURE_DOWNLOADER_THREAD_ID = new AtomicInteger(0);
    @Nullable
    private final File cacheFile;
    private final String imageUrl;
    @Nullable
    private final IImageBuffer imageBuffer;
    @Nullable
    private Thread imageThread;
    private volatile boolean textureUploaded;

    /**
     * @param cacheFile       The location to cache the downloaded image. If this file already exists it will not be re-downloaded.
     * @param imageUrl        The image URL.
     * @param textureLocation This should be a new resource location linked to some "Loading" texture. Once the image is downloaded this location will be rebound to the downloaded image automatically.
     * @param imageBuffer     Optional image buffer used to process a downloaded image.
     */
    public ThreadedImageDownloader(File cacheFile, String imageUrl, DLResourceLocation textureLocation, @Nullable IImageBuffer imageBuffer) {
        super(textureLocation);
        this.cacheFile = cacheFile;
        this.imageUrl = imageUrl;
        this.imageBuffer = imageBuffer;
    }

    /**
     * If sized location is set its width and height will be updated when the texture download is complete
     */
    public void setDlLocation(DLResourceLocation dlLocation) {
        this.dlLocation = dlLocation;
    }

    private void uploadImage(NativeImage nativeImageIn) {
        TextureUtil.prepareImage(this.getGlTextureId(), nativeImageIn.getWidth(), nativeImageIn.getHeight());
        nativeImageIn.uploadTextureSub(0, 0, 0, false);
    }

    public void setImage(NativeImage nativeImageIn) {
        if (this.imageBuffer != null) {
            this.imageBuffer.skinAvailable();
        }

        synchronized (this) {
            this.uploadImage(nativeImageIn);
            this.textureUploaded = true;
        }
    }


    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        if (!this.textureUploaded) {
            synchronized (this) {
                super.loadTexture(manager);
                this.textureUploaded = true;
            }
        }

        if (this.imageThread == null) {
            if (this.cacheFile != null && this.cacheFile.isFile()) {
                LogHelperBC.debug("Loading http texture from local cache ({})", (Object) this.cacheFile);
                NativeImage nativeimage = null;

                try {
                    nativeimage = NativeImage.read(new FileInputStream(this.cacheFile));
                    if (this.imageBuffer != null) {
                        nativeimage = this.imageBuffer.parseUserSkin(nativeimage);
                    }

                    this.setImage(nativeimage);
                    dlLocation.width = nativeimage.getWidth();
                    dlLocation.height = nativeimage.getHeight();
                    dlLocation.sizeSet = true;
                    dlLocation.dlFailed = false;
                    dlLocation.dlFinished = true;

                }
                catch (IOException ioexception) {
                    LOGGER.error("Couldn't load skin {}", this.cacheFile, ioexception);
                    this.loadTextureFromServer();
                }
                finally {
                    if (nativeimage != null) {
                        nativeimage.close();
                    }
                }
            } else {
                this.loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer() {
        this.imageThread = new Thread("Texture Downloader #" + TEXTURE_DOWNLOADER_THREAD_ID.incrementAndGet()) {
            public void run() {
                HttpURLConnection connection = null;
                LogHelperBC.debug("Downloading http texture from %s to %s", ThreadedImageDownloader.this.imageUrl, ThreadedImageDownloader.this.cacheFile);

                try {
                    connection = FileHandler.openConnection(ThreadedImageDownloader.this.imageUrl, Minecraft.getInstance().getProxy());
                    int response = connection.getResponseCode();

                    if (response / 100 == 2) {
                        InputStream inputstream;
                        if (ThreadedImageDownloader.this.cacheFile != null) {
                            FileUtils.copyInputStreamToFile(connection.getInputStream(), ThreadedImageDownloader.this.cacheFile);
                            inputstream = new FileInputStream(ThreadedImageDownloader.this.cacheFile);
                        } else {
                            inputstream = connection.getInputStream();
                        }

                        Minecraft.getInstance().execute(() -> {
                            NativeImage nativeimage = null;

                            try {
                                nativeimage = NativeImage.read(inputstream);
                                if (ThreadedImageDownloader.this.imageBuffer != null) {
                                    nativeimage = ThreadedImageDownloader.this.imageBuffer.parseUserSkin(nativeimage);
                                }

                                ThreadedImageDownloader.this.setImage(nativeimage);
                                dlLocation.width = nativeimage.getWidth();
                                dlLocation.height = nativeimage.getHeight();
                                dlLocation.sizeSet = true;
                                dlLocation.dlFailed = false;
                                dlLocation.dlFinished = true;
                            }
                            catch (IOException ioexception) {
                                LogHelperBC.warn("Error while loading texture", (Throwable) ioexception);
                            }
                            finally {
                                if (nativeimage != null) {
                                    nativeimage.close();
                                }

                                IOUtils.closeQuietly(inputstream);
                            }

                        });
                        return;
                    }
                }
                catch (Exception exception) {
                    LogHelperBC.error("Couldn\'t download http texture " + exception);
                    if (ThreadedImageDownloader.this.dlLocation != null) {
                        ThreadedImageDownloader.this.dlLocation.dlFailed = ThreadedImageDownloader.this.dlLocation.dlFinished = true;
                    }
                    return;
                }
                finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.imageThread.start();
    }

    @Override
    protected SimpleTexture.TextureData func_215246_b(IResourceManager resourceManager) {
        return SimpleTexture.TextureData.func_217799_a(resourceManager, this.textureLocation);
    }
}
