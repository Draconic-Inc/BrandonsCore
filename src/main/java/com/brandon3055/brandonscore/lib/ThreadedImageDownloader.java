package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by brandon3055 on 13/09/2016.
 */
public class ThreadedImageDownloader extends SimpleTexture{

    private static final ResourceLocation RESOURCE_BROKEN_DOWNLOAD = new ResourceLocation(BrandonsCore.MODID.toLowerCase() + ":textures/loading_texture_failed.png");
    private static final AtomicInteger TEXTURE_DOWNLOADER_THREAD_ID = new AtomicInteger(0);
    private final File cacheFile;
    private final String resourceUrl;
    @Nullable
    private final IImageBuffer imageBuffer;
    @Nullable
    private Thread downloadThread;
    @Nullable
    private BufferedImage bufferedImage;
    private boolean textureUploadedToGPU;
    private volatile boolean downloadFailed = false;
    private DLResourceLocation dlLocation = null;

    /**
     * @param cacheFile The location to cache the downloaded image. If this file already exists it will not be re-downloaded.
     * @param imageUrl The image URL.
     * @param textureLocation This should be a new resource location linked to some "Loading" texture. Once the image is downloaded this location will be rebound to the downloaded image automatically.
     * @param imageBuffer Optional image buffer used to process a downloaded image.
     */
    public ThreadedImageDownloader(File cacheFile, String imageUrl, ResourceLocation textureLocation, @Nullable IImageBuffer imageBuffer)
    {
        super(textureLocation);
        this.cacheFile = cacheFile;
        this.resourceUrl = imageUrl;
        this.imageBuffer = imageBuffer;
    }

    /**
     * If sized location is set its width and height will be updated when the texture download is complete
     */
    public void setDlLocation(DLResourceLocation dlLocation) {
        this.dlLocation = dlLocation;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (this.bufferedImage == null && this.textureLocation != null)
        {
            if (dlLocation != null && textureLocation instanceof DLResourceLocation && ((DLResourceLocation) textureLocation).sizeSet) {
                dlLocation.sizeSet = ((DLResourceLocation) textureLocation).sizeSet;
                dlLocation.width = ((DLResourceLocation) textureLocation).width;
                dlLocation.height = ((DLResourceLocation) textureLocation).height;
                dlLocation.dlFailed = false;
                dlLocation.dlFinished = false;
            }
            super.loadTexture(resourceManager);
        }

        if (this.downloadThread == null)
        {
            if (this.cacheFile != null && this.cacheFile.isFile())
            {
                LogHelperBC.debug("Loading texture from local cache (%s)", this.cacheFile);
                try
                {
                    this.bufferedImage = ImageIO.read(this.cacheFile);

                    if (this.imageBuffer != null)
                    {
                        this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
                    }
                    if (dlLocation != null) {
                        dlLocation.width = bufferedImage.getWidth();
                        dlLocation.height = bufferedImage.getHeight();
                        dlLocation.sizeSet = true;
                        dlLocation.dlFailed = false;
                        dlLocation.dlFinished = true;
                    }
                }
                catch (IOException ioexception)
                {
                    LogHelperBC.error("Couldn\'t load texture %s %s", this.cacheFile, ioexception);
                    this.downloadTextureFromURL();
                }
            }
            else
            {
                this.downloadTextureFromURL();
            }
        }
        else if (downloadFailed) {
            loadBrokenTexture(resourceManager);
        }
    }

    private void loadBrokenTexture(IResourceManager resourceManager) {
        this.deleteGlTexture();
        IResource iresource = null;

        try
        {
            iresource = resourceManager.getResource(RESOURCE_BROKEN_DOWNLOAD);
            BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
            boolean flag = false;
            boolean flag1 = false;

            if (iresource.hasMetadata())
            {
                try
                {
                    TextureMetadataSection texturemetadatasection = iresource.getMetadata("texture");

                    if (texturemetadatasection != null)
                    {
                        flag = texturemetadatasection.getTextureBlur();
                        flag1 = texturemetadatasection.getTextureClamp();
                    }
                }
                catch (RuntimeException runtimeexception)
                {
                    LogHelperBC.warn("Failed reading metadata of: %s %a", RESOURCE_BROKEN_DOWNLOAD, runtimeexception);
                }
            }

            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, flag, flag1);
            LogHelperBC.info(dlLocation);
            if (dlLocation != null) {
                dlLocation.width = bufferedimage.getWidth();
                dlLocation.height = bufferedimage.getHeight();
                dlLocation.sizeSet = true;
            }
        }
        catch (Exception e) {
            LogHelperBC.error("Failed to load broken texture");
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(iresource);
        }
    }

    private void checkTextureUploadedToGPU() {
        if (!this.textureUploadedToGPU)
        {
            if (this.bufferedImage != null)
            {
                if (this.textureLocation != null)
                {
                    this.deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
                this.textureUploadedToGPU = true;
            }
        }
        if (downloadFailed) {
            downloadFailed = false;
            loadBrokenTexture(Minecraft.getMinecraft().getResourceManager());
            if (dlLocation != null) {
                dlLocation.dlFailed = dlLocation.dlFinished = true;
            }
        }
    }

    public int getGlTextureId() {
        this.checkTextureUploadedToGPU();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn) {
        this.bufferedImage = bufferedImageIn;

        if (this.imageBuffer != null)
        {
            this.imageBuffer.skinAvailable();
        }
    }

    protected void downloadTextureFromURL()
    {
        this.downloadThread = new Thread("Texture Downloader #" + TEXTURE_DOWNLOADER_THREAD_ID.incrementAndGet())
        {
            public void run()
            {
                HttpURLConnection httpurlconnection = null;
                LogHelperBC.debug("Downloading http texture from %s to %s", ThreadedImageDownloader.this.resourceUrl, ThreadedImageDownloader.this.cacheFile);

                try
                {
                    httpurlconnection = (HttpURLConnection)(new URL(ThreadedImageDownloader.this.resourceUrl)).openConnection(Minecraft.getMinecraft().getProxy());
                    httpurlconnection.setDoInput(true);
                    httpurlconnection.setDoOutput(false);
                    httpurlconnection.connect();

                    if (httpurlconnection.getResponseCode() / 100 == 2)
                    {
                        BufferedImage bufferedimage;

                        if (ThreadedImageDownloader.this.cacheFile != null)
                        {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), ThreadedImageDownloader.this.cacheFile);
                            bufferedimage = ImageIO.read(ThreadedImageDownloader.this.cacheFile);
                        }
                        else
                        {
                            bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                        }

                        if (ThreadedImageDownloader.this.imageBuffer != null)
                        {
                            bufferedimage = ThreadedImageDownloader.this.imageBuffer.parseUserSkin(bufferedimage);
                        }

                        if (dlLocation != null) {
                            dlLocation.width = bufferedimage.getWidth();
                            dlLocation.height = bufferedimage.getHeight();
                            dlLocation.sizeSet = true;
                            dlLocation.dlFailed = false;
                            dlLocation.dlFinished = true;
                        }

                        ThreadedImageDownloader.this.setBufferedImage(bufferedimage);

                        return;
                    }
                    else {
                        LogHelperBC.error("Could not download resource. Server returned response code " + httpurlconnection.getResponseCode());
                        ThreadedImageDownloader.this.downloadFailed = true;
                        if (ThreadedImageDownloader.this.dlLocation != null) {
                            ThreadedImageDownloader.this.dlLocation.dlFailed =  ThreadedImageDownloader.this.dlLocation.dlFinished = true;
                        }
                    }
                }
                catch (Exception exception)
                {
                    LogHelperBC.error("Couldn\'t download http texture " + exception);
                    ThreadedImageDownloader.this.downloadFailed = true;
                    if (ThreadedImageDownloader.this.dlLocation != null) {
                        ThreadedImageDownloader.this.dlLocation.dlFailed =  ThreadedImageDownloader.this.dlLocation.dlFinished = true;
                    }
                    return;
                }
                finally
                {
                    if (httpurlconnection != null)
                    {
                        httpurlconnection.disconnect();
                    }
                }
            }
        };
        this.downloadThread.setDaemon(true);
        this.downloadThread.start();
    }
}
