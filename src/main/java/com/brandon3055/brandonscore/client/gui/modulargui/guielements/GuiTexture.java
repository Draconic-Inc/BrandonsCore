package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

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
    private Supplier<Integer> texXGetter = null;
    private Supplier<Integer> texYGetter = null;

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

    public GuiTexture(int xSize, int ySize, ResourceLocation texture) {
        super(0, 0, xSize, ySize);
        this.texture = texture;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        bindTexture(texture);
        if (preDrawCallback == null) {
            GlStateManager.color(1, 1, 1, 1);
        }

        if (texSizeOverride) {
            drawScaledCustomSizeModalRect(xPos(), yPos(), getTexU(), getTexV(), texUSize, texVSize, xSize(), ySize(), textSheetSizeX, textSheetSizeY);
        }
        else {
            drawModalRectWithCustomSizedTexture(xPos(), yPos(), getTexU(), getTexV(), xSize(), ySize(), textSheetSizeX, textSheetSizeY);
        }
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFFFFFF00);
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

    public GuiTexture setTexXGetter(Supplier<Integer> texXGetter) {
        this.texXGetter = texXGetter;
        return this;
    }

    public GuiTexture setTexYGetter(Supplier<Integer> texYGetter) {
        this.texYGetter = texYGetter;
        return this;
    }

    public int getTexU() {
        return texXGetter == null ? texU : texXGetter.get();
    }

    public int getTexV() {
        return texYGetter == null ? texV : texYGetter.get();
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
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos(), 256 - (xSize() / 2), 0, xSize() / 2, ySize() / 2);

                drawTexturedModalRect(xPos(), yPos() + (ySize() / 2), 0, 256 - (ySize() / 2), xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos() + (ySize() / 2), 256 - (xSize() / 2), 256 - (ySize() / 2), xSize() / 2, ySize() / 2);

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
                drawTexturedModalRect(xPos(), yPos(), 0, 0, xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos(), 256 - (xSize() / 2), 0, xSize() / 2, ySize() / 2);

                drawTexturedModalRect(xPos(), yPos() + (ySize() / 2), 0, 256 - (ySize() / 2), xSize() / 2, ySize() / 2);
                drawTexturedModalRect(xPos() + (xSize() / 2), yPos() + (ySize() / 2), 256 - (xSize() / 2), 256 - (ySize() / 2), xSize() / 2, ySize() / 2);

                for (MGuiElementBase element : childElements) {
                    if (element.isEnabled()) {
                        element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                    }
                }
            }
        };
    }

}
