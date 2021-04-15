package com.brandon3055.brandonscore.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

/**
 * Created by Brandon on 17/09/2014.
 */
@Deprecated //May keep these for when i need to inject a button onto a vanilla gui. But should not be used for anything else.
//TODO get rid of this ASAP
public class GuiButtonAHeight extends Button {

	public GuiButtonAHeight(int xPos, int yPos, int width, int hight, String displayString, IPressable onPress) {
		super(xPos, yPos, width, hight, ITextComponent.nullToEmpty(displayString), onPress);
	}

	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.visible)
		{
			Minecraft mc = Minecraft.getInstance();
			FontRenderer fontrenderer = mc.font;
			mc.getTextureManager().bind(WIDGETS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.blit(matrixStack, this.x, this.y, 0, 46 + k * 20, width % 2 + this.width / 2, this.height);
			this.blit(matrixStack, width % 2 + this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
			if (this.height < 20){

				this.blit(matrixStack, x, y+3, 0, (46 + k * 20)+20-height+3, width % 2 + width / 2, height-3);
				this.blit(matrixStack, width % 2 + x + width / 2, y+3, 200 - width / 2, (46 + k * 20)+20-height+3, width / 2, height-3);
			}
//			this.mouseDragged(mc, mouseX, mouseY);
			int l = 14737632;

			if (packedFGColor != 0)
			{
				l = packedFGColor;
			}
			else if (!this.active)
			{
				l = 10526880;
			}
			else if (this.isHovered)
			{
				l = 16777120;
			}
			this.drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
		}
	}
}
