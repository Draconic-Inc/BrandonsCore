package com.brandon3055.brandonscore.api.hud;

import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Implementing this interface will do absolutely nothing. This is the base interface containing the base default methods used in both
 * {@link IHudItem} and {@link IHudBlock}
 * <p>
 * Created by brandon3055 on 19/8/21
 */
public interface IHudDisplay {
    
    @OnlyIn(Dist.CLIENT)
    default double computeHudWidth(Minecraft mc, List<ITextComponent> displayList) {
        double maxWidth = 0;
        for (ITextComponent text : displayList) {
            maxWidth = Math.max(maxWidth, mc.font.width(text));
        }
        return maxWidth + 8;
    }

    @OnlyIn(Dist.CLIENT)
    default double computeHudHeight(Minecraft mc, List<ITextComponent> displayList) {
        return (displayList.size() * 10) + 6;
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHudBackground(IRenderTypeBuffer getter, MatrixStack mStack, double width, double height, List<ITextComponent> displayList) {
        GuiHelper.drawHoverRect(getter, mStack, 0, 0, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHudContent(FontRenderer font, MatrixStack mStack, double width, double height, List<ITextComponent> displayList) {
        mStack.translate(4, 4, 0);
        for (ITextComponent text : displayList) {
            font.drawShadow(mStack, text, 0, 0, 0xFFFFFF);
            mStack.translate(0, 10, 0);
        }
    }
}
