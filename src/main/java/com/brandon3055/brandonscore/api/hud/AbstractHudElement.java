package com.brandon3055.brandonscore.api.hud;

import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase.PopoutDialog;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 30/7/21
 * <p>
 * This represents a client side hud element that will be rendered on the client screen.
 * Your custom hud elements must be registered to the AbstractHudElement forge registry.
 * Just keep in mind this is a client side only registry.
 */
public abstract class AbstractHudElement extends ForgeRegistryEntry<AbstractHudElement> {

    protected Vector2 rawPos;
    protected double width = 100;
    protected double height = 20;
    protected boolean enabled = true;
    protected double dragXOffset = 0;
    protected double dragYOffset = 0;
    private Runnable changeListener = null;

    public AbstractHudElement(Vector2 defaultRawPos) {
        this.rawPos = defaultRawPos;
    }

    /**
     * This method should return the absolute width of this hud element. Ideally nothing rendered by the element
     * should extend outside the bounds set by {@link #width()} and {@link #height()}
     *
     * @return the width of this hud element.
     */
    public double width() {
        return width;
    }

    /**
     * This method should return the absolute height of this hud element. Ideally nothing rendered by the element
     * should extend outside the bounds set by {@link #width()} and {@link #height()}
     *
     * @return the height of this hud element.
     */
    public double height() {
        return height;
    }

    /**
     * Called every client tick. Use this to update any values and do any calculations required for this hud.
     *
     * @param configuring This will be true when the hud config gui is open.
     */
    public abstract void tick(boolean configuring);

    /**
     * Use this to render your hud element. The given matrix has not had any transformation applied
     * but is wrapped in push and pop for convenience.
     *
     * @param mStack      The matrix stack.
     * @param configuring This will be true when the hud config gui is open.
     */
    public abstract void render(MatrixStack mStack, float partialTicks, boolean configuring);

    public boolean shouldRender(ElementType type, boolean preRenderEvent) {
        //TODO maybe refine this a bit
        Minecraft mc = Minecraft.getInstance();
        return preRenderEvent && type == ElementType.ALL/* && !mc.options.renderDebug && !(mc.screen instanceof ChatScreen)*/;
    }

    public void addConfigElements(List<GuiElement<?>> list, GuiElement<?> parent) {
        list.add(createButton(() -> I18n.get("gui.brandonscore.hud_config.enabled." + enabled), parent, runDirty(() -> enabled = !enabled)));
    }

    protected GuiButton createButton(Supplier<String> textSupplier, @Nullable GuiElement<?> parent, @Nullable Runnable onPressed) {
        GuiButton button = new GuiButton();
        button.setDisplaySupplier(textSupplier);
        if (onPressed != null) {
            button.onReleased(onPressed);
        }
        if (parent != null) {
            parent.addChild(button);
        }
        button.setSize(120, 12);
        button.setInsets(0, 0, 0, 0);
        button.setRectFillColourGetter((hovering, disabled) -> hovering ? 0xFF475b6a : 0xFF151515);
        return button;
    }

    protected GuiButton createButton(Supplier<String> textSupplier, @Nullable GuiElement<?> parent) {
        return createButton(textSupplier, parent, null);
    }

    public GuiElement<?> createConfigDialog(GuiElement<?> parentElement) {
        PopoutDialog dialog = new PopoutDialog(parentElement).setCloseOnOutsideClick(false);
        dialog.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> {
            IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            GuiHelper.drawHoverRect(getter, new MatrixStack(), dialog.xPos(), dialog.yPos(), dialog.xSize(), dialog.ySize(), 0xFF100010, 0x500000FF, false);
            getter.endBatch();
        });

        List<GuiElement<?>> list = new ArrayList<>();
        addConfigElements(list, dialog);
        int yPos = dialog.yPos() + 3;
        int maxWidth = 0;
        for (GuiElement<?> element : list) {
            element.setPos(dialog.xPos() + 3, yPos);
            maxWidth = Math.max(maxWidth, element.xSize());
            yPos += element.ySize();
        }
        for (GuiElement<?> element : list) {
            element.setXSize(maxWidth);
        }

        Rectangle rect = dialog.getEnclosingRect();
        dialog.setSize(rect.width + 3, rect.height + 3);
        return dialog;
    }

    protected Runnable runDirty(Runnable run) {
        return () -> {
            run.run();
            markDirty();
        };
    }

    //Utils & stuff

    /**
     * @return This is the X position for the top left corner of this hud element.
     */
    public double xPos() {
        int screen = screenWidth();
        double pos = screen * rawPos.x;
        pos -= width() * rawPos.x;
        return (int) pos; //TODO Remove int casts when modular gui supports floating point positions.
    }

    /**
     * @return This is the Y position for the top left corner of this hud element.
     */
    public double yPos() {
        int screen = screenHeight();
        double pos = screen * rawPos.y;
        pos -= height() * rawPos.y;
        return (int) pos;
    }

    public int screenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public int screenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public void writeNBT(CompoundNBT nbt) {
        nbt.putBoolean("enabled", enabled);
        nbt.putDouble("pos_x", rawPos.x);
        nbt.putDouble("pos_y", rawPos.y);
    }

    public void readNBT(CompoundNBT nbt) {
        enabled = nbt.getBoolean("enabled");
        rawPos.x = nbt.getDouble("pos_x");
        rawPos.y = nbt.getDouble("pos_y");
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void markDirty() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public void startMoving(double mouseX, double mouseY) {
        dragXOffset = (int) mouseX - xPos();
        dragYOffset = (int) mouseY - yPos();
    }

    public void onDragged(double mouseX, double mouseY) {
        double xMove = (int) ((mouseX - dragXOffset) - xPos());
        double yMove = (int) ((mouseY - dragYOffset) - yPos());
        rawPos.x = MathHelper.clamp(rawPos.x + (xMove / screenWidth()), 0D, 1D);
        rawPos.y = MathHelper.clamp(rawPos.y + (yMove / screenHeight()), 0D, 1D);
    }

    public void stopMoving() {
        markDirty();
    }

    public AbstractHudElement setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}