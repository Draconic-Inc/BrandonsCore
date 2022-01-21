package com.brandon3055.brandonscore.client.render;

import codechicken.lib.util.SneakyUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    public static IRenderTypeBuffer.Impl getTypeBuffer() {
        return IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
    }

    public static void endBatch(IRenderTypeBuffer getter) {
        if (getter instanceof IRenderTypeBuffer.Impl) {
            ((IRenderTypeBuffer.Impl) getter).endBatch();
        }
    }

}
