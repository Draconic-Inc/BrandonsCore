package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    /**
     * * @return The buffer source used for GUI rendering. You must ALWAYS call endBatch on this when you are done with it.
     */
    public static MultiBufferSource.BufferSource getGuiBuffers() {
        return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    }

    public static MultiBufferSource.BufferSource getBuffers() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public static void endBatch(MultiBufferSource getter) {
        if (getter instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) getter).endBatch();
        }
    }
}