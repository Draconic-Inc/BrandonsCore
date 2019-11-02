package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.BCTextures;
import com.brandon3055.brandonscore.client.gui.BCGuiToolkit;
import com.brandon3055.brandonscore.client.gui.BCGuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
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
    public GuiButton rsButton;
    public MGuiElementBase playerSlots;
    public GuiEnergyBar energyBar;
    public GuiSlotRender powerSlot;
    public InfoPanel infoPanel;

    private TileBCore tile;
//    private boolean tileTitleOverride = false;
    private BCGuiToolkit toolkit;

    public TBasicMachine(TileBCore tile) {
        this.tile = tile;
    }

    @Override
    public void addElements(IGuiParentElement parent, BCGuiToolkit toolkit) {
        this.toolkit = toolkit;
        parent.addChild(background = toolkit.createBackground(true));

        title = toolkit.createHeading(getTitle(), background, true);//setEnabled(false);
//        if (tile.getDisplayName() != null && !tile.getDisplayName().getFormattedText().isEmpty()) {
//            title.setEnabled(true).setLabelText(tile.getDisplayName().getFormattedText());
//            tileTitleOverride = true;
//        }

        themeButton = toolkit.createThemeButton(background, true);
        themeButton.setRelPosRight(background, -15, 3);

        playerSlots = toolkit.createPlayerSlots(background, 4, true);
        toolkit.placeInside(playerSlots, background, BCGuiToolkit.LayoutPos.BOTTOM_CENTER, 0, -7);

        infoPanel = toolkit.createInfoPanel(background, false);
        infoPanel.setOrigin(() -> new Point(themeButton.xPos(), themeButton.maxYPos()));
        infoPanel.setEnabled(false);

        if (tile instanceof IRSSwitchable) {
            rsButton = toolkit.createRSSwitch(background, (IRSSwitchable) tile);
            rsButton.setXPos(themeButton.xPos());
            rsButton.setYPosMod(() -> infoPanel.isEnabled() ? (infoPanel.origin.get().y + (int) ((1D - InfoPanel.animState) * 10)) : themeButton.maxYPos());
        }

        isInitialized = true;
    }

    public void addEnergyBar(IOPStorage opStorage) {
        if (!isInitialized) {
            throw new RuntimeException("Template must first be initialized by adding it to the BCGuiToolkit!");
        }
        energyBar = toolkit.createEnergyBar(background, opStorage);
        energyBar.setPos(playerSlots.xPos() + 2, background.yPos() + 8);
        energyBar.setXSize(14).setMaxYPos(playerSlots.yPos() - 4, true);
    }

    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = new GuiSlotRender());
        powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());

        if (chargeItem != null) {
            GuiButton powerToggle = new GuiButton();
            powerToggle.setFillColours(0, 0x4000FF00);
            powerToggle.setListener(chargeItem::invert);
            powerToggle.setSize(14, 14);
            powerToggle.setHoverText(element -> I18n.format("gui.bc." + (chargeItem.get() ? "charging" : "discharging") + "_item.txt"));

            GuiTexture toggleTex = new GuiTexture(14, 14, BCTextures.WIDGETS_GENERIC);
            toggleTex.setTexXGetter(() -> !chargeItem.get() ? 33 : 49);
            toggleTex.texV = 13;
            toggleTex.setRelPos(powerToggle, 0, 0);
            powerToggle.addChild(toggleTex);

            powerToggle.setPos(powerSlot.xPos() + 2, powerSlot.yPos() - powerToggle.ySize() - 2);
            background.addChild(powerToggle);
        }
    }

    public void addEnergyItemSlot(boolean chargeItem) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = new GuiSlotRender());
        powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());

        GuiTexture toggleTex = new GuiTexture(14, 14, BCTextures.WIDGETS_GENERIC);
        if (chargeItem) {
            toggleTex.setTexturePos(97, 13);
            toggleTex.setPos(powerSlot.xPos(), powerSlot.yPos() - toggleTex.ySize() - 2);
        }
        else {
            toggleTex.setTexturePos(81, 13);
            toggleTex.setPos(powerSlot.xPos() + 1, powerSlot.yPos() - toggleTex.ySize());
        }

        background.addChild(toggleTex);
    }

    //Will add this back if i need it
//    public void setTitle(String unlocalizedTitle) {
//        setTitle(unlocalizedTitle, false);
//    }
//
//    public void setTitle(String unlocalizedTitle, boolean force) {
//        if (checkInit() && (!tileTitleOverride || force)) {
//            title.setLabelText(I18n.format(unlocalizedTitle));
//            title.setEnabled(true);
//        }
//    }
    private boolean checkInit() {
        if (!isInitialized) {
            LogHelperBC.bigError("Machine template must be initialized before applying any other operations.");
        }
        return isInitialized;
    }

    private String getTitle() {
        return tile.getDisplayName() == null ? "[Invalid name supplied by tile]" : tile.getDisplayName().getFormattedText();
    }
}
