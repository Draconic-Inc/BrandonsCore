package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import net.minecraft.client.gui.screen.Screen;

/**
 * Created by brandon3055 on 9/7/19.
 * Template for a basic machine with a standardized layout and size.
 */
public class TBasicMachine extends TGuiBase {

    public GuiButton rsButton;
    private TileBCore tile;

    public TBasicMachine(Screen gui, TileBCore tile) {
        super(gui);
        this.tile = tile;
    }

    public TBasicMachine(Screen gui, TileBCore tile, ContainerSlotLayout slotLayout) {
        super(gui, slotLayout);
        this.tile = tile;
    }

    @Override
    public void addElements(IGuiParentElement<?> parent, GuiToolkit<?> toolkit) {
        super.addElements(parent, toolkit);

        addPlayerSlots();

        if (tile instanceof IRSSwitchable) {
            rsButton = toolkit.createRSSwitch(background, (IRSSwitchable) tile);
            rsButton.setXPos(themeButton.xPos());
            rsButton.setYPosMod(() -> infoPanel.isEnabled() ? (infoPanel.origin.get().y + (int) ((1D - InfoPanel.animState) * 9)) : themeButton.maxYPos());
        }
    }

    @Override
    protected String getTitle() {
        return tile == null || tile.getDisplayName() == null ? gui.getTitle().getFormattedText() : tile.getDisplayName().getFormattedText();
    }
}
