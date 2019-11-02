package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.templates.IGuiTemplate;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static com.brandon3055.brandonscore.client.gui.BCGuiToolkit.GuiLayout.CUSTOM;

/**
 * Created by brandon3055 on 5/7/19.
 */
public class BCGuiToolkit<T extends GuiScreen & IModularGui> {

    public static int HEADING_LIGHT = 0x111111;
    public static int HEADING_DARK = 0xAFB1B3;

    private static Map<String, ResourceLocation> resourceCache = new HashMap<>();
    private List<MGuiElementBase> jeiExclusions = new ArrayList<>();

    private T gui;
    private GuiLayout layout;

    public BCGuiToolkit(T gui, GuiLayout layout) {
        this(gui, layout.xSize, layout.ySize);
        this.layout = layout;
        if (layout == CUSTOM) {
            throw new UnsupportedOperationException("For custom gui size use the alternate constructor that allows you to specify the size");
        }
    }

    public BCGuiToolkit(T gui, int xSize, int ySize) {
        this.gui = gui;
        this.layout = CUSTOM;
        this.gui.setUISize(xSize, ySize);
        gui.getManager().setJeiExclusions(() -> jeiExclusions);
    }

    //Create Theme Button
    public GuiButton createThemeButton() {
        GuiButton button = new GuiButton();
        button.setSize(12, 12);
        GuiTexture icon = new GuiTexture(12, 12, BCTextures.WIDGETS_GENERIC);
        icon.setTexXGetter(() -> darkMode ? 12 : 0);
//        icon.setTexYGetter(() -> button.getHoverTime() > 0 ? 12 : 0);
        icon.setTexYGetter(() -> 0);
        button.addChild(icon);
        button.setHoverText(element -> darkMode ? I18n.format("bc.guitoolkit.theme.set.light") : I18n.format("bc.guitoolkit.theme.set.dark"));
        button.setListener(() -> {
            darkMode = !darkMode;
            Property property = ModConfigParser.findUnwrappedProperty(BrandonsCore.MODID, "darkMode", "Client");
            if (property != null) {
                property.set(darkMode);
                ModConfigParser.saveModConfig(BrandonsCore.MODID);
            }
            else {
                LogHelperBC.error("Something went wrong when saving config values! Property could not be found");
            }
        });
        return button;
    }

    public GuiButton createThemeButton(MGuiElementBase parent) {
        return createThemeButton(parent, false);
    }

    public GuiButton createThemeButton(MGuiElementBase parent, boolean jeiExclude) {
        GuiButton button = createThemeButton();
        parent.addChild(button);
        if (jeiExclude) {
            jeiExclude(button);
        }
        return button;
    }

    public GuiButton createRSSwitch(IRSSwitchable switchable) {
        GuiButton button = new GuiButton();
        button.setSize(12, 12);
        GuiTexture icon = new GuiTexture(12, 12, BCTextures.WIDGETS_GENERIC);
        icon.setTexXGetter(() -> 36 + (switchable.getRSMode().index * 12));
        button.addChild(icon);
        icon.setYPosMod(button::yPos);
        button.setHoverText(element -> I18n.format("bc.guitoolkit.rs_mode." + switchable.getRSMode().name().toLowerCase()));
        button.setListener((guiButton, pressed) -> switchable.setRSMode(switchable.getRSMode().next(GuiScreen.isShiftKeyDown() || pressed == 1)));
        return button;
    }

    public GuiButton createRSSwitch(MGuiElementBase parent, IRSSwitchable switchable) {
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
    public GuiButton createButton(String unlocalizedText, @Nullable MGuiElementBase parent) {
        GuiButton button = new GuiButton(I18n.format(unlocalizedText));
        button.setTextureSupplier(BCTextures::widgets);
        button.enableVanillaRender();
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createButton(String unlocalizedText) {
        return createButton(unlocalizedText, null);
    }

    public GuiButton createRectButton(String unlocalizedText, @Nullable MGuiElementBase parent) {
        GuiButton button = new GuiButton(I18n.format(unlocalizedText));
        button.setRectFillColourGetter((hovering, disabled) -> hovering ? (darkMode ? 0xFF6060AA : 0xFF60AAFF) : (darkMode ? 0xFF606060 : 0xFF808080));
        button.setRectBorderColourGetter((hovering, disabled) -> hovering ? (darkMode ? 0xFFFFFFFF : 0xFF000000) : (darkMode ? 0xFFDDDDDD : 0xFF000000));

//        if (!GuiHelper.isInRect(10, 10, 50, 100, mouseX, mouseY)) {
//            drawBorderedRect(10, 10, 50, 14, 1, 0xFF808080, 0xFF000000);
//            drawBorderedRect(10, 100, 50, 14, 1, 0xFF606060, 0xFFDDDDDD);
//        }
//        else {
//            drawBorderedRect(10, 10, 50, 14, 1, 0xFF60AAFF, 0xFF000000);
//            drawBorderedRect(10, 100, 50, 14, 1, 0xFF6060AA, 0xFFFFFFFF);
//        }

        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createRectButton(String unlocalizedText) {
        return createRectButton(unlocalizedText, null);
    }

    //UI Heading
    public GuiLabel createHeading(String unlocalizedHeading, @Nullable MGuiElementBase parent, boolean layout) {
        GuiLabel heading = new GuiLabel(I18n.format(unlocalizedHeading));
        heading.setTextColGetter(hovering -> darkMode ? HEADING_DARK : HEADING_LIGHT);
        heading.setShadowStateSupplier(() -> darkMode);
        if (parent != null) {
            parent.addChild(heading);
            if (layout) {
                heading.setSize(parent.xSize(), 8).setAlignment(GuiAlign.CENTER).setRelPos(parent, 0, 4);
            }
        }
        return heading;
    }

    public GuiLabel createHeading(String unlocalizedHeading, @Nullable MGuiElementBase parent) {
        return createHeading(unlocalizedHeading, parent, false);
    }

    public GuiLabel createHeading(String unlocalizedHeading) {
        return createHeading(unlocalizedHeading, null, false);
    }

    /**
     * Creates a generic set of inventory slots with the specified dimensions.
     */
    public MGuiElementBase createSlots(MGuiElementBase parent, int columns, int rows, int spacing) {
        MGuiElementBase element = new MGuiElementBase() {
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

    public MGuiElementBase createSlots(MGuiElementBase parent, int columns, int rows) {
        return createSlots(parent, columns, rows, 0);
    }

    public MGuiElementBase createSlots(int columns, int rows) {
        return createSlots(null, columns, rows, 0);
    }

    /**
     * Creates the standard player inventory slot layout.
     */
    public MGuiElementBase createPlayerSlots(MGuiElementBase parent, int hotBarSpacing, boolean title) {
        MGuiElementBase container = new MGuiElementBase();
        MGuiElementBase main = createSlots(container, 9, 3);
        MGuiElementBase bar = createSlots(container, 9, 1);
        bar.setYPos(main.maxYPos() + hotBarSpacing);

        if (title) {
            GuiLabel invTitle = new GuiLabel(I18n.format("container.inventory"));
            invTitle.setAlignment(GuiAlign.LEFT).setTextColGetter(hovering -> darkMode ? HEADING_DARK : HEADING_LIGHT);
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

    public MGuiElementBase createPlayerSlots(int hotBarSpacing) {
        return createPlayerSlots(null, hotBarSpacing, true);
    }

    public MGuiElementBase createPlayerSlots() {
        return createPlayerSlots(4);
    }

    //Create Slot
    // - Slot Ghost Images

    //Create Progress Bar..

    //Create Power Bar
    public GuiEnergyBar createEnergyBar(MGuiElementBase parent) {
        GuiEnergyBar energyBar = new GuiEnergyBar();
        //TODO add ability to bind to
        //TODO Theme? Maybe? Maybe not needed?

        if (parent != null) {
            parent.addChild(energyBar);
        }
        return energyBar;
    }

    public GuiEnergyBar createEnergyBar(MGuiElementBase parent, IOPStorage storage) {
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
    public InfoPanel createInfoPanel(MGuiElementBase parent, boolean leftSide) {
        InfoPanel panel = new InfoPanel(parent, leftSide);
        parent.addChild(panel);
        jeiExclude(panel);
        return panel;
    }

    //etc...

    //LayoutUtils
    public void center(MGuiElementBase element, MGuiElementBase centerOn, int xOffset, int yOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2));
        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2));
    }

    public void centerX(MGuiElementBase element, MGuiElementBase centerOn, int xOffset) {
        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2));
    }

    public void centerY(MGuiElementBase element, MGuiElementBase centerOn, int yOffset) {
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
    public void placeInside(MGuiElementBase element, MGuiElementBase placeInside, LayoutPos position, int xOffset, int yOffset) {
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
    public void placeOutside(MGuiElementBase element, MGuiElementBase placeOutside, LayoutPos position, int xOffset, int yOffset) {
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

    public void jeiExclude(MGuiElementBase element) {
        jeiExclusions.add(element);
    }

    public static ResourceLocation getRS(String resource) {
        return resourceCache.computeIfAbsent(resource, s -> new ResourceLocation(resource));
    }

    //TODO add additional standard layouts as needed.
    public enum GuiLayout {
        FULL_SCREEN(-1, -1),
        DEFAULT_CONTAINER(176, 166),
        //        CREATIVE_GUI(195, 136),
        CUSTOM(-1, -1);

        public final int xSize;
        public final int ySize;

        GuiLayout(int xSize, int ySize) {
            this.xSize = xSize;
            this.ySize = ySize;
        }

        public String texture() {
            return "background-" + xSize + "x" + ySize + ".png";
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

    public static class InfoPanel extends MGuiElementBase<InfoPanel> {
        private Map<MGuiElementBase, Dimension> elementsDimMap = new LinkedHashMap<>();
        private final MGuiElementBase parent;
        private boolean leftSide = false;
        private boolean hasPI = true;
        public static boolean expanded = false;
        public static double animState = 0;
        public Supplier<Point> origin;
        public String hoverText = I18n.format("bc.guitoolkit.uiinfo");

        public InfoPanel(MGuiElementBase parent, boolean leftSide) {
            this.parent = parent;
            this.leftSide = leftSide;
            setEnabled(false);
            if (animState == -0.5) {
                setHoverText(hoverText);
            }
            updatePosSize();
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

        public InfoPanel addElement(MGuiElementBase element, Dimension preferredSize) {
            if (elementsDimMap.isEmpty()) {
                setEnabled(true);
            }
            elementsDimMap.put(element, preferredSize);
            addChild(element);
            updatePosSize();
            return this;
        }

        public InfoPanel addElement(MGuiElementBase element) {
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

        public MGuiElementBase addLabeledValue(String labelText, int valueOffset, int lineHeight, Supplier<String> valueSupplier, boolean multiLine) {
            MGuiElementBase container = new MGuiElementBase();
            GuiLabel label = new GuiLabel(labelText).setAlignment(GuiAlign.LEFT);
            label.setSize(multiLine ? fontRenderer.getStringWidth(labelText) : valueOffset, lineHeight);
            container.addChild(label);

            Dimension dimension;//
            if (multiLine) {
                dimension = new Dimension(Math.max(label.xSize(), valueOffset + fontRenderer.getStringWidth(valueSupplier.get())), lineHeight * 2);
            }
            else {
                dimension = new Dimension(valueOffset + fontRenderer.getStringWidth(valueSupplier.get()), lineHeight);
            }

            GuiLabel valueLabel = new GuiLabel(){
                @Override
                public boolean onUpdate() {
                    int lastWidth = dimension.width;
                    if (multiLine) {
                        dimension.width = Math.max(label.xSize(), valueOffset + fontRenderer.getStringWidth(valueSupplier.get()));
                    }
                    else {
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
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
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
            for (MGuiElementBase element : elementsDimMap.keySet()) {
                if (animState >= 1) {
                    element.setEnabled(true);
                    element.setPos(xPos() + 4, y);
                    Dimension dim = elementsDimMap.get(element);
                    element.setXSize(Math.min(actSize.width, dim.width));
                    element.setYSize(Math.min((int) (((double) actSize.height / prefBounds.height) * dim.height), dim.height));
                    y += element.ySize();
                }
                else {
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
            }
            else if (!expanded && animState > -0.5) {
                animState = Math.max(-0.5, animState - 0.2);
                if (animState == -0.5) {
                    setHoverText(hoverText);
                }
                updatePosSize();
            }

            return super.onUpdate();
        }
    }
}
