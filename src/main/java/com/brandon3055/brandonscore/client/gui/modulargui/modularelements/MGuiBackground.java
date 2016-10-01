package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 1/10/2016.
 */
public class MGuiBackground extends MGuiElementBase {
    private final int textureX;
    private final int textureY;
    private final String texture;

    public MGuiBackground(IModularGui modularGui, int xPos, int yPos, int textureX, int textureY, int xSize, int ySize, String texture) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.textureX = textureX;
        this.textureY = textureY;
        this.texture = texture;
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW(texture));
        drawTexturedModalRect(xPos, yPos, textureX, textureY, xSize, ySize);
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }
}
