package com.brandon3055.brandonscore.client.utills;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Brandon on 28/06/2014.
 */
public class GuiHelper {
	public static final double PXL128 = 0.0078125;
	public static final double PXL256 = 0.00390625;

	public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY){
		return ((mouseX >= x && mouseX <= x+xSize) && (mouseY >= y && mouseY <= y+ySize));
	}

	public static void drawTexturedRect(int x, int y, int u, int v, int width, int height){
		drawTexturedRect(x, y, width, height, u, v, width, height, 0, PXL256);
	}

	public static void drawTexturedRect(double x, double y, double width, double height, int u, int v, int uSize, int vSize, double zLevel, double pxl){
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, zLevel).tex( u * pxl, (v + vSize) * pxl).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex((u + uSize) * pxl, (v + vSize) * pxl).endVertex();
		worldrenderer.pos(x + width, y, zLevel).tex((u + uSize) * pxl, v * pxl).endVertex();
		worldrenderer.pos(x, y, 		   zLevel).tex(u * pxl, v * pxl).endVertex();
		tessellator.draw();
	}

	public static void drawHoveringText(List list, int x, int y, FontRenderer font, float fade, double scale, int guiWidth, int guiHeight) {
		if (!list.isEmpty())
		{
			GL11.glPushMatrix();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glScaled(scale, scale, 1);
			x = (int)(x/scale);
			y = (int)(y/scale);

			int k = 0;
			Iterator iterator = list.iterator();

			while (iterator.hasNext())
			{
				String s = (String)iterator.next();
				int l = font.getStringWidth(s);

				if (l > k)
				{
					k = l;
				}
			}

			int adjX = x + 12;
			int adjY = y - 12;
			int i1 = 8;

			if (list.size() > 1)
			{
				i1 += 2 + (list.size() - 1) * 10;
			}

			if (adjX + k > (int)(guiWidth/scale))
			{
				adjX -= 28 + k;
			}

			if (adjY + i1 + 6 > (int)(guiHeight/scale))
			{
				adjY = (int)(guiHeight/scale) - i1 - 6;
			}

			int j1 = -267386864;
			drawGradientRect(adjX - 3, adjY - 4, adjX + k + 3, adjY - 3, j1, j1, fade, scale);
			drawGradientRect(adjX - 3, adjY + i1 + 3, adjX + k + 3, adjY + i1 + 4, j1, j1, fade, scale);
			drawGradientRect(adjX - 3, adjY - 3, adjX + k + 3, adjY + i1 + 3, j1, j1, fade, scale);
			drawGradientRect(adjX - 4, adjY - 3, adjX - 3, adjY + i1 + 3, j1, j1, fade, scale);
			drawGradientRect(adjX + k + 3, adjY - 3, adjX + k + 4, adjY + i1 + 3, j1, j1, fade, scale);
			int k1 = 1347420415;
			int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
			drawGradientRect(adjX - 3, adjY - 3 + 1, adjX - 3 + 1, adjY + i1 + 3 - 1, k1, l1, fade, scale);
			drawGradientRect(adjX + k + 2, adjY - 3 + 1, adjX + k + 3, adjY + i1 + 3 - 1, k1, l1, fade, scale);
			drawGradientRect(adjX - 3, adjY - 3, adjX + k + 3, adjY - 3 + 1, k1, k1, fade, scale);
			drawGradientRect(adjX - 3, adjY + i1 + 2, adjX + k + 3, adjY + i1 + 3, l1, l1, fade, scale);

			for (int i2 = 0; i2 < list.size(); ++i2)
			{
				String s1 = (String)list.get(i2);

				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				font.drawStringWithShadow(s1, adjX, adjY, ((int)(fade*240F)+0x10 << 24) | 0x00FFFFFF);

				adjY += 10;
			}

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}

	public static void drawGradientRect(int x1, int y1, int x2, int y2, int colour1, int colour2, float fade, double scale) {
		float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
		float f1 = (float)(colour1 >> 16 & 255) / 255.0F;
		float f2 = (float)(colour1 >> 8 & 255) / 255.0F;
		float f3 = (float)(colour1 & 255) / 255.0F;
		float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
		float f5 = (float)(colour2 >> 16 & 255) / 255.0F;
		float f6 = (float)(colour2 >> 8 & 255) / 255.0F;
		float f7 = (float)(colour2 & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.color(f1, f2, f3, f);
		worldrenderer.pos((double) x2, (double) y1, 300D).endVertex();
		worldrenderer.pos((double) x1, (double) y1, 300D).endVertex();
		worldrenderer.color(f5, f6, f7, f4);
		worldrenderer.pos((double) x1, (double) y2, 300D).endVertex();
		worldrenderer.pos((double) x2, (double)y2, 300D).endVertex();
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
