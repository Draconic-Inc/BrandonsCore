package com.brandon3055.brandonscore.api.hud;

import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
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
    default double computeHudWidth(Minecraft mc, List<Component> displayList) {
        double maxWidth = 0;
        for (Component text : displayList) {
            maxWidth = Math.max(maxWidth, mc.font.width(text));
        }
        return maxWidth + 8;
    }

    @OnlyIn(Dist.CLIENT)
    default double computeHudHeight(Minecraft mc, List<Component> displayList) {
        return (displayList.size() * 10) + 6;
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHudBackground(MultiBufferSource getter, PoseStack mStack, double width, double height, List<Component> displayList) {
        GuiHelper.drawHoverRect(getter, mStack, 0, 0, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHudContent(Font font, PoseStack mStack, double width, double height, List<Component> displayList) {
        mStack.translate(4, 4, 0);
        for (Component text : displayList) {
            font.drawShadow(mStack, text, 0, 0, 0xFFFFFF);
            mStack.translate(0, 10, 0);
        }
    }
}
