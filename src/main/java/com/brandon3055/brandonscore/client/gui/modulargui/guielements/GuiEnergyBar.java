package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.shader.ShaderProgram;
import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.render.BCShaders;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 18/10/2016.
 */
public class GuiEnergyBar extends GuiElement<GuiEnergyBar> {

//    public static ShaderProgram barShaderH = ShaderProgramBuilder.builder()
//            .addShader("vert", shader -> shader
//                    .type(VERTEX)
//                    .source(new ResourceLocation(BrandonsCore.MODID, "shaders/common.vert"))
//            )
//            .addShader("frag", shader -> shader
//                    .type(FRAGMENT)
//                    .source(new ResourceLocation(BrandonsCore.MODID, "shaders/power_bar_horizontal.frag"))
//                    .uniform("time", UniformType.FLOAT)
//                    .uniform("charge", UniformType.FLOAT)
//                    .uniform("ePos", UniformType.I_VEC2)
//                    .uniform("eSize", UniformType.I_VEC2)
//                    .uniform("screenSize", UniformType.I_VEC2)
//            )
//            .whenUsed(cache -> cache.glUniform1f("time", BCClientEventHandler.elapsedTicks / 10F))
//            .build();
//
//    public static ShaderProgram barShaderV = ShaderProgramBuilder.builder()
//            .addShader("vert", shader -> shader
//                    .type(VERTEX)
//                    .source(new ResourceLocation(BrandonsCore.MODID, "shaders/common.vert"))
//            )
//            .addShader("frag", shader -> shader
//                    .type(FRAGMENT)
//                    .source(new ResourceLocation(BrandonsCore.MODID, "shaders/power_bar.frag"))
//                    .uniform("time", UniformType.FLOAT)
//                    .uniform("charge", UniformType.FLOAT)
//                    .uniform("ePos", UniformType.I_VEC2)
//                    .uniform("eSize", UniformType.I_VEC2)
//                    .uniform("screenSize", UniformType.I_VEC2)
//            )
//            .whenUsed(cache -> cache.glUniform1f("time", BCClientEventHandler.elapsedTicks / 10F))
//            .build();

    private IOPStorage energyHandler = null;
    private Supplier<Long> capacitySupplier = null;
    private Supplier<Long> energySupplier = null;
    private boolean horizontal = false;
    private boolean rfMode = false;
    private Supplier<Boolean> shaderEnabled = () -> true;
    private Supplier<Boolean> drawHoveringText = () -> true;
    private Supplier<Boolean> disabled = () -> false;

    public GuiEnergyBar() {
        setSize(14, 14);
        elementTranslationExt = "energy_bar";
    }

    public GuiEnergyBar(int xPos, int yPos) {
        super(xPos, yPos);
        elementTranslationExt = "energy_bar";
    }

    public GuiEnergyBar(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
        elementTranslationExt = "energy_bar";
    }

    public GuiEnergyBar setEnergySupplier(Supplier<Long> energySupplier) {
        this.energySupplier = energySupplier;
        return this;
    }

    public GuiEnergyBar setCapacitySupplier(Supplier<Long> capacitySupplier) {
        this.capacitySupplier = capacitySupplier;
        return this;
    }

    public GuiEnergyBar setItemSupplier(Supplier<ItemStack> stackSupplier) {
        this.capacitySupplier = () -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getMaxEnergyStored(stackSupplier.get()) : 0;
        this.energySupplier = () -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getEnergyStored(stackSupplier.get()) : 0;
        return this;
    }

    public GuiEnergyBar setShaderEnabled(Supplier<Boolean> shaderEnabled) {
        this.shaderEnabled = shaderEnabled;
        return this;
    }

    public GuiEnergyBar setDisabled(Supplier<Boolean> disabled) {
        this.disabled = disabled;
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
        this.drawHoveringText = () -> drawHoveringText;
        return this;
    }

    public GuiEnergyBar setDrawHoveringText(Supplier<Boolean> drawHoveringText) {
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
        } else if (energyHandler != null) {
            return energyHandler.getMaxOPStored();
        }
        return 0;
    }

    protected long getEnergy() {
        if (energySupplier != null) {
            return energySupplier.get();
        } else if (energyHandler != null) {
            return energyHandler.getOPStored();
        }
        return 0;
    }

    protected IOInfo getIOInfo() {
        return energyHandler != null ? energyHandler.getIOInfo() : null;
    }

    protected float getSOC() {
        return MathHelper.clip((float) getEnergy() / (float) Math.max(1, getCapacity()), 0F, 1F);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        int barLength = horizontal ? xSize() : ySize();
        int barWidth = horizontal ? ySize() : xSize();
        double charge = getSOC();
        if (Double.isNaN(charge)) charge = 0;
        int draw = (int) (charge * (barLength - 2));

        int posY = yPos();
        int posX = xPos();

        if (horizontal) {
            int x = posY;
            posY = posX;
            posX = x;
//            RenderSystem.pushMatrix();
//            RenderSystem.translated(barLength + (posY * 2), 0, 0);
//            RenderSystem.rotatef(90, 0, 0, 1);
        }
        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int light = darkMode ? 0xFFFFFFFF : 0xFFFFFFFF;
        int dark = darkMode ? 0xFF808080 : 0xFF505050;
        drawShadedRect(getter, posX, posY, barWidth, barLength, 1, 0, dark, light, midColour(light, dark));
        getter.endBatch();

        if (disabled.get()) {
            drawColouredRect(posX + 1, posY + 1, barWidth - 2, barLength - 2, 0xFF000000);
        } else if (!shaderEnabled.get()) {
            Material matBase = BCSprites.get("bars/energy_empty");
            Material matOverlay = BCSprites.get("bars/energy_full");
            sliceSprite(getter.getBuffer(BCSprites.GUI_TYPE), posX + 1, posY + 1, barWidth - 2, barLength - 2, matBase.sprite());
            sliceSprite(getter.getBuffer(BCSprites.GUI_TYPE), posX + 1, posY + barLength - draw - 1, barWidth - 2, draw, matOverlay.sprite());
            getter.endBatch();
        }
        else {
//            bindShader(horizontal ? barShaderH : barShaderV);
//            drawColouredRect(posX + 1, posY + 1, barWidth - 2, barLength - 2, 0xFF000000);
//            drawColouredRect(posX + 1, posY + barLength - draw - 1, barWidth - 2, draw, 0xFFFF0000);
//            releaseShader(horizontal ? barShaderH : barShaderV);
        }

        if (horizontal) {
//            RenderSystem.popMatrix();
        }
    }
    public void sliceSprite(VertexConsumer buffer, int xPos, int yPos, int xSize, int ySize, TextureAtlasSprite sprite) {
        float texU = sprite.getU0();
        float texV = sprite.getV0();
        int texWidth = sprite.getWidth();
        int texHeight = sprite.getHeight();
        float uScale = (sprite.getU1() - texU) / texWidth;
        float vScale = (sprite.getV1() - texV) / texHeight;
        for (int i = 0; i < ySize; i += Math.min(texHeight - 2, ySize - i)) {
            int partSize = Math.min(texHeight, ySize - i);
            bufferRect(buffer, xPos, yPos + ySize - i, xSize, -partSize, sprite.getU0(), sprite.getV0(), xSize * uScale, partSize * vScale);
        }
    }

    private void bufferRect(VertexConsumer buffer, float x, float y, float width, float height, float minU, float minV, float tWidth, float tHeight) {
        double zLevel = getRenderZLevel();
        //@formatter:off
        buffer.vertex(x,           y + height, zLevel).color(1F, 1F, 1F, 1F).uv(minU, minV + tHeight).endVertex();
        buffer.vertex(x + width,   y + height, zLevel).color(1F, 1F, 1F, 1F).uv(minU + tWidth, minV + tHeight).endVertex();
        buffer.vertex(x + width,   y,          zLevel).color(1F, 1F, 1F, 1F).uv(minU + tWidth, minV).endVertex();
        buffer.vertex(x,           y,          zLevel).color(1F, 1F, 1F, 1F).uv(minU, minV).endVertex();
        //@formatter:on
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
        if (drawHoveringText.get() && isMouseOver(mouseX, mouseY)) {
            long maxEnergy = getCapacity();
            long energy = getEnergy();

            String title = rfMode ? i18ni("energy_storage") : i18ni("operational_potential");
            boolean shift = Screen.hasShiftDown();
            String suffix = rfMode ? i18ni("rf") : i18ni("op");
            String capString = (shift ? Utils.addCommas(maxEnergy) : Utils.formatNumber(maxEnergy)) + " " + suffix;
            String storedString = (shift ? Utils.addCommas(energy) : Utils.formatNumber(energy)) + " " + suffix;
            String percent = " (" + MathUtils.round(((double) energy / (double) maxEnergy) * 100D, 100) + "%)";

            StringBuilder builder = new StringBuilder();
            builder.append(DARK_AQUA).append(I18n.get(title)).append("\n");

            builder.append(GOLD).append(i18ni("capacity")).append(" ").append(GRAY).append(capString).append("\n");
            builder.append(GOLD).append(i18ni("stored")).append(" ").append(GRAY).append(storedString).append(percent).append("\n");

            IOInfo ioInfo = getIOInfo();
            if (ioInfo != null) {
                if (shift) {
                    builder.append(GOLD).append(i18ni("input")).append(" ").append(GREEN).append("+").append(Utils.formatNumber(ioInfo.currentInput()));
                    builder.append(" ").append(suffix).append("/t\n");

                    builder.append(GOLD).append(i18ni("output")).append(" ").append(RED).append("-").append(Utils.formatNumber(ioInfo.currentOutput()));
                    builder.append(" ").append(suffix).append("/t\n");
                } else {
                    long io = ioInfo.currentInput() - ioInfo.currentOutput();
                    builder.append(GOLD).append(i18ni("io")).append(" ").append(io > 0 ? GREEN + "+" : io < 0 ? RED : GRAY);
                    builder.append(Utils.formatNumber(io)).append(" ").append(suffix).append("/t\n");
                }
            }

            PoseStack poseStack = new PoseStack();
            poseStack.translate(0, 0, getRenderZLevel());
            renderToolTipStrings(poseStack, Lists.newArrayList(builder.toString().split("\n")), mouseX, mouseY);
//            drawHoveringText(Lists.newArrayList(builder.toString().split("\n")), mouseX, mouseY, fontRenderer, displayWidth(), displayHeight());
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        return super.onUpdate();
    }


    public void bindShader(ShaderProgram program) {
//        if (useShaders()) {
//            Rectangle rect = toScreenSpace(xPos() + 1, yPos() + 1, xSize() - 2, ySize() - 2);
//            UniformCache uniforms = program.pushCache();
//            uniforms.glUniform1f("charge", getSOC() * 1.01F);
//            uniforms.glUniform2i("ePos", rect.x, rect.y);
//            uniforms.glUniform2i("eSize", rect.width, rect.height);
//            uniforms.glUniform2i("screenSize", displayWidth(), displayHeight());
//            program.use();
//            program.popCache(uniforms);
//        }
    }

    public void releaseShader(ShaderProgram program) {
        if (useShaders()) {
            program.release();
        }
    }

    public boolean useShaders() {
        return !rfMode && shaderEnabled.get() && BCShaders.useShaders();
    }
}
