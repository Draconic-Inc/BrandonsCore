package com.brandon3055.brandonscore.client.gui;

import codechicken.lib.gui.modular.ModularGui;
import codechicken.lib.gui.modular.ModularGuiScreen;
import codechicken.lib.gui.modular.elements.*;
import codechicken.lib.gui.modular.lib.ColourState;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.GuiProvider;
import codechicken.lib.gui.modular.lib.geometry.GeoParam;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.BCGuiTextures;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingBehavior;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.WingElytraCompat;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Player player;
    private final ContributorProperties props;
    private final ContributorConfig cfg;
    private final ColourState wingColourA;
    private final ColourState wingColourB;
    private final ColourState shieldColour;
    private final Map<Integer, GuiColourPicker> pickers = new HashMap<>();
    private boolean dragging = false;
    private GuiManipulable container;
    private GuiEntityRenderer playerRender;

    public ContributorConfigGui(Player player, ContributorProperties props) {
        this.player = player;
        this.props = props;
        this.cfg = props.getConfig();
        this.wingColourA = ColourState.create(cfg::getWingsOverrideBoneColour, cfg::setWingsOverrideBoneColour);
        this.wingColourB = ColourState.create(cfg::getWingsOverrideWebColour, cfg::setWingsOverrideWebColour);
        this.shieldColour = ColourState.create(cfg::getShieldOverride, cfg::setShieldOverride);
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

//        GuiManipulable titleMovable = new GuiManipulable(root)
//                .addMoveHandle(13)
//                .setCursors(GuiToolkit.CURSORS);
//        Constraints.size(titleMovable, 150, 13);
//        Constraints.placeInside(titleMovable, root, TOP_CENTER, 0, 15);
//
//        GuiElement<?> titleBackground = new GuiRectangle(titleMovable.getContentElement())
//                .fill(0x80000000);
//        Constraints.bind(titleBackground, titleMovable.getContentElement());
//        Constraints.bind(toolkit.createHeading(titleBackground, Component.literal("Contributor Configuration")), titleBackground);

        GuiElement<?> title = toolkit.floatingHeading(gui);
        Constraints.placeInside(title, root, TOP_CENTER, 0, 15);

        container = new GuiManipulable(root)
                .addMoveHandle(100)
                .enableCursors(true);
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

        createControls(root);
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

        last = createEnableButton(root, "Shield Colour: ", cfg::overrideShield, cfg::setOverrideShield, "Custom", "Default")
                .setEnabled(props::hasShieldRGB);
        Constraints.size(last, width, height);
        Constraints.placeInside(last, root, BOTTOM_LEFT, 3, -3);

        GuiButton pickerBtn = colourPickerButton(root, shieldColour, 2, () -> cfg.getBaseColourI(cfg.getWingsTier()));
        pickerBtn.setEnabled(cfg::overrideShield);
        Constraints.size(pickerBtn, height, height);
        Constraints.placeOutside(pickerBtn, last, MIDDLE_RIGHT, 1, 0);

        GuiButton rgbBtn = colourRGBButton(root, cfg::getShieldRGB, cfg::setShieldRGB);
        rgbBtn.setEnabled(cfg::overrideShield);
        Constraints.size(rgbBtn, height, height);
        Constraints.placeOutside(rgbBtn, pickerBtn, MIDDLE_RIGHT, 1, 0);

        last = createEnableButton(root, "Wings Shader B: ", cfg::getWingsWebShader, cfg::setWingsWebShader)
                .setEnabled(props::hasWings)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnableButton(root, "Wings Shader A: ", cfg::getWingsBoneShader, cfg::setWingsBoneShader)
                .setEnabled(props::hasWings)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnableButton(root, "Wings Colour B: ", cfg::overrideWingWebColour, cfg::setOverrideWingsWebColour, "Custom", "Default")
                .setEnabled(props::hasWingsRGB)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        pickerBtn = colourPickerButton(root, wingColourB, 1, () -> cfg.getBaseColourI(cfg.getWingsTier()));
        pickerBtn.setEnabled(cfg::overrideWingWebColour);
        Constraints.size(pickerBtn, height, height);
        Constraints.placeOutside(pickerBtn, last, MIDDLE_RIGHT, 1, 0);

        rgbBtn = colourRGBButton(root, cfg::getWingRGBWebColour, cfg::setWingRGBWebColour);
        rgbBtn.setEnabled(cfg::overrideWingWebColour);
        Constraints.size(rgbBtn, height, height);
        Constraints.placeOutside(rgbBtn, pickerBtn, MIDDLE_RIGHT, 1, 0);

        last = createEnableButton(root, "Wings Colour A: ", cfg::overrideWingBoneColour, cfg::setOverrideWingBoneColour, "Custom", "Default")
                .setEnabled(props::hasWingsRGB)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        pickerBtn = colourPickerButton(root, wingColourA, 0, () -> cfg.getBaseColourI(cfg.getWingsTier()));
        pickerBtn.setEnabled(cfg::overrideWingBoneColour);
        Constraints.size(pickerBtn, height, height);
        Constraints.placeOutside(pickerBtn, last, MIDDLE_RIGHT, 1, 0);

        rgbBtn = colourRGBButton(root, cfg::getWingRGBBoneColour, cfg::setWingRGBBoneColour);
        rgbBtn.setEnabled(cfg::overrideWingBoneColour);
        Constraints.size(rgbBtn, height, height);
        Constraints.placeOutside(rgbBtn, pickerBtn, MIDDLE_RIGHT, 1, 0);

        last = createListButton(root, "Wings: ", cfg::getWingsTier, cfg::setWingsTier, props.getWingTiers(), true)
                .setEnabled(() -> !props.getWingTiers().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnumButton(root, "Elytra: ", WingElytraCompat.class, cfg::getWingsElytra, cfg::setWingsElytra)
                .setEnabled(props::hasWings);
        Constraints.size(last, width, height);
        Constraints.placeInside(last, root, BOTTOM_RIGHT, -3, -3);

        last = createEnumButton(root, "Creative: ", WingBehavior.class, cfg::getWingsCreative, cfg::setWingsCreative)
                .setEnabled(props::hasWings)
                .setTooltip(Component.literal("When using creative style flight"))
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnumButton(root, "Grounded: ", WingBehavior.class, cfg::getWingsGround, cfg::setWingsGround)
                .setEnabled(props::hasWings)
                .setTooltip(Component.literal("What happens when you are not flying"))
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = new GuiText(root, Component.literal(" Wing Behavior ").withStyle(ChatFormatting.UNDERLINE))
                .setEnabled(props::hasWings)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createListButton(root, "Back Badge: ", cfg::getBackBadge, cfg::setBackBadge, props.getBadges(), false)
                .setEnabled(() -> !props.getBadges().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createListButton(root, "Front Badge: ", cfg::getChestBadge, cfg::setChestBadge, props.getBadges(), false)
                .setEnabled(() -> !props.getBadges().isEmpty())
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);

        last = createEnableButton(root, "Welcome Message: ", cfg::showWelcome, cfg::setShowWelcome)
                .constrain(GeoParam.LEFT, match(last.get(GeoParam.LEFT)))
                .constrain(GeoParam.BOTTOM, relative(last.get(GeoParam.TOP), -1));
        Constraints.size(last, width, height);
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
    }

    private GuiButton createButton(GuiElement<?> parent, String text) {
        GuiButton button = GuiButton.flatColourButton(parent, () -> Component.literal(text), hover -> hover ? 0x90116011 : 0x90000000, hover -> hover ? 0xFF9090FF : 0xFF606060);
        button.getLabel().setTextColour(() -> Palette.Ctrl.textH(button.isMouseOver()));
        return button;
    }

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

    private GuiButton colourPickerButton(GuiElement<?> parent, ColourState state, int index, Supplier<Integer> defCol) {
        GuiButton button = toolkit.createIconButton(parent, 14, BCGuiTextures.getter("color_picker"));
        button.onPress(() -> {
            GuiColourPicker picker = pickers.remove(index);
            if (picker != null && !picker.isRemoved()) {
                picker.getParent().removeChild(picker);
                return;
            }

            pickers.put(index, (picker = GuiColourPicker.create(parent, state, true)));
            picker.addMoveHandle((int) picker.ySize());
            picker.enableCursors(true);

            if (index < 2) {
                Constraints.placeInside(picker, parent, TOP_LEFT, 4 + ((picker.xSize() + 4) * index), 4);
            } else {
                Constraints.placeInside(picker, parent, TOP_RIGHT, -4 - ((picker.xSize() + 4) * (index - 2)), 4);
            }
        });
        return button;
    }

    private GuiButton colourRGBButton(GuiElement<?> parent, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return toolkit.createIconButton(parent, 14, BCGuiTextures.getter("rgb_checker"))
                .setToggleMode(getter)
                .setTooltip(Component.literal("Enable rainbow RGB mode."),
                        Component.literal("Use the colour picker to configure,"),
                        Component.literal("Red = Cycle Speed"),
                        Component.literal("Green = Saturation"),
                        Component.literal("Blue = Brightness"))
                .onPress(() -> setter.accept(!getter.get()));
    }

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