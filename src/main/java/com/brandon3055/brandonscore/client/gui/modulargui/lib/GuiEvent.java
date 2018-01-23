package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiPickColourDialog;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSelectDialog;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;

/**
 * Created by brandon3055 on 1/07/2017.
 */
//TODO Add new events as needed
@Deprecated
public class GuiEvent<E extends MGuiElementBase, EVENT extends GuiEvent> {
    private E element;
    /**
     * Added for easier integration with legacy code.
     */
    private String eventString = "";

    public GuiEvent(E element) {
        this.element = element;
    }

    public E getElement() {
        return element;
    }

    @SuppressWarnings("unchecked")
    public EVENT setEventString(String eventString) {
        this.eventString = eventString;
        return (EVENT) this;
    }

    public String getEventString() {
        return eventString;
    }

    //region Event Helpers

    //Button

    /**
     * @return true if this is a button event.
     */
    public boolean isButton() {
        return this instanceof ButtonEvent;
    }

    /**
     * @return this as a ButtonEvent or null if this is not an instance of ButtonEvent.
     */
    public ButtonEvent asButton() {
        return isButton() ? (ButtonEvent) this : null;
    }

    //Selector

    public boolean isSelector() {
        return this instanceof SelectEvent;
    }

    public SelectEvent asSelect() {
        return isSelector() ? (SelectEvent) this : null;
    }

    //Text Field

    public boolean isTextFiled() {
        return this instanceof TextFieldEvent;
    }

    public TextFieldEvent asTextField() {
        return isTextFiled() ? (TextFieldEvent) this : null;
    }

    //Slider

    public boolean isSlider() {
        return this instanceof SliderMoveEvent;
    }

    public SliderMoveEvent asSlider() {
        return isSlider() ? (SliderMoveEvent) this : null;
    }

    //Colour Picker

    public boolean isColour() {
        return this instanceof ColourEvent;
    }

    public ColourEvent asColour() {
        return isColour() ? (ColourEvent) this : null;
    }

    //endregion

    //region Sub Event Classes

    @Deprecated
    public static class ButtonEvent extends GuiEvent<GuiButton, ButtonEvent> {
        public ButtonEvent(GuiButton element) {
            super(element);
        }
    }

    /**
     * Used by selector type elements such as the select dialog.
     */
    @Deprecated
    public static class SelectEvent extends GuiEvent<GuiSelectDialog, SelectEvent> {
        private final Object selected;
        private final MGuiElementBase itemRenderer;

        public SelectEvent(GuiSelectDialog element, Object selected, MGuiElementBase itemRenderer) {
            super(element);
            this.selected = selected;
            this.itemRenderer = itemRenderer;
        }

        public Object getSelectedItem() {
            return selected;
        }

        public MGuiElementBase getSelectedItemRenderer() {
            return itemRenderer;
        }
    }

    @Deprecated
    public static class TextFieldEvent extends GuiEvent<GuiTextField, TextFieldEvent> {
        private final String theText;
        private final boolean textChanged;
        private final boolean enterPressed;

        public TextFieldEvent(GuiTextField element, String theText, boolean textChanged, boolean enterPressed) {
            super(element);
            this.theText = theText;
            this.textChanged = textChanged;
            this.enterPressed = enterPressed;
        }

        public String getText() {
            return theText;
        }

        public boolean textChanged() {
            return textChanged;
        }

        public boolean textEnterPressed() {
            return enterPressed;
        }
    }

    @Deprecated
    public static class SliderMoveEvent extends GuiEvent<GuiSlideControl, SliderMoveEvent> {

        private final double position;
        private final double prevPosition;
        private final boolean isPressed;

        public SliderMoveEvent(GuiSlideControl element, double position, double prevPosition, boolean isPressed) {
            super(element);
            this.position = position;
            this.prevPosition = prevPosition;
            this.isPressed = isPressed;
        }

        /**
         * @return true if the slider is currently pressed as in left click has not yet been released. This event will be fires again when released.
         */
        public boolean sliderPressed() {
            return isPressed;
        }

        /**
         * @return The position the slider is not in.
         */
        public double sliderPosition() {
            return position;
        }

        /**
         * @return The previous position the slider was in.
         */
        public double getPrevPosition() {
            return prevPosition;
        }
    }

    /**
     * Used by elements like the colour picker.
     */
    @Deprecated
    public static class ColourEvent extends GuiEvent<GuiPickColourDialog, ColourEvent> {
        private final int colour;
        private final boolean canceled;
        private final boolean selected;

        public ColourEvent(GuiPickColourDialog element, int colour, boolean canceled, boolean selected) {
            super(element);
            this.colour = colour;
            this.canceled = canceled;
            this.selected = selected;
        }

        public int getColour() {
            return colour;
        }

        public boolean colourCanceled() {
            return canceled;
        }

        public boolean colourPicked() {
            return selected;
        }
    }

    //endregion
}
