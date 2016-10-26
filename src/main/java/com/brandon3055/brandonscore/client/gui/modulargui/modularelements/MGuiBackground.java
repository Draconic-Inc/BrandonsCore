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
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
        ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW(texture));
        drawTexturedModalRect(xPos, yPos, textureX, textureY, xSize, ySize);
    }

    public static MGuiBackground newGenericBackground(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        return new MGuiBackground(modularGui, xPos, yPos, 0, 0, xSize, ySize, ""){
            @Override
            public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW("brandonscore:textures/gui/base_gui.png"));
                drawTexturedModalRect(xPos, yPos, 0, 0, xSize / 2, ySize / 2);
                drawTexturedModalRect(xPos + (xSize / 2), yPos, 255 - (xSize / 2), 0, xSize / 2, ySize / 2);

                drawTexturedModalRect(xPos, yPos + (ySize / 2), 0, 255 - (ySize / 2), xSize / 2, ySize / 2);
                drawTexturedModalRect(xPos + (xSize / 2), yPos + (ySize / 2), 255 - (xSize / 2), 255 - (ySize / 2), xSize / 2, ySize / 2);

            }
        };
    }

}
