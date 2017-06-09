package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 1/10/2016.
 */
public class MGuiBackground extends MGuiElementBase {
    public int textureX;
    public int textureY;
    public int textureSizeX = 256;
    public int textureSizeY = 256;
    public String texture;

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
        drawModalRectWithCustomSizedTexture(xPos, yPos, textureX, textureY, xSize, ySize, textureSizeX, textureSizeY);
    }

    public MGuiBackground setTexturePos(int textureX, int textureY) {
        this.textureX = textureX;
        this.textureY = textureY;
        return this;
    }

    public MGuiBackground setTextureSize(int textureSize) {
        return setTextureSize(textureSize, textureSize);
    }

    public MGuiBackground setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public MGuiBackground setTextureSize(int textureSizeX, int textureSizeY) {
        this.textureSizeX = textureSizeX;
        this.textureSizeY = textureSizeY;
        return this;
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
