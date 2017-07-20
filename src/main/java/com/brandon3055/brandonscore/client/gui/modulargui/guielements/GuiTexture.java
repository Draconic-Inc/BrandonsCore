package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 1/10/2016.
 */
public class GuiTexture extends MGuiElementBase<GuiTexture> {
    public int texU;
    public int texV;
    public int textSheetSizeX = 256;
    public int textSheetSizeY = 256;
    private int texUSize = 0;
    private int texVSize = 0;
    private boolean texSizeOverride = false;

    public ResourceLocation texture;

    public GuiTexture(int xPos, int yPos, int textureX, int textureY, int xSize, int ySize, ResourceLocation texture) {
        super(xPos, yPos, xSize, ySize);
        this.texU = textureX;
        this.texV = textureY;
        this.texture = texture;
    }

    public GuiTexture(int textureX, int textureY, int xSize, int ySize, ResourceLocation texture) {
        super(0, 0, xSize, ySize);
        this.texU = textureX;
        this.texV = textureY;
        this.texture = texture;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        bindTexture(texture);
        if (texSizeOverride) {
            drawScaledCustomSizeModalRect(xPos(), yPos(), texU, texV, texUSize, texVSize, xSize(), ySize(), textSheetSizeX, textSheetSizeY);
        }
        else {
            drawModalRectWithCustomSizedTexture(xPos(), yPos(), texU, texV, xSize(), ySize(), textSheetSizeX, textSheetSizeY);
        }
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    /**
     * Set the x and y (u/v) position of the texture on the provided texture sheet.
     * By default the width and height if the texture will be set from the element width and height but
     * this can be overridden using {@link #setTexSizeOverride(int, int)}
     */
    public GuiTexture setTexturePos(int textureX, int textureY) {
        this.texU = textureX;
        this.texV = textureY;
        return this;
    }

    public GuiTexture setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public GuiTexture setTexture(String texture) {
        this.texture = ResourceHelperBC.getResourceRAW(texture);
        return this;
    }

    /**
     * Allows you to override the texture size. When set the texture will be scaled up or down as needed to fit
     * the bounds of this element.
     */
    public GuiTexture setTexSizeOverride(int texXOverride, int texYOverride) {
        this.texUSize = texXOverride;
        this.texVSize = texYOverride;
        texSizeOverride = true;
        return this;
    }

    /**
     * Set the size of the texture sheet being used. Default is the standard 256x256
     */
    public GuiTexture setTexSheetSize(int textureSizeX, int textureSizeY) {
        this.textSheetSizeX = textureSizeX;
        this.textSheetSizeY = textureSizeY;
        return this;
    }

    /**
     * @see #setTexSheetSize(int, int)
     */
    public GuiTexture setTexSheetSize(int textureSize) {
        return setTexSheetSize(textureSize, textureSize);
    }


    /**
     * Creates a new GuiTexture with the specified size and the BrandonsCore default gui background texture.
     * This is the background used in a lot of Draconic Evolution's Gui's
     */
    public static GuiTexture newBCTexture(int xSize, int ySize) {
        return new GuiTexture(0, 0, xSize, ySize, ResourceHelperBC.getResourceRAW("brandonscore:textures/gui/base_gui.png")){
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                ResourceHelperBC.bindTexture(texture);
                drawTexturedModalRect(xPos(), yPos(), 0, 0, xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos(), 255 - (xSize() / 2), 0, xSize() / 2, ySize() / 2);

                drawTexturedModalRect(xPos(), yPos() + (ySize() / 2), 0, 255 - (ySize() / 2), xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos() + (ySize() / 2), 255 - (xSize() / 2), 255 - (ySize() / 2), xSize() / 2, ySize() / 2);

                for (MGuiElementBase element : childElements) {
                    if (element.isEnabled()) {
                        element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                    }
                }
            }
        };
    }

    /**
     * Creates a new GuiTexture with the specified size and the normal vanilla gui background texture.
     */
    public static GuiTexture newVanillaGuiTexture(int xSize, int ySize) {
        return new GuiTexture(0, 0, xSize, ySize, ResourceHelperBC.getResourceRAW("brandonscore:textures/gui/base_vanilla_gui.png")){
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                ResourceHelperBC.bindTexture(texture);
//                LogHelperBC.dev("R " + xPos() + " " + yPos() + " " + xSize() + " " + ySize() + " " + isMouseOver(mouseX, mouseY));
//                setYSize(10);
                drawTexturedModalRect(xPos(), yPos(), 0, 0, xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos(), 255 - (xSize() / 2), 0, xSize() / 2, ySize() / 2);

                drawTexturedModalRect(xPos(), yPos() + (ySize() / 2), 0, 255 - (ySize() / 2), xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos() + (ySize() / 2), 255 - (xSize() / 2), 255 - (ySize() / 2), xSize() / 2, ySize() / 2);

                for (MGuiElementBase element : childElements) {
                    if (element.isEnabled()) {
                        element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                    }
                }
            }
        };
    }

}
