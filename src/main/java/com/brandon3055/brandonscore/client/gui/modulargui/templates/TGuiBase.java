package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotData;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 9/7/19.
 * This is the base template for DE gui's. This includes most of the basic features common to all DE GUI's Such as
 * - The standard background with light / dark theme selector
 * - Title
 * - Info Panel
 * As well as the base code for adding
 * - Player slots with automatic container slot layout support
 * - Power slots with optional energy flow direction control
 * - Energy bar
 */
public class TGuiBase implements IGuiTemplate {

    private boolean isInitialized = false;
    public GuiElement<?> background;
    public GuiLabel title;
    public GuiButton themeButton;
    public GuiElement<?> playerSlots;
    public GuiEnergyBar energyBar;
    public GuiSlotRender powerSlot;
    public InfoPanel infoPanel;

    protected Screen gui;
    protected GuiToolkit<?> toolkit;
    protected ContainerSlotLayout slotLayout;

    public TGuiBase(Screen gui) {this.gui = gui;}

    public TGuiBase(Screen gui, ContainerSlotLayout slotLayout) {
        this.slotLayout = slotLayout;
    }

    @Override
    public void addElements(IGuiParentElement<?> parent, GuiToolkit<?> toolkit) {
        this.toolkit = toolkit;
        //Background
        if (background == null) {
            parent.addChild(background = toolkit.createBackground(true));
        } else if (!parent.hasChild(background)) {
            parent.addChild(background);
        }

        //Title
        title = toolkit.createHeading(getTitle(), background, true);//setEnabled(false);

        //Theme Button
        themeButton = toolkit.createThemeButton(background);
        themeButton.setRelPosRight(background, -15, 3);

//        //Player Slots
//        playerSlots = toolkit.createPlayerSlots(background, 4, true);
//        toolkit.placeInside(playerSlots, background, GuiToolkit.LayoutPos.BOTTOM_CENTER, 0, -7);

        //Info Panel
        infoPanel = toolkit.createInfoPanel(background, false);
        infoPanel.setOrigin(() -> new Point(themeButton.xPos(), themeButton.maxYPos()));
        infoPanel.setEnabled(false);

        isInitialized = true;
    }

    public void addPlayerSlots() {
        addPlayerSlots(true, false, false);
    }

    public void addPlayerSlots(boolean title, boolean armor, boolean offHand) {
        playerSlots = toolkit.createPlayerSlots(background, title, armor, offHand);
        toolkit.placeInside(playerSlots, background, GuiToolkit.LayoutPos.BOTTOM_CENTER, 0, -7);
    }

    public void addEnergyBar(IOPStorage opStorage, boolean inventoryAligned) {
        checkInit();
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
        GuiTexture bgTexture = new GuiTexture(16, 16, BCSprites.get("slot_energy")).setPos(1, 1);
        powerSlot.addChild(bgTexture);

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
        GuiTexture toggleTex = new GuiTexture(14, 14, BCSprites.get(bellowBar ? "vertical_discharge" : "right_discharge"));
        toggleTex.setPos(powerToggle);

        if (bellowBar) {
            energyBar.translate(2, 0);
            toggleTex.setSize(12, 10);
            powerToggle.setSize(12, 10);
            powerSlot.setPos(energyBar.xPos() - 2, playerSlots.yPos() - powerSlot.ySize() - 6);
        } else {
            powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());
        }

        if (chargeItem != null) {
            toggleTex.setMaterialSupplier(() -> BCSprites.get(!chargeItem.get() ? bellowBar ? "btn_vertical_discharge" : "btn_right_discharge" : bellowBar ? "btn_vertical_charge" : "btn_right_charge"));
            powerToggle.addChild(toggleTex);

            if (bellowBar) {
                powerToggle.setPos(powerSlot.xPos() + 3, powerSlot.yPos() - toggleTex.ySize() - 1);
                energyBar.setMaxYPos(toggleTex.yPos() - 1, true);
            } else {
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
        GuiTexture bgTexture = new GuiTexture(16, 16, BCSprites.get("slots/energy")).setPos(1, 1);
        powerSlot.addChild(bgTexture);

        if (slotData != null) {
            bgTexture.setEnabledCallback(() -> !slotData.slot.getHasStack());
            powerSlot.addPosChangeListener((x, y) -> slotData.setPos(x + 1 - toolkit.guiLeft(), y + 1 - toolkit.guiTop()));
        }
        GuiTexture toggleTex = new GuiTexture(14, 14, BCSprites.get("item_charge/" + ((bellowBar ? "vertical" : "right") + "_" + (chargeItem ? "charge" : "discharge"))));

        if (bellowBar) {
            energyBar.translate(2, 0);
            toggleTex.setSize(12, 10);
            powerSlot.setPos(energyBar.xPos() - 2, playerSlots.yPos() - powerSlot.ySize() - 2);
            toggleTex.setPos(powerSlot.xPos() + 3, powerSlot.yPos() - toggleTex.ySize() - 1);
            energyBar.setMaxYPos(toggleTex.yPos() - 1, true);
        } else {
            powerSlot.setPos(energyBar.maxXPos() + 2, energyBar.maxYPos() - powerSlot.ySize());
            if (chargeItem) {
                toggleTex.setPos(powerSlot.xPos(), powerSlot.yPos() - toggleTex.ySize() - 2);
            } else {
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

    private boolean checkInit() {
        if (!isInitialized) {
            LogHelperBC.bigError("Machine template must be initialized before applying any other operations.");
        }
        return isInitialized;
    }

    protected String getTitle() {
        return gui.getTitle().getString();
    }
}
