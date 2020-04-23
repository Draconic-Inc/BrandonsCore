package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotData;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.awt.*;

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
    public void addElements(IGuiParentElement parent, GuiToolkit toolkit) {
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
