package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiPickColourDialog;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSelectDialog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BCConfig.darkMode;

/**
 * Created by brandon3055 on 8/5/20.
 */
public class ThemedElements {

    /**
     * Background Highlight Colour
     */
    public static int getBgLight() {
        return darkMode ? 0xFF5b5b5b : 0xFFFFFFFF;
    }

    /**
     * Background Shadow Colour
     */
    public static int getBgDark() {
        return darkMode ? 0xFF282828 : 0xFF505050;
    }

    /**
     * Background Fill Colour
     */
    public static int getBgFill() {
        return darkMode ? 0xFF3c3c3c : 0xFFc6c6c6;
    }


    public static int getLightScroll() {
        return darkMode ? 0xFF5d5e68 : 0xFFFFFFFF;
    }

    public static int getDarkScroll() {
        return darkMode ? 0xFF353535 : 0xFF505050;
    }

    /**
     * This is a simple shaded rectangle that matches gui background colours.
     */
    public static class ShadedRect extends GuiElement<ShadedRect> {
        private Supplier<Boolean> inset;
        private boolean fill;

        public ShadedRect() {
            this(false, true);
        }

        public ShadedRect(boolean inset, boolean fill) {
            this(() -> inset, fill);
        }

        public ShadedRect(Supplier<Boolean> inset, boolean fill) {
            this.inset = inset;
            this.fill = fill;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            boolean inset = this.inset.get();
            int light = inset ? getBgLight() : getBgDark();
            int dark = inset ? getBgDark() : getBgLight();
            drawShadedRect(getter, xPos(), yPos(), xSize(), ySize(), 1, fill ? getBgFill() : 0, dark, light, getBgFill());
            getter.endBatch();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * This is a rectangle with a raised / inset 2 pixel border.
     * The colours used are meant to match the themed gui background colours.
     * See the basic view properties window in DE's GuiConfigurableItem for an example
     */
    public static class ContentRect extends GuiElement<ContentRect> {
        private Supplier<Boolean> inset;
        private boolean fill = true;

        public ContentRect() {
            this(false, true);
        }

        public ContentRect(boolean inset, boolean fill) {
            this(() -> inset, fill);
        }

        public ContentRect(Supplier<Boolean> inset, boolean fill) {
            this.inset = inset;
            this.fill = fill;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            boolean inset = this.inset.get();
            int light = inset ? getBgDark() : getBgLight();
            int dark = inset ? getBgLight() : getBgDark();
            int fill = getBgFill();
            drawShadedRect(getter, xPos(), yPos(), xSize(), ySize(), 1, 0, light, dark, fill);
            drawShadedRect(getter, xPos() + 1, yPos() + 1, xSize() - 2, ySize() - 2, 1, this.fill ? fill : 0, dark, light, fill);
            getter.endBatch();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }

    public static class ScrollBar extends GuiElement<ScrollBar> {
        private boolean background;
        private Supplier<Boolean> dragging = () -> false;

        public ScrollBar(boolean background) {
            this.background = background;
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            GuiElement<?> parent = getParent();
            if (parent instanceof GuiSlideControl) {
                dragging = ((GuiSlideControl) parent)::isDragging;
            }
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            if (background) {
                boolean highlight = isMouseOver(mouseX, mouseY) || dragging.get();
                drawColouredRect(getter, xPos(), yPos(), xSize(), ySize(), GuiElement.mixColours(ThemedElements.getBgFill(), 0x00303030 + (highlight ? 0x00151515 : 0), !darkMode));
            } else {
                int light = getLightScroll();//mixColours(getLight(), 0x00101010, true);
                int dark = getDarkScroll();//mixColours(getDark(), 0x00101010, true);
                drawShadedRect(getter, xPos(), yPos(), xSize(), ySize(), 1, midColour(light, dark), light, dark, midColour(light, dark));
            }
            getter.endBatch();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }

//    @Deprecated
//    public static class DialogBackground extends GuiElement<DialogBackground> {
//
//        private Supplier<Boolean> hasHeading;
//
//        public DialogBackground(Supplier<Boolean> hasHeading) {
//            this.hasHeading = hasHeading;
//        }
//
//        @Override
//        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//            IRenderTypeBuffer.Impl getter = minecraft.getRenderTypeBuffers().getBufferSource();
//            int backgroundColor = 0xF0100010;
//            int borderColorStart = 0x90FFFFFF;
//            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
//            //@formatter:off
//            drawGradient(getter, xPos() + 1,           yPos(),               xSize() - 2, 1, backgroundColor, backgroundColor);             // Top
//            drawGradient(getter, xPos() + 1,           yPos() + ySize() - 1, xSize() - 2, 1, backgroundColor, backgroundColor);             // Bottom
//            drawGradient(getter, xPos(),               yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Left
//            drawGradient(getter, xPos() + xSize() - 1, yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Right
//            drawGradient(getter, xPos() + 1,           yPos() + 1,           xSize() - 2, ySize() - 2, backgroundColor, backgroundColor);   // Fill
//            drawGradient(getter, xPos() + 1,           yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Left Accent
//            drawGradient(getter, xPos() + xSize() - 2, yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Right Accent
//            drawGradient(getter, xPos() + 2,           yPos() + 1,           xSize() - 4, 1, borderColorStart, borderColorStart);           // Top Accent
//            drawGradient(getter, xPos() + 2,           yPos() + ySize() - 2, xSize() - 4, 1, borderColorEnd, borderColorEnd);               // Bottom Accent
//            if (hasHeading.get()) {
//                drawGradient(getter, xPos() + 2,       yPos() + 12,           xSize() - 4, 1, borderColorStart, borderColorStart);          // Heading Divider
//            }
//            //@formatter:on
//            getter.finish();
//            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        }
//    }

//    @Deprecated
//    public static class DialogBar extends GuiElement<DialogBar> {
//        private boolean background;
//        private Supplier<Boolean> dragging = () -> false;
//
//        public DialogBar(boolean background) {
//            this.background = background;
//        }
//
//        @Override
//        public void reloadElement() {
//            super.reloadElement();
//            GuiElement<?> parent = getParent();
//            if (parent instanceof GuiSlideControl) {
//                dragging = ((GuiSlideControl) parent)::isDragging;
//            }
//        }
//
//        @Override
//        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//            IRenderTypeBuffer.Impl getter = minecraft.getRenderTypeBuffers().getBufferSource();
//            if (background && (dragging.get() || isMouseOver(mouseX, mouseY))) {
//                drawColouredRect(getter, xPos() + 1, yPos(), xSize() - 1, ySize(), 0x30b341ff);
//            } else if (!background) {
//                drawColouredRect(getter, xPos() + 1, yPos(), xSize() - 1, ySize(), 0x8cb341ff);
//            }
//            getter.finish();
//            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        }
//    }

    public static class TestDialog extends GuiElement<TestDialog> {

        private GuiPickColourDialog background;
        private GuiPickColourDialog border;
        private GuiPickColourDialog scroll;

        @Override
        public void addChildElements() {
            super.addChildElements();

//            GuiSelectDialog<TextFormatting> dialog = GuiToolkit.createStandardDialog(this, "Test Heading", TextFormatting::name, Arrays.asList(TextFormatting.values()));
//            dialog.setPos(10, 10).setCloseOnOutsideClick(false);
//            dialog.show(600);
//
//            dialog = GuiToolkit.createStandardDialog(this, null, TextFormatting::name, Arrays.asList(TextFormatting.values()));
//            dialog.setPos(110, 10).setCloseOnOutsideClick(false);
//            dialog.show(600);
//
//            dialog = GuiToolkit.createStandardDialog(this, null, TextFormatting::getFriendlyName, Arrays.asList(TextFormatting.values()));
//            dialog.setPos(210, 10).setCloseOnOutsideClick(false);
//            dialog.setYSize(Math.min(dialog.ySize(), 150));
//            dialog.show(600);
//
//            dialog = GuiToolkit.createStandardDialog(this, "Test Heading", TextFormatting::getFriendlyName, Arrays.asList(TextFormatting.values()), 50, 100);
//            dialog.setPos(310, 10).setCloseOnOutsideClick(false);
//            dialog.setYSize(Math.min(dialog.ySize(), 150));
//            dialog.show(600);

            background = new GuiPickColourDialog(this);
            background.setPos(screenWidth / 2 - 90, screenHeight / 2);
            background.setIncludeAlpha(true);
            background.setColour(0xF0100010);
            background.setCloseOnOutsideClick(false);
//            background.show();

            border = new GuiPickColourDialog(this);
            border.setPos(screenWidth / 2 - 180, screenHeight / 2);
            border.setIncludeAlpha(true);
            border.setColour(0x90FFFFFF);
            border.setCloseOnOutsideClick(false);
//            border.show();

            scroll = new GuiPickColourDialog(this);
            scroll.setPos(screenWidth / 2 - 270, screenHeight / 2);
            scroll.setIncludeAlpha(true);
            scroll.setColour(0x90FFFFFF);
            scroll.setCloseOnOutsideClick(false);
//            scroll.show();

            //ff5b5b5b
            //90FFFFFF
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            setPos(screenWidth / 2 + 10, screenHeight / 2).setSize(150, 50);


            int backgroundColor = background.getColourARGB();//0xF0100010;
            int borderColorStart = border.getColourARGB();//0x90FFFFFF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

            //@formatter:off
//            drawGradient(getter, xPos() + 1,           yPos(),               xSize() - 2, 1, backgroundColor, backgroundColor);             // Top
//            drawGradient(getter, xPos() + 1,           yPos() + ySize() - 1, xSize() - 2, 1, backgroundColor, backgroundColor);             // Bottom
//            drawGradient(getter, xPos(),               yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Left
//            drawGradient(getter, xPos() + xSize() - 1, yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Right
//            drawGradient(getter, xPos() + 1,           yPos() + 1,           xSize() - 2, ySize() - 2, backgroundColor, backgroundColor);   // Fill

//            drawGradient(getter, xPos() + 1,           yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Left Accent
//            drawGradient(getter, xPos() + xSize() - 2, yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Right Accent
//            drawGradient(getter, xPos() + 2,           yPos() + 1,           xSize() - 4, 1, borderColorStart, borderColorStart);           // Top Accent
//            drawGradient(getter, xPos() + 2,           yPos() + ySize() - 2, xSize() - 4, 1, borderColorEnd, borderColorEnd);               // Bottom Accent
            //@formatter:on

//            drawGradient(getter, xPos() + 2, yPos() + 12, xSize() - 4, 1, borderColorStart, borderColorStart);           // Heading Divider
//            drawGradient(getter, xPos() + 2,           yPos() + 12,           xSize() - 4, 1, borderColorEnd, borderColorEnd);               // Bottom Accent

//            zOffset++;
//            drawString(fontRenderer, "Show / Hide Inventory", xPos() + 4, yPos() + 4, 0xFFFFFF, true);
//            zOffset--;

//            drawColouredRect(getter, xPos() + xSize() - 7, yPos() + 3, 4, ySize() - 6, 0x30b341ff);
//            drawColouredRect(getter, xPos() + xSize() - 7, yPos() + 7, 4, 18, 0x8cb341ff);
            getter.endBatch();
        }
    }
}
