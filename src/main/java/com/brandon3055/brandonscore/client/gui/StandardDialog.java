package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.ThemedElements;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSelectDialog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.YELLOW;

/**
 * Created by brandon3055 on 18/5/20.
 * This will be the standard vanilla-ish style popup dialog used by my mods in 1.15+
 * There are several helpers in {@link GuiToolkit} that can be used to create an instance of this dialog.
 */
public class StandardDialog<T> extends GuiSelectDialog<T> {
    private int maxWidth = 0;
    private int maxHeight = 0;
    private GuiLabel heading;
    private boolean autoHeight = true;
    private boolean autoNormalize = true;
    private int renderHeight;
    private boolean draggable;

    public StandardDialog(GuiElement<?> parent) {
        super(parent);
        setInsets(3, 3, 3, 3);
        setPlayClickSound(true);
        setReloadOnSelection(true);
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        addBackGroundChild(new DialogBackground(() -> heading != null).setPos(this).bindSize(this, false));
        GuiSlideControl scrollBar = getScrollElement().getVerticalScrollBar();
        scrollBar.setBackgroundElement(new DialogBar(true));
        scrollBar.setSliderElement(new DialogBar(false));
        scrollBar.setXSize(5).setInsets(0, 0, 0, 0);
        scrollBar.updateElements();
    }

    @Override
    public void reloadElement() {
        if (autoHeight) {
            int height = sectionElements.values().stream().mapToInt(GuiElement::ySize).sum() + (heading == null ? 6 : 17);
            setYSize(height);
        }
        super.reloadElement();

        if (maxWidth > 0) {
            setXSize(Math.min(xSize(), maxWidth));
        }
        if (maxHeight > 0) {
            setYSize(Math.min(ySize(), maxHeight));
        }
        if (draggable) {
            setDragBar(heading == null ? 3 : 12);
        }
        if (autoNormalize) {
            normalizePosition();
        }
    }

    public StandardDialog<T> setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public StandardDialog<T> setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public StandardDialog<T> setMaxSize(int maxWidth, int maxHeight) {
        setMaxWidth(maxWidth);
        setMaxHeight(maxHeight);
        return this;
    }

    public StandardDialog<T> setHeading(String headingText) {
        return setHeading(() -> headingText);
    }

    public StandardDialog<T> setHeading(Supplier<String> headingSupplier) {
        heading = new GuiLabel(headingSupplier);
        heading.setPos(xPos() + 4, yPos() + 4);
        heading.setYSize(8);
        heading.setXSizeMod(() -> xSize() - 8);
        heading.setAlignment(LEFT);
        addChild(heading);
        setInsets(14, 3, 3, 3);
        return this;
    }

    public StandardDialog<T> setDefaultRenderer(Function<T, String> nameSupplier) {
        setRendererBuilder(e -> {
            GuiLabel label = new GuiLabel(() -> nameSupplier.apply(e));
            label.setResetHoverOnClick(true);
            label.setInsets(0, 2, 0, 2);
            label.setYSize(10);
            label.setTextColour(GRAY, YELLOW);
            label.setAlignment(LEFT);
            GuiToolkit.addHoverHighlight(label, 0, 0, 0, 0, false);
            return label;
        });

        onReload(d -> {
            int height = (d.getItems().size() * 10) + (heading == null ? 6 : 17);
            int width = d.getItems().stream()
                    .map(nameSupplier)
                    .mapToInt(e -> fontRenderer.getStringWidth(e))
                    .max().orElse(50);
            width = Math.max(width, heading == null ? 0 : fontRenderer.getStringWidth(heading.getLabelText())) + (height > d.ySize() ? 15 : 10);
            d.setXSize(width);
        }, false);
        renderHeight = 10;
        if (isVisible()) {
            reloadElement();
        }
        return this;
    }

    public StandardDialog<T> setAutoHeight(boolean autoHeight) {
        this.autoHeight = autoHeight;
        if (isVisible()) {
            reloadElement();
        }
        return this;
    }

    public StandardDialog<T> setAutoNormalize(boolean autoNormalize) {
        this.autoNormalize = autoNormalize;
        return this;
    }

    public StandardDialog<T> setDraggable(boolean draggable) {
        this.draggable = draggable;
        if (isVisible()) {
            reloadElement();
        }
        return this;
    }

    @Override
    public StandardDialog<T> setToolTipHandler(BiConsumer<T, GuiElement<?>> toolTipHandler) {
        return (StandardDialog<T>) super.setToolTipHandler(toolTipHandler);
    }

    @Override
    public StandardDialog<T> addItem(T item) {
        super.addItem(item);
        return this;
    }

    @Override
    public StandardDialog<T> addItem(T item, @Nullable GuiElement<?> itemRenderer) {
        super.addItem(item, itemRenderer);
        if (isVisible()) reloadElement();
        return this;
    }

    @Override
    public StandardDialog<T> addItems(Map<T, GuiElement<?>> itemMap) {
        super.addItems(itemMap);
        if (isVisible()) reloadElement();
        return this;
    }

    @Override
    public StandardDialog<T> addItems(Collection<T> itemMap) {
        super.addItems(itemMap);
        if (isVisible()) reloadElement();
        return this;
    }

    @Override
    public StandardDialog<T> addItemAt(T item, int index, @Nullable GuiElement<?> itemRenderer) {
        super.addItemAt(item, index, itemRenderer);
        if (isVisible()) reloadElement();
        return this;
    }

    @Override
    public StandardDialog<T> removeItem(T item) {
        super.removeItem(item);
        if (isVisible()) reloadElement();
        return this;
    }

    public static class DialogBackground extends GuiElement<DialogBackground> {
        private final Supplier<Boolean> hasHeading;

        public DialogBackground(Supplier<Boolean> hasHeading) {
            this.hasHeading = hasHeading;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            IRenderTypeBuffer.Impl getter = minecraft.getRenderTypeBuffers().getBufferSource();
            int backgroundColor = 0xF0100010;
            int borderColorStart = 0x90FFFFFF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            //@formatter:off
            drawGradient(getter, xPos() + 1,           yPos(),               xSize() - 2, 1, backgroundColor, backgroundColor);             // Top
            drawGradient(getter, xPos() + 1,           yPos() + ySize() - 1, xSize() - 2, 1, backgroundColor, backgroundColor);             // Bottom
            drawGradient(getter, xPos(),               yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Left
            drawGradient(getter, xPos() + xSize() - 1, yPos() + 1,           1,           ySize() - 2, backgroundColor, backgroundColor);   // Right
            drawGradient(getter, xPos() + 1,           yPos() + 1,           xSize() - 2, ySize() - 2, backgroundColor, backgroundColor);   // Fill
            drawGradient(getter, xPos() + 1,           yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Left Accent
            drawGradient(getter, xPos() + xSize() - 2, yPos() + 1,           1,           ySize() - 2, borderColorStart, borderColorEnd);   // Right Accent
            drawGradient(getter, xPos() + 2,           yPos() + 1,           xSize() - 4, 1, borderColorStart, borderColorStart);           // Top Accent
            drawGradient(getter, xPos() + 2,           yPos() + ySize() - 2, xSize() - 4, 1, borderColorEnd, borderColorEnd);               // Bottom Accent
            if (hasHeading.get()) {
                drawGradient(getter, xPos() + 2,       yPos() + 12,           xSize() - 4, 1, borderColorStart, borderColorStart);          // Heading Divider
            }
            //@formatter:on
            getter.finish();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }

    public static class DialogBar extends GuiElement<DialogBar> {
        private boolean background;
        private Supplier<Boolean> dragging = () -> false;

        public DialogBar(boolean background) {
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
            IRenderTypeBuffer.Impl getter = minecraft.getRenderTypeBuffers().getBufferSource();
            if (background && (dragging.get() || isMouseOver(mouseX, mouseY))) {
                drawColouredRect(getter, xPos() + 1, yPos(), xSize() - 1, ySize(), 0x30b341ff);
            } else if (!background) {
                drawColouredRect(getter, xPos() + 1, yPos(), xSize() - 1, ySize(), 0x8cb341ff);
            }
            getter.finish();
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }
}
