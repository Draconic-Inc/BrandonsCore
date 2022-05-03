package com.brandon3055.brandonscore.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 21/09/2016.
 */
public interface IRecipeRenderer {

    int getWidth();

    int getHeight();

    Component getTitle();

    void render(Minecraft mc, int xPos, int yPos, int mouseX, int mouseY);

    void renderOverlay(Minecraft mc, int mouseX, int mouseY);

    boolean handleRecipeClick(Minecraft minecraft, double mouseX, double mouseY, boolean usage);

    @Nullable
    Object getIngredientUnderMouse(double mouseX, double mouseY);
}