package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.CursorHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiManipulable;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.hud.HudData;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

/**
 * Created by brandon3055 on 18/8/21
 */
public class HudConfigGui extends ModularGuiScreen {
    protected GuiToolkit<HudConfigGui> toolkit = new GuiToolkit<>(this, 0, 0).setTranslationPrefix("gui.brandonscore.hud_config");

    public HudConfigGui() {
        super(Component.translatable("gui.brandonscore.hud_config.name"));
    }

    @Override
    public void addElements(GuiElementManager manager) {
        GuiManipulable titleElement = new GuiManipulable().setEnableCursors(true).setSize(150, 13);
        GuiElement<?> bg = new GuiBorderedRect().setFillColour(0x80000000);
        titleElement.addChild(bg.setPosAndSize(titleElement).translate(0, -1));
        toolkit.createHeading("gui.brandonscore.hud_config.name", bg).setPosAndSize(titleElement).setAlignment(GuiAlign.CENTER);
        manager.addChild(titleElement);
        titleElement.setPos((width - titleElement.xSize()) / 2, 30);

        for (AbstractHudElement element : HudManager.getHudElements().values()) {
            manager.addChild(new ElementHandler(element));
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        HudData.saveIfDirty();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    private class ElementHandler extends GuiElement<ElementHandler> {
        private AbstractHudElement element;
        private float borderAnim = 0;
        private float bgAnim = 0;
        private int tick = 0;
        boolean dragging = false;
        private GuiButton settings;
        private GuiElement<?> settingsElement = null;

        public ElementHandler(AbstractHudElement element) {
            this.element = element;
        }

        @Override
        public void addChildElements() {
            settings = toolkit.createIconButton(this, 10, BCGuiSprites.getter("dark/gear"));
            settings.setPosModifiers(() -> (int) (element.xPos() + element.width() - settings.xSize()) - 2, () -> (int) element.yPos() + 2);
            settings.setHoverText(toolkit.i18n("settings"));
            settings.setResetHoverOnClick(true);

            settings.onReleased(() -> {
                if (settingsElement != null) {
                    removeChild(settingsElement);
                    settingsElement = null;
                } else {
                    settingsElement = element.createConfigDialog(settings);
                    settingsElement.setPos(settings.maxXPos(), settings.yPos());
                    addChild(settingsElement);
                    settingsElement.normalizePosition();
                }
            });

            ResourceLocation loc = HudManager.HUD_REGISTRY.getKey(element);
            String infoKey = String.format("hud.%s.%s.info", loc.getNamespace(), loc.getPath());
            String translatedInfo = I18n.get(infoKey);
            if (!infoKey.equals(translatedInfo)) {
                GuiButton infoButton = toolkit.createIconButton(this, 10, BCGuiSprites.getter("dark/info_icon"));
                infoButton.setPosModifiers(() -> (int) (element.xPos() + 2), () -> (int) element.yPos() + 2);
                infoButton.setHoverText(translatedInfo);
                infoButton.setHoverTextDelay(0);
                infoButton.playClick = false;
            }
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
            PoseStack mStack = new PoseStack();
            mStack.translate(0.0D, 0.0D, getRenderZLevel());
            double x = element.xPos();
            double y = element.yPos();
            double w = element.width();
            double h = element.height();
            int rgb = Color.HSBtoRGB((tick + partialTicks) / 200F, 1F, 1F);
            drawBackground(getter, x, y, w, h, partialTicks, rgb);

            if (bgAnim > 0) {
            	ResourceLocation loc = HudManager.HUD_REGISTRY.getKey(element);
                Component name = Component.translatable(String.format("hud.%s.%s.name", loc.getNamespace(), loc.getPath()));
                int tw = fontRenderer.width(name);
                float bgAnim = Math.min(this.bgAnim + (partialTicks * 0.1F), 1);
                fontRenderer.drawInBatch(name, (float) (x + (w - tw) / 2), (float) y + ((float) h - 8F) / 2F, (0x00FFFFFF | ((int) (0xFF * bgAnim) << 24)), false, mStack.last().pose(), getter, false, 0, 0xf000f0);
            }

            getter.endBatch();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean onUpdate() {
            if (borderAnim < 1) {
                borderAnim += 0.1;
            } else if (bgAnim < 1) {
                bgAnim += 0.1;
            }
            tick++;
            double mouseX = getMouseX();
            double mouseY = getMouseY();
            if (isMouseOver(mouseX, mouseY) && childElements.stream().noneMatch(e -> e.isMouseOver(mouseX, mouseY))) {
                modularGui.getManager().setCursor(CursorHelper.DRAG);
            }
            return super.onUpdate();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean captured = super.mouseClicked(mouseX, mouseY, button);
            if (!captured && settingsElement != null && !settingsElement.isMouseOver(mouseX, mouseY)) {
                removeChild(settingsElement);
                settingsElement = null;
                return true;
            } else if (!captured && isMouseOver(mouseX, mouseY)) {
                dragging = true;
                element.startMoving(mouseX, mouseY);
                return true;
            }
            return captured;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
            if (dragging) {
                element.onDragged(mouseX, mouseY);
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (dragging) {
                element.stopMoving();
                dragging = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return GuiHelper.isInRect(element.xPos(), element.yPos(), element.width(), element.height(), mouseX, mouseY);
        }

        private void drawBackground(MultiBufferSource getter, double x, double y, double w, double h, float partialTicks, int colour) {
            float borderAnim = this.borderAnim + (partialTicks * 0.1F);
            float bgAnim = Math.min(this.bgAnim + (partialTicks * 0.1F), 1);
            drawColouredRect(getter, x, y, w, h, 0x00000000 | ((int) (0x8F * bgAnim) << 24));

            double boarderLength = w * 2 + h * 2;
            double bp = boarderLength * borderAnim;
            if (bp > 0) {
                double sw = Math.min(w, bp);
                //Top Boarder
                drawGradientQuad(getter, x, y, x + sw, y, x + sw - 1, y + 1, x + 1, y + 1, 0xFF000000, colour);
                drawGradientQuad(getter, x + 1, y + 1, x + sw - 1, y + 1, x + sw - 2, y + 2, x + 2, y + 2, colour, 0xFF000000);
            }

            if (bp - w > 0) {
                double sh = Math.min(h, bp - w);
                double p = sh / h;
                //Right Boarder
                drawGradientQuad(getter, x + w, y, x + w, y + sh, x + w - 1, y + sh - 1 * p, x + w - 1, y + 1, 0xFF000000, colour);
                drawGradientQuad(getter, x + w - 1, y + 1, x + w - 1, y + sh - 1, x + w - 2, y + sh - 2 * p, x + w - 2, y + 2, colour, 0xFF000000);
            }

            if (bp - w - h > 0) {
                double sw = Math.min(w, bp - w - h);
                double sx = x + w - sw;
                double p = sw / w;
                //Bottom Boarder
                drawGradientQuad(getter, sx + 2 * p, y + h - 2, sx + sw - 2, y + h - 2, sx + sw - 1, y + h - 1, sx + 1, y + h - 1, 0xFF000000, colour);
                drawGradientQuad(getter, sx + 1 * p, y + h - 1, sx + sw - 1, y + h - 1, sx + sw, y + h, sx, y + h, colour, 0xFF000000);
            }

            if (bp - w - h - w > 0) {
                double sh = Math.min(h, bp - w - h - w);
                double sy = y + h - sh;
                double p = sh / h;
                //Left Boarder
                drawGradientQuad(getter, x + 1, sy + 1 * p, x + 1, sy + sh - 1, x, sy + sh, x, sy, colour, 0xFF000000);
                drawGradientQuad(getter, x + 2, sy + 2 * p, x + 2, sy + sh - 2, x + 1, sy + sh - 1, x + 1, sy + 1 * p, 0xFF000000, colour);
            }
        }

        private void drawGradientQuad(MultiBufferSource getter, double p1A, double p1B, double p2A, double p2B, double p3A, double p3B, double p4A, double p4B, int startColor, int endColor) {
            if (startColor == endColor && endColor == 0) return;
            double zLevel = getRenderZLevel();
            //@formatter:off
            float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
            float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
            float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
            float startBlue  = (float)(startColor       & 255) / 255.0F;
            float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
            float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
            float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
            float endBlue    = (float)(endColor         & 255) / 255.0F;

            VertexConsumer builder = getter.getBuffer(transColourType);
            builder.vertex(p1A, p1B, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            builder.vertex(p2A, p2B, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            builder.vertex(p3A, p3B, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
            builder.vertex(p4A, p4B, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
            //@formatter:on
        }
    }
}
