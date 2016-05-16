package com.brandon3055.brandonscore.client.utills;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Created by Brandon on 28/06/2014.
 */
public class GuiHelper { //TODO replace all GL11 calls with GLStateManager //Note to self: GuiUtils is a thing.
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
		VertexBuffer vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexBuffer.pos(x, y + height, zLevel).tex( u * pxl, (v + vSize) * pxl).endVertex();
		vertexBuffer.pos(x + width, y + height, zLevel).tex((u + uSize) * pxl, (v + vSize) * pxl).endVertex();
		vertexBuffer.pos(x + width, y, zLevel).tex((u + uSize) * pxl, v * pxl).endVertex();
		vertexBuffer.pos(x, y, zLevel).tex(u * pxl, v * pxl).endVertex();
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
			int i1 = 6;

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

	public static void drawGradientRect(int left, int top, int right, int bottom, int colour1, int colour2, float fade, double scale) {
		float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
		float f1 = (float)(colour1 >> 16 & 255) / 255.0F;
		float f2 = (float)(colour1 >> 8 & 255) / 255.0F;
		float f3 = (float)(colour1 & 255) / 255.0F;
		float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
		float f5 = (float)(colour2 >> 16 & 255) / 255.0F;
		float f6 = (float)(colour2 >> 8 & 255) / 255.0F;
		float f7 = (float)(colour2 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double)right, (double)top, 300D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)left, (double)top, 300D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)left, (double)bottom, 300D).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, 300D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
	}

    /**Draws a simple vertical energy bar with no tool tip*/
    public static void drawEnergyBar(Gui gui, int posX, int posZ, int size, long energy, long maxEnergy) {
        drawEnergyBar(gui, posX, posZ, size, false, energy, maxEnergy, false, 0, 0);
    }

    /**Draws an energy bar in a gui at the given position
     * @param size is the length of the energy bar.
     * @param horizontal will rotate the bar clockwise 90 degrees.
     */
    @SuppressWarnings("all")
    public static void drawEnergyBar(Gui gui, int posX, int posY, int size, boolean horizontal, long energy, long maxEnergy, boolean toolTip, int mouseX, int mouseY){
        ResourceHelperBC.bindTexture("textures/gui/energyGui.png");
        int draw = (int)((double)energy / (double)maxEnergy * (size-2));

        boolean inRect = isInRect(posX, posY, size, 14, mouseX, mouseY);

        if (horizontal){
            int x = posY;
            posY = posX;
            posX = x;
            GlStateManager.pushMatrix();
            GlStateManager.translate(size + (posY * 2), 0, 0);
            GlStateManager.rotate(90, 0, 0, 1);
        }

        GlStateManager.color(1F, 1F, 1F);
        gui.drawTexturedModalRect(posX, posY, 0, 0, 14, size);
        gui.drawTexturedModalRect(posX, posY + size - 1, 0, 255, 14, 1);
        gui.drawTexturedModalRect(posX + 1, posY + size - draw - 1, 14, size - draw, 12, draw);

        if (horizontal){
            GlStateManager.popMatrix();
        }

        if (toolTip && inRect){
            List<String> list = new ArrayList<String>();
            list.add(InfoHelper.ITC()+I18n.translateToLocal("gui.de.energyStorage.txt"));
            list.add(InfoHelper.HITC()+ Utils.formatNumber(energy)+" / "+ Utils.formatNumber(maxEnergy));
            list.add(TextFormatting.GRAY+"["+ Utils.addCommas(energy)+" RF]");
            drawHoveringText(list, mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj, 1F, 1F, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        }
    }

    public static void drawGuiBaseBackground(Gui gui, int posX, int posY, int xSize, int ySize){
        ResourceHelperBC.bindTexture("textures/gui/baseGui.png");
        GlStateManager.color(1F, 1F, 1F);
        gui.drawTexturedModalRect(posX, posY, 0, 0, xSize-3, ySize-3);
        gui.drawTexturedModalRect(posX + xSize - 3, posY, 253, 0, 3, ySize-3);
        gui.drawTexturedModalRect(posX, posY + ySize - 3, 0, 253, xSize - 3, 3);
        gui.drawTexturedModalRect(posX + xSize - 3, posY + ySize - 3, 253, 253, 3, 3);
    }

    /**
     * Draws the players inventory slots into the gui.
     * note. X-Size is 162
     * */
    public static void drawPlayerSlots(Gui gui, int posX, int posY, boolean center){
        ResourceHelperBC.bindTexture("textures/gui/widgets.png");

        if (center) {
            posX -= 81;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                gui.drawTexturedModalRect(posX + x * 18, posY + y * 18, 138, 0, 18, 18);
            }
        }

        for (int x = 0; x < 9; x++) {
            gui.drawTexturedModalRect(posX + x*18, posY + 58, 138, 0, 18, 18);
        }
    }

    public static void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color, boolean dropShadow)
    {
        fontRendererIn.drawString(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color, dropShadow);
    }
}
