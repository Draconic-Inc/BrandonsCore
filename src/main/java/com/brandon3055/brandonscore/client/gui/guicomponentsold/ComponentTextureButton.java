package com.brandon3055.brandonscore.client.gui.guicomponentsold;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 31/7/2015.
 */
public class ComponentTextureButton extends ComponentButton {
    public int textureXPos;
    public int textureYPos;
    private ResourceLocation texture;
    private boolean forceFullRender = false;

    //public ComponentTextureButton(int id, int xPos, int yPos, int textureXPos, int textureYPos, int xSise, int ySise, String text) {
    public ComponentTextureButton(int x, int y, int textureXPos, int textureYPos, int xSize, int ySize, int id, GUIBase gui, String displayString, String hoverText, ResourceLocation texture) {
        super(x, y, xSize, ySize, id, gui, displayString, hoverText);
        this.textureXPos = textureXPos;
        this.textureYPos = textureYPos;
        this.texture = texture;
    }

    public ComponentTextureButton forceFullRender() {
        forceFullRender = true;
        return this;
    }

    @Override//todo I dont even know if this will work..
    public void renderForground(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY) {

        GL11.glPushMatrix();
        minecraft.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (!forceFullRender) {
            int k = isMouseOver(mouseX, mouseY) ? 2 : 1;
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, textureXPos, textureYPos + k * ySize, this.xSize, this.ySize);
            int l = 14737632;

            if (packedFGColour != 0) {
                l = packedFGColour;
            } else if (!this.enabled) {
                l = 10526880;
            } else if (isMouseOver(mouseX, mouseY)) {
                l = 16777120;
            }
            this.drawCenteredString(fontRendererObj, this.displayString, this.x + this.xSize / 2, this.y + (this.ySize - 8) / 2, l);
        } else {
            int xPos = this.x;
            int yPos = this.y;
            int xSize = this.xSize;
            int ySize = this.ySize;
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer worldrenderer = tessellator.getBuffer();

            if (isMouseOver(mouseX, mouseY)) {
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                worldrenderer.color(0F, 0F, 0F, 255);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos((double) xPos, (double) (yPos + ySize), (double) this.zLevel).endVertex();
                worldrenderer.pos((double) (xPos + xSize), (double) (yPos + ySize), (double) this.zLevel).endVertex(); //todo By that i mean this
                worldrenderer.pos((double) (xPos + xSize), (double) yPos, (double) this.zLevel).endVertex();
                worldrenderer.pos((double) xPos, (double) yPos, (double) this.zLevel).endVertex();
                tessellator.draw();
                GL11.glEnable(GL11.GL_ALPHA_TEST);
            }

            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double) xPos, (double) (yPos + ySize), (double) this.zLevel).tex(0, 1).endVertex();
            worldrenderer.pos((double) (xPos + xSize), (double) (yPos + ySize), (double) this.zLevel).tex(1, 1).endVertex();
            worldrenderer.pos((double) (xPos + xSize), (double) yPos, (double) this.zLevel).tex(1, 0).endVertex();
            worldrenderer.pos((double) xPos, (double) yPos, (double) this.zLevel).tex(0, 0).endVertex();
            tessellator.draw();
        }
        GL11.glPopMatrix();
    }
}
