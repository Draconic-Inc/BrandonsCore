package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.templates.IGuiTemplate;
import com.brandon3055.brandonscore.config.BCConfig;
import com.brandon3055.brandonscore.inventory.ContainerBCBase;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotData;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.client.gui.GuiToolkit.GuiLayout.CUSTOM;
import static com.brandon3055.brandonscore.config.BCConfig.darkMode;
import static com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotType.PLAYER_INV;

/**
 * Created by brandon3055 on 5/7/19.
 */
public class GuiToolkit<T extends Screen & IModularGui> {

    private static Map<String, ResourceLocation> resourceCache = new HashMap<>();
    private List<GuiElement> jeiExclusions = new ArrayList<>();

    private T gui;
    private GuiLayout layout;
    private ContainerSlotLayout slotLayout;

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
        if (gui instanceof ModularGuiContainer && ((ModularGuiContainer) gui).getContainer() instanceof ContainerBCBase) {
            setSlotLayout(((ContainerBCBase) ((ModularGuiContainer) gui).getContainer()).getSlotLayout());
        }
    }

    public GuiLayout getLayout() {
        return layout;
    }

    public void setSlotLayout(ContainerSlotLayout slotLayout) {
        this.slotLayout = slotLayout;
    }

    //Create Theme Button
    public GuiButton createThemeButton() {
        GuiButton button = new GuiButton();
        addButtonHoverHighlight(button);
        button.setHoverTextDelay(10);
        button.setSize(12, 12);
        GuiTexture icon = new GuiTexture(12, 12, BCTextures.WIDGETS_GENERIC);
        icon.setTexXGetter(() -> darkMode ? 12 : 0);
//        icon.setTexYGetter(() -> button.getHoverTime() > 0 ? 12 : 0);
        icon.setTexYGetter(() -> 0);
        button.addChild(icon);
        button.setHoverText(element -> darkMode ? I18n.format("bc.guitoolkit.theme.set.light") : I18n.format("bc.guitoolkit.theme.set.dark"));
        button.onPressed(() -> {
            BCConfig.CLIENT.dark_mode.set(!darkMode); //TODO check if this auto saves
        });
        return button;
    }

    public GuiButton createThemeButton(GuiElement parent) {
        return createThemeButton(parent, false);
    }

    public GuiButton createThemeButton(GuiElement parent, boolean jeiExclude) {
        GuiButton button = createThemeButton();
        parent.addChild(button);
        if (jeiExclude) {
            jeiExclude(button);
        }
        return button;
    }

    public GuiButton createRSSwitch(IRSSwitchable switchable) {
        GuiButton button = new GuiButton();
        addButtonHoverHighlight(button);
        button.setHoverTextDelay(10);
        button.setSize(12, 12);
        GuiTexture icon = new GuiTexture(12, 12, BCTextures.WIDGETS_GENERIC);
        icon.setTexXGetter(() -> 36 + (switchable.getRSMode().index * 12));
        button.addChild(icon);
        icon.setYPosMod(button::yPos);
        button.setHoverText(element -> I18n.format("bc.guitoolkit.rs_mode." + switchable.getRSMode().name().toLowerCase()));
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
        GuiTexture texture = new GuiTexture(() -> getRS(BrandonsCore.MODID + ":textures/gui/" + (darkMode ? "dark" : "light") + "/" + layout.texture()));
        texture.setSize(layout.xSize, layout.ySize);
        if (addToManager) {
            gui.getManager().addChild(texture);
        }
        if (center) {
            texture.addAndFireReloadCallback(guiTex -> guiTex.setPos(gui.guiLeft(), gui.guiTop()));
        }
        return texture;
    }

    public GuiTexture createBackground(boolean center) {
        return createBackground(false, center);
    }

    public GuiTexture createBackground() {
        return createBackground(false);
    }

    //Create Themed Button
    public GuiButton createVanillaButton(String unlocalizedText, @Nullable GuiElement parent) {
        GuiButton button = new GuiButton(I18n.format(unlocalizedText));
        button.setHoverTextDelay(10);
        button.setTextureSupplier(BCTextures::widgets);
        button.enableVanillaRender();
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createVanillaButton(String unlocalizedText) {
        return createVanillaButton(unlocalizedText, null);
    }

    public GuiButton createButton(String unlocalizedText, @Nullable GuiElement parent, boolean inset3d) {
        GuiButton button = new GuiButton(I18n.format(unlocalizedText));
        button.setInsets(5, 2, 5, 2);
        button.setHoverTextDelay(10);
        if (inset3d) {
            button.set3dText(true);
            GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(1);
            //I use modifiers here to account for the possibility that this button may have modifiers. Something i need to account for when i re write modular gui
            buttonBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
            buttonBG.setFillColourL(hovering -> Palette.Ctrl.fill(hovering || button.isPressed()));
            buttonBG.setBorderColourL(Palette.Ctrl::border3D);
            buttonBG.set3dTopLeftColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentDark(true) : Palette.Ctrl.accentLight(hovering));
            buttonBG.set3dBottomRightColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentLight(true) : Palette.Ctrl.accentDark(hovering));
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

    public GuiButton createButton(String unlocalizedText, @Nullable GuiElement parent) {
        return createButton(unlocalizedText, parent, true);
    }

    public GuiButton createButton(String unlocalizedText, boolean shadeEdges) {
        return createButton(unlocalizedText, null, shadeEdges);
    }

    public GuiButton createButton(String unlocalizedText) {
        return createButton(unlocalizedText, null, true);
    }

    //UI Heading
    public GuiLabel createHeading(String unlocalizedHeading, @Nullable GuiElement parent, boolean layout) {
        GuiLabel heading = new GuiLabel(I18n.format(unlocalizedHeading));
        heading.setHoverableTextCol(hovering -> Palette.BG.text());
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
     */
    public GuiElement createSlots(GuiElement parent, int columns, int rows, int spacing, BiFunction<Integer, Integer, SlotData> slotMapper, SpriteData spriteData) {
        GuiElement element = new GuiElement() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                bindTexture(BCTextures.widgets());

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

                for (int x = 0; x < columns; x++) {
                    for (int y = 0; y < rows; y++) {
                        quad(buffer, xPos() + (x * (18 + spacing)), yPos() + (y * (18 + spacing)), 0, 0, 18, 18);
                    }
                }

                tessellator.draw();

                if (spriteData != null) {
                    buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                    bindTexture(spriteData.texture);

                    for (int x = 0; x < columns; x++) {
                        for (int y = 0; y < rows; y++) {
                            if (slotMapper != null) {
                                SlotData data = slotMapper.apply(x, y);
                                if (data != null && data.slot.getHasStack()) {
                                    continue;
                                }
                            }
                            quad(buffer, xPos() + (x * (18 + spacing)) + 1, yPos() + (y * (18 + spacing)) + 1, spriteData.x, spriteData.y, spriteData.width, spriteData.height);
                        }
                    }

                    tessellator.draw();
                }
            }

            @Override
            public GuiElement translate(int xAmount, int yAmount) {
                GuiElement ret = super.translate(xAmount, yAmount);
                if (slotMapper != null) {
                    for (int x = 0; x < columns; x++) {
                        for (int y = 0; y < rows; y++) {
                            SlotData data = slotMapper.apply(x, y);
                            if (data != null) {
                                data.setPos((xPos() + (x * (18 + spacing))) - gui.guiLeft() + 1, (yPos() + (y * (18 + spacing))) - gui.guiTop() + 1);
                            }
                        }
                    }
                }
                return ret;
            }

            public void quad(BufferBuilder buffer, int x, int y, int textureX, int textureY, int width, int height) {
                double zLevel = getRenderZLevel();
                buffer.pos(x, y + height, zLevel).tex((float) (textureX) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
                buffer.pos(x + width, y + height, zLevel).tex((float) (textureX + width) * 0.00390625F, (float) (textureY + height) * 0.00390625F).endVertex();
                buffer.pos(x + width, y, zLevel).tex((float) (textureX + width) * 0.00390625F, (float) (textureY) * 0.00390625F).endVertex();
                buffer.pos(x, y, zLevel).tex((float) (textureX) * 0.00390625F, (float) (textureY) * 0.00390625F).endVertex();
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

    public GuiElement createSlots(GuiElement parent, int columns, int rows, int spacing, SpriteData sprite) {
        return createSlots(parent, columns, rows, spacing, null, sprite);
    }

    public GuiElement createSlots(GuiElement parent, int columns, int rows, SpriteData sprite) {
        return createSlots(parent, columns, rows, 0, null, sprite);
    }

    public GuiElement createSlots(int columns, int rows) {
        return createSlots(null, columns, rows, 0);
    }

    /**
     * Creates the standard player inventory slot layout.
     */
    public GuiElement createPlayerSlots(GuiElement parent, int hotBarSpacing, boolean title) {
        GuiElement container = new GuiElement();
        GuiElement main = createSlots(container, 9, 3, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column + row * 9 + 9), null);
        GuiElement bar = createSlots(container, 9, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column), null);
        bar.setYPos(main.maxYPos() + hotBarSpacing);

        if (title) {
            GuiLabel invTitle = new GuiLabel(I18n.format("bc.guitoolkit.your_inventory"));
            invTitle.setAlignment(GuiAlign.LEFT).setHoverableTextCol(hovering -> Palette.BG.text());
            invTitle.setShadowStateSupplier(() -> darkMode);
            container.addChild(invTitle);
            invTitle.setSize(main.xSize(), 8);
            main.translate(0, 10);
            bar.translate(0, 10);
        }

        container.setSize(container.getEnclosingRect());
        if (parent != null) {
            parent.addChild(container);
        }

        return container;
    }

    public GuiElement createPlayerSlots(int hotBarSpacing) {
        return createPlayerSlots(null, hotBarSpacing, true);
    }

    public GuiElement createPlayerSlots() {
        return createPlayerSlots(4);
    }

    public GuiButton createLargeViewButton() {
        GuiButton button = new GuiButton();
        addButtonHoverHighlight(button);
        button.setHoverTextDelay(10);
        button.setSize(10, 10);
        GuiTexture icon = new GuiTexture(BCTextures::widgets).setSize(10, 10);
        icon.setTexturePos(1, 19);
        button.addChild(icon);
        button.setHoverText(element -> I18n.format("bc.guitoolkit.large_view"));
        return button;
    }

    private void addButtonHoverHighlight(GuiButton button) {
        GuiBorderedRect rect = new GuiBorderedRect();
        rect.setBorderColourL(hovering -> Palette.Ctrl.fill(hovering));
        rect.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
        rect.setEnabledCallback(() -> button.getHoverTime() > 0);
        button.addBackGroundChild(rect);
    }

    //Create Slot
    // - Slot Ghost Images

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

    //etc...

    //LayoutUtils
    public void center(GuiElement element, GuiElement centerOn, int xOffset, int yOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2));
        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2));
    }

    public void centerX(GuiElement element, GuiElement centerOn, int xOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2));
    }

    public void centerY(GuiElement element, GuiElement centerOn, int yOffset) {
        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2));
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
            case MIDDLE_LEFT:   element.setRelPos(-element.xSize() + xOffset, ((placeOutside.ySize() - element.ySize()) / 2) + yOffset); break;
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

    //TODO add additional standard layouts as needed.

    private static final int DEFAULT_WIDTH = 176;
    private static final int WIDE_WIDTH = 200;
    private static final int EXTRA_WIDE_WIDTH = 250;

    private static final int DEFAULT_HEIGHT = 166;
    private static final int TALL_HEIGHT = 200;
    private static final int EXTRA_TALL_HEIGHT = 250;

    public enum GuiLayout {
        FULL_SCREEN(-1, -1, true, true),
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

        public String texture() {
            return "background-" + xSize + "x" + ySize + ".png";
        }

        public boolean isTall() {
            return tall;
        }

        public boolean isWide() {
            return wide;
        }
    }

    public enum LayoutPos {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_LEFT,
        MIDDLE_LEFT
    }

    public static class SpriteData {
        private final ResourceLocation texture;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public SpriteData(ResourceLocation texture, int x, int y, int width, int height) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class InfoPanel extends GuiElement<InfoPanel> {
        private Map<GuiElement, Dimension> elementsDimMap = new LinkedHashMap<>();
        private final GuiElement parent;
        private boolean leftSide = false;
        private boolean hasPI = true;
        public static boolean expanded = false;
        public static double animState = 0;
        public Supplier<Point> origin;
        public String hoverText = I18n.format("bc.guitoolkit.uiinfo");

        public InfoPanel(GuiElement parent, boolean leftSide) {
            this.parent = parent;
            this.leftSide = leftSide;
            setEnabled(false);
            if (animState == -0.5) {
                setHoverText(hoverText);
            }
            updatePosSize();
            setHoverTextDelay(10);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            if (expanded) {
                updatePosSize();
            }
        }

        public void setOrigin(Supplier<Point> origin) {
            this.origin = origin;
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

        public GuiElement addLabeledValue(String labelText, int valueOffset, int lineHeight, Supplier<String> valueSupplier, boolean multiLine) {
            GuiElement container = new GuiElement();
            GuiLabel label = new GuiLabel(labelText).setAlignment(GuiAlign.LEFT);
            label.setSize(multiLine ? fontRenderer.getStringWidth(labelText) : valueOffset, lineHeight);
            container.addChild(label);

            Dimension dimension;//
            if (multiLine) {
                dimension = new Dimension(Math.max(label.xSize(), valueOffset + fontRenderer.getStringWidth(valueSupplier.get())), lineHeight * 2);
            } else {
                dimension = new Dimension(valueOffset + fontRenderer.getStringWidth(valueSupplier.get()), lineHeight);
            }

            GuiLabel valueLabel = new GuiLabel() {
                @Override
                public boolean onUpdate() {
                    int lastWidth = dimension.width;
                    if (multiLine) {
                        dimension.width = Math.max(label.xSize(), valueOffset + fontRenderer.getStringWidth(valueSupplier.get()));
                    } else {
                        dimension.width = valueOffset + fontRenderer.getStringWidth(valueSupplier.get());
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
                expanded = !expanded;
                GuiButton.playGenericClick(mc);
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

//            Dimension available = new Dimension();
//            available.height = parent.screenHeight - parent.yPos() - (leftSide && hasPI ? 25 : 0);
//            available.width = leftSide ? parent.xPos() - 10 : parent.screenWidth - parent.maxXPos() - 10;
            Dimension actSize = prefBounds;//new Dimension(Math.min(available.width, prefBounds.width), Math.min(available.height, prefBounds.height));
            int xPos = leftSide ? parent.xPos() - xSize() - 2 : parent.maxXPos() + 2;
            int yPos = parent.yPos() + (leftSide && hasPI ? 25 : 0);
            Rectangle bounds = /*new Rectangle(xPos, yPos, prefBounds.width + 6, prefBounds.height + 6);*/new Rectangle(xPos, yPos, actSize.width + 8, actSize.height + 8);
            Point origin = this.origin == null ? new Point(xPos, yPos) : this.origin.get();
            Rectangle collapsed = new Rectangle(origin.x, origin.y, 12, 12);

            double animState = Math.max(0, InfoPanel.animState);
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
            double fadeAlpha = Math.min(1, ((animState + 0.5) * 2));
            int col1 = 0x100010 | (int) (0xf0 * fadeAlpha) << 24;
            int col2 = 0x0080ff | (int) (0xB0 * fadeAlpha) << 24;
            int col3 = 0x00408f | (int) (0x80 * fadeAlpha) << 24;

            if (animState < 0 && hoverTime > 0) {
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), Palette.Ctrl.fill(true));
            }

            if (fadeAlpha < 1) {
                bindTexture(BCTextures.WIDGETS_GENERIC);
                drawTexturedModalRect(xPos(), yPos(), 24, 0, 12, 12);
            }

            drawColouredRect(xPos(), yPos() + 1, xSize(), ySize() - 2, col1);
            drawColouredRect(xPos() + 1, yPos(), xSize() - 2, ySize(), col1);

            drawGradientRect(xPos() + 1, yPos() + 1, xPos() + xSize() - 1, yPos() + ySize() - 1, col2, col3);
            drawColouredRect(xPos() + 2, yPos() + 2, xSize() - 4, ySize() - 4, col1);

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean onUpdate() {
            if (expanded && animState < 1) {
                animState = Math.min(1, animState + 0.2);
                setHoverTextEnabled(false);
                updatePosSize();
            } else if (!expanded && animState > -0.5) {
                animState = Math.max(-0.5, animState - 0.2);
                if (animState == -0.5) {
                    setHoverText(hoverText);
                }
                updatePosSize();
            }

            return super.onUpdate();
        }
    }

    public static class  Palette {
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

        //bgColour, bgBorderColour, bgAccentLight, bgAccentDark, ctrlBgColour, ctrlBorderColourz
    }
}
