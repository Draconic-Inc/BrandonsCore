package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 10/09/2016.
 * See the Draconic Reactor gui for an example of this in use.
 * Simply allows you to define a texture for a "pointer" that moves between min pos and max pos.
 */
public class MGuiTexturedPointer extends MGuiElementBase {

    private int texPosX;
    private int texPosY;
    private int sliderWitdh;
    private ResourceLocation texture;
    public IMGuiListener listener;
    public double position = 0;
    public boolean horizontal = false;
    private boolean isDragging = false;
    public int barColour = 0xFF000000;
    public int sliderColour = 0xFFFFFFFF;
    public int backFillColour = 0x00000000;
    public int backBorderColour = 0x00000000;

    protected MGuiTexturedPointer(IModularGui modularGui) {
        super(modularGui);
    }

    protected MGuiTexturedPointer(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiTexturedPointer(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize, int texPosX, int texPosY, int sliderWidth, ResourceLocation texture) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.texPosX = texPosX;
        this.texPosY = texPosY;
        this.sliderWitdh = sliderWidth;
        this.texture = texture;
    }

    @Override
    public void initElement() {
        super.initElement();
    }

    //region Render

    //endregion

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);

        if (texture != null) {
            bindTexture(texture);
        }

        if (horizontal) {
            double offset = (1D - getPos()) * (xSize - sliderWitdh);
            drawTexturedModalRect(xPos + offset, yPos, texPosX, texPosY, sliderWitdh, ySize);
        }
        else {
            double offset = (1D - getPos()) * (ySize - sliderWitdh);
            drawTexturedModalRect(xPos, yPos + offset, texPosX, texPosY, xSize, sliderWitdh);
        }
    }

    //region Interact & Setters


    public MGuiTexturedPointer setColours(int barColour, int sliderColour) {
        this.barColour = barColour;
        this.sliderColour = sliderColour;
        return this;
    }

//    public MGuiTexturedPointer setBackground(int backFillColour, int backBorderColour) {
//        this.backFillColour = backFillColour;
//        this.backBorderColour = backBorderColour;
//        this.backBorderWidth = backBourderWidth;
//        return this;
//    }

    public MGuiTexturedPointer setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    /**
     * @return the position of the scroll bar between (0 to 1)
     */
    public double getPos() {
        return position;
    }

    /**
     * Sets the position of the scroll bar.
     *
     * @param pos position between 0 and 1
     */
    public void setPos(double pos) {
        if (pos > 1) {
            pos = 1;
        }
        else if (pos < 0) {
            pos = 0;
        }

        this.position = pos;
    }

    //endregion
}
