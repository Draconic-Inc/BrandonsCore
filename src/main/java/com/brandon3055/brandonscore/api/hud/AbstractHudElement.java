package com.brandon3055.brandonscore.api.hud;

import codechicken.lib.gui.modular.elements.GuiContextMenu;
import codechicken.lib.gui.modular.elements.GuiElement;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Created by brandon3055 on 30/7/21
 * <p>
 * This represents a client side hud element that will be rendered on the client screen.
 * Your custom hud elements must be registered to the AbstractHudElement forge registry.
 * Just keep in mind this is a client side only registry.
 */
public abstract class AbstractHudElement {

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
    public abstract void render(PoseStack mStack, float partialTicks, boolean configuring);

    public boolean shouldRender(boolean preRenderEvent) {
        return preRenderEvent;
    }

    public void addConfigElements(GuiContextMenu menu) {
        menu.addOption(() -> Component.translatable("gui.brandonscore.hud_config.enabled." + enabled), runDirty(() -> enabled = !enabled));
    }

    public GuiContextMenu createConfigDialog(GuiElement<?> parentElement) {
        GuiContextMenu dialog = GuiContextMenu.tooltipStyleMenu(parentElement);
        addConfigElements(dialog);
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

    public void writeNBT(CompoundTag nbt) {
        nbt.putBoolean("enabled", enabled);
        nbt.putDouble("pos_x", rawPos.x);
        nbt.putDouble("pos_y", rawPos.y);
    }

    public void readNBT(CompoundTag nbt) {
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
        rawPos.x = Mth.clamp(rawPos.x + (xMove / screenWidth()), 0D, 1D);
        rawPos.y = Mth.clamp(rawPos.y + (yMove / screenHeight()), 0D, 1D);
    }

    public void stopMoving() {
        markDirty();
    }

    public AbstractHudElement setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}