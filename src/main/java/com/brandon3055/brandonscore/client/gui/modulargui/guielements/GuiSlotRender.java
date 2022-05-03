package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;

/**
 * Created by brandon3055 on 3/09/2016.
 * Renders a simple inventory slot background
 */
@Deprecated //GO AWAY!
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
//        Material mat = BCSprites.getThemed("slot");
//        bindTexture(mat.atlasLocation());
//        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//        drawSprite(getter.getBuffer(mat.renderType(BCSprites::makeType)), getInsetRect().x, getInsetRect().y, 18, 18, mat.sprite());
//        getter.endBatch();
//        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
}
