package com.brandon3055.brandonscore.client.gui;

import codechicken.lib.gui.modular.elements.GuiElement;
import codechicken.lib.gui.modular.elements.GuiRectangle;
import codechicken.lib.gui.modular.elements.GuiText;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.GuiRender;
import codechicken.lib.gui.modular.lib.geometry.Align;
import codechicken.lib.gui.modular.lib.geometry.Borders;
import codechicken.lib.gui.modular.lib.geometry.ConstrainedGeometry;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.*;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.*;

public class InfoPanel extends GuiElement<InfoPanel> {
    private static AtomicBoolean globalExpanded = new AtomicBoolean(false);

    private AtomicBoolean expanded = globalExpanded;
    private double animState = 0; //0 = collapsed, 1 = expanded
    private List<GuiElement<?>> items = new ArrayList<>();
    private GuiElement<?> lastItem;
    private Borders borders = Borders.create(4, 3);
    private double maxWidth = 200;

    protected InfoPanel(@NotNull GuiParent<?> parent) {
        super(parent);
        jeiExclude();
        setInfoPos(InfoPos.RIGHT_TOP, 2);
        animState = expanded() ? 1 : 0;
        setEnabled(() -> expanded() || animState > 0);
        lastItem = new GuiElement<>(this);
        Constraints.size(lastItem, 0, 0);
        lastItem.constrain(TOP, relative(get(TOP), () -> borders.top));
        lastItem.constrain(LEFT, relative(get(LEFT), () -> borders.left));

        constrain(WIDTH, dynamic(() -> (items.stream().mapToDouble(ConstrainedGeometry::xSize).max().orElse(0) + borders.left + borders.right) * animState));
        constrain(HEIGHT, dynamic(() -> (items.stream().mapToDouble(ConstrainedGeometry::ySize).sum() + borders.top + borders.bottom) * animState));
    }

    public static InfoPanel create(GuiElement<?> parent) {
        return create(parent, e -> background(e, 0x100010, 0x0080ff));
    }

    public static InfoPanel create(GuiElement<?> parent, Function<InfoPanel, GuiElement<?>> backgroundFunc) {
        InfoPanel panel = new InfoPanel(parent);
        GuiElement<?> bg = backgroundFunc.apply(panel);
        Constraints.bind(bg, panel);
        return panel;
    }

    /**
     * By default, we use a single static field for all info panels so the panel state persists between all guis.
     * You can override that by setting a custom state holder here.
     */
    public InfoPanel setExpandedStateHolder(AtomicBoolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public double getFadeAlpha() {
        return animState;
    }

    public boolean expanded() {
        return expanded.get();
    }

    public boolean fullExpansion() {
        return expanded() && animState >= 1;
    }

    public void toggleExpanded() {
        expanded.set(!expanded.get());
    }

    public InfoPanel setInfoPos(InfoPos pos, int offset) {
        constrain(TOP, null).constrain(LEFT, null).constrain(BOTTOM, null).constrain(RIGHT, null);
        GuiElement<?> root = getModularGui().getRoot();
        return switch (pos) {
            case RIGHT_TOP -> constrain(TOP, match(root.get(TOP))).constrain(LEFT, relative(root.get(RIGHT), offset));
            case RIGHT_BOTTOM -> constrain(BOTTOM, match(root.get(BOTTOM))).constrain(LEFT, relative(root.get(RIGHT), offset));
            case LEFT_TOP -> constrain(TOP, match(root.get(TOP))).constrain(RIGHT, relative(root.get(LEFT), offset));
            case LEFT_BOTTOM -> constrain(BOTTOM, match(root.get(BOTTOM))).constrain(RIGHT, relative(root.get(LEFT), offset));
        };
    }

    public InfoPanel setBorders(Borders borders) {
        this.borders = borders;
        return this;
    }

    public InfoPanel setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    @Override
    public void tick(double mouseX, double mouseY) {
        super.tick(mouseX, mouseY);
        if (expanded() && animState < 1) {
            animState = Math.min(1, animState + 0.1);
        } else if (!expanded() && animState > 0) {
            animState = Math.max(0, animState - 0.1);
        }
    }

    public InfoPanel labeledValue(Component labelComp,   Supplier<Component> valueSupply) {
        return labeledValue(labelComp, 6, 2, valueSupply);
    }

    public InfoPanel labeledValue(Component labelComp, int valueOffset, int separation, Supplier<Component> valueSupply) {
        GuiElement<?> container = new GuiElement<>(this);
        container.setEnabled(this::fullExpansion);

        GuiText label = new GuiText(container, labelComp)
                .constrain(TOP, match(container.get(TOP)))
                .constrain(LEFT, match(container.get(LEFT)))
                .constrain(WIDTH, dynamic(() -> Math.min(font().width(labelComp), maxWidth)))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();

        GuiText value = new GuiText(container, valueSupply)
                .constrain(TOP, relative(label.get(BOTTOM), separation))
                .constrain(LEFT, relative(container.get(LEFT), valueOffset))
                .constrain(WIDTH, dynamic(() -> Math.min(font().width(valueSupply.get()), maxWidth)))
                .setAlignment(Align.LEFT)
                .setWrap(true)
                .autoHeight();

        container.constrain(WIDTH, dynamic(() -> Math.max(label.xSize(), value.xSize() + valueOffset)));
        container.constrain(HEIGHT, dynamic(() -> label.ySize() + separation + value.ySize() + 1));

        items.add(container);
        container.constrain(TOP, relative(lastItem.get(BOTTOM), 0));
        container.constrain(LEFT, relative(lastItem.get(LEFT), 0));

        lastItem = container;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver()) {
            toggleExpanded();
            mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            return true;
        }
        return false;
    }

//        public InfoPanel addElement(GuiElement<?> element, Dimension preferredSize) {
//            if (elementsDimMap.isEmpty()) {
//                setEnabled(true);
//            }
//            elementsDimMap.put(element, preferredSize);
//            addChild(element);
//            updatePosSize();
//            return this;
//        }

//        public InfoPanel addElement(GuiElement<?> element) {
//            return addElement(element, new Dimension(element.xSize(), element.ySize()));
//        }

//        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, Dimension preferredSize) {
//            GuiLabel label = new GuiLabel().setAlignment(GuiAlign.LEFT);
//            label.setSize(preferredSize.width, preferredSize.height);
//            label.setDisplaySupplier(stringSupplier);
//            addElement(label, preferredSize);
//            return label;
//        }

//        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, int xSize, int ySize) {
//            return addDynamicLabel(stringSupplier, new Dimension(xSize, ySize));
//        }

//        public GuiLabel addDynamicLabel(Supplier<String> stringSupplier, int ySize) {
//            Dimension dimension = new Dimension(fontRenderer.width(stringSupplier.get()), ySize);
//            GuiLabel label = new GuiLabel(stringSupplier) {
//                @Override
//                public boolean onUpdate() {
//                    int lastWidth = dimension.width;
//                    dimension.width = fontRenderer.width(stringSupplier.get());
//
//                    if (dimension.width != lastWidth) {
//                        updatePosSize();
//                    }
//                    return super.onUpdate();
//                }
//            };
//            label.setTrim(false);
//            label.setAlignment(GuiAlign.LEFT);
//            addElement(label, dimension);
//            return label;
//        }

//        public GuiElement<?> addLabeledValue(String labelText, int valueOffset, int lineHeight, Supplier<String> valueSupplier, boolean multiLine) {
//            GuiElement<?> container = new GuiElement<>();
//            GuiLabel label = new GuiLabel(labelText).setAlignment(GuiAlign.LEFT);
//            label.setSize(multiLine ? fontRenderer.width(labelText) : valueOffset, lineHeight);
//            label.setWrap(true);
//            container.addChild(label);
//            String value = valueSupplier.get();
//            int extraHeiht = fontRenderer.lineHeight;
//            if (value.contains("\n")) {
//                String[] strs = value.split("\n");
//                value = "";
//                for (String s : strs)
//                    if (s.length() > value.length()) {
//                        extraHeiht += fontRenderer.lineHeight;
//                        value = s;
//                    }
//            }
//            extraHeiht -= fontRenderer.lineHeight;
//
//            Dimension dimension;
//            if (multiLine) {
//                dimension = new Dimension(Math.max(label.xSize(), valueOffset + fontRenderer.width(value)), (lineHeight * 2) + extraHeiht);
//            } else {
//                dimension = new Dimension(valueOffset + fontRenderer.width(value), lineHeight);
//            }
//
//            GuiLabel valueLabel = new GuiLabel() {
//                @Override
//                public boolean onUpdate() {
//                    int lastWidth = dimension.width;
//                    String value = valueSupplier.get();
//                    if (value.contains("\n")) {
//                        String[] strs = value.split("\n");
//                        value = "";
//                        for (String s : strs) if (s.length() > value.length()) value = s;
//                    }
//                    if (multiLine) {
//                        dimension.width = Math.max(label.xSize(), valueOffset + fontRenderer.width(value));
//                    } else {
//                        dimension.width = valueOffset + fontRenderer.width(value);
//                    }
//
//                    if (dimension.width != lastWidth) {
//                        updatePosSize();
//                    }
//                    setMaxXPos(container.maxXPos(), true);
//                    return super.onUpdate();
//                }
//            };
//            valueLabel.setTrim(false);
//            valueLabel.setAlignment(GuiAlign.LEFT);
//            valueLabel.setDisplaySupplier(valueSupplier);
//            valueLabel.setYSize(lineHeight);
//            valueLabel.setXPos(valueOffset);
//            valueLabel.setYPos(multiLine ? lineHeight : 0);
//            container.addChild(valueLabel);
//
//            addElement(container, dimension);
//            return container;
//        }

    public enum InfoPos {
        RIGHT_TOP,
        RIGHT_BOTTOM,
        LEFT_TOP,
        LEFT_BOTTOM
    }

    public static GuiRectangle background(InfoPanel panel, int background, int border) {
        return new GuiRectangle(panel) {
            @Override
            public void renderBackground(GuiRender render, double mouseX, double mouseY, float partialTicks) {
                int bgColour = background | (int) (0xf0 * panel.getFadeAlpha()) << 24;
                int borderColour = border | (int) (0xB0 * panel.getFadeAlpha()) << 24;
                int borderColourEnd = (borderColour & 0xFEFEFE) >> 1 | borderColour & 0xFF000000;
                render.toolTipBackground(xMin(), yMin(), xSize(), ySize(), bgColour, borderColour, borderColourEnd);
            }
        };
    }


//        public InfoPanel(GuiElement<?> parent, boolean leftSide) {
//            super(parent);
//            this.parent = parent;
//            this.leftSide = leftSide;
//            this.expanded = expandedHolder;
//            this.animState = isExpanded() ? 1 : -0.5;
//            setEnabled(false);

//            if (animState == -0.5) {
//                setHoverText(hoverText);
//            }
//            updatePosSize();
//            setHoverTextDelay(10);
//        }

//        public InfoPanel(GuiElement<?> parent, boolean leftSide) {
//            this(parent, leftSide, globalExpanded);
//        }

//        }

//        @Override
//        public void addChildElements() {
//            super.addChildElements();
//
//            toggleButton = new GuiButton()
//                    .setHoverTextDelay(10)
//                    .setSize(12, 12)
//                    .onPressed(this::toggleExpanded)
//                    .setPosModifiers(() -> getOrigin().x, () -> getOrigin().y)
//                    .setEnabledCallback(() -> origin != null || animState <= 0);
//
//            addHoverHighlight(toggleButton);
//
//            GuiTexture icon = new GuiTexture(12, 12, BCGuiSprites.getter("info_panel"))
//                    .setPosModifiers(() -> getOrigin().x, () -> getOrigin().y);
//
//            toggleButton.addChild(icon);
//            addChild(toggleButton);
//        }

//        @Override
//        public void reloadElement() {
//            super.reloadElement();
//            if (isExpanded()) {
//                updatePosSize();
//            }
//        }

//        public void setOrigin(Supplier<Point> origin) {
//            this.origin = origin;
//        }

//        public Point getOrigin() {
//            if (origin == null) {
//                int xPos = leftSide ? parent.xPos() - xSize() - 2 : parent.maxXPos() + 2;
//                int yPos = parent.yPos() + (leftSide && hasPI ? 25 : 0);
//                return new Point(xPos, yPos);
//            }
//            return origin.get();
//        }


//        @Override
//        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//            boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
//            if (!ret && isMouseOver(mouseX, mouseY)) {
//                toggleExpanded();
//                GuiButton.playGenericClick();
//                return true;
//            }
//
//            return ret;
//        }

//        private void updatePosSize() {
//            Dimension prefBounds = new Dimension();
//            for (Dimension dim : elementsDimMap.values()) {
//                prefBounds.width = Math.max(prefBounds.width, dim.width);
//                prefBounds.height += dim.height;
//            }
//
//            Dimension actSize = prefBounds;
//            int xPos = leftSide ? parent.xPos() - xSize() - 2 : parent.maxXPos() + 2;
//            int yPos = parent.yPos() + (leftSide && hasPI ? 25 : 0);
//            Rectangle bounds = /*new Rectangle(xPos, yPos, prefBounds.width + 6, prefBounds.height + 6);*/new Rectangle(xPos, yPos, actSize.width + 8, actSize.height + 6);
//            Point origin = getOrigin();
//            Rectangle collapsed = new Rectangle(origin.x, origin.y, 12, 12);
//
//            double animState = Math.max(0, this.animState);
//            int sx = (int) MathUtils.map(animState, 0, 1, collapsed.x, bounds.x);
//            int sy = (int) MathUtils.map(animState, 0, 1, collapsed.y, bounds.y);
//            int sw = (int) MathUtils.map(animState, 0, 1, collapsed.width, bounds.width);
//            int sh = (int) MathUtils.map(animState, 0, 1, collapsed.height, bounds.height);
//            if (sx + sw > screenWidth) {
//                sx -= (sx + sw) - screenWidth;
//            }
//            setPosAndSize(sx, sy, sw, sh);
//
//            int y = yPos + 3;
//            for (GuiElement<?> element : elementsDimMap.keySet()) {
//                if (animState >= 1) {
//                    element.setEnabled(true);
//                    element.setPos(xPos() + 4, y);
//                    Dimension dim = elementsDimMap.get(element);
//                    element.setXSize(Math.min(actSize.width, dim.width));
//                    element.setYSize(Math.min((int) (((double) actSize.height / prefBounds.height) * dim.height), dim.height));
//                    y += element.ySize();
//                } else {
//                    element.setEnabled(false);
//                }
//            }
//        }

//        @Override
//        public void renderBackground(GuiRender render, double mouseX, double mouseY, float partialTicks) {
//            toggleButton.renderElement(minecraft, mouseX, mouseY, partialTicks);
//            double fadeAlpha = Math.min(1, ((animState + 0.5) * 2));
//            int col1 = 0x100010 | (int) (0xf0 * fadeAlpha) << 24;
//            int col2 = 0x0080ff | (int) (0xB0 * fadeAlpha) << 24;
//            MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
//            PoseStack poseStack = new PoseStack();
//            GuiHelper.drawHoverRect(getter, poseStack, xPos(), yPos(), xSize(), ySize(), col1, col2, false);
//            getter.endBatch();
//
//
//            for (GuiElement<?> element : childElements) {
//                if (element.isEnabled() && element != toggleButton) {
//                    element.preDraw(minecraft, mouseX, mouseY, partialTicks);
//                    element.renderElement(minecraft, mouseX, mouseY, partialTicks);
//                    element.postDraw(minecraft, mouseX, mouseY, partialTicks);
//                }
//            }
//        }

//        @Override
//        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//            toggleButton.renderElement(minecraft, mouseX, mouseY, partialTicks);
//            double fadeAlpha = Math.min(1, ((animState + 0.5) * 2));
//            int col1 = 0x100010 | (int) (0xf0 * fadeAlpha) << 24;
//            int col2 = 0x0080ff | (int) (0xB0 * fadeAlpha) << 24;
//            MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
//            PoseStack poseStack = new PoseStack();
//            GuiHelper.drawHoverRect(getter, poseStack, xPos(), yPos(), xSize(), ySize(), col1, col2, false);
//            getter.endBatch();
//
//
//            for (GuiElement<?> element : childElements) {
//                if (element.isEnabled() && element != toggleButton) {
//                    element.preDraw(minecraft, mouseX, mouseY, partialTicks);
//                    element.renderElement(minecraft, mouseX, mouseY, partialTicks);
//                    element.postDraw(minecraft, mouseX, mouseY, partialTicks);
//                }
//            }
//        }

//        @Override
//        public boolean onUpdate() {
//            if (isExpanded() && animState < 1) {
//                animState = Math.min(1, animState + 0.2);
//                setHoverTextEnabled(false);
//                updatePosSize();
//            } else if (!isExpanded() && animState > -0.5) {
//                animState = Math.max(-0.5, animState - 0.2);
//                if (animState == -0.5) {
//                    setHoverText(hoverText);
//                }
//                updatePosSize();
//            }
//
//            return super.onUpdate();
//        }
//
//        public void clear() {
//            elementsDimMap.keySet().forEach(this::removeChild);
//            elementsDimMap.clear();
//        }
}