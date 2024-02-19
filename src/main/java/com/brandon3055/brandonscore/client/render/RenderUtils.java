package com.brandon3055.brandonscore.client.render;

import codechicken.lib.gui.modular.lib.GuiRender;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    public static RenderType FAN_TYPE = RenderType.create("tri_fan_type", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN, 256, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//            .setAlphaState(RenderStateShard.NO_ALPHA)
//            .setTexturingState(new RenderStateShard.TexturingStateShard("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
                    .createCompositeState(false)
    );

    /**
     * * @return The buffer source used for GUI rendering. You must ALWAYS call endBatch on this when you are done with it.
     */
    @Deprecated
    public static MultiBufferSource.BufferSource getGuiBuffers() {
        return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    }

    @Deprecated
    public static MultiBufferSource.BufferSource getBuffers() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    @Deprecated
    public static void endBatch(MultiBufferSource getter) {
        if (getter instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) getter).endBatch();
        }
    }

    public static void drawPieProgress(GuiRender render, double x, double y, double diameter, double progress, double offsetAngle, int colour) {
        drawPieProgress(render, x, y, diameter, progress, offsetAngle, colour, colour);
    }

    public static void drawPieProgress(GuiRender render, double x, double y, double diameter, double progress, double offsetAngle, int innerColour, int outerColour) {
        double radius = diameter / 2;
        VertexConsumer builder = new TransformingVertexConsumer(render.buffers().getBuffer(FAN_TYPE), render.pose());
        builder.vertex(x + radius, y + radius, 0).color(innerColour).endVertex();
        for (double d = 0; d <= 1; d += 1D / 30D) {
            double angle = (d * progress) + 0.5 - progress;
            angle *= Math.PI * 2;
            angle += MathHelper.torad * offsetAngle;
            double vertX = x + radius + Math.sin(angle) * radius;
            double vertY = y + radius + Math.cos(angle) * radius;
            builder.vertex(vertX, vertY, 0).color(outerColour).endVertex();
        }
    }
}