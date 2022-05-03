package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    public static MultiBufferSource.BufferSource getTypeBuffer() {
        return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    }

    public static void endBatch(MultiBufferSource getter) {
        if (getter instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) getter).endBatch();
        }
    }

}
