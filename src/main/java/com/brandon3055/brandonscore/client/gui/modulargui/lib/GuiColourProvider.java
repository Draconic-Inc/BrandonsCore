package com.brandon3055.brandonscore.client.gui.modulargui.lib;

/**
 * Created by brandon3055 on 2/07/2017.
 * Used by modular elements such as buttons for providing things like text colour.
 * Return type will usually be anm int but it is left generic to support things like {@link codechicken.lib.colour.Colour} from CCL
 */
@FunctionalInterface
public interface GuiColourProvider<R> {

    /**
     * @return ether an ARGB or RGB colour value, for text colour it ism generally RGB and for things like element background it is generally ARGB.
     * This is in the case of integer colours of course
     */
    R getColour();

    /**
     * Used where the colour provided may change depending on whether or not the mouse is over the element.
     */
    public static interface HoverColour<R> {

        /**
         * @param hovering true if the mouse is currently over this element.
         * @return ether an ARGB or RGB colour value, for text colour it ism generally RGB and for things like element background it is generally ARGB.
         */
        R getColour(boolean hovering);

    }

    /**
     * Used where the colour provided may change depending on whether or not the mouse is over the element or the element is disabled (such as in the case of buttons).
     */
    public static interface HoverDisableColour<R> {

        /**
         * @param hovering true if the mouse is currently over this element.
         * @param disabled true if this element is disables (only applicable with elements like buttons that can be disabled)
         * @return ether an ARGB or RGB colour value, for text colour it ism generally RGB and for things like element background it is generally ARGB.
         */
        R getColour(boolean hovering, boolean disabled);

    }
}
