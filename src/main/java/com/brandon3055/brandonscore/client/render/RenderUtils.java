package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;

/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    public static void drawTextInWorld(Minecraft mc, String text, double x, double y, double z, int color) {
        drawTextInWorld(mc, text, x, y, z, color, 0x7f000000, 1F);
    }

    public static void drawTextInWorld(Minecraft mc, String text, double x, double y, double z, int color, int backgroundColour, float scale) {
        int strWidth = mc.fontRenderer.getStringWidth(text);
        int strCenter = strWidth / 2;
        int yOffset = -4;

        EntityRendererManager renderManager = mc.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(-0.025F * scale, -0.025F * scale, 0.025F * scale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GuiHelper.drawColouredRect(-strCenter - 1, yOffset - 1, strWidth + 1, 9, backgroundColour);

        GlStateManager.depthMask(true);
        mc.fontRenderer.drawString(text, -strCenter, yOffset, color);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}
