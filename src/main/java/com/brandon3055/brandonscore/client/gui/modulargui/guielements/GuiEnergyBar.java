package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.render.shader.ShaderProgram;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.blocks.TileEnergyBase;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.render.BCShaders;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 18/10/2016.
 */
public class GuiEnergyBar extends MGuiElementBase<GuiEnergyBar> {

    private long energy = 0;
    private long maxEnergy = 0;
    private boolean drawHoveringText = true;
    private IOPStorage energyHandler;
    private boolean horizontal = false;
    private boolean rfMode = false;
    private static ShaderProgram shaderProgram;
    private static ShaderProgram shaderProgramH;

    public GuiEnergyBar() {
        setSize(14, 14);
    }

    public GuiEnergyBar(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiEnergyBar(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiEnergyBar setEnergy(int energy, int maxEnergy) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        return this;
    }

    public GuiEnergyBar setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    public GuiEnergyBar setRfMode(boolean rfMode) {
        this.rfMode = rfMode;
        return this;
    }

    public GuiEnergyBar setDrawHoveringText(boolean drawHoveringText) {
        this.drawHoveringText = drawHoveringText;
        return this;
    }

    public GuiEnergyBar setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public GuiEnergyBar setEnergyHandler(IOPStorage energyHandler) {
        this.energyHandler = energyHandler;
        return this;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        ResourceHelperBC.bindTexture("textures/gui/energy_gui.png");

//        energy = 100;
//        maxEnergy = 200;
        int size = horizontal ? xSize() : ySize();
        int draw = (int) ((double) energy / (double) maxEnergy * (size - 2));

        int posY = yPos();
        int posX = xPos();

        if (horizontal) {
            int x = posY;
            posY = posX;
            posX = x;
            GlStateManager.pushMatrix();
            GlStateManager.translate(size + (posY * 2), 0, 0);
            GlStateManager.rotate(90, 0, 0, 1);
        }

        GlStateManager.color(1F, 1F, 1F);
        drawTexturedModalRect(posX, posY, 0, 0, 14, size);
        drawTexturedModalRect(posX, posY + size - 1, 0, 255, 14, 1);

        drawTexturedModalRect(posX + 1, posY + size - draw - 1, 14, size - draw, 12, draw);

        bindShader();
        drawColouredRect(posX + 1, posY + 1, 14 - 2, size - 2, 0x00FFFF);
//        drawColouredRect(screenWidth - 10, 0, 10, screenHeight, 0xFF00FFFF);
        releaseShader();


        if (horizontal) {
            GlStateManager.popMatrix();
        }
    }

    public Rectangle toScreenSpace(int xPos, int yPos, int xSize, int ySize) {
        double yResScale = (double) mc.displayHeight / screenHeight;
        double xResScale = (double) mc.displayWidth / screenWidth;
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (mc.displayHeight - (yPos * yResScale) - scaledHeight);
        return new Rectangle(x, y, (int) scaledWidth, (int) scaledHeight);
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (drawHoveringText && isMouseOver(mouseX, mouseY)) {
            List<String> list = new ArrayList<>();
            if (rfMode) {
                list.add(InfoHelper.ITC() + I18n.format("gui.bc.energy_storage.txt"));
            }
            else {
                list.add(InfoHelper.ITC() + I18n.format("gui.bc.operational_potential.txt"));
            }

            //gui.bc.shift_for_more_info.txt

            list.add(InfoHelper.HITC() + Utils.formatNumber(energy) + " / " + Utils.formatNumber(maxEnergy));
            list.add(TextFormatting.GRAY + "[" + Utils.addCommas(energy) + " RF]");

            if (energyHandler != null && energyHandler.getIOInfo() != null) {
                list.add("Input: " + energyHandler.getIOInfo().currentInput());
                list.add("Output: " + energyHandler.getIOInfo().currentOutput());
            }

            drawHoveringText(list, mouseX, mouseY, fontRenderer, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        if (energyHandler != null) {
            maxEnergy = energyHandler.getMaxOPStored();
            energy = energyHandler.getOPStored();
        }
        if (energyHandler instanceof TileEnergyBase) {
            energy = ((TileEnergyBase) energyHandler).energySync.get();
        }
        return super.onUpdate();
    }

    public void bindShader() {
        try {
            if (useShaders()) {
                ShaderProgram shader;
                if (horizontal) {
                    if (shaderProgramH == null) {
                        shaderProgramH = new ShaderProgram();
                        shaderProgramH.attachShader(BCShaders.energyBarH);
                        shaderProgramH.attachShader(BCShaders.commonVert);
                    }
                    shader = shaderProgramH;
                }
                else {
                    if (shaderProgram == null) {
                        shaderProgram = new ShaderProgram();
                        shaderProgram.attachShader(BCShaders.energyBar);
                        shaderProgram.attachShader(BCShaders.commonVert);
                    }
                    shader = shaderProgram;
                }
                Rectangle rect = toScreenSpace(xPos() + 1, yPos() + 1, xSize() - 2, ySize() - 2);

                shader.useShader(cache -> {
                    cache.glUniform1F("time", BCClientEventHandler.elapsedTicks / 10F);
                    cache.glUniform1F("charge", (float) energy / (float) maxEnergy);
                    cache.glUniform2I("ePos", rect.x, rect.y);
                    cache.glUniform2I("eSize", rect.width, rect.height);
                    cache.glUniform2I("screenSize", mc.displayWidth, mc.displayHeight);
                });
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void releaseShader() {
        if (useShaders()) {
            shaderProgram.releaseShader();
        }
    }

    public boolean useShaders() {
        return !rfMode && OpenGlHelper.shadersSupported && BCConfig.useShaders;
    }
}
