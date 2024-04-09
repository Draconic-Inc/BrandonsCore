package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import codechicken.lib.gui.modular.elements.GuiElement;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.geometry.Direction;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.function.Function;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.*;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 09/02/2024
 */
public class ButtonRow extends GuiElement<ButtonRow> {
    private final LinkedList<GuiElement<?>> buttons = new LinkedList<>();
    private GuiElement<?> lastElement;
    private final Direction layoutDirection;
    private double width = 12;
    private double height = 12;
    private double spacing = 0;

    public ButtonRow(@NotNull GuiParent<?> parent, Direction layoutDirection) {
        super(parent);
        this.layoutDirection = layoutDirection;
        this.lastElement = new GuiElement<>(this);
        Constraints.size(lastElement, 0, 0);
        switch (layoutDirection) {
            case UP -> {
                lastElement.constrain(BOTTOM, match(get(BOTTOM))).constrain(LEFT, match(get(LEFT)));
                constrain(WIDTH, dynamic(() -> width));
                constrain(TOP, dynamic(() -> lastElement.getValue(TOP)));
            }
            case LEFT -> {
                lastElement.constrain(RIGHT, match(get(RIGHT))).constrain(TOP, match(get(TOP)));
                constrain(HEIGHT, dynamic(() -> height));
                constrain(LEFT, dynamic(() -> lastElement.getValue(LEFT)));
            }
            case DOWN -> {
                lastElement.constrain(TOP, match(get(TOP))).constrain(LEFT, match(get(LEFT)));
                constrain(WIDTH, dynamic(() -> width));
                constrain(BOTTOM, dynamic(() -> lastElement.getValue(BOTTOM)));
            }
            case RIGHT -> {
                lastElement.constrain(LEFT, match(get(LEFT))).constrain(TOP, match(get(TOP)));
                constrain(HEIGHT, dynamic(() -> height));
                constrain(RIGHT, dynamic(() -> lastElement.getValue(RIGHT)));
            }
        }
    }

    public ButtonRow(@NotNull GuiParent<?> parent) {
        this(parent, Direction.DOWN);
    }

    public ButtonRow setButtonSize(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public ButtonRow setSpacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public void addButton(Function<ButtonRow, GuiElement<?>> buttonFunc) {
        GuiElement<?> button = buttonFunc.apply(this);
        GuiElement<?> finalLast = lastElement;
        boolean first = buttons.isEmpty();
        switch (layoutDirection) {
            case UP -> button
                    .constrain(BOTTOM, relative(finalLast.get(TOP), () -> !first && button.isEnabled() ? -spacing : 0))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(WIDTH, dynamic(() -> width))
                    .constrain(HEIGHT, dynamic(() -> button.isEnabled() ? height : 0));
            case LEFT -> button
                    .constrain(RIGHT, relative(finalLast.get(LEFT), () -> !first && button.isEnabled() ? -spacing : 0))
                    .constrain(TOP, match(get(TOP)))
                    .constrain(HEIGHT, dynamic(() -> height))
                    .constrain(WIDTH, dynamic(() -> button.isEnabled() ? width : 0));
            case DOWN -> button
                    .constrain(TOP, relative(finalLast.get(BOTTOM), () -> !first && button.isEnabled() ? spacing : 0))
                    .constrain(LEFT, match(get(LEFT)))
                    .constrain(WIDTH, dynamic(() -> width))
                    .constrain(HEIGHT, dynamic(() -> button.isEnabled() ? height : 0));
            case RIGHT -> button
                    .constrain(LEFT, relative(finalLast.get(RIGHT), () -> !first && button.isEnabled() ? spacing : 0))
                    .constrain(TOP, match(get(TOP)))
                    .constrain(HEIGHT, dynamic(() -> height))
                    .constrain(WIDTH, dynamic(() -> button.isEnabled() ? width : 0));
        }
        lastElement = button;
        buttons.add(button);
    }

    public static ButtonRow topRightInside(GuiElement<?> parent, Direction layoutDirection, int xOffset, int yOffset) {
        ButtonRow buttonRow = new ButtonRow(parent, layoutDirection);
        Constraints.placeInside(buttonRow, parent, Constraints.LayoutPos.TOP_RIGHT, -xOffset, yOffset);
        return buttonRow;
    }

    public static ButtonRow topLeftInside(GuiElement<?> parent, Direction layoutDirection, int xOffset, int yOffset) {
        ButtonRow buttonRow = new ButtonRow(parent, layoutDirection);
        Constraints.placeInside(buttonRow, parent, Constraints.LayoutPos.TOP_LEFT, xOffset, yOffset);
        return buttonRow;
    }

    public static ButtonRow bottomRightInside(GuiElement<?> parent, Direction layoutDirection, int xOffset, int yOffset) {
        ButtonRow buttonRow = new ButtonRow(parent, layoutDirection);
        Constraints.placeInside(buttonRow, parent, Constraints.LayoutPos.BOTTOM_RIGHT, -xOffset, -yOffset);
        return buttonRow;
    }

    public static ButtonRow bottomLeftInside(GuiElement<?> parent, Direction layoutDirection, int xOffset, int yOffset) {
        ButtonRow buttonRow = new ButtonRow(parent, layoutDirection);
        Constraints.placeInside(buttonRow, parent, Constraints.LayoutPos.BOTTOM_LEFT, xOffset, -yOffset);
        return buttonRow;
    }
}
