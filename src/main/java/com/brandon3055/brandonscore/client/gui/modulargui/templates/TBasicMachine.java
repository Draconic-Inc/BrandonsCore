package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.BCGuiToolkit;
import com.brandon3055.brandonscore.client.gui.BCGuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.resources.I18n;

import java.awt.*;

/**
 * Created by brandon3055 on 9/7/19.
 * Template for a basic machine with a standardized layout and size.
 */
public class TBasicMachine implements IGuiTemplate {

    private boolean isInitialized = false;
    public GuiTexture background;
    public GuiLabel title;
    public GuiButton themeButton;
    public MGuiElementBase playerSlots;
    public GuiEnergyBar energyBar;
    public GuiSlotRender powerSlot;
    public InfoPanel infoPanel;

    private TileBCore tile;
    private boolean tileTitleOverride = false;

    public TBasicMachine(TileBCore tile) {
        this.tile = tile;
    }

    @Override
    public void addElements(IGuiParentElement parent, BCGuiToolkit toolkit) {
        parent.addChild(background = toolkit.createBackground(true));

        title = toolkit.createHeading("", background, true).setEnabled(false);
        if (tile.getDisplayName() != null && !tile.getDisplayName().getFormattedText().isEmpty()) {
            title.setEnabled(true).setLabelText(tile.getDisplayName().getFormattedText());
            tileTitleOverride = true;
        }

        themeButton = toolkit.createThemeButton(background, true);
        themeButton.setRelPosRight(background, -15, 3);

        playerSlots = toolkit.createPlayerSlots(background, 4, true);
        toolkit.placeInside(playerSlots, background, BCGuiToolkit.LayoutPos.BOTTOM_CENTER, 0, -7);

//        if (tile.) TODO only if tile is an energy tile
        background.addChild(powerSlot = new GuiSlotRender());
        powerSlot.setPos(playerSlots.xPos(), background.yPos() + 5);
        powerSlot.setEnabled(false);

        energyBar = toolkit.createEnergyBar(background, 32000);
        energyBar.setPos(playerSlots.xPos() + 2, background.yPos() + 8);
        energyBar.setXSize(14).setMaxYPos(playerSlots.yPos() - 4, true);

        infoPanel = toolkit.createInfoPanel(background, false);
        infoPanel.setOrigin(() -> new Point(themeButton.xPos(), themeButton.maxYPos()));
        infoPanel.setEnabled(false);

        isInitialized = true;
    }

    public void setTitle(String unlocalizedTitle) {
        setTitle(unlocalizedTitle, false);
    }

    public void setTitle(String unlocalizedTitle, boolean force) {
        if (checkInit() && (!tileTitleOverride || force)) {
            title.setLabelText(I18n.format(unlocalizedTitle));
            title.setEnabled(true);
        }
    }

    private boolean checkInit() {
        if (!isInitialized) {
            LogHelperBC.bigError("Machine template must be initialized before applying any other operations.");
        }
        return isInitialized;
    }
}
