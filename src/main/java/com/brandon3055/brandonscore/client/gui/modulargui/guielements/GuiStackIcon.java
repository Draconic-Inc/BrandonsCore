package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.lib.StringyStacks;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class GuiStackIcon extends GuiElement<GuiStackIcon> implements IModularGui.JEITargetAdapter {
    public static Map<String, ItemStack> stackCache = new HashMap<>();

    public boolean drawCount = true;
    public boolean drawToolTip = true;
    public boolean drawHoverHighlight = false;
    private GuiElement background = null;
    protected List<Component> toolTipOverride = null;
    private String stackString;
    private ItemStack stack = ItemStack.EMPTY;
    private Runnable clickListener = null;
    private Consumer<Object> ingredientDropListener = null;

    public GuiStackIcon() {
        this(ItemStack.EMPTY);
    }

    public GuiStackIcon(String stackString) {
        this.stackString = stackString;
        setSize(18, 18);
        setInsets(1, 1, 1, 1);
    }

    public GuiStackIcon(ItemStack stack) {
        this.stack = stack;
        setSize(18, 18);
        setInsets(1, 1, 1, 1);
    }

    public GuiStackIcon(int xPos, int yPos, String stackString) {
        super(xPos, yPos);
        this.stackString = stackString;
        setSize(18, 18);
        setInsets(1, 1, 1, 1);
    }

    public GuiStackIcon(int xPos, int yPos, int xSize, int ySize, String stackString) {
        super(xPos, yPos, xSize, ySize);
        this.stackString = stackString;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (drawHoverHighlight && isMouseOver(mouseX, mouseY)) {
            drawColouredRect(xPos(), yPos(), xSize(), ySize(), -2130706433);
        }
        renderStack(minecraft);
    }

    public GuiStackIcon setDrawCount(boolean drawCount) {
        this.drawCount = drawCount;
        return this;
    }

    private void renderStack(Minecraft minecraft) {
        if (getStack().isEmpty()) return;
        float xScale = xSize() / 18F;
        float yScale = ySize() / 18F;
        double itemX = xPos() + (xScale * getInsets().left);
        double itemY = yPos() + (yScale * getInsets().top);
        renderGuiItem(minecraft, getStack(), itemX, itemY, getRenderZLevel(), xScale, yScale);
        renderItemOverlay(getStack(), itemX, itemY, getRenderZLevel(), xScale, yScale);
    }

    protected void renderGuiItem(Minecraft minecraft, ItemStack stack, double x, double y, double z, float xScale, float yScale) {
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, null, null, 0);

        minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(x, y, z + 100);
        posestack.translate(8.0D * xScale, 8.0D * yScale, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        posestack.scale(xScale, yScale, 1F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void renderItemOverlay(ItemStack stack, double x, double y, double z, float xScale, float yScale) {
        if (!stack.isEmpty()) {
            PoseStack poseStack = new PoseStack();
            if (stack.getCount() != 1 && drawCount) {
                String s = String.valueOf(stack.getCount());
                poseStack.pushPose();
                poseStack.translate(x, y, z + 200.0F);
                poseStack.scale(xScale, yScale, 1F);
                MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                fontRenderer.drawInBatch(s, (float) (19 - 2 - fontRenderer.width(s)), (float) (6 + 3), 16777215, true, poseStack.last().pose(), buffer, false, 0, 15728880);
                buffer.endBatch();
                poseStack.popPose();
            }

            if (stack.isBarVisible()) {
                MultiBufferSource getter = RenderUtils.getGuiBuffers();
                poseStack.translate(x, y, z + 200.0F);
                poseStack.scale(xScale, yScale, 1F);
                int i = stack.getBarWidth();
                int colour = stack.getBarColor();
                GuiHelper.drawRect(getter, poseStack, 2, 13, 13, 2, 0xFF000000);
                GuiHelper.drawRect(getter, poseStack, 2, 13, i, 1, 0xFF000000 | colour);
                RenderUtils.endBatch(getter);
            }
        }
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (getInsetRect().contains(mouseX, mouseY) && (drawToolTip || toolTipOverride != null) && !getStack().isEmpty()) {
            List<Component> list = toolTipOverride != null ? toolTipOverride : getTooltipFromItem(getStack());
            PoseStack poseStack = new PoseStack();
            poseStack.translate(0, 0, getRenderZLevel());
            renderTooltip(poseStack, list, mouseX, mouseY);
            return true;
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (clickListener != null && isMouseOver(mouseX, mouseY)) {
            clickListener.run();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void setClickListener(Runnable clickListener) {
        this.clickListener = clickListener;
    }

    @Deprecated
    public GuiStackIcon setStack(@Nullable String stackString) {
        this.stackString = stackString;
        return this;
    }

    public GuiStackIcon setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    /**
     * Add an element to be used as the background for this stack.<br>
     * Recommend {@link GuiTexture#newSlot()} or {@link GuiBorderedRect}<br>
     * But really you can use any element base including buttons which will make the element function as a button.<br>
     * When you add a background element its size and position will automatically be adjusted to match the stack icon.
     *
     * @param background a MGuiElementBase object.
     * @return the MGuiStackIcon
     */
    public GuiStackIcon setBackground(GuiElement background) {
        if (background == null) {
            if (this.background != null) {
                removeChild(this.background);
                this.background = null;
            }
        } else {
            if (this.background != null) {
                removeChild(this.background);
            }
            this.background = background;
            background.setPos(this);
            background.setSize(this);
            addChild(background);
        }

        return this;
    }

    /**
     * Will render the normal item tool tip when you hover over the stack.
     *
     * @return the MGuiStackIcon
     */
    public GuiStackIcon setToolTip(boolean drawToolTip) {
        this.drawToolTip = drawToolTip;
        return this;
    }

    public ItemStack getStack() {
        if (stackString == null) {
            return stack;
        } else if (stackString.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!stackCache.containsKey(stackString)) {
            ItemStack stack = StringyStacks.fromString(stackString, null);
            if (stack == null) {
                stack = new ItemStack(Blocks.BARRIER);
                toolTipOverride = new ArrayList<>();
                toolTipOverride.add(new TextComponent("Failed to load Item Stack"));
                toolTipOverride.add(new TextComponent("This may mean the mod the stack belongs to is not installed"));
                toolTipOverride.add(new TextComponent("Or its just broken..."));
            }
            stackCache.put(stackString, stack);
        }

        return stackCache.get(stackString);
    }

    @Deprecated
    public GuiStackIcon setToolTipOverride(List<String> toolTipOverride) {
        this.toolTipOverride = toolTipOverride == null ? null : toolTipOverride.stream().map(TextComponent::new).collect(Collectors.toList());
        return this;
    }

    public GuiStackIcon setHoverOverride(List<Component> toolTipOverride) {
        this.toolTipOverride = toolTipOverride;
        return this;
    }

    public GuiStackIcon setDrawHoverHighlight(boolean drawHoverHighlight) {
        this.drawHoverHighlight = drawHoverHighlight;
        return this;
    }

    public GuiStackIcon addSlotBackground() {
        setBackground(null);
        addChild(background = GuiTexture.newSlot().setPos(this).setSizeModifiers((guiSlotRender, integer) -> GuiStackIcon.this.xSize(), (guiSlotRender, integer) -> GuiStackIcon.this.ySize()));
        return this;
    }

    public GuiElement getBackground() {
        return background;
    }

    @Override
    public Rectangle getArea() {
        return getRect();
    }

    public void setIngredientDropListener(Consumer<Object> ingredientDropListener) {
        this.ingredientDropListener = ingredientDropListener;
    }

    @Override
    public void accept(Object ingredient) {
        if (ingredientDropListener != null) {
            ingredientDropListener.accept(ingredient);
        }
    }
}
