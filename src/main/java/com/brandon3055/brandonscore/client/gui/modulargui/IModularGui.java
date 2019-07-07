package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMouseOver;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.NotImplementedException;

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

    default void setUISize(int xSize, int ySize) {
        throw new NotImplementedException("setUISize has not been implemented in " + this.getClass().getName());
    }

    int guiLeft();

    int guiTop();

    GuiElementManager getManager();

    void setZLevel(int zLevel);

    int getZLevel();

    default MGuiElementBase addElement(MGuiElementBase element) {
        return getManager().add(element);
    }

    @Override
    default boolean isMouseOver(int mouseX, int mouseY) {
        return GuiHelper.isInRect(guiLeft(), guiTop(), xSize(), ySize(), mouseX, mouseY);
    }
}
