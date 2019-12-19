package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 10/09/2016.
 * See the Draconic Reactor gui for an example of this in use.
 * Simply allows you to define a texture for a "pointer" that moves between min pos and max pos.
 */
public class GuiTexturedPointer extends GuiElement<GuiTexturedPointer> {

    private int texPosX;
    private int texPosY;
    private int sliderWitdh;
    private ResourceLocation texture;
    public IGuiEventListener listener;
    public double position = 0;
    public boolean horizontal = false;
    public int barColour = 0xFF000000;
    public int sliderColour = 0xFFFFFFFF;

    protected GuiTexturedPointer() {}

    protected GuiTexturedPointer(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTexturedPointer(int xPos, int yPos, int xSize, int ySize, int texPosX, int texPosY, int sliderWidth, ResourceLocation texture) {
        super(xPos, yPos, xSize, ySize);
        this.texPosX = texPosX;
        this.texPosY = texPosY;
        this.sliderWitdh = sliderWidth;
        this.texture = texture;
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
    }

    //region Render

    //endregion

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (texture != null) {
            bindTexture(texture);
        }

        if (horizontal) {
            double offset = (1D - getPos()) * (xSize() - sliderWitdh);
            drawTexturedModalRect(xPos() + offset, yPos(), texPosX, texPosY, sliderWitdh, ySize());
        }
        else {
            double offset = (1D - getPos()) * (ySize() - sliderWitdh);
            drawTexturedModalRect(xPos(), yPos() + offset, texPosX, texPosY, xSize(), sliderWitdh);
        }
    }

    //region Interact & Setters


    public GuiTexturedPointer setColours(int barColour, int sliderColour) {
        this.barColour = barColour;
        this.sliderColour = sliderColour;
        return this;
    }


    public GuiTexturedPointer setHorizontal(boolean horizontal) {
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
