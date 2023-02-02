package com.brandon3055.brandonscore.client.gui;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingBehavior;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingElytraCompat;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.ChatFormatting.UNDERLINE;


/**
 * Created by brandon3055 on 18/8/21
 */
public class ContributorConfigGui extends ModularGuiScreen {
    protected GuiToolkit<ContributorConfigGui> toolkit = new GuiToolkit<>(this, 0, 0).setTranslationPrefix("");
    private Player player;
    private ContributorProperties props;
    private ContributorConfig cfg;
    private GuiEntityRenderer playerRender;
    private GuiManipulable container;
    private GuiPickColourDialog wingColourA;
    private GuiPickColourDialog wingColourB;
    private GuiPickColourDialog shieldColour;
    private boolean dragging = false;

    public ContributorConfigGui(Player player, ContributorProperties props) {
        super(new TextComponent("Contributor Configuration"));
        this.player = player;
        this.props = props;
        this.cfg = props.getConfig();
    }

    @Override
    public void addElements(GuiElementManager manager) {
        GuiManipulable titleElement = manager.addChild(new GuiManipulable())
                .setEnableCursors(true)
                .setSize(150, 13)
                .onReload(e -> e.setPos((width - e.xSize()) / 2, 15));
        GuiElement<?> bg = titleElement.addChild(new GuiBorderedRect())
                .setFillColour(0x80000000)
                .setPosAndSize(titleElement)
                .translate(0, -1);
        toolkit.createHeading("Contributor Configuration", bg).setPosAndSize(titleElement).setAlignment(GuiAlign.CENTER);

        container = manager.addChild(new GuiManipulable())
                .setSize(100, 100)
                .setDragBarHeight(100)
                .setCanResizeH(() -> false)
                .setCanResizeV(() -> false)
                .setEnableCursors(true)
                .onReload(e -> e.setPos((width / 2) - (e.xSize() / 2), (height / 2) - (e.ySize() / 2)));

        playerRender = container.addChild(new GuiEntityRenderer())
                .setEntity(player)
                .setSize(100, 100)
                .onReload(e -> toolkit.center(e, container, 0, 0));

        manager.addChild(createButton("Rotate")
                .setSize(80, 16)
                .onReload(e -> e.setPos(width / 2 - 40, height - 19))
                .setToggleMode(true)
                .setToggleStateSupplier(() -> !playerRender.isRotationLocked())
                .onPressed(() -> playerRender.rotationLocked(!playerRender.isRotationLocked())));

        List<GuiElement<?>> leftControls = new ArrayList<>();
        List<GuiElement<?>> rightControls = new ArrayList<>();
        createControls(leftControls, rightControls);
        arrangeControls(manager.addChild(new GuiElement<>()), leftControls).onReload(e -> e.setPos(3, height - e.ySize() - 3));
        arrangeControls(manager.addChild(new GuiElement<>()), rightControls).onReload(e -> e.setPos(width - e.xSize() - 3, height - e.ySize() - 3));
    }

    private void createControls(List<GuiElement<?>> leftControls, List<GuiElement<?>> rightControls) {
        int width = 150;
        int height = 14;

        leftControls.add(createListButton("Wings: ", () -> cfg.getWingsTier(), e -> cfg.setWingsTier(e), props.getWingTiers(), true).setEnabledCallback(() -> !props.getWingTiers().isEmpty()).setSize(width, height));

        wingColourA = createColourPicker(() -> cfg.getWingsOverrideBoneColour(), e -> cfg.setWingsOverrideBoneColour(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        leftControls.add(createEnableButton("Wings Colour A: ", () -> cfg.overrideWingBoneColour(), e -> cfg.setOverrideWingBoneColour(e), "Custom", "Default").setEnabledCallback(() -> props.hasWingsRGB())
                .setSize(width, height)
                .addChild(colourPickerButton(wingColourA, () -> this.width - 83, () -> 3).setEnabledCallback(() -> cfg.overrideWingBoneColour())
                        .setXPos(width + 1)
                        .setSize(height, height))
                .addChild(colourRGBButton(() -> cfg.getWingRGBBoneColour(), e -> cfg.setWingRGBBoneColour(e)).setEnabledCallback(() -> cfg.overrideWingBoneColour())
                        .setXPos(width + 16)
                        .setSize(height, height))
                .getParent().getParent());

        wingColourB = createColourPicker(() -> cfg.getWingsOverrideWebColour(), e -> cfg.setWingsOverrideWebColour(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        leftControls.add(createEnableButton("Wings Colour B: ", () -> cfg.overrideWingWebColour(), e -> cfg.setOverrideWingsWebColour(e), "Custom", "Default").setEnabledCallback(() -> props.hasWingsRGB())
                .setSize(width, height)
                .addChild(colourPickerButton(wingColourB, () -> this.width - 83, () -> 80).setEnabledCallback(() -> cfg.overrideWingWebColour())
                        .setXPos(width + 1)
                        .setSize(height, height))
                .addChild(colourRGBButton(() -> cfg.getWingRGBWebColour(), e -> cfg.setWingRGBWebColour(e)).setEnabledCallback(() -> cfg.overrideWingWebColour())
                        .setXPos(width + 16)
                        .setSize(height, height))
                .getParent().getParent());

        leftControls.add(createEnableButton("Wings Shader A: ", () -> cfg.getWingsBoneShader(), e -> cfg.setWingsBoneShader(e)).setEnabledCallback(() -> props.hasWings())
                .setSize(width, height));

        leftControls.add(createEnableButton("Wings Shader B: ", () -> cfg.getWingsWebShader(), e -> cfg.setWingsWebShader(e)).setEnabledCallback(() -> props.hasWings())
                .setSize(width, height));

        shieldColour = createColourPicker(() -> cfg.getShieldOverride(), e -> cfg.setShieldOverride(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        leftControls.add(createEnableButton("Shield Colour: ", () -> cfg.overrideShield(), e -> cfg.setOverrideShield(e), "Custom", "Default").setEnabledCallback(() -> props.hasShieldRGB()).setSize(width, height)
                .addChild(colourPickerButton(shieldColour, () -> 3, () -> 3).setEnabledCallback(() -> cfg.overrideShield()).setXPos(width + 1).setSize(height, height))
                .addChild(colourRGBButton(() -> cfg.getShieldRGB(), e -> cfg.setShieldRGB(e)).setEnabledCallback(() -> cfg.overrideShield()).setXPos(width + 16).setSize(height, height)).getParent().getParent());

        rightControls.add(createEnableButton("Welcome Message: ", () -> cfg.showWelcome(), e -> cfg.setShowWelcome(e))
                .setSize(width, height));

        rightControls.add(createListButton("Front Badge: ", () -> cfg.getChestBadge(), e -> cfg.setChestBadge(e), props.getBadges(), false)
                .setEnabledCallback(() -> !props.getBadges().isEmpty())
                .setSize(width, height));

        rightControls.add(createListButton("Back Badge: ", () -> cfg.getBackBadge(), e -> cfg.setBackBadge(e), props.getBadges(), false)
                .setEnabledCallback(() -> !props.getBadges().isEmpty())
                .setSize(width, height));

        rightControls.add(new GuiLabel(UNDERLINE + " Wing Behavior ").setEnabledCallback(() -> props.hasWings())
                .setSize(width, height));

        rightControls.add(createEnumButton("Grounded: ", WingBehavior.class, () -> cfg.getWingsGround(), e -> cfg.setWingsGround(e)).setEnabledCallback(() -> props.hasWings())
                .setSize(width, height)
                .setHoverText("What happens when you are not flying"));

        rightControls.add(createEnumButton("Creative: ", WingBehavior.class, () -> cfg.getWingsCreative(), e -> cfg.setWingsCreative(e)).setEnabledCallback(() -> props.hasWings())
                .setSize(width, height)
                .setHoverText("When using creative style flight"));

        rightControls.add(createEnumButton("Elytra: ", WingElytraCompat.class, () -> cfg.getWingsElytra(), e -> cfg.setWingsElytra(e)).setEnabledCallback(() -> props.hasWings())
                .setSize(width, height));
    }

    private GuiElement<?> arrangeControls(GuiElement<?> container, List<GuiElement<?>> controls) {
        int y = 0;
        for (GuiElement<?> control : controls) {
            if (!control.isEnabled()) continue;
            container.addChild(control).setYPos(y);
            y += control.ySize() + 1;
        }
        container.setBoundsToChildren();
        return container;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && playerRender.isMouseOver(mouseX, mouseY)) {
            dragging = true;
            return true;
        }
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        dragging = !ret;
        return ret;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (dragging) {
            playerRender.setLockedRotation(playerRender.getLockedRotation() + (float) dragX);
        }
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        int size = playerRender.xSize();
        size = (int) MathHelper.clip(size + (scrollAmount * Math.max(size / 10, 1)), 10, 500);
        playerRender.setSize(size, size);
        playerRender.reloadElement();
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        cfg = props.getConfig();
    }

    private GuiButton createButton(String text) {
        return new GuiButton(text)
                .setFillColours(0x90000000, 0x90116011)
                .setTextColour(Palette.Ctrl.textH(false), Palette.Ctrl.textH(true))
                .setBorderColours(0xFF606060, 0xFF9090FF);
    }

    private GuiButton createEnableButton(String text, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return createEnableButton(text, getter, setter, "Enabled", "Disabled");
    }

    private GuiButton createEnableButton(String text, Supplier<Boolean> getter, Consumer<Boolean> setter, String trueText, String falseText) {
        return new GuiButton()
                .setDisplaySupplier(() -> text + (getter.get() ? trueText : falseText))
                .setFillColours(0x90000000, 0x90116011)
                .setTextColour(Palette.Ctrl.textH(false), Palette.Ctrl.textH(true))
                .setBorderColours(0xFF606060, 0xFF9090FF)
                .setToggleStateSupplier(getter)
                .onPressed(() -> setter.accept(!getter.get()));
    }

    private <T> GuiButton createListButton(String text, Supplier<T> getter, Consumer<T> setter, List<T> values, boolean nullOption) {
        return new GuiButton()
                .setDisplaySupplier(() -> text + (getter.get() == null ? "Disabled" : getter.get().toString()))
                .setFillColours(0x90000000, 0x90116011)
                .setTextColour(Palette.Ctrl.textH(false), Palette.Ctrl.textH(true))
                .setBorderColours(0xFF606060, 0xFF9090FF)
                .setHoverText(e -> getter.get() instanceof ContributorConfig.HoverText h ? h.getHoverText() : null)
                .onPressed(() -> {
                    if (values.isEmpty()) return;
                    T current = getter.get();
                    if (current == null) setter.accept(values.get(0));
                    else if (nullOption && values.indexOf(current) == values.size() - 1) setter.accept(null);
                    else setter.accept(values.get((values.indexOf(current) + 1) % values.size()));
                });
    }

    private GuiButton colourPickerButton(GuiPickColourDialog dialog, Supplier<Integer> xPos, Supplier<Integer> yPos) {
        GuiButton button = toolkit.createIconButton(null, 14, BCGuiSprites.getter("color_picker"));
        dialog.setEnabledCallback(button::isEnabled);
        button.onPressed(() -> {
            dialog.setPos(xPos.get(), yPos.get());
            dialog.toggleShown(false, 200);
        });
        return button;
    }

    private GuiButton colourRGBButton(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return toolkit.createIconButton(null, 14, BCGuiSprites.getter("rgb_checker"))
                .setToggleStateSupplier(getter)
                .setHoverText("Enable rainbow RGB mode.", "Use the colour picker to configure,", "Red = Cycle Speed", "Green = Saturation", "Blue = Brightness")
                .onPressed(() -> setter.accept(!getter.get()));
    }

    private GuiPickColourDialog createColourPicker(Supplier<Integer> getter, Consumer<Integer> setter, Supplier<Integer> getDefault) {
        GuiPickColourDialog dialog = new GuiPickColourDialog(container)
                .setBackgroundElement(new GuiTooltipBackground())
                .setColour(getter.get())
                .setColourChangeListener(setter)
                .setIncludeAlpha(false)
                .setCloseOnOutsideClick(false)
                .setCancelEnabled(true);
        dialog.onReload(e -> {
            e.cancelButton.setText("Reset")
                    .setTrim(false)
                    .setHoverText("Apply the default colours for the currently selected Wings tier")
                    .onPressed(() -> dialog.updateColour(getDefault.get()));

        }, false);
        return dialog;
    }

    private <E extends Enum<E>> GuiButton createEnumButton(String text, Class<E> clazz, Supplier<E> getter, Consumer<E> setter) {
        return new GuiButton()
                .setDisplaySupplier(() -> text + getter.get().toString())
                .setHoverText(e -> getter.get() instanceof ContributorConfig.HoverText hover ? hover.getHoverText() : null)
                .setFillColours(0x90000000, 0x90116011)
                .setTextColour(Palette.Ctrl.textH(false), Palette.Ctrl.textH(true))
                .setBorderColours(0xFF606060, 0xFF9090FF)
                .onButtonPressed((button) -> setter.accept(clazz.getEnumConstants()[(getter.get().ordinal() + (button == 0 ? 1 : -1)) % clazz.getEnumConstants().length]));
    }
}
