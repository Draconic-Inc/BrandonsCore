package com.brandon3055.projectintelligence.api;

import net.minecraft.client.gui.GuiScreen;

import java.util.Collection;

/**
 * Created by brandon3055 on 20/09/18.
 * This registry is used to bind documentation pages to mod gui's.
 * Take a look at Draconic Evolution PIIntegration for an example implementation.
 */
public interface IGuiDocRegistry {

    /**
     * This method can be used to determine if a gui handler has already been registered for the specified
     * class before adding your own handler.
     *
     * @param guiClass the gui class.
     * @return true if there is a gui handler assigned to this class.
     * @since PI 1.0.0
     */
    boolean hasGuiHandler(Class<? extends GuiScreen> guiClass);

    /**
     * This method can be used to set the GUI handler for the specified class. This will replace any existing handler
     * registered by you or another mod for that class.
     *
     * @param guiClass The class or superclass of the target GUI.
     * @param handler The handler for this gui class.
     * @since PI 1.0.0
     */
    <T extends GuiScreen> void registerGuiHandler(Class<T> guiClass, IGuiDocHandler<T> handler);

    /**
     * This method allows you to assign documentation pages to a specific gui class. It should be noted
     * that this will not apply to subclasses of the target class. For that you need to register an {@link IPageSupplier}
     *
     * @param guiClass The target gui class
     * @param pageURIs one or more page uris to assign to the target class.
     * @since PI 1.0.0
     */
    <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, String... pageURIs);

    /**
     * This method allows you to assign documentation pages to a specific gui class. It should be noted
     * that this will not apply to subclasses of the target class. For that you need to register an {@link IPageSupplier}
     *
     * @param guiClass The target gui class
     * @param pageURIs the page uri's to assign to the target class.
     * @since PI 1.0.0
     */
    <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, Collection<String> pageURIs);

    /**
     * This method can be used to assign documentation pages go a gui class using an {@link IPageSupplier}.
     * The IPageSupplier allows you to supply pages on a per instance basis.<br><br>
     *
     * Note: you can also assign an IPageSupplier to a superclass of your actual target class(es) and use the
     * page supplier to supply the correct page(s) for each gui instance.
     *
     * @param guiClass The class or superclass of the target gui.
     * @param pageSupplier A page supplier for the GUI.
     * @since PI 1.0.0
     */
    <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, IPageSupplier<T> pageSupplier);

}
