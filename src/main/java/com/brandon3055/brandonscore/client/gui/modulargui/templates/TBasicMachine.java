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
    public GuiElement playerSlots;
    public GuiEnergyBar energyBar;
    public GuiSlotRender powerSlot;
    public InfoPanel infoPanel;

    private TileBCore tile;
    private ContainerSlotLayout slotLayout;
    //    private boolean tileTitleOverride = false;
    private GuiToolkit toolkit;

    public TBasicMachine(TileBCore tile) {
        this.tile = tile;
    }

    public TBasicMachine(TileBCore tile, ContainerSlotLayout slotLayout) {
        this.tile = tile;
        this.slotLayout = slotLayout;
    }

    @Override
    public void addElements(IGuiParentElement parent, GuiToolkit toolkit) {
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
        toolkit.placeInside(playerSlots, background, GuiToolkit.LayoutPos.BOTTOM_CENTER, 0, -7);

        infoPanel = toolkit.createInfoPanel(background, false);
        infoPanel.setOrigin(() -> new Point(themeButton.xPos(), themeButton.maxYPos()));
        infoPanel.setEnabled(false);

        if (tile instanceof IRSSwitchable) {
            rsButton = toolkit.createRSSwitch(background, (IRSSwitchable) tile);
            rsButton.setXPos(themeButton.xPos());
            rsButton.setYPosMod(() -> infoPanel.isEnabled() ? (infoPanel.origin.get().y + (int) ((1D - InfoPanel.animState) * 9)) : themeButton.maxYPos());
        }

        isInitialized = true;
    }

    public void addEnergyBar(IOPStorage opStorage, boolean inventoryAligned) {
        if (!isInitialized) {
            throw new RuntimeException("Template must first be initialized by adding it to the BCGuiToolkit!");
        }
        energyBar = toolkit.createEnergyBar(background, opStorage);
        if (inventoryAligned)
            energyBar.setPos(playerSlots.xPos(), background.yPos() + 6);
        else
            energyBar.setPos(background.xPos() + 6, background.yPos() + 6);
        energyBar.setXSize(14).setMaxYPos(playerSlots.yPos() - 4, true);
    }

    public void addEnergyBar(IOPStorage opStorage) {
        addEnergyBar(opStorage, !toolkit.getLayout().isWide());
    }

    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, boolean bellowBar, SlotData slotData) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = new GuiSlotRender());
        if (slotData != null) {
            powerSlot.addPosChangeListener((x, y) -> slotData.setPos(x + 1 - toolkit.guiLeft(), y + 1 - toolkit.guiTop()));
        }
        GuiButton powerToggle = new GuiButton();
        powerToggle.setFillColours(0, 0x4000FF00);
        if (chargeItem != null) {
            powerToggle.onPressed(chargeItem::invert);
        }
        powerToggle.setSize(14, 14);
        powerToggle.setHoverText(element -> I18n.format("gui.bc." + (chargeItem.get() ? "charging" : "discharging") + "_item.txt"));
        GuiTexture toggleTex = new GuiTexture(14, 14, BCTextures.WIDGETS_GENERIC);
        toggleTex.texV = bellowBar ? 1 : 13;
        toggleTex.setPos(powerToggle);

        if (bellowBar) {
            energyBar.translate(2, 0);
            toggleTex.setSize(12, 10);
            powerToggle.setSize(12, 10);
            powerSlot.setPos(energyBar.xPos() - 2, playerSlots.yPos() - powerSlot.ySize() - 6);
        }
        else {
            powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());
        }

        if (chargeItem != null) {
            toggleTex.setTexXGetter(() -> !chargeItem.get() ? bellowBar ? 84 : 33 : bellowBar ? 96 : 49);
            powerToggle.addChild(toggleTex);

            if (bellowBar) {
                powerToggle.setPos(powerSlot.xPos() + 3, powerSlot.yPos() - toggleTex.ySize() - 1);
                energyBar.setMaxYPos(toggleTex.yPos() - 1, true);
            }
            else {
                powerToggle.setPos(powerSlot.xPos() + 2, powerSlot.yPos() - powerToggle.ySize() - 2);
            }
            background.addChild(powerToggle);
        }
    }

    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, boolean bellowBar) {
        addEnergyItemSlot(chargeItem, bellowBar, null);
    }

    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, SlotData slotData) {
        addEnergyItemSlot(chargeItem, toolkit.getLayout().isTall());
    }

    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem) {
        addEnergyItemSlot(chargeItem, null);
    }


    public void addEnergyItemSlot(boolean chargeItem, boolean bellowBar, SlotData slotData) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = new GuiSlotRender());
        GuiTexture bgTexture = new GuiTexture(16, 16, BCTextures.WIDGETS_GENERIC).setTexturePos(112, 12).setPos(1, 1);
        powerSlot.addChild(bgTexture);

        if (slotData != null) {
            bgTexture.setEnabledCallback(() -> !slotData.slot.getHasStack());
            powerSlot.addPosChangeListener((x, y) -> slotData.setPos(x + 1 - toolkit.guiLeft(), y + 1 - toolkit.guiTop()));
        }
        GuiTexture toggleTex = new GuiTexture(14, 14, BCTextures.WIDGETS_GENERIC);

        if (bellowBar) {
            energyBar.translate(2, 0);
            toggleTex.setSize(12, 10);
            powerSlot.setPos(energyBar.xPos() - 2, playerSlots.yPos() - powerSlot.ySize() - 2);
            if (chargeItem)
                toggleTex.setTexturePos(132, 1);
            else
                toggleTex.setTexturePos(120, 1);
            toggleTex.setPos(powerSlot.xPos() + 3, powerSlot.yPos() - toggleTex.ySize() - 1);
            energyBar.setMaxYPos(toggleTex.yPos() - 1, true);
        }
        else {
            powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());
            if (chargeItem) {
                toggleTex.setTexturePos(97, 13);
                toggleTex.setPos(powerSlot.xPos(), powerSlot.yPos() - toggleTex.ySize() - 2);
            }
            else {
                toggleTex.setTexturePos(81, 13);
                toggleTex.setPos(powerSlot.xPos() + 1, powerSlot.yPos() - toggleTex.ySize());
            }
        }
        powerSlot.addChild(toggleTex);
    }

    public void addEnergyItemSlot(boolean chargeItem, boolean bellowBar) {
        addEnergyItemSlot(chargeItem, bellowBar, null);
    }

    public void addEnergyItemSlot(boolean chargeItem) {
        addEnergyItemSlot(chargeItem, null);
    }

    public void addEnergyItemSlot(boolean chargeItem, SlotData slotData) {
        addEnergyItemSlot(chargeItem, toolkit.getLayout().isTall(), slotData);
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
        return tile == null || tile.getDisplayName() == null ? "[Invalid name supplied by tile]" : tile.getDisplayName().getFormattedText();
    }
}
