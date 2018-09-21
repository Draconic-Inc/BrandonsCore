package com.brandon3055.projectintelligence.api;

import net.minecraft.client.gui.GuiScreen;

import java.util.Collection;

/**
 * Created by brandon3055 on 24/08/18.
 *
 * Used by the gui doc system for displaying documentation in other mod gui's.
 *
 * This can be used to define what documentation pages apply to a specific instance of a GUI class.
 * This can be used if for example you register your base GUI class and need to define what pages
 * apply to each instance of that class.
 */
public interface IPageSupplier<T extends GuiScreen> {

    /**
     * Use this to provide a collection of one or more documentation page URIs for the current gui screen instance.
     *
     * @param gui current gui instance
     * @return a collection containing at least 1 page URI
     * @since PI 1.0.0
     */
    Collection<String> getPageURIs(T gui);

    /**
     * Used to define weather or not this particular gui instance has documentation page(s) associated with it.
     *
     * @param gui current gui instance
     * @return true if this gui instance has documentation pages.
     * @since PI 1.0.0
     */
    default boolean hasPages(T gui) {
        return true;
    }
}
