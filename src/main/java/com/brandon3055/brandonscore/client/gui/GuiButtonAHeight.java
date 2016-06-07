package com.brandon3055.brandonscore.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by Brandon on 17/09/2014.
 */
public class GuiButtonAHeight extends GuiButton {

	public GuiButtonAHeight(int id, int xPos, int yPos, int width, int hight, String displayString) {
		super(id, xPos, yPos, width, hight, displayString);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.visible)
		{
			FontRenderer fontrenderer = minecraft.fontRendererObj;
			minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int k = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, width % 2 + this.width / 2, this.height);
			this.drawTexturedModalRect(width % 2 + this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
			if (this.height < 20){

				this.drawTexturedModalRect(xPosition, yPosition+3, 0, (46 + k * 20)+20-height+3, width % 2 + width / 2, height-3);
				this.drawTexturedModalRect(width % 2 + xPosition + width / 2, yPosition+3, 200 - width / 2, (46 + k * 20)+20-height+3, width / 2, height-3);
			}
			this.mouseDragged(minecraft, mouseX, mouseY);
			int l = 14737632;

			if (packedFGColour != 0)
			{
				l = packedFGColour;
			}
			else if (!this.enabled)
			{
				l = 10526880;
			}
			else if (this.hovered)
			{
				l = 16777120;
			}
			this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
		}
	}
}
