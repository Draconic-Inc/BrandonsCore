package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.gui.modular.ModularGui;
import codechicken.lib.gui.modular.elements.*;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.TextState;
import codechicken.lib.gui.modular.lib.geometry.Axis;
import codechicken.lib.gui.modular.lib.geometry.GuiParent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.*;
import static codechicken.lib.gui.modular.lib.geometry.Constraint.match;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 16/02/2024
 */
public class GuiListDialog<T> extends GuiElement<GuiListDialog<T>> {

    protected GuiList<T> list;
    protected GuiSlider scrollBar;
    private boolean closeOnItemClicked = false;
    private boolean closeOnOutsideClick = true;
    private boolean actionOnClick = false;
    private Function<T, String> searchStringFunc = t -> toString();

    public GuiListDialog(ModularGui gui) {
        super(gui.getRoot());
        jeiExclude();
        setOpaque(true);
    }

    public static <C> GuiListDialog<C> create(GuiParent<?> parent) {
        return create(parent, GuiRectangle::toolTipBackground);
    }

    public static <C> GuiListDialog<C> create(GuiParent<?> parent, Function<GuiListDialog<C>, GuiElement<?>> backgroundFunc) {
        GuiListDialog<C> dialog = new GuiListDialog<>(parent.getModularGui());
        Constraints.bind(backgroundFunc.apply(dialog), dialog);

        dialog.list = new GuiList<C>(dialog)
                .setZStacking(false)
                .constrain(TOP, relative(dialog.get(TOP), 3))
                .constrain(LEFT, relative(dialog.get(LEFT), 3))
                .constrain(RIGHT, relative(dialog.get(RIGHT), () -> dialog.list.hiddenSize() > 0 ? -8D : -3D))
                .constrain(BOTTOM, relative(dialog.get(BOTTOM), -15));

        dialog.scrollBar = new GuiSlider(dialog, Axis.Y)
                .setEnabled(() -> dialog.list.hiddenSize() > 0)
                .setSliderState(dialog.list.scrollState())
                .setScrollableElement(dialog.list)
                .constrain(TOP, match(dialog.list.get(TOP)))
                .constrain(LEFT, relative(dialog.list.get(RIGHT), 1))
                .constrain(BOTTOM, match(dialog.list.get(BOTTOM)))
                .constrain(WIDTH, literal(4));

        Constraints.bind(new GuiRectangle(dialog.scrollBar).fill(0x20FFFFFF), dialog.scrollBar);

        dialog.scrollBar
                .installSlider(new GuiRectangle(dialog.scrollBar).fill(0x50FFFFFF))
                .bindSliderLength()
                .bindSliderWidth();

        GuiRectangle searchBg = new GuiRectangle(dialog)
                .fill(0x20FFFFFF)
                .constrain(TOP, relative(dialog.list.get(BOTTOM), 1))
                .constrain(BOTTOM, relative(dialog.get(BOTTOM), -3))
                .constrain(LEFT, relative(dialog.get(LEFT), 3))
                .constrain(RIGHT, relative(dialog.get(RIGHT), -3));

        GuiTextField search = new GuiTextField(searchBg)
                .setSuggestion(Component.translatable("mod_gui.brandonscore.list_dialog.search"))
                .setTextState(TextState.simpleState("", s -> dialog.list.markDirty()));
        Constraints.bind(search, searchBg, 0, 2, 0, 2);

        dialog.list.setFilter(c -> search.getValue().isEmpty() || dialog.searchStringFunc.apply(c).toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)));

        return dialog;
    }

    public static <C> GuiListDialog<C> createNoSearch(GuiParent<?> parent) {
        return createNoSearch(parent, GuiRectangle::toolTipBackground);
    }

    public static <C> GuiListDialog<C> createNoSearch(GuiParent<?> parent, Function<GuiListDialog<C>, GuiElement<?>> backgroundFunc) {
        GuiListDialog<C> dialog = new GuiListDialog<>(parent.getModularGui());
        Constraints.bind(backgroundFunc.apply(dialog), dialog);

        dialog.list = new GuiList<C>(dialog)
                .setZStacking(false)
                .constrain(TOP, relative(dialog.get(TOP), 3))
                .constrain(LEFT, relative(dialog.get(LEFT), 3))
                .constrain(RIGHT, relative(dialog.get(RIGHT), () -> dialog.list.hiddenSize() > 0 ? -8D : -3D))
                .constrain(BOTTOM, relative(dialog.get(BOTTOM), -3));

        dialog.scrollBar = new GuiSlider(dialog, Axis.Y)
                .setEnabled(() -> dialog.list.hiddenSize() > 0)
                .setSliderState(dialog.list.scrollState())
                .setScrollableElement(dialog.list)
                .constrain(TOP, match(dialog.list.get(TOP)))
                .constrain(LEFT, relative(dialog.list.get(RIGHT), 1))
                .constrain(BOTTOM, match(dialog.list.get(BOTTOM)))
                .constrain(WIDTH, literal(4));

        Constraints.bind(new GuiRectangle(dialog.scrollBar).fill(0x20FFFFFF), dialog.scrollBar);

        dialog.scrollBar
                .installSlider(new GuiRectangle(dialog.scrollBar).fill(0x50FFFFFF))
                .bindSliderLength()
                .bindSliderWidth();
        return dialog;
    }

    public GuiList<T> getList() {
        return list;
    }

    public GuiListDialog<T> addItems(Collection<T> items) {
        list.getList().addAll(items);
        list.markDirty();
        return this;
    }

    public GuiListDialog<T> addItems(T... items) {
        return addItems(List.of(items));
    }

    public GuiListDialog<T> setCloseOnItemClicked(boolean closeOnItemClicked) {
        this.closeOnItemClicked = closeOnItemClicked;
        return this;
    }

    public GuiListDialog<T> setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        this.closeOnOutsideClick = closeOnOutsideClick;
        return this;
    }

    public GuiListDialog<T> setSearchStringFunc(Function<T, String> searchStringFunc) {
        this.searchStringFunc = searchStringFunc;
        return this;
    }

    /**
     * By default, the option action fires on mouse button release (like pretty much any UI button in existence)
     * Calling this will change that to action on mouse button press, (What minecraft uses for its buttons)
     */
    public GuiListDialog<T> actionOnClick() {
        this.actionOnClick = true;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean consumed) {
        consumed = super.mouseClicked(mouseX, mouseY, button, consumed);
        if (isMouseOver() || consumed) {
            if (actionOnClick) {
                if (consumed && closeOnItemClicked) {
                    close();
                }
                return true;
            }
        } else if (closeOnOutsideClick) {
            close();
            return true;
        }

        return consumed;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, boolean consumed) {
        consumed = super.mouseReleased(mouseX, mouseY, button, consumed);
        if (isMouseOver() || consumed) {
            if (!actionOnClick) {
                if (consumed && closeOnItemClicked) {
                    close();
                }
                return true;
            }
        }
        return consumed;
    }

    public void close() {
        getParent().removeChild(this);
    }

    public GuiListDialog<T> setNormalizedPos(double x, double y) {
        constrain(LEFT, dynamic(() -> Math.min(Math.max(x, 0), scaledScreenWidth() - xSize())));
        constrain(TOP, dynamic(() -> Math.min(Math.max(y, 0), scaledScreenHeight() - ySize())));
        return this;
    }

    public GuiListDialog<T> placeCenter() {
        constrain(LEFT, dynamic(() -> (scaledScreenWidth() - xSize()) / 2D));
        constrain(TOP, dynamic(() -> (scaledScreenHeight() - ySize()) / 2D));
        return this;
    }
}
