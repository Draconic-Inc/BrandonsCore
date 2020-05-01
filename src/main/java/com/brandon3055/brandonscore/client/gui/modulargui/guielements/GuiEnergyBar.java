package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.render.shader.ShaderProgram;
import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.render.BCShaders;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.function.Supplier;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Created by brandon3055 on 18/10/2016.
 */
public class GuiEnergyBar extends GuiElement<GuiEnergyBar> {

    private boolean drawHoveringText = true;
    private IOPStorage energyHandler = null;
    private Supplier<Long> capacitySupplier = null;
    private Supplier<Long> energySupplier = null;
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

    public GuiEnergyBar setEnergySupplier(Supplier<Long> energySupplier) {
        this.energySupplier = energySupplier;
        return this;
    }

    public GuiEnergyBar setCapacitySupplier(Supplier<Long> capacitySupplier) {
        this.capacitySupplier = capacitySupplier;
        return this;
    }

    /**
     * Forces this energy bar to display as RF/Energy Storage instead of OP/Operational Potential
     */
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

    public GuiEnergyBar setEnergyStorage(IOPStorage energyHandler) {
        this.energyHandler = energyHandler;
        return this;
    }

    protected boolean isRfMode() {
        return rfMode;
    }

    protected long getCapacity() {
        if (capacitySupplier != null) {
            return capacitySupplier.get();
        }
        else if (energyHandler != null) {
            return energyHandler.getMaxOPStored();
        }
        return 0;
    }

    protected long getEnergy() {
        if (energySupplier != null) {
            return energySupplier.get();
        }
        else if (energyHandler != null) {
            return energyHandler.getOPStored();
        }
        return 0;
    }

    protected IOInfo getIOInfo() {
        return energyHandler != null ? energyHandler.getIOInfo() : null;
    }

    protected float getSOC() {
        return (float) getEnergy() / (float) Math.max(1, getCapacity());
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        ResourceHelperBC.bindTexture("textures/gui/energy_gui.png");

        int size = horizontal ? xSize() : ySize();
        int draw = (int) ((double) getEnergy() / (double) getCapacity() * (size - 2));

        int posY = yPos();
        int posX = xPos();

        if (horizontal) {
            int x = posY;
            posY = posX;
            posX = x;
            RenderSystem.pushMatrix();
            RenderSystem.translated(size + (posY * 2), 0, 0);
            RenderSystem.rotatef(90, 0, 0, 1);
        }

        RenderSystem.color3f(1F, 1F, 1F);
        drawTexturedModalRect(posX, posY, 0, 0, 14, size);
        drawTexturedModalRect(posX, posY + size - 1, 0, 255, 14, 1);

        drawTexturedModalRect(posX + 1, posY + size - draw - 1, 14, size - draw, 12, draw);

        bindShader();
        drawColouredRect(posX + 1, posY + 1, 14 - 2, size - 2, 0x00FFFF);
//        drawColouredRect(screenWidth - 10, 0, 10, screenHeight, 0xFF00FFFF);
        releaseShader();


        if (horizontal) {
            RenderSystem.popMatrix();
        }
    }

    public Rectangle toScreenSpace(int xPos, int yPos, int xSize, int ySize) {
        double yResScale = (double) displayHeight() / screenHeight;
        double xResScale = (double) displayWidth() / screenWidth;
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (displayHeight() - (yPos * yResScale) - scaledHeight);
        return new Rectangle(x, y, (int) scaledWidth, (int) scaledHeight);
    }

    //TODO some sort of "modular tool tip" that would allow me to properly define key: value columns in the tooltip
    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (drawHoveringText && isMouseOver(mouseX, mouseY)) {
            long maxEnergy = getCapacity();
            long energy = getEnergy();

            String title = rfMode ? "gui.bc.energy_storage.txt" : "gui.bc.operational_potential.txt";
            boolean shift = Screen.hasShiftDown();
            String suffix = rfMode ? "RF" : "OP";
            String capString = (shift ? Utils.addCommas(maxEnergy) : Utils.formatNumber(maxEnergy)) + " " + suffix;
            String storedString = (shift ? Utils.addCommas(energy) : Utils.formatNumber(energy)) + " " + suffix;
            String percent = " (" + MathUtils.round(((double)energy / (double)maxEnergy) * 100D, 100) + "%)";

            StringBuilder builder = new StringBuilder();
            builder.append(DARK_AQUA).append(I18n.format(title)).append("\n");

            builder.append(GOLD).append(I18n.format("gui.bc.capacity.txt")).append(" ").append(GRAY).append(capString).append("\n");
            builder.append(GOLD).append(I18n.format("gui.bc.stored.txt")).append(" ").append(GRAY).append(storedString).append(percent).append("\n");

            IOInfo ioInfo = getIOInfo();
            if (ioInfo != null) {
                if (shift) {
                    builder.append(GOLD).append(I18n.format("gui.bc.input.txt")).append(" ").append(GREEN).append("+").append(Utils.formatNumber(ioInfo.currentInput()));
                    builder.append(" ").append(suffix).append("/t\n");

                    builder.append(GOLD).append(I18n.format("gui.bc.output.txt")).append(" ").append(RED).append("-").append(Utils.formatNumber(ioInfo.currentOutput()));
                    builder.append(" ").append(suffix).append("/t\n");
                }
                else {
                    long io = ioInfo.currentInput() - ioInfo.currentOutput();
                    builder.append(GOLD).append(I18n.format("gui.bc.io.txt")).append(" ").append(io > 0 ? GREEN + "+" : io < 0 ? RED : GRAY);
                    builder.append(Utils.formatNumber(io)).append(" ").append(suffix).append("/t\n");
                }
            }

            drawHoveringText(Lists.newArrayList(builder.toString().split("\n")), mouseX, mouseY, fontRenderer, displayWidth(), displayHeight());
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
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
                    cache.glUniform1F("charge", getSOC());
                    cache.glUniform2I("ePos", rect.x, rect.y);
                    cache.glUniform2I("eSize", rect.width, rect.height);
                    cache.glUniform2I("screenSize", displayWidth(), displayHeight());
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
        return !rfMode && BCShaders.useShaders();
    }
}
