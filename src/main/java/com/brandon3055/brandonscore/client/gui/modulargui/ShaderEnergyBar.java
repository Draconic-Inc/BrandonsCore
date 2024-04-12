package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.gui.modular.elements.GuiEnergyBar;
import codechicken.lib.gui.modular.elements.GuiRectangle;
import codechicken.lib.gui.modular.lib.GuiRender;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import codechicken.lib.gui.modular.sprite.Material;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.util.FormatUtil;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.api.power.IOInfo;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCGuiTextures;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static net.minecraft.ChatFormatting.*;
import static net.minecraft.ChatFormatting.GRAY;

/**
 * Created by brandon3055 on 13/02/2024
 */
public class ShaderEnergyBar extends GuiEnergyBar {
    private static final RenderType SHADER_TYPE = RenderType.create("starfield", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> BCShaders.energyBarShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(BCGuiTextures.getAtlasHolder().atlasLocation(), false, false))
                    .createCompositeState(false)
    );

    private Supplier<Boolean> shaderEnabled = () -> true;
    private Supplier<Boolean> disabled = () -> false;

    public ShaderEnergyBar(@NotNull GuiParent<?> parent) {
        super(parent);
    }

    public static BiFunction<Long, Long, List<Component>> opEnergyFormatter(@Nullable IOPStorage storage) {
        return (energy, capacity) -> {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.operational_potential").withStyle(DARK_AQUA));
            boolean shift = Screen.hasShiftDown();
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.capacity")
                    .withStyle(GOLD)
                    .append(" ")
                    .append(Component.literal(shift ? FormatUtil.addCommas(capacity) : FormatUtil.formatNumber(capacity))
                            .withStyle(GRAY)
                            .append(" ")
                            .append(Component.translatable("mod_gui.brandonscore.energy_bar.op")
                                    .withStyle(GRAY)
                            )
                    )
            );
            tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.stored")
                    .withStyle(GOLD)
                    .append(" ")
                    .append(Component.literal(shift ? FormatUtil.addCommas(energy) : FormatUtil.formatNumber(energy))
                            .withStyle(GRAY)
                    )
                    .append(" ")
                    .append(Component.translatable("mod_gui.brandonscore.energy_bar.op")
                            .withStyle(GRAY)
                    )
                    .append(Component.literal(String.format(" (%.2f%%)", ((double) energy / (double) capacity) * 100D))
                            .withStyle(GRAY)
                    )
            );
            if (storage != null && storage.getIOInfo() != null) {
                IOInfo ioInfo = storage.getIOInfo();
                if (Screen.hasShiftDown()) {
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.input")
                            .withStyle(GOLD)
                            .append(Component.literal(" +")
                                    .withStyle(GREEN)
                                    .append(Utils.formatNumber(ioInfo.currentInput()))
                            )
                    );
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.output")
                            .withStyle(GOLD)
                            .append(Component.literal(" -")
                                    .withStyle(RED)
                                    .append(Utils.formatNumber(ioInfo.currentOutput()))
                            )
                    );
                } else {
                    long io = ioInfo.currentInput() - ioInfo.currentOutput();
                    tooltip.add(Component.translatable("mod_gui.brandonscore.energy_bar.io")
                            .withStyle(GOLD)
                            .append(Component.literal((io > 0 ? " +" : " ") + io + " ")
                                    .withStyle(io > 0 ? GREEN : io < 0 ? RED : GRAY)
                                    .append(Component.translatable("mod_gui.brandonscore.energy_bar.op"))
                            )
                    );
                }
            }
            return tooltip;
        };
    }

    public ShaderEnergyBar setShaderEnabled(Supplier<Boolean> shaderEnabled) {
        this.shaderEnabled = shaderEnabled;
        return this;
    }

    public GuiEnergyBar setDisabled(Supplier<Boolean> disabled) {
        this.disabled = disabled;
        return this;
    }

    public ShaderEnergyBar bindOpStorage(@Nullable IOPStorage storage) {
        if (storage == null) {
            setEnergy(0).setCapacity(0);
        } else {
            setEnergy(storage::getOPStored).setCapacity(storage::getMaxOPStored);
        }
        return this;
    }

    public ShaderEnergyBar setItemSupplier(Supplier<ItemStack> stackSupplier) {
        setCapacity(() -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getMaxEnergyStored(stackSupplier.get()) : 0);
        setEnergy(() -> EnergyUtils.isEnergyItem(stackSupplier.get()) ? EnergyUtils.getEnergyStored(stackSupplier.get()) : 0);
        return this;
    }

    @Override
    public void renderBackground(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        boolean horizontal = xSize() > ySize();
        double barLength = horizontal ? xSize() : ySize();
        double barWidth = horizontal ? ySize() : xSize();
        double charge = getCapacity() <= 0 ? 0 : getEnergy() / (double) getCapacity();
        if (Double.isNaN(charge)) charge = 0;
        double draw = charge * barLength;

        double posY = yMin();
        double posX = xMin();



        if (horizontal) {
            double x = posY;
            posY = posX;
            posX = x;
            render.pose().pushPose();
            render.pose().translate(barLength + (posY * 2), 0, 0);
            render.pose().mulPose(Axis.ZP.rotationDegrees(90));
        }

        MultiBufferSource.BufferSource getter = render.buffers();
        if (disabled.get()) {
            render.rect(posX, posY, barWidth, barLength, 0xFF000000);
        } else if (!shaderEnabled.get()) {
            Material matBase = BCGuiTextures.get("bars/energy_empty");
            Material matOverlay = BCGuiTextures.get("bars/energy_full");
            VertexConsumer shaderConsumer = new TransformingVertexConsumer(matBase.buffer(getter, GuiRender::texColType), render.pose());
            sliceSprite(shaderConsumer, posX, posY, barWidth, barLength, matBase.sprite());
            sliceSprite(shaderConsumer, posX, posY + barLength - draw, barWidth, draw, matOverlay.sprite());
        } else {
            Rectangle rect = toScreenSpace(xMin(), yMin(), xSize(), ySize());
            BCShaders.energyBarCharge.glUniform1f((float) charge * 1.01F);
            BCShaders.energyBarEPos.glUniform2i(rect.x, rect.y);
            BCShaders.energyBarESize.glUniform2i(rect.width, rect.height);
            BCShaders.energyBarScreenSize.glUniform2i(mc().getWindow().getWidth(), mc().getWindow().getHeight());
            VertexConsumer shaderConsumer = new TransformingVertexConsumer(getter.getBuffer(SHADER_TYPE), render.pose());
            drawShaderRect(shaderConsumer, posX, posY, barWidth, barLength);
        }
        getter.endBatch();
        if (horizontal) {
            render.pose().popPose();
        }
    }

    private void drawShaderRect(VertexConsumer buffer, double x, double y, double width, double height) {
        //@formatter:off
        buffer.vertex(x,           y + height, 0).endVertex();
        buffer.vertex(x + width,   y + height, 0).endVertex();
        buffer.vertex(x + width,   y,          0).endVertex();
        buffer.vertex(x,           y,          0).endVertex();
        //@formatter:on
    }

    public void sliceSprite(VertexConsumer buffer, double xPos, double yPos, double xSize, double ySize, TextureAtlasSprite sprite) {
        float texU = sprite.getU0();
        float texV = sprite.getV0();
        int texWidth = sprite.contents().width();
        int texHeight = sprite.contents().height();
        float uScale = (sprite.getU1() - texU) / texWidth;
        float vScale = (sprite.getV1() - texV) / texHeight;
        for (double i = 0; i < ySize; i += Math.min(texHeight - 2, ySize - i)) {
            double partSize = Math.min(texHeight, ySize - i);
            bufferRect(buffer, xPos, yPos + ySize - i, xSize, -partSize, sprite.getU0(), sprite.getV0(), (float) xSize * uScale, (float) partSize * vScale);
        }
    }

    private void bufferRect(VertexConsumer buffer, double x, double y, double width, double height, float minU, float minV, float tWidth, float tHeight) {
        //@formatter:off
        buffer.vertex(x,           y + height, 0).color(1F, 1F, 1F, 1F).uv(minU, minV + tHeight).endVertex();
        buffer.vertex(x + width,   y + height, 0).color(1F, 1F, 1F, 1F).uv(minU + tWidth, minV + tHeight).endVertex();
        buffer.vertex(x + width,   y,          0).color(1F, 1F, 1F, 1F).uv(minU + tWidth, minV).endVertex();
        buffer.vertex(x,           y,          0).color(1F, 1F, 1F, 1F).uv(minU, minV).endVertex();
        //@formatter:on
    }

    public Rectangle toScreenSpace(double xPos, double yPos, double xSize, double ySize) {
        double yResScale = (double) mc().getWindow().getHeight() / scaledScreenHeight();
        double xResScale = (double) mc().getWindow().getWidth() / scaledScreenWidth();
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (mc().getWindow().getHeight() - (yPos * yResScale) - scaledHeight);
        return new Rectangle(x, y, (int) scaledWidth, (int) scaledHeight);
    }

    public record EnergyBar(GuiRectangle container, ShaderEnergyBar bar) {}

}
