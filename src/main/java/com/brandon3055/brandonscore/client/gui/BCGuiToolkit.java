package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brandon3055.brandonscore.BCConfig.darkMode;
import static com.brandon3055.brandonscore.client.gui.BCGuiToolkit.GuiLayout.CUSTOM;

/**
 * Created by brandon3055 on 5/7/19.
 */
public class BCGuiToolkit<T extends GuiScreen & IModularGui> {

    public static final ResourceLocation WIDGETS_LIGHT = new ResourceLocation(BrandonsCore.MODID + ":textures/gui/light/widgets.png");
    public static final ResourceLocation WIDGETS_DARK = new ResourceLocation(BrandonsCore.MODID + ":textures/gui/dark/widgets.png");
    public static final ResourceLocation MISC_TEXTURES = new ResourceLocation(BrandonsCore.MODID + ":textures/gui/misc_textures.png");

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
        GuiTexture icon = new GuiTexture(12, 12, MISC_TEXTURES);
        icon.setTexXGetter(() -> darkMode ? 12 : 0);
        icon.setTexYGetter(() -> button.getHoverTime() > 0 ? 12 : 0);
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

    //Create Background (various sizes)
    public GuiTexture createBackground(boolean addToManager, boolean center) {
        if (layout.xSize == -1 || layout.ySize == -1) {
            throw new UnsupportedOperationException("Layout type " + layout + " does not have an associated default background.");
        }

        GuiTexture texture = new GuiTexture(() -> getRS(BrandonsCore.MODID + ":textures/gui/" + (darkMode ? "dark" : "light") + "/" + layout.texture()));
        texture.setSize(layout.xSize, layout.ySize);
        if (addToManager) {
            gui.getManager().add(texture);
        }
        if (center) {
            texture.addAndFireReloadCallback(guiTex -> guiTex.setPos(gui.guiLeft(), gui.guiTop()));
        }
        return texture;
    }

    public GuiTexture createBackground(boolean center) {
        return createBackground(false, false);
    }

    public GuiTexture createBackground() {
        return createBackground(false);
    }

    //Create Themed Button
    public GuiButton createButton(String unlocalizedText, @Nullable MGuiElementBase parent) {
        GuiButton button = new GuiButton(I18n.format(unlocalizedText));
        button.setTextureSupplier(() -> darkMode ? WIDGETS_DARK : WIDGETS_LIGHT);
        button.enableVanillaRender();
        if (parent != null) {
            parent.addChild(button);
        }
        return button;
    }

    public GuiButton createButton(String unlocalizedText) {
        return createButton(unlocalizedText, null);
    }

    //UI Heading
    public GuiLabel createHeading(String unlocalizedHeading, @Nullable MGuiElementBase parent, boolean layout) {
        GuiLabel heading = new GuiLabel(I18n.format(unlocalizedHeading)) {
            @Override
            public boolean hasShadow() {
                return darkMode;
            }
        };
        heading.setTextColGetter(hovering -> darkMode ? HEADING_DARK : HEADING_LIGHT);
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
     * */
    public MGuiElementBase createSlots(MGuiElementBase parent, int columns, int rows, int spacing) {
        MGuiElementBase element = new MGuiElementBase() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                bindTexture(darkMode ? WIDGETS_DARK : WIDGETS_LIGHT);

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
     * */
    public MGuiElementBase createPlayerSlots(MGuiElementBase parent, int hotBarSpacing) {
        MGuiElementBase container = new MGuiElementBase();
        MGuiElementBase main = createSlots(container, 9, 3);
        MGuiElementBase bar = createSlots(container, 9, 1);
        bar.setYPos(main.maxYPos() + hotBarSpacing);
        container.setSize(container.getEnclosingRect());
        if (parent != null) {
            parent.addChild(container);
        }
        return container;
    }

    public MGuiElementBase createPlayerSlots(int hotBarSpacing) {
        return createPlayerSlots(null, hotBarSpacing);
    }

    public MGuiElementBase createPlayerSlots() {
        return createPlayerSlots(4);
    }

        //Create Slot
    // - Slot Ghost Images

    //Create Progress Bar..

    //Create Power Bar
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
}
