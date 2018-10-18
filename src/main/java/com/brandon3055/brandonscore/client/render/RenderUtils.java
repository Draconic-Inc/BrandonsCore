package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

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

        RenderManager renderManager = mc.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GuiHelper.drawColouredRect(-strCenter - 1, yOffset - 1, strWidth + 1, 9, backgroundColour);

        GlStateManager.depthMask(true);
        mc.fontRenderer.drawString(text, -strCenter, yOffset, color);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}
