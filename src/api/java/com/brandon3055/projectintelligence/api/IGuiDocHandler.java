package com.brandon3055.projectintelligence.api;

import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public interface IGuiDocHandler<T extends GuiScreen> {

    /**
     * Use this to define the size and position of the "collapsed" pi documentation window. When you click the PI Tab/Button
     * that is usually attached to the top left of a GUI that tab expands into the full size PI documentation window.
     * Use this to define the size and position that Tab/Button. Keep in mind as long as animation is enabled this button
     * will visually expand into the full size PI window and the full size PI window will shrink back into this button.
     * This means you probably want this button on the same side of your UI as the PI window unless you disable animation
     * in which case you can put it anywhere.<br><br>
     *
     * Note: The recommended size is 20x20 unless you have the button disabled in which case the size should be 0x0 so
     * that the window shrinks completely out of existence.
     *
     * @param gui the gui screen instance
     * @return a rectangle defining the size and position of the collapsed documentation window.
     * @since PI 1.0.0
     */
    Rectangle getCollapsedArea(T gui);

    /**
     * Use this to define the size and position of the "expanded" pi documentation window. When pi is toggled between
     * hidden and shown the window will visually expand from the collapsed window area defined by {@link #getCollapsedArea(GuiScreen)}
     * into the full window area defined by this method. And then shrink from this area back to the collapsed area when PI
     * is hidden again. (Unless animation is disabled in which case the window will just snap between the 2 areas)
     *
     * @param gui the gui screen instance
     * @return a rectangle defining the size and position of the expanded documentation window.
     * @since PI 1.0.0
     */
    Rectangle getExpandedArea(T gui);

    /**
     * This method can be used to disable the animated transition when the pi documentation is toggled between hidden and
     * shown.
     *
     * @param gui the gui screen instance
     * @return false to disable the show/hide animation.
     * @since PI 1.0.0
     */
    default boolean enableAnimation(T gui) {
        return true;
    }

    /**
     * This can be used to disable the PI button. If disabled the collapsed PI window will not be clickable and the
     * book icon will not render. If you are disabling the PI button it is recommended that you also define a
     * collapsed window area size of 0x0.
     *
     * Disabling the built in pi button allows you to handle the button implementation yourself. In which case you can
     * use {@link #docDisplayOverride()} to hide/show the documentation using your own button implementation.
     *
     * @param gui the gui screen instance
     * @return false to disable the builtin PI button functionality.
     * @since PI 1.0.0
     */
    default boolean enableButton(T gui) {
        return true;
    }

    /**
     * This allows you to override the default handling for weather or not the documentation is visible.
     * If defined this completely overrides and disables the built in PI button.
     * This is best used in situations where you want to disable the built in PI button and add your own.
     *
     * @param gui the gui screen instance
     * @return a boolean supplier that can be used to display or hide the documentation.
     * @since PI 1.0.0
     */
    default Supplier<Boolean> docDisplayOverride(T gui) {
        return null;
    }
}
