package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.RenderMaterial;

/**
 * Created by brandon3055 on 3/09/2016.
 * Renders a simple inventory slot background
 */
public class GuiSlotRender extends GuiElement<GuiSlotRender> {

    public GuiSlotRender() {
        setSize(18, 18);
    }

    public GuiSlotRender(int xPos, int yPos) {
        super(xPos, yPos);
        setSize(18, 18);
    }

    public GuiSlotRender(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        RenderMaterial mat = BCSprites.getThemed("slot");
        bindTexture(mat.getAtlasLocation());
        IRenderTypeBuffer.Impl getter = minecraft.getRenderTypeBuffers().getBufferSource();
        drawSprite(mat.getBuffer(getter, BCSprites::makeType), getInsetRect().x, getInsetRect().y, 18, 18, mat.getSprite());
        getter.finish();
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
}
