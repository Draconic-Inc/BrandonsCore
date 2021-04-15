package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 11/5/20.
 */
public class CursorHelper {

    public static final ResourceLocation DRAG = new ResourceLocation(BrandonsCore.MODID, "textures/gui/cursors/drag.png");
    public static final ResourceLocation RESIZE_H = new ResourceLocation(BrandonsCore.MODID, "textures/gui/cursors/resize_h.png");
    public static final ResourceLocation RESIZE_V = new ResourceLocation(BrandonsCore.MODID, "textures/gui/cursors/resize_v.png");
    public static final ResourceLocation RESIZE_TRBL = new ResourceLocation(BrandonsCore.MODID, "textures/gui/cursors/resize_diag_trbl.png");
    public static final ResourceLocation RESIZE_TLBR = new ResourceLocation(BrandonsCore.MODID, "textures/gui/cursors/resize_diag_tlbr.png");
    private static Map<ResourceLocation, Long> cursors = new HashMap<>();
    private static ResourceLocation active = null;

    private static long createCursor(ResourceLocation resource) {
        try {
            BufferedImage bufferedimage = ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(resource).getInputStream());
            GLFWImage glfwImage = imageToGLFWImage(bufferedimage);
            return GLFW.glfwCreateCursor(glfwImage, 16, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //TODO make this less bad xD
    private static GLFWImage imageToGLFWImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            final BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            final Graphics2D graphics = convertedImage.createGraphics();
            final int targetWidth = image.getWidth();
            final int targetHeight = image.getHeight();
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
            image = convertedImage;
        }
        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int colorSpace = image.getRGB(j, i);
                buffer.put((byte) ((colorSpace << 8) >> 24));
                buffer.put((byte) ((colorSpace << 16) >> 24));
                buffer.put((byte) ((colorSpace << 24) >> 24));
                buffer.put((byte) (colorSpace >> 24));
            }
        }
        buffer.flip();
        final GLFWImage result = GLFWImage.create();
        result.set(image.getWidth(), image.getHeight(), buffer);
        return result;
    }

    public static void closeGui(GuiOpenEvent event) {
        if (event.getGui() == null) {
            resetCursor();
        }
    }

    public static void setCursor(@Nullable ResourceLocation cursor) {
        if (cursor != active) {
            active = cursor;
            long window = Minecraft.getInstance().getWindow().getWindow();
            long newCursor = active == null ? 0 : cursors.computeIfAbsent(cursor, CursorHelper::createCursor);
            try {
                GLFW.glfwSetCursor(window, newCursor);
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void resetCursor() {
        if (active != null) {
            setCursor(null);
        }
    }
}
