package com.brandon3055.brandonscore.api.hud;

import codechicken.lib.gui.modular.lib.GuiRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    default void renderHudBackground(GuiRender render, double width, double height, List<Component> displayList) {
        render.toolTipBackground(0, 0, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    default void renderHudContent(GuiRender render, double width, double height, List<Component> displayList) {
        render.pose().translate(4, 4, 0);
        for (Component text : displayList) {
            render.drawString(text, 0, 0, 0xFFFFFF, true);
            render.pose().translate(0, 10, 0);
        }
    }
}
