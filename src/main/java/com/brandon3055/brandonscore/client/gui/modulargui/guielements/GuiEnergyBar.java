package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.BCShaders;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static net.minecraft.ChatFormatting.*;

/**
 * Created by brandon3055 on 18/10/2016.
 */
public class GuiEnergyBar extends GuiElement<GuiEnergyBar> {

    private static final RenderType SHADER_TYPE = RenderType.create("starfield", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> BCShaders.energyBarShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(BCGuiSprites.ATLAS_LOCATION, false, false))
                    .createCompositeState(false)
    );

    private IOPStorage energyHandler = null;
    private Supplier<Long> capacitySupplier = null;
    private Supplier<Long> energySupplier = null;
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
        boolean horizontal = xSize() > ySize();

        int barLength = horizontal ? xSize() : ySize();
        int barWidth = horizontal ? ySize() : xSize();
        double charge = getSOC();
        if (Double.isNaN(charge)) charge = 0;
        int draw = (int) (charge * (barLength - 2));

        int posY = yPos();
        int posX = xPos();

        PoseStack poseStack = new PoseStack();
        if (horizontal) {
            int x = posY;
            posY = posX;
            posX = x;
            poseStack.translate(barLength + (posY * 2), 0, getRenderZLevel());
            poseStack.mulPose(new Quaternion(0, 0, 90, true));
        }

        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int light = 0xFFFFFFFF;
        int dark = darkMode ? 0xFF808080 : 0xFF505050;
        GuiHelper.drawShadedRect(getter, poseStack, posX, posY, barWidth, barLength, 1, 0, dark, light, midColour(light, dark));

        if (disabled.get()) {
            GuiHelper.drawRect(getter, poseStack, posX + 1, posY + 1, barWidth - 2, barLength - 2, 0xFF000000);
        } else if (!shaderEnabled.get()) {
            Material matBase = BCGuiSprites.get("bars/energy_empty");
            Material matOverlay = BCGuiSprites.get("bars/energy_full");
            VertexConsumer shaderConsumer = new TransformingVertexConsumer(getter.getBuffer(BCGuiSprites.GUI_TYPE), poseStack);
            sliceSprite(shaderConsumer, posX + 1, posY + 1, barWidth - 2, barLength - 2, matBase.sprite());
            sliceSprite(shaderConsumer, posX + 1, posY + barLength - draw - 1, barWidth - 2, draw, matOverlay.sprite());
        } else {
            Rectangle rect = toScreenSpace(xPos() + 1, yPos() + 1, xSize() - 2, ySize() - 2);
            BCShaders.energyBarCharge.glUniform1f(getSOC() * 1.01F);
            BCShaders.energyBarEPos.glUniform2i(rect.x, rect.y);
            BCShaders.energyBarESize.glUniform2i(rect.width, rect.height);
            BCShaders.energyBarScreenSize.glUniform2i(displayWidth(), displayHeight());
            VertexConsumer shaderConsumer = new TransformingVertexConsumer(getter.getBuffer(SHADER_TYPE), poseStack);
            drawShaderRect(shaderConsumer, posX + 1, posY + 1, barWidth - 2, barLength - 2);
        }
        getter.endBatch();
    }

    private void drawShaderRect(VertexConsumer buffer, float x, float y, float width, float height) {
        //@formatter:off
        buffer.vertex(x,           y + height, 0).endVertex();
        buffer.vertex(x + width,   y + height, 0).endVertex();
        buffer.vertex(x + width,   y,          0).endVertex();
        buffer.vertex(x,           y,          0).endVertex();
        //@formatter:on
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
            renderTooltip(poseStack, Arrays.stream(builder.toString().split("\n")).map(TextComponent::new).collect(Collectors.toList()), mouseX, mouseY);
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        return super.onUpdate();
    }
}
