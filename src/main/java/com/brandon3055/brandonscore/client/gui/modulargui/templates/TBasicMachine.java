package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/**
 * Created by brandon3055 on 9/7/19.
 * Template for a basic machine with a standardized layout and size.
 */
public class TBasicMachine extends TGuiBase {

    public GuiButton rsButton;
    private TileBCore tile;
    private boolean addPlayerSlots = true;

    public TBasicMachine(Screen gui, TileBCore tile) {
        this(gui, tile, true);
    }

    public TBasicMachine(Screen gui, TileBCore tile, ContainerSlotLayout slotLayout) {
        this(gui, tile, slotLayout, true);
    }

    public TBasicMachine(Screen gui, TileBCore tile, boolean addPlayerSlots) {
        super(gui);
        this.tile = tile;
        this.addPlayerSlots = addPlayerSlots;
    }

    public TBasicMachine(Screen gui, TileBCore tile, ContainerSlotLayout slotLayout, boolean addPlayerSlots) {
        super(gui, slotLayout);
        this.tile = tile;
        this.addPlayerSlots = addPlayerSlots;
    }

    @Override
    public void addElements(IGuiParentElement<?> parent, GuiToolkit<?> toolkit) {
        super.addElements(parent, toolkit);

        if (addPlayerSlots) {
            addPlayerSlots();
        }
    }

    @Override
    public void addDynamicButtons(List<GuiElement<?>> dynamicButtons) {
        super.addDynamicButtons(dynamicButtons);
        if (tile instanceof IRSSwitchable) {
            rsButton = toolkit.createRSSwitch(background, (IRSSwitchable) tile);
            dynamicButtons.add(rsButton);
        }
    }

    @Override
    public String getTitle() {
        return tile == null || tile.getDisplayName() == null ? gui.getTitle().getString() : tile.getDisplayName().getString();
    }
}
