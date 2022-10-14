package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.templates.IGuiTemplate;
import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.SlotMover;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static com.brandon3055.brandonscore.BrandonsCore.equipmentManager;
import static com.brandon3055.brandonscore.client.gui.GuiToolkit.GuiLayout.CUSTOM;
import static com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotType.*;

/**
 * Created by brandon3055 on 5/7/19.
 */
public class GuiToolkit<T extends Screen & IModularGui> {
    private static final String INTERNAL_TRANSLATION_PREFIX = "gui_tkt.brandonscore.";

    private static Map<String, ResourceLocation> resourceCache = new HashMap<>();
    private List<GuiElement<?>> jeiExclusions = new ArrayList<>();

    private T gui;
    private GuiLayout layout;
    private ContainerSlotLayout slotLayout;
    private String translationPrefix = "";

    public GuiToolkit(T gui, GuiLayout layout) {
        this(gui, layout.xSize, layout.ySize);
        this.layout = layout;
        if (layout == CUSTOM) {
            throw new UnsupportedOperationException("For custom gui size use the alternate constructor that allows you to specify the size");
        }
    }

    public GuiToolkit(T gui, int xSize, int ySize) {
        this.gui = gui;
        this.layout = CUSTOM;
        this.gui.setUISize(xSize, ySize);
        gui.getManager().setJeiExclusions(() -> jeiExclusions);
        if (gui instanceof ModularGuiContainer && ((ModularGuiContainer<?>) gui).getMenu() instanceof ContainerBCore) {
            setSlotLayout(((ContainerBCore) ((ModularGuiContainer) gui).getMenu()).getSlotLayout());
        }
    }

    public GuiToolkit<T> setTranslationPrefix(String translationPrefix) {
        if (!translationPrefix.endsWith(".")) {
            translationPrefix = translationPrefix + ".";
        }
        this.translationPrefix = translationPrefix;
        return this;
    }

    public String i18n(String translationKey, Object... args) {
        if (translationKey.startsWith(".")) {
            translationKey = translationKey.substring(1);
        }
        return I18n.get(translationPrefix + translationKey, args);
    }

    public Supplier<String> i18n(Supplier<String> translationKey) {
        return () -> I18n.get(translationPrefix + translationKey.get());
    }

    /**
     * Internal translator for use inside Toolkit
     */
    protected static String i18ni(String translationKey) {
        if (translationKey.startsWith(".")) {
            translationKey = translationKey.substring(1);
        }
        return I18n.get(INTERNAL_TRANSLATION_PREFIX + translationKey);
    }

    public GuiLayout getLayout() {
        return layout;
    }

    public void setSlotLayout(ContainerSlotLayout slotLayout) {
        this.slotLayout = slotLayout;
    }

    public GuiButton createRSSwitch(IRSSwitchable switchable) {
        GuiButton button = new GuiButton();
        addHoverHighlight(button);
        button.setHoverTextDelay(10);
        button.setSize(12, 12);
        GuiTexture icon = new GuiTexture(12, 12, () -> BCGuiSprites.get("redstone/" + switchable.getRSMode().name().toLowerCase(Locale.ENGLISH)));
        button.addChild(icon);
        icon.setYPosMod(button::yPos);
        button.setHoverText(element -> i18ni("rs_mode." + switchable.getRSMode().name().toLowerCase(Locale.ENGLISH)));
        button.onButtonPressed((pressed) -> switchable.setRSMode(switchable.getRSMode().next(Screen.hasShiftDown() || pressed == 1)));
        return button;
    }

    public GuiButton createRSSwitch(GuiElement parent, IRSSwitchable switchable) {
        GuiButton button = createRSSwitch(switchable);
        parent.addChild(button);
        return button;
    }

    //Create Background (various sizes)
    public GuiTexture createBackground(boolean addToManager, boolean center) {
        if (layout.xSize == -1 || layout.ySize == -1) {  //TODO maybe add a way do provide a background builder in this case?
            throw new UnsupportedOperationException("Layout type " + layout + " does not have an associated default background.");
        }

        //TODO move to a function in BCTextures?
        GuiTexture texture = new GuiTexture(() -> BCGuiSprites.getThemed(layout.textureName()));
        texture.setSize(layout.xSize, layout.ySize);
        if (addToManager) {
            gui.getManager().addChild(texture);
        }
        if (center) {
            texture.onReload(guiTex -> guiTex.setPos(gui.guiLeft(), gui.guiTop()));
        }
        return texture;
    }

    public GuiTexture createBackground(boolean center) {
        return createBackground(false, center);
    }

    public GuiTexture createBackground() {
        return createBackground(false);
    }

    //UI Heading
    public GuiLabel createHeading(String unlocalizedHeading, @Nullable GuiElement parent, boolean layout) {
        GuiLabel heading = new GuiLabel(I18n.get(unlocalizedHeading));
        heading.setTextColGetter(Palette.BG::text);
        heading.setShadowStateSupplier(() -> darkMode);
        if (parent != null) {
            parent.addChild(heading);
            if (layout) {
                heading.setSize(parent.xSize(), 8).setAlignment(GuiAlign.CENTER).setRelPos(parent, 0, 4);
            }
        }
        return heading;
    }

    public GuiLabel createHeading(String unlocalizedHeading, @Nullable GuiElement parent) {
        return createHeading(unlocalizedHeading, parent, false);
    }

    public GuiLabel createHeading(String unlocalizedHeading) {
        return createHeading(unlocalizedHeading, null, false);
    }

    /**
     * Creates a generic set of inventory slots with the specified dimensions.
     * background is an optional 16x16 sprite that will be used as the slot background.
     *
     * @param slotMapper (column, row, slotData)
     */
    public GuiElement createSlots(GuiElement parent, int columns, int rows, int spacing, BiFunction<Integer, Integer, SlotMover> slotMapper, Material background) {
        GuiElement element = new GuiElement() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                Material slot = BCGuiSprites.getThemed("slot");
                MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                VertexConsumer buffer = getter.getBuffer(BCGuiSprites.GUI_TYPE);

                for (int x = 0; x < columns; x++) {
                    for (int y = 0; y < rows; y++) {
                        drawSprite(buffer, xPos() + (x * (18 + spacing)), yPos() + (y * (18 + spacing)), 18, 18, slot.sprite());
                    }
                }

                if (background != null) {
                    for (int x = 0; x < columns; x++) {
                        for (int y = 0; y < rows; y++) {
                            if (slotMapper != null) {
                                SlotMover data = slotMapper.apply(x, y);
                                if (data != null && data.slot.hasItem()) {
                                    continue;
                                }
                            }
                            drawSprite(buffer, xPos() + (x * (18 + spacing)) + 1, yPos() + (y * (18 + spacing)) + 1, 16, 16, background.sprite());
                        }
                    }
                }

                getter.endBatch();
            }

            @Override
            public GuiElement translate(int xAmount, int yAmount) {
                GuiElement ret = super.translate(xAmount, yAmount);
                if (slotMapper != null) {
                    for (int x = 0; x < columns; x++) {
                        for (int y = 0; y < rows; y++) {
                            SlotMover data = slotMapper.apply(x, y);
                            if (data != null) {
                                data.setPos((xPos() + (x * (18 + spacing))) - gui.guiLeft() + 1, (yPos() + (y * (18 + spacing))) - gui.guiTop() + 1);
                            }
                        }
                    }
                }
                return ret;
            }
        };
        element.setSize((columns * 18) + ((columns - 1) * spacing), (rows * 18) + ((rows - 1) * spacing));
        if (parent != null) {
            parent.addChild(element);
        }

        return element;
    }

    public GuiElement createSlots(GuiElement parent, int columns, int rows, int spacing) {
        return createSlots(parent, columns, rows, spacing, null, null);
    }

    public GuiElement createSlots(GuiElement parent, int columns, int rows) {
        return createSlots(parent, columns, rows, 0);
    }

    public GuiElement createSlots(GuiElement parent, int columns, int rows, int spacing, Material slotTexture) {
        return createSlots(parent, columns, rows, spacing, null, slotTexture);
    }

    public GuiElement createSlots(GuiElement parent, int columns, int rows, Material slotTexture) {
        return createSlots(parent, columns, rows, 0, null, slotTexture);
    }

    public GuiElement createSlots(int columns, int rows) {
        return createSlots(null, columns, rows, 0);
    }

    public GuiElement createSlot(GuiElement parent, SlotMover slotMover, Supplier<Material> background, boolean largeSlot) {
        int size = largeSlot ? 26 : 18;
        GuiElement element = new GuiElement() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                Material slot = BCGuiSprites.getThemed(largeSlot ? "slot_large" : "slot");
                MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                VertexConsumer buffer = getter.getBuffer(BCGuiSprites.GUI_TYPE);
                drawSprite(buffer, xPos(), yPos(), size, size, slot.sprite());
                if (background != null && (slotMover == null || !slotMover.slot.hasItem())) {
                    int offset = largeSlot ? 5 : 1;
                    drawSprite(buffer, xPos() + offset, yPos() + offset, 16, 16, background.get().sprite());
                }
                getter.endBatch();
            }

            @Override
            public GuiElement translate(int xAmount, int yAmount) {
                GuiElement ret = super.translate(xAmount, yAmount);
                if (slotMover != null) {
                    slotMover.setPos(xPos() - gui.guiLeft() + (largeSlot ? 5 : 1), yPos() - gui.guiTop() + (largeSlot ? 5 : 1));
                }
                return ret;
            }
        };
        element.setSize(size, size);
        if (parent != null) {
            parent.addChild(element);
        }

        return element;
    }

    /**
     * Creates the standard player inventory slot layout.
     */
    public GuiElement createPlayerSlots(GuiElement parent, boolean title) {
        return createPlayerSlots(parent, title, false, false);
    }

    public GuiElement createPlayerSlots(GuiElement parent, boolean title, boolean addArmor, boolean addOffHand) {
        GuiElement container = new GuiElement();
        GuiElement main = createSlots(container, 9, 3, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column + row * 9 + 9), null);
        GuiElement bar = createSlots(container, 9, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column), null);
        bar.setYPos(main.maxYPos() + 3);

        if (title) {
            GuiLabel invTitle = new GuiLabel(i18ni("your_inventory"));
            invTitle.setAlignment(GuiAlign.LEFT).setHoverableTextCol(hovering -> Palette.BG.text());
            invTitle.setShadowStateSupplier(() -> darkMode);
            container.addChild(invTitle);
            invTitle.setSize(main.xSize(), 8);
            main.translate(0, 10);
            bar.translate(0, 10);
        }

        if (addArmor) {
            for (int i = 0; i < 4; i++) {
                int finalI = 3 - i;
                GuiElement element = createSlots(container, 1, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_ARMOR, finalI), BCGuiSprites.getArmorSlot(finalI));
                element.setMaxXPos(main.xPos() - 3, false);
                element.setYPos(main.yPos() + (i * 19));
            }
        }

        if (addOffHand) {
            GuiElement element = createSlots(container, 1, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_OFF_HAND, 4), BCGuiSprites.get("slots/armor_shield"));
            element.setXPos(main.maxXPos() + 3);
            element.setMaxYPos(bar.maxYPos(), false);
        }

        container.setBoundsToChildren();

        if (parent != null) {
            parent.addChild(container);
        }

        return container;
    }

    public GuiElement createPlayerSlotsManualMovers(GuiElement parent, boolean title, Function<Integer, SlotMover> slotGetter) {
        GuiElement container = new GuiElement();
        GuiElement main = createSlots(container, 9, 3, 0, (column, row) -> slotGetter.apply(column + row * 9 + 9), null);
        GuiElement bar = createSlots(container, 9, 1, 0, (column, row) -> slotGetter.apply(column), null);
        bar.setYPos(main.maxYPos() + 3);

        if (title) {
            GuiLabel invTitle = new GuiLabel(i18ni("your_inventory"));
            invTitle.setAlignment(GuiAlign.LEFT).setHoverableTextCol(hovering -> Palette.BG.text());
            invTitle.setShadowStateSupplier(() -> darkMode);
            container.addChild(invTitle);
            invTitle.setSize(main.xSize(), 8);
            main.translate(0, 10);
            bar.translate(0, 10);
        }

        container.setBoundsToChildren();

        if (parent != null) {
            parent.addChild(container);
        }

        return container;
    }

    public GuiElement createEquipModSlots(GuiElement parent, Player player, boolean jeiExclude, Predicate<ItemStack> showFilter) {
        GuiElement fallback = new GuiElement();
        if (equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(player);
            GuiElement container = GuiTexture.newDynamicTexture(() -> BCGuiSprites.getThemed("bg_dynamic_small"));
            container.setXSize(26);
            optional.ifPresent(handler -> {
                if (jeiExclude) {
                    jeiExclude(container);
                }
                parent.addBackGroundChild(container);
                int c = 0;
                for (int i = 0; i < handler.getSlots(); i++) {
                    int finalI = i;
                    SlotMover data = slotLayout.getSlotData(PLAYER_EQUIPMENT, finalI);
                    if (showFilter != null && !showFilter.test(data.slot.getItem())) {
                        data.setPos(-9999, -9999);
                        continue;
                    }
                    GuiElement element = createSlots(container, 1, 1, 0, (column, row) -> data, null);
                    element.setXPos(container.xPos() + 4, false);
                    element.setYPos(container.yPos() + (c * 19) + 4);
                    container.setMaxYPos(element.maxYPos() + 4, true);
                    c++;
                }
            });
            return container.getChildElements().isEmpty() ? fallback : container;
        }
        return fallback;
    }

    public GuiElement createPlayerSlots() {
        return createPlayerSlots(null, true);
    }

    //region  Buttons
    //####################################################################################################

    public GuiButton createVanillaButton(String unlocalizedText, @Nullable GuiElement parent) {
        GuiButton button = new GuiButton(I18n.get(unlocalizedText));
        button.setHoverTextDelay(10);
        button.enableVanillaRender();
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createVanillaButton(String unlocalizedText) {
        return createVanillaButton(unlocalizedText, null);
    }

    public GuiButton createBorderlessButton(String unlocalizedText) {
        return createBorderlessButton(null, unlocalizedText);
    }

    public GuiButton createBorderlessButton(@Nullable GuiElement parent, String unlocalizedText) {
        GuiButton button = new GuiButton(I18n.get(unlocalizedText));
        button.setInsets(5, 2, 5, 2);
        button.setHoverTextDelay(10);
        button.set3dText(true);
        GuiTexture texture = GuiTexture.newDynamicTexture(() -> BCGuiSprites.getThemed("button_borderless" + (button.isPressed() ? "_invert" : "")));
        button.addChild(texture);
        addHoverHighlight(button, 0, 0, true);
        texture.bindSize(button, false);
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    @Deprecated
    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement parent, boolean inset3d, double doubleBoarder) {
        GuiButton button = new GuiButton(I18n.get(unlocalizedText));
        button.setInsets(5, 2, 5, 2);
        button.setHoverTextDelay(10);
        if (inset3d) {
            button.set3dText(true);
            GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(doubleBoarder);
            //I use modifiers here to account for the possibility that this button may have modifiers. Something i need to account for when i re write modular gui
            buttonBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
            buttonBG.setFillColourL(hovering -> Palette.Ctrl.fill(hovering || button.isPressed()));
            buttonBG.setBorderColourL(Palette.Ctrl::border3D);
            buttonBG.set3dTopLeftColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentDark(true) : Palette.Ctrl.accentLight(hovering));
            buttonBG.set3dBottomRightColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentLight(true) : Palette.Ctrl.accentDark(hovering));
            GuiTexture disabledBG = GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_disabled"));
            disabledBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
            disabledBG.setEnabledCallback(button::isDisabled);
            buttonBG.addChild(disabledBG);

            button.addChild(buttonBG);
        } else {
            button.setRectFillColourGetter((hovering, disabled) -> Palette.Ctrl.fill(hovering));
            button.setRectBorderColourGetter((hovering, disabled) -> Palette.Ctrl.border(hovering));
        }
        button.setTextColGetter((hovering, disabled) -> Palette.Ctrl.textH(hovering));

        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    @Deprecated
    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement parent, boolean inset3d) {
        return createButton_old(unlocalizedText, parent, inset3d, 1);
    }

    @Deprecated
    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement parent) {
        return createButton_old(unlocalizedText, parent, true);
    }

    @Deprecated
    public GuiButton createButton_old(String unlocalizedText, boolean shadeEdges) {
        return createButton_old(unlocalizedText, null, shadeEdges);
    }

    @Deprecated
    public GuiButton createButton_old(String unlocalizedText) {
        return createButton_old(unlocalizedText, null, true);
    }

    public GuiButton createButton(Supplier<String> toolkitI18nText, @Nullable GuiElement parent, boolean inset3d, double doubleBoarder) {
        GuiButton button = new GuiButton().setDisplaySupplier(i18n(toolkitI18nText));
        button.setInsets(5, 2, 5, 2);
        button.setHoverTextDelay(10);
        if (inset3d) {
            button.set3dText(true);
            GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(doubleBoarder);
            //I use modifiers here to account for the possibility that this button may have modifiers. Something i need to account for when i re write modular gui
            buttonBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
            buttonBG.setFillColourL(hovering -> Palette.Ctrl.fill(hovering || button.isPressed()));
            buttonBG.setBorderColourL(Palette.Ctrl::border3D);
            buttonBG.set3dTopLeftColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentDark(true) : Palette.Ctrl.accentLight(hovering));
            buttonBG.set3dBottomRightColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentLight(true) : Palette.Ctrl.accentDark(hovering));
            GuiTexture disabledBG = GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_disabled"));
            disabledBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
            disabledBG.setEnabledCallback(button::isDisabled);
            buttonBG.addChild(disabledBG);

            button.addChild(buttonBG);
        } else {
            button.setRectFillColourGetter((hovering, disabled) -> Palette.Ctrl.fill(hovering));
            button.setRectBorderColourGetter((hovering, disabled) -> Palette.Ctrl.border(hovering));
        }
        button.setTextColGetter((hovering, disabled) -> disabled ? 0xa0a0a0 : Palette.Ctrl.textH(hovering));

        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createButton(Supplier<String> toolkitI18nText, @Nullable GuiElement parent) {
        return createButton(toolkitI18nText, parent, true, 1);
    }

    public GuiButton createButton(String toolkitI18nText, @Nullable GuiElement parent) {
        return createButton(() -> toolkitI18nText, parent, true, 1);
    }


    public GuiButton createThemeButton(GuiElement<?> parent) {
        GuiButton button = createThemedIconButton(parent, "theme");
        button.setHoverText(element -> darkMode ? i18ni("theme.light") : i18ni("theme.dark"));
        button.onPressed(() -> BCConfig.modifyClientProperty("darkMode", e -> e.setBoolean(!darkMode)));
        return button;
    }

    public GuiButton createThemeButton() {
        return createThemeButton(null);
    }

    public GuiButton createResizeButton() {
        return createResizeButton(null);
    }

    public GuiButton createResizeButton(GuiElement<?> parent) {
        GuiButton button = createThemedIconButton(parent, "resize");
        button.setHoverText(element -> i18ni("large_view"));
        return button;
    }

    public GuiButton createGearButton() {
        return createGearButton(null);
    }

    public GuiButton createGearButton(GuiElement<?> parent) {
        GuiButton button = createThemedIconButton(parent, "gear");
        return button;
    }

    public GuiButton createAdvancedButton() {
        return createAdvancedButton(null);
    }

    public GuiButton createAdvancedButton(GuiElement<?> parent) {
        GuiButton button = createThemedIconButton(parent, "advanced");
        return button;
    }

    public GuiButton createThemedIconButton(GuiElement<?> parent, String iconString) {
        return createThemedIconButton(parent, 12, iconString);
    }

    public GuiButton createThemedIconButton(GuiElement<?> parent, int size, String iconString) {
        return createIconButton(parent, size, BCGuiSprites.themedGetter(iconString));
    }

    public GuiButton createIconButton(GuiElement<?> parent, int size, Supplier<Material> iconSupplier) {
        return createIconButton(parent, size, size, iconSupplier);
    }

    public GuiButton createIconButton(GuiElement<?> parent, int buttonSize, int iconSize, String iconString) {
        return createIconButton(parent, buttonSize, iconSize, BCGuiSprites.getter(iconString));
    }

    public GuiButton createIconButton(GuiElement<?> parent, int buttonSize, int iconSize, Supplier<Material> iconSupplier) {
//        GuiButton button = new GuiButton();
//        button.setHoverTextDelay(10);
//        button.setSize(buttonSize, buttonSize);
//        addHoverHighlight(button);
//        GuiTexture icon = new GuiTexture(iconSize, iconSize, iconSupplier);
//        icon.setPosModifiers(() -> button.xPos() + -((iconSize - buttonSize) / 2), () -> button.yPos() + -((iconSize - buttonSize) / 2));
//        button.addChild(icon);
//        if (parent != null) {
//            parent.addChild(button);
//        }
        return createIconButton(parent, buttonSize, buttonSize, iconSize, iconSize, iconSupplier);
    }

    public GuiButton createIconButton(GuiElement<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, String iconString) {
        return createIconButton(parent, buttonWidth, buttonHeight, iconWidth, iconHeight, BCGuiSprites.getter(iconString));
    }

    public GuiButton createIconButton(GuiElement<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, Supplier<Material> iconSupplier) {
        GuiButton button = new GuiButton();
        button.setHoverTextDelay(10);
        button.setSize(buttonWidth, buttonHeight);
        addHoverHighlight(button);
        GuiTexture icon = new GuiTexture(iconWidth, iconHeight, iconSupplier);
        icon.setPosModifiers(() -> button.xPos() + -((iconWidth - buttonWidth) / 2), () -> button.yPos() + -((iconHeight - buttonHeight) / 2));
        button.addChild(icon);
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public static GuiBorderedRect addHoverHighlight(GuiElement button) {
        return addHoverHighlight(button, 0, 0);
    }

    public static GuiBorderedRect addHoverHighlight(GuiElement button, int xOversize, int yOversize) {
        return addHoverHighlight(button, xOversize / 2, yOversize / 2, false);
    }

    public static GuiBorderedRect addHoverHighlight(GuiElement button, int xOversize, int yOversize, boolean transparent) {
        return addHoverHighlight(button, xOversize, xOversize, yOversize, yOversize, transparent);
    }

    public static GuiBorderedRect addHoverHighlight(GuiElement button, int leftOversize, int rightOversize, int topOversize, int bottomOversize, boolean transparent) {
        GuiBorderedRect rect = new GuiBorderedRect();
        rect.setBorderColourL(hovering -> Palette.Ctrl.fill(hovering) & (transparent ? 0x80FFFFFF : 0xFFFFFFFF));
        rect.setPosModifiers(() -> button.xPos() - leftOversize, () -> button.yPos() - topOversize);
        rect.setSizeModifiers(() -> button.xSize() + leftOversize + rightOversize, () -> button.ySize() + topOversize + bottomOversize);
        rect.setEnabledCallback(() -> button.getHoverTime() > 0 || (button instanceof GuiButton b && b.getToggleState()));
        if (transparent) {
            button.addChild(rect);
        } else {
            button.addBackGroundChild(rect);
        }
        return rect;
    }

    public GuiElement createHighlightIcon(GuiElement parent, int xSize, int ySize, int xOversize, int yOversize, Supplier<Material> matSupplier) {
        GuiElement<?> base = new GuiElement<>().setSize(xSize, ySize);
        GuiTexture icon = new GuiTexture(matSupplier).setSize(xSize, ySize);
        addHoverHighlight(base, xOversize, yOversize).setEnabledCallback(() -> base.getHoverTime() > 0);
        base.addChild(icon);
        parent.addChild(base);
        return base;
    }

    public GuiElement createHighlightIcon(GuiElement parent, int xSize, int ySize, int xOversize, int yOversize, Supplier<Material> matSupplier, Function<GuiElement, Boolean> highlight) {
        GuiElement<?> base = new GuiElement<>().setSize(xSize, ySize);
        GuiTexture icon = new GuiTexture(matSupplier).setSize(xSize, ySize);
        addHoverHighlight(base, xOversize, yOversize).setEnabledCallback(() -> highlight.apply(base));
        base.addChild(icon);
        parent.addChild(base);
        return base;
    }


    //endregion
    //Create Progress Bar..

    //Create Power Bar
    public GuiEnergyBar createEnergyBar(GuiElement parent) {
        GuiEnergyBar energyBar = new GuiEnergyBar();
        //TODO add ability to bind to
        //TODO Theme? Maybe? Maybe not needed?

        if (parent != null) {
            parent.addChild(energyBar);
        }
        return energyBar;
    }

    public GuiEnergyBar createEnergyBar(GuiElement parent, IOPStorage storage) {
        GuiEnergyBar energyBar = new GuiEnergyBar();
        energyBar.setEnergyStorage(storage);

        if (parent != null) {
            parent.addChild(energyBar);
        }
        return energyBar;
    }

    public GuiEnergyBar createEnergyBar(IOPStorage storage) {
        return createEnergyBar(null, storage);
    }

    public GuiEnergyBar createEnergyBar() {
        return createEnergyBar((IOPStorage) null);
    }

    //Info Panel
    public InfoPanel createInfoPanel(GuiElement parent, boolean leftSide) {
        InfoPanel panel = new InfoPanel(parent, leftSide);
        parent.addChild(panel);
        jeiExclude(panel);
        return panel;
    }

    //Create Text Field
    public GuiTextField createTextField() {
        return createTextField(null);
    }

    public GuiTextField createTextField(GuiElement parent) {
        return createTextField(parent, true);
    }

    public GuiTextField createTextField(GuiElement parent, boolean background) {
        GuiTextField textField = new GuiTextField();
        textField.setTextColor(Palette.Ctrl::text);
        textField.setShadow(false);

        if (background) {
            textField.addBackground(Palette.Ctrl::fill, hovering -> Palette.Ctrl.accentLight(false));
        }

        if (parent != null) {
            parent.addChild(textField);
        }
        return textField;
    }

    //Create Scroll Bars
    public GuiSlideControl createVanillaScrollBar(GuiSlideControl.SliderRotation rotation, boolean forceEnabled) {
        GuiSlideControl scrollBar = new GuiSlideControl(rotation);
        scrollBar.setBackgroundElement(GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_disabled")));
        scrollBar.setSliderElement(GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_borderless")));
        if (forceEnabled) {
            scrollBar.setEnabledCallback(() -> true);
        }
        scrollBar.onReload(GuiSlideControl::updateElements);
        return scrollBar;
    }

    public GuiSlideControl createVanillaScrollBar(GuiSlideControl.SliderRotation rotation) {
        return createVanillaScrollBar(rotation, true);
    }

    public GuiSlideControl createVanillaScrollBar() {
        return createVanillaScrollBar(GuiSlideControl.SliderRotation.VERTICAL, true);
    }


    //LayoutUtils
    public void center(GuiElement element, GuiElement centerOn, int xOffset, int yOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2) + xOffset);
        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2) + yOffset);
    }

    public void center(GuiElement element, int xPos, int yPos) {
        element.setXPos(xPos - (element.xSize() / 2));
        element.setYPos(yPos - (element.ySize() / 2));
    }

    public void centerX(GuiElement element, GuiElement centerOn, int xOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2) + xOffset);
    }

    public void centerY(GuiElement element, GuiElement centerOn, int yOffset) {
        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2) + yOffset);
    }

    //Templates
    public <TEM extends IGuiTemplate> TEM loadTemplate(TEM template) {
        template.addElements(gui.getManager(), this);
        return template;
    }

    /**
     * Place inside the target element https://ss.brandon3055.com/e89a6
     */
    public void placeInside(GuiElement element, GuiElement placeInside, LayoutPos position, int xOffset, int yOffset) {
        //@formatter:off
        switch (position) {
            case TOP_LEFT:      element.setRelPos(placeInside, xOffset, yOffset); break;
            case TOP_CENTER:    element.setRelPos(placeInside, ((placeInside.xSize() - element.xSize()) / 2) + xOffset, yOffset); break;
            case TOP_RIGHT:     element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, yOffset); break;
            case MIDDLE_RIGHT:  element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, ((placeInside.ySize() - element.ySize()) / 2) + yOffset); break;
            case BOTTOM_RIGHT:  element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
            case BOTTOM_CENTER: element.setRelPos(placeInside, ((placeInside.xSize() - element.xSize()) / 2) + xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
            case BOTTOM_LEFT:   element.setRelPos(placeInside, xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
            case MIDDLE_LEFT:   element.setRelPos(xOffset, ((placeInside.ySize() - element.ySize()) / 2) + yOffset); break;
        }
        //@formatter:on
    }

    /**
     * Place outside the target element https://ss.brandon3055.com/baa7c
     */
    public void placeOutside(GuiElement element, GuiElement placeOutside, LayoutPos position, int xOffset, int yOffset) {
        //@formatter:off
        switch (position) {
            case TOP_LEFT:      element.setRelPos(placeOutside, -element.xSize() + xOffset, -element.ySize() + yOffset); break;
            case TOP_CENTER:    element.setRelPos(placeOutside, ((placeOutside.xSize() - element.xSize()) / 2) + xOffset, -element.ySize() + yOffset); break;
            case TOP_RIGHT:     element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, -element.ySize() + yOffset); break;
            case MIDDLE_RIGHT:  element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, ((placeOutside.ySize() - element.ySize()) / 2) + yOffset); break;
            case BOTTOM_RIGHT:  element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, placeOutside.ySize() + yOffset); break;
            case BOTTOM_CENTER: element.setRelPos(placeOutside, ((placeOutside.xSize() - element.xSize()) / 2) + xOffset, placeOutside.ySize() + yOffset); break;
            case BOTTOM_LEFT:   element.setRelPos(placeOutside, -element.xSize() + xOffset, placeOutside.ySize() + yOffset); break;
            case MIDDLE_LEFT:   element.setRelPos(placeOutside, -element.xSize() + xOffset, ((placeOutside.ySize() - element.ySize()) / 2) + yOffset); break;
        }
        //@formatter:on
    }

    public void jeiExclude(GuiElement element) {
        jeiExclusions.add(element);
    }

    public static ResourceLocation getRS(String resource) {
        return resourceCache.computeIfAbsent(resource, s -> new ResourceLocation(resource));
    }

    public int guiLeft() {
        return gui.guiLeft();
    }

    public int guiTop() {
        return gui.guiTop();
    }

    public Predicate<String> catchyValidator(Predicate<String> predicate) {
        return s -> {
            try {
                return predicate.test(s);
            }
            catch (Throwable e) {
                return false;
            }
        };
    }

    //TODO add additional standard layouts as needed.

    public static final int DEFAULT_WIDTH = 176;
    public static final int WIDE_WIDTH = 200;
    public static final int EXTRA_WIDE_WIDTH = 250;

    public static final int DEFAULT_HEIGHT = 166;
    public static final int TALL_HEIGHT = 200;
    public static final int EXTRA_TALL_HEIGHT = 250;

    public enum GuiLayout {
        //        FULL_SCREEN(-1, -1, true, true),
        DEFAULT(DEFAULT_WIDTH, DEFAULT_HEIGHT, false, false),

        WIDE(WIDE_WIDTH, DEFAULT_HEIGHT, true, false),
        TALL(DEFAULT_WIDTH, TALL_HEIGHT, false, true),

        WIDE_TALL(WIDE_WIDTH, TALL_HEIGHT, true, true),

        EXTRA_WIDE(EXTRA_WIDE_WIDTH, DEFAULT_HEIGHT, true, false),
        EXTRA_TALL(DEFAULT_WIDTH, EXTRA_TALL_HEIGHT, false, true),

        EXTRA_WIDE_TALL(EXTRA_WIDE_WIDTH, TALL_HEIGHT, true, true),
        WIDE_EXTRA_TALL(WIDE_WIDTH, EXTRA_TALL_HEIGHT, true, true),
        EXTRA_WIDE_EXTRA_TALL(EXTRA_WIDE_WIDTH, EXTRA_TALL_HEIGHT, true, true),

        CUSTOM(-1, -1, false, false);

        public final int xSize;
        public final int ySize;
        private final boolean wide;
        private final boolean tall;

        GuiLayout(int xSize, int ySize, boolean wide, boolean tall) {
            this.xSize = xSize;
            this.ySize = ySize;
            this.wide = wide;
            this.tall = tall;
        }

        public String textureName() {
            return "background-" + xSize + "x" + ySize;
        }

        public boolean isTall() {
            return tall;
        }

        public boolean isWide() {
            return wide;
        }

        public static GuiLayout getBestFit(int width, int height) {
            GuiLayout bestFit = EXTRA_WIDE_EXTRA_TALL;
            int widthDeviation = bestFit.xSize - width;
            int heightDeviation = bestFit.ySize - height;
            for (GuiLayout layout : values()) {
                if (/*layout == FULL_SCREEN || */layout == CUSTOM) continue;
                int newXD = layout.xSize - width;
                int newYD = layout.ySize - height;
                if (newXD >= 0 && (newXD <= widthDeviation || widthDeviation < 0) && newYD >= 0 && (newYD <= heightDeviation || heightDeviation < 0)) {
                    bestFit = layout;
                    widthDeviation = newXD;
                    heightDeviation = newYD;
                }
            }

            return bestFit;
        }
    }

    public enum LayoutPos {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_RIGHT,
        MIDDLE_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_LEFT
    }

//    public static class SpriteData {
//        private final ResourceLocation texture;
////        private final int x;
////        private final int y;
////        private final int width;
////        private final int height;
//
//        public SpriteData(ResourceLocation texture, int x, int y, int width, int height) {
//            this.texture = texture;
//            this.x = x;
//            this.y = y;
//            this.width = width;
//            this.height = height;
//        }
//    }

    public static class InfoPanel extends GuiElement<InfoPanel> {
        private static AtomicBoolean globalExpanded = new AtomicBoolean(false);
        private Map<GuiElement, Dimension> elementsDimMap = new LinkedHashMap<>();
        private final GuiElement parent;
        private boolean leftSide = false;
        private boolean hasPI = true;
        private AtomicBoolean expanded;
        public double animState = 0;
        private Supplier<Point> origin;
        private GuiButton toggleButton;
        public String hoverText = GuiToolkit.i18ni("info_panel");

        public InfoPanel(GuiElement parent, boolean leftSide, AtomicBoolean expandedHolder) {
            this.parent = parent;
            this.leftSide = leftSide;
            this.expanded = expandedHolder;
            this.animState = isExpanded() ? 1 : -0.5;
            setEnabled(false);

            if (animState == -0.5) {
                setHoverText(hoverText);
            }
            updatePosSize();
            setHoverTextDelay(10);
        }

        public InfoPanel(GuiElement parent, boolean leftSide) {
            this(parent, leftSide, globalExpanded);
        }

        public void setExpandedHolder(AtomicBoolean expanded) {
            this.expanded = expanded;
            this.animState = isExpanded() ? 1 : -0.5;
            setHoverTextEnabled(false);
            if (animState == -0.5) {
                setHoverText(hoverText);
                setHoverTextEnabled(true);
            }
        }

        public boolean isExpanded() {
            return expanded.get();
        }

        public void toggleExpanded() {
            expanded.set(!expanded.get());
        }

        @Override
        public void addChildElements() {
            super.addChildElements();

            toggleButton = new GuiButton()
                    .setHoverTextDelay(10)
                    .setSize(12, 12)
                    .onPressed(this::toggleExpanded)
                    .setPosModifiers(() -> getOrigin().x, () -> getOrigin().y)
                    .setEnabledCallback(() -> origin != null || animState <= 0);

            addHoverHighlight(toggleButton);

            GuiTexture icon = new GuiTexture(12, 12, BCGuiSprites.getter("info_panel"))
                    .setPosModifiers(() -> getOrigin().x, () -> getOrigin().y);

            toggleButton.addChild(icon);
            addChild(toggleButton);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            if (isExpanded()) {
                updatePosSize();
            }
        }

        public void setOrigin(Supplier<Point> origin) {
            this.origin = origin;
        }

        public Point getOrigin() {
            if (origin == null) {
                int xPos = leftSide ? parent.xPos() - xSize() - 2 : parent.maxXPos() + 2;
                int yPos = parent.yPos() + (leftSide && hasPI ? 25 : 0);
                return new Point(xPos, yPos);
            }
            return origin.get();
        }

        public InfoPanel addElement(GuiElement element, Dimension preferredSize) {
            if (elementsDimMap.isEmpty()) {
                setEnabled(true);
            }
            elementsDimMap.put(element, preferredSize);
            addChild(element);
            updatePosSize();
            return this;
        }

        public InfoPanel addElement(GuiElement element) {
            return addElement(element, new Dimension(element.xSize(), element.ySize()));
        }

        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, Dimension preferredSize) {
            GuiLabel label = new GuiLabel().setAlignment(GuiAlign.LEFT);
            label.setSize(preferredSize.width, preferredSize.height);
            label.setDisplaySupplier(stringSupplier);
            addElement(label, preferredSize);
            return label;
        }

        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, int xSize, int ySize) {
            return addDynamicLabel(stringSupplier, new Dimension(xSize, ySize));
        }

        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, int ySize) {
            Dimension dimension = new Dimension(fontRenderer.width(stringSupplier.get()), ySize);
            GuiLabel label = new GuiLabel(stringSupplier) {
                @Override
                public boolean onUpdate() {
                    int lastWidth = dimension.width;
                    dimension.width = fontRenderer.width(stringSupplier.get());

                    if (dimension.width != lastWidth) {
                        updatePosSize();
                    }
                    return super.onUpdate();
                }
            };
            label.setTrim(false);
            label.setAlignment(GuiAlign.LEFT);
            addElement(label, dimension);
            return label;
        }

        public GuiElement addLabeledValue(String labelText, int valueOffset, int lineHeight, Supplier<String> valueSupplier, boolean multiLine) {
            GuiElement container = new GuiElement();
            GuiLabel label = new GuiLabel(labelText).setAlignment(GuiAlign.LEFT);
            label.setSize(multiLine ? fontRenderer.width(labelText) : valueOffset, lineHeight);
            label.setWrap(true);
            container.addChild(label);
            String value = valueSupplier.get();
            int extraHeiht = fontRenderer.lineHeight;
            if (value.contains("\n")) {
                String[] strs = value.split("\n");
                value = "";
                for (String s : strs)
                    if (s.length() > value.length()) {
                        extraHeiht += fontRenderer.lineHeight;
                        value = s;
                    }
            }
            extraHeiht -= fontRenderer.lineHeight;

            Dimension dimension;
            if (multiLine) {
                dimension = new Dimension(Math.max(label.xSize(), valueOffset + fontRenderer.width(value)), (lineHeight * 2) + extraHeiht);
            } else {
                dimension = new Dimension(valueOffset + fontRenderer.width(value), lineHeight);
            }

            GuiLabel valueLabel = new GuiLabel() {
                @Override
                public boolean onUpdate() {
                    int lastWidth = dimension.width;
                    String value = valueSupplier.get();
                    if (value.contains("\n")) {
                        String[] strs = value.split("\n");
                        value = "";
                        for (String s : strs) if (s.length() > value.length()) value = s;
                    }
                    if (multiLine) {
                        dimension.width = Math.max(label.xSize(), valueOffset + fontRenderer.width(value));
                    } else {
                        dimension.width = valueOffset + fontRenderer.width(value);
                    }

                    if (dimension.width != lastWidth) {
                        updatePosSize();
                    }
                    setMaxXPos(container.maxXPos(), true);
                    return super.onUpdate();
                }
            };
            valueLabel.setTrim(false);
            valueLabel.setAlignment(GuiAlign.LEFT);
            valueLabel.setDisplaySupplier(valueSupplier);
            valueLabel.setYSize(lineHeight);
            valueLabel.setXPos(valueOffset);
            valueLabel.setYPos(multiLine ? lineHeight : 0);
            container.addChild(valueLabel);

            addElement(container, dimension);
            return container;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
            if (!ret && isMouseOver(mouseX, mouseY)) {
                toggleExpanded();
                GuiButton.playGenericClick();
                return true;
            }

            return ret;
        }

        private void updatePosSize() {
            Dimension prefBounds = new Dimension();
            for (Dimension dim : elementsDimMap.values()) {
                prefBounds.width = Math.max(prefBounds.width, dim.width);
                prefBounds.height += dim.height;
            }

            Dimension actSize = prefBounds;
            int xPos = leftSide ? parent.xPos() - xSize() - 2 : parent.maxXPos() + 2;
            int yPos = parent.yPos() + (leftSide && hasPI ? 25 : 0);
            Rectangle bounds = /*new Rectangle(xPos, yPos, prefBounds.width + 6, prefBounds.height + 6);*/new Rectangle(xPos, yPos, actSize.width + 8, actSize.height + 6);
            Point origin = getOrigin();
            Rectangle collapsed = new Rectangle(origin.x, origin.y, 12, 12);

            double animState = Math.max(0, this.animState);
            int sx = (int) MathUtils.map(animState, 0, 1, collapsed.x, bounds.x);
            int sy = (int) MathUtils.map(animState, 0, 1, collapsed.y, bounds.y);
            int sw = (int) MathUtils.map(animState, 0, 1, collapsed.width, bounds.width);
            int sh = (int) MathUtils.map(animState, 0, 1, collapsed.height, bounds.height);
            if (sx + sw > screenWidth) {
                sx -= (sx + sw) - screenWidth;
            }
            setPosAndSize(sx, sy, sw, sh);

            int y = yPos + 3;
            for (GuiElement element : elementsDimMap.keySet()) {
                if (animState >= 1) {
                    element.setEnabled(true);
                    element.setPos(xPos() + 4, y);
                    Dimension dim = elementsDimMap.get(element);
                    element.setXSize(Math.min(actSize.width, dim.width));
                    element.setYSize(Math.min((int) (((double) actSize.height / prefBounds.height) * dim.height), dim.height));
                    y += element.ySize();
                } else {
                    element.setEnabled(false);
                }
            }
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            toggleButton.renderElement(minecraft, mouseX, mouseY, partialTicks);
            double fadeAlpha = Math.min(1, ((animState + 0.5) * 2));
            int col1 = 0x100010 | (int) (0xf0 * fadeAlpha) << 24;
            int col2 = 0x0080ff | (int) (0xB0 * fadeAlpha) << 24;
            int col3 = 0x00408f | (int) (0x80 * fadeAlpha) << 24;
            MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            drawColouredRect(getter, xPos(), yPos() + 1, xSize(), ySize() - 2, col1);
            drawColouredRect(getter, xPos() + 1, yPos(), xSize() - 2, ySize(), col1);

            drawGradientRect(getter, xPos() + 1, yPos() + 1, xPos() + xSize() - 1, yPos() + ySize() - 1, col2, col3);
            drawColouredRect(getter, xPos() + 2, yPos() + 2, xSize() - 4, ySize() - 4, col1);

            getter.endBatch();

            for (GuiElement<?> element : childElements) {
                if (element.isEnabled() && element != toggleButton) {
                    element.preDraw(minecraft, mouseX, mouseY, partialTicks);
                    element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                    element.postDraw(minecraft, mouseX, mouseY, partialTicks);
                }
            }
        }

        @Override
        public boolean onUpdate() {
            if (isExpanded() && animState < 1) {
                animState = Math.min(1, animState + 0.2);
                setHoverTextEnabled(false);
                updatePosSize();
            } else if (!isExpanded() && animState > -0.5) {
                animState = Math.max(-0.5, animState - 0.2);
                if (animState == -0.5) {
                    setHoverText(hoverText);
                }
                updatePosSize();
            }

            return super.onUpdate();
        }

        public void clear() {
            elementsDimMap.keySet().forEach(this::removeChild);
            elementsDimMap.clear();
        }
    }

    public static abstract class Palette {
        /**
         * Background colours. These match the colours used in the default background textures.
         */
        public static class BG {
            public static int fill() {
                return darkMode ? 0xFF3c3c3c : 0xFFc6c6c6;
            }

            public static int border() {
                return darkMode ? 0xFF141414 : 0xFF000000;
            }

            public static int accentLight() {
                return darkMode ? 0xFF5b5b5b : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF282828 : 0xFF555555;
            }

            public static int text() {
                return darkMode ? 0xAFB1B3 : 0x111111;
            }
        }

        /**
         * Slot Colours. These match the colours used in the default background textures.
         */
        public static class Slot {
            public static int fill() {
                return darkMode ? 0xFF6a6a6a : 0xFF8b8b8b;
            }

            public static int border3D(boolean hovering) {
                return darkMode ? 0xFFFFFFFF : 0xFF000000;
            }

            public static int accentLight() {
                return darkMode ? 0xFFc3c3c3 : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF2a2a2a : 0xFF373737;
            }

            public static int text() {
                return darkMode ? 0xdee0e2 : 0x1e2027;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0 : 0 : text();
            }
        }

        /**
         * Things like items/controls in a display list that uses the slot background.
         */
        public static class SubItem {
            public static int fill() {
                return darkMode ? 0xFF5e5f66 : 0xFFbdc6cf;
            }

//            public static int border() {
//                return darkMode ? 0xFF141414 : 0xFF000000;
//            }

            public static int accentLight() {
                return darkMode ? 0xFF77787f : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF46474e : 0xFF4a5760;
            }

            public static int text() {
                return darkMode ? 0xdee0e2 : 0x1e2012;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0 : 0 : text();
            }

            public static int border3d() {
                return darkMode ? 0xFFFFFFFF : 0xFF000000;
            }
        }

        /**
         * Buttons and other controls that require a colour pallet.
         */
        public static class Ctrl {
            public static int fill(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF475b6a : 0xFF647baf;
                } else {
                    return darkMode ? 0xFF5b5b5b : 0xFF808080;
                }
            }

            public static int border(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFFa8b0e1 : 0xFF000000;
                } else {
                    return darkMode ? 0xFFd3d3d3 : 0xFF000000;
                }
            }

            public static int border3D(boolean hovering) {
                return 0xFF000000;
            }

            public static int accentLight(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF75a8c2 : 0xFFa8afe1;
                } else {
                    return darkMode ? 0xFFa8a8a8 : 0xFFffffff;
                }
            }

            public static int accentDark(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF21303f : 0xFF515a8a;
                } else {
                    return darkMode ? 0xFF303030 : 0xFF555555;
                }
            }

            public static int text() {
                return darkMode ? 0xe1e3e5 : 0xFFFFFF;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0xffffa0 : 0xffffa0 : text();
            }
        }

//        public abstract int fill();
//        public abstract int border();
//        public abstract int accentLight();
//        public abstract int accentDark();
//        public abstract int text();
//
//        public static abstract class CtrlPallet extends Palette {
//            public abstract int border3D(boolean hovering);
//        }

    }
}
