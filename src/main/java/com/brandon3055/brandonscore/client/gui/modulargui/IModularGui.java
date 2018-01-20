package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMouseOver;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.gui.GuiScreen;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public interface IModularGui<T extends GuiScreen> extends IMouseOver {

    void addElements(GuiElementManager manager);

    /**
     * @return the ModularGui main gui screen
     */
    T getScreen();

    /**
     * @return the x size of the modular gui
     */
    int xSize();

    /**
     * @return the y size of the modular gui
     */
    int ySize();

    int guiLeft();

    int guiTop();

    GuiElementManager getManager();

    void setZLevel(int zLevel);

    int getZLevel();

    @Override
    default boolean isMouseOver(int mouseX, int mouseY) {
        return GuiHelper.isInRect(guiLeft(), guiTop(), xSize(), ySize(), mouseX, mouseY);
    }
}
