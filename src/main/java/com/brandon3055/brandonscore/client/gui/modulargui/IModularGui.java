package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.api.IJEIClearance;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMouseOver;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.gui.screen.Screen;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public interface IModularGui<T extends Screen> extends IMouseOver, IJEIClearance {

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

    default GuiElement addElement(GuiElement element) {
        return getManager().addChild(element);
    }

    @Override
    default boolean isMouseOver(int mouseX, int mouseY) {
        return GuiHelper.isInRect(guiLeft(), guiTop(), xSize(), ySize(), mouseX, mouseY);
    }

    @Override
    default List<Rectangle> getGuiExtraAreas() {
        return getManager().getJeiExclusions();
    }

    default List<JEITargetAdapter> getJEIDropTargets() {
        return getManager().getJeiGhostTargets();
    }

    interface JEITargetAdapter extends Consumer<Object> {

        Rectangle getArea();

        @Override
        void accept(Object ingredient);

        default boolean isEnabled() {
            return true;
        }
    }
}
