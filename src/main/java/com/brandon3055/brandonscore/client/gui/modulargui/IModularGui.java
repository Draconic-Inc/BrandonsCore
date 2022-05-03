package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.api.IJEIClearance;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMouseOver;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
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
    default boolean isMouseOver(double mouseX, double mouseY) {
        return GuiHelperOld.isInRect(guiLeft(), guiTop(), xSize(), ySize(), mouseX, mouseY);
    }

    @Override
    default List<Rect2i> getGuiExtraAreas() {
        return getManager().getJeiExclusions();
    }

    default List<JEITargetAdapter> getJEIDropTargets() {
        return getManager().getJeiGhostTargets();
    }

    interface JEITargetAdapter extends Consumer<Object> {

        Rectangle getArea();

        default Rect2i getMCRect() {
            Rectangle rect = getArea();
            return new Rect2i(rect.x, rect.y, rect.width, rect.height);
        }

        @Override
        void accept(Object ingredient);

        default boolean isEnabled() {
            return true;
        }
    }
}
