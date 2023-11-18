package com.brandon3055.brandonscore.client.gui;

import codechicken.lib.gui.modular.ModularGui;
import codechicken.lib.gui.modular.ModularGuiScreen;
import codechicken.lib.gui.modular.elements.*;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.GuiProvider;
import codechicken.lib.gui.modular.lib.geometry.GeoParam;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingBehavior;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingElytraCompat;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static codechicken.lib.gui.modular.lib.Constraints.LayoutPos.*;
import static codechicken.lib.gui.modular.lib.geometry.Constraint.match;
import static codechicken.lib.gui.modular.lib.geometry.Constraint.relative;


/**
 * Created by brandon3055 on 18/8/21
 */
public class ContributorConfigGui implements GuiProvider {
    protected GuiToolkit toolkit = new GuiToolkit("");
    private Player player;
    private ContributorProperties props;
    private ContributorConfig cfg;
    private GuiEntityRenderer playerRender;
    private GuiManipulable container;
    //    private GuiPickColourDialog wingColourA;
//    private GuiPickColourDialog wingColourB;
//    private GuiPickColourDialog shieldColour;
    private boolean dragging = false;

    public ContributorConfigGui(Player player, ContributorProperties props) {
        this.player = player;
        this.props = props;
        this.cfg = props.getConfig();
    }


    @Override
    public GuiElement<?> createRootElement(ModularGui gui) {
        return GuiProvider.super.createRootElement(gui);
    }

    @Override
    public void buildGui(ModularGui gui) {
        gui.initFullscreenGui();
        gui.setGuiTitle(Component.literal("Contributor Configuration"));

        GuiElement<?> root = gui.getRoot();

        GuiManipulable titleMovable = new GuiManipulable(root)
                .addMoveHandle(13)
                .setCursors(GuiToolkit.CURSORS);
        Constraints.size(titleMovable, 150, 13);
        Constraints.placeInside(titleMovable, root, TOP_CENTER, 0, 15);

        GuiElement<?> titleBackground = new GuiRectangle(titleMovable.getContentElement())
                .fill(0x80000000);
        Constraints.bind(titleBackground, titleMovable.getContentElement());
        Constraints.bind(toolkit.createHeading(titleBackground, Component.literal("Contributor Configuration")), titleBackground);


        container = new GuiManipulable(root)
                .addMoveHandle(100)
                .setCursors(GuiToolkit.CURSORS);
        Constraints.size(container, 100, 100);
        Constraints.center(container, root);
        gui.onResize(() -> container.resetBounds());

        playerRender = new GuiEntityRenderer(container.getContentElement())
                .setEntity(player);
        Constraints.size(playerRender, 100, 100);
        Constraints.center(playerRender, container.getContentElement());


        GuiButton rotateButton = createButton(root, "Rotate")
                .setToggleMode(() -> !playerRender.isRotationLocked())
                .onPress(() -> playerRender.setRotationLocked(!playerRender.isRotationLocked()));
        Constraints.size(rotateButton, 80, 16);
        Constraints.placeInside(rotateButton, root, BOTTOM_CENTER, 0, -9);


//        List<GuiElement<?>> leftControls = new ArrayList<>();
//        List<GuiElement<?>> rightControls = new ArrayList<>();
        createControls(root);
//        arrangeControls(manager.addChild(new GuiElement<>()), leftControls).onReload(e -> e.setPos(3, height - e.ySize() - 3));
//        arrangeControls(manager.addChild(new GuiElement<>()), rightControls).onReload(e -> e.setPos(width - e.xSize() - 3, height - e.ySize() - 3));

        new GuiEventProvider(root)
                .onMouseClick(this::mouseClicked)
                .onMouseRelease(this::mouseReleased)
                .onScroll(this::mouseScrolled)
                .onMouseMove(this::mouseDragged);

    }


    private void createControls(GuiElement<?> root) {
        double width = 150;
        double height = 14;


        GuiElement<?> last;

        //        shieldColour = createColourPicker(() -> cfg.getShieldOverride(), e -> cfg.setShieldOverride(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        last = createEnableButton(root, "Shield Colour: ", () -> cfg.overrideShield(), e -> cfg.setOverrideShield(e), "Custom", "Default")
                .setEnabled(() -> props.hasShieldRGB());
        Constraints.size(last, width, height);
        Constraints.placeInside(last, root, BOTTOM_LEFT, 3, -3);
        //                .addChild(colourPickerButton(shieldColour, () -> 3, () -> 3).setEnabledCallback(() -> cfg.overrideShield()).setXPos(width + 1).setSize(height, height))
//                .addChild(colourRGBButton(() -> cfg.getShieldRGB(), e -> cfg.setShieldRGB(e)).setEnabledCallback(() -> cfg.overrideShield()).setXPos(width + 16).setSize(height, height)).getParent().getParent());

        last = createEnableButton(root, "Wings Shader B: ", () -> cfg.getWingsWebShader(), e -> cfg.setWingsWebShader(e))
                .setEnabled(() -> props.hasWings())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnableButton(root, "Wings Shader A: ", () -> cfg.getWingsBoneShader(), e -> cfg.setWingsBoneShader(e))
                .setEnabled(() -> props.hasWings())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);


        //        wingColourB = createColourPicker(() -> cfg.getWingsOverrideWebColour(), e -> cfg.setWingsOverrideWebColour(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        last = createEnableButton(root, "Wings Colour B: ", () -> cfg.overrideWingWebColour(), e -> cfg.setOverrideWingsWebColour(e), "Custom", "Default")
                .setEnabled(() -> props.hasWingsRGB())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);
//                .addChild(colourPickerButton(wingColourB, () -> this.width - 83, () -> 80).setEnabledCallback(() -> cfg.overrideWingWebColour())
//                        .setXPos(width + 1)
//                        .setSize(height, height))
//                .addChild(colourRGBButton(() -> cfg.getWingRGBWebColour(), e -> cfg.setWingRGBWebColour(e)).setEnabledCallback(() -> cfg.overrideWingWebColour())
//                        .setXPos(width + 16)
//                        .setSize(height, height))
//                .getParent().getParent());


        //        wingColourA = createColourPicker(() -> cfg.getWingsOverrideBoneColour(), e -> cfg.setWingsOverrideBoneColour(e), () -> cfg.getBaseColourI(cfg.getWingsTier()));
        last = createEnableButton(root, "Wings Colour A: ", () -> cfg.overrideWingBoneColour(), e -> cfg.setOverrideWingBoneColour(e), "Custom", "Default")
                .setEnabled(() -> props.hasWingsRGB())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

//                .setSize(width, height)
//                .addChild(colourPickerButton(wingColourA, () -> this.width - 83, () -> 3).setEnabledCallback(() -> cfg.overrideWingBoneColour())
//                        .setXPos(width + 1)
//                        .setSize(height, height))
//                .addChild(colourRGBButton(() -> cfg.getWingRGBBoneColour(), e -> cfg.setWingRGBBoneColour(e)).setEnabledCallback(() -> cfg.overrideWingBoneColour())
//                        .setXPos(width + 16)
//                        .setSize(height, height))
//                .getParent().getParent();
//


        last = createListButton(root, "Wings: ", () -> cfg.getWingsTier(), e -> cfg.setWingsTier(e), props.getWingTiers(), true)
                .setEnabled(() -> !props.getWingTiers().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);


        last = createEnumButton(root, "Elytra: ", WingElytraCompat.class, () -> cfg.getWingsElytra(), e -> cfg.setWingsElytra(e))
                .setEnabled(() -> props.hasWings());
        Constraints.size(last, width, height);
        Constraints.placeInside(last, root, BOTTOM_RIGHT, -3, -3);

        last = createEnumButton(root, "Creative: ", WingBehavior.class, () -> cfg.getWingsCreative(), e -> cfg.setWingsCreative(e))
                .setEnabled(() -> props.hasWings())
                .setTooltip(Component.literal("When using creative style flight"))
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnumButton(root, "Grounded: ", WingBehavior.class, () -> cfg.getWingsGround(), e -> cfg.setWingsGround(e))
                .setEnabled(() -> props.hasWings())
                .setTooltip(Component.literal("What happens when you are not flying"))
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = new GuiText(root, Component.literal(" Wing Behavior ").withStyle(ChatFormatting.UNDERLINE))
                .setEnabled(() -> props.hasWings())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createListButton(root, "Back Badge: ", () -> cfg.getBackBadge(), e -> cfg.setBackBadge(e), props.getBadges(), false)
                .setEnabled(() -> !props.getBadges().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createListButton(root, "Front Badge: ", () -> cfg.getChestBadge(), e -> cfg.setChestBadge(e), props.getBadges(), false)
                .setEnabled(() -> !props.getBadges().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnableButton(root, "Welcome Message: ", () -> cfg.showWelcome(), e -> cfg.setShowWelcome(e))
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);
    }

    private GuiElement<?> arrangeControls(GuiElement<?> container, List<GuiElement<?>> controls) {
//        int y = 0;
//        for (GuiElement<?> control : controls) {
//            if (!control.isEnabled()) continue;
//            container.addChild(control).setYPos(y);
//            y += control.ySize() + 1;
//        }
//        container.setBoundsToChildren();
        return container;
    }


    double lastMouseX = 0;

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!playerRender.isMouseOver()) {
            dragging = true;
            lastMouseX = mouseX;
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            playerRender.setLockedRotation(playerRender.getLockedRotation() + (float) (mouseX - lastMouseX));
            lastMouseX = mouseX;
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        double size = playerRender.xSize();
        size = (int) MathHelper.clip(size + (scrollAmount * Math.max(size / 10, 1)), 10, 500);
        playerRender.setSize(size, size);
//        playerRender.reloadElement();
    }

    //
//    @Override
//    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
//        renderBackground(mStack);
//        super.render(mStack, mouseX, mouseY, partialTicks);
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        cfg = props.getConfig();
//    }
//
    private GuiButton createButton(GuiElement<?> parent, String text) {
        GuiButton button = GuiButton.flatColourButton(parent, () -> Component.literal(text), hover -> hover ? 0x90116011 : 0x90000000, hover -> hover ? 0xFF9090FF : 0xFF606060);
        button.getLabel().setTextColour(() -> Palette.Ctrl.textH(button.isMouseOver()));
        return button;
    }

    //
    private GuiButton createEnableButton(GuiElement<?> parent, String text, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return createEnableButton(parent, text, getter, setter, "Enabled", "Disabled");
    }

    private GuiButton createEnableButton(GuiElement<?> parent, String text, Supplier<Boolean> getter, Consumer<Boolean> setter, String trueText, String falseText) {
        GuiButton button = GuiButton.flatColourButton(parent, () -> Component.literal(text + (getter.get() ? trueText : falseText)), hover -> hover ? 0x90116011 : 0x90000000, hover -> hover ? 0xFF9090FF : 0xFF606060)
                .setToggleMode(getter)
                .onPress(() -> setter.accept(!getter.get()));
        button.getLabel().setTextColour(() -> Palette.Ctrl.textH(button.isMouseOver()));
        return button;
    }

    private <T> GuiButton createListButton(GuiElement<?> parent, String text, Supplier<T> getter, Consumer<T> setter, List<T> values, boolean nullOption) {
        Consumer<Integer> onPress = dir -> {
            if (values.isEmpty()) return;
            T current = getter.get();
            int index = current == null ? values.size() : values.indexOf(current);
            index = Math.floorMod(index + dir, (nullOption ? values.size() + 1 : values.size()));
            setter.accept(index == values.size() ? null : values.get(index));
        };
        GuiButton button = GuiButton.flatColourButton(parent, () -> Component.literal(text + (getter.get() == null ? "Disabled" : getter.get().toString())), hover -> hover ? 0x90116011 : 0x90000000, hover -> hover ? 0xFF9090FF : 0xFF606060)
                .setTooltip(() -> getter.get() instanceof ContributorConfig.HoverText h ? h.getHoverText() : Collections.emptyList())
                .onPress(() -> onPress.accept(1), GuiButton.LEFT_CLICK)
                .onPress(() -> onPress.accept(-1), GuiButton.RIGHT_CLICK);
        button.getLabel().setTextColour(() -> Palette.Ctrl.textH(button.isMouseOver()));
        return button;
    }

    //
//    private GuiButton colourPickerButton(GuiPickColourDialog dialog, Supplier<Integer> xPos, Supplier<Integer> yPos) {
//        GuiButton button = toolkit.createIconButton(null, 14, BCGuiSprites.getter("color_picker"));
//        dialog.setEnabledCallback(button::isEnabled);
//        button.onPressed(() -> {
//            dialog.setPos(xPos.get(), yPos.get());
//            dialog.toggleShown(false, 200);
//        });
//        return button;
//    }
//
//    private GuiButton colourRGBButton(Supplier<Boolean> getter, Consumer<Boolean> setter) {
//        return toolkit.createIconButton(null, 14, BCGuiSprites.getter("rgb_checker"))
//                .setToggleStateSupplier(getter)
//                .setHoverText("Enable rainbow RGB mode.", "Use the colour picker to configure,", "Red = Cycle Speed", "Green = Saturation", "Blue = Brightness")
//                .onPressed(() -> setter.accept(!getter.get()));
//    }
//
//    private GuiPickColourDialog createColourPicker(Supplier<Integer> getter, Consumer<Integer> setter, Supplier<Integer> getDefault) {
//        GuiPickColourDialog dialog = new GuiPickColourDialog(container)
//                .setBackgroundElement(new GuiTooltipBackground())
//                .setColour(getter.get())
//                .setColourChangeListener(setter)
//                .setIncludeAlpha(false)
//                .setCloseOnOutsideClick(false)
//                .setCancelEnabled(true);
//        dialog.onReload(e -> {
//            e.cancelButton.setText("Reset")
//                    .setTrim(false)
//                    .setHoverText("Apply the default colours for the currently selected Wings tier")
//                    .onPressed(() -> dialog.updateColour(getDefault.get()));
//
//        }, false);
//        return dialog;
//    }
//
    private <E extends Enum<E>> GuiButton createEnumButton(GuiElement<?> parent, String text, Class<E> clazz, Supplier<E> getter, Consumer<E> setter) {
        GuiButton button = GuiButton.flatColourButton(parent, () -> Component.literal(text + getter.get().toString()), hover -> hover ? 0x90116011 : 0x90000000, hover -> hover ? 0xFF9090FF : 0xFF606060)
                .setTooltip(() -> getter.get() instanceof ContributorConfig.HoverText h ? h.getHoverText() : Collections.emptyList())
                .onPress(() -> setter.accept(clazz.getEnumConstants()[(getter.get().ordinal() + 1) % clazz.getEnumConstants().length]), GuiButton.LEFT_CLICK)
                .onPress(() -> setter.accept(clazz.getEnumConstants()[(getter.get().ordinal() - 1) % clazz.getEnumConstants().length]), GuiButton.RIGHT_CLICK);
        button.getLabel().setTextColour(() -> Palette.Ctrl.textH(button.isMouseOver()));
        return button;
    }

    //Dedicated screen class provided for better compatibility with other mods.
    public static class Screen extends ModularGuiScreen {
        public Screen(Player player, ContributorProperties props) {
            super(new ContributorConfigGui(player, props));
        }
    }
}
