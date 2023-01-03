package com.brandon3055.brandonscore.client.gui.modulargui.templates;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.InfoPanel;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IGuiParentElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiManipulable;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.SlotMover;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
    public GuiElement<?> playerSlots;
    public GuiEnergyBar energyBar;
    public GuiElement<?> powerSlot;
    public boolean makeDraggable = true;
    public int dragZoneHeight = 20;

    protected Screen gui;
    protected GuiToolkit<?> toolkit;
    protected ContainerSlotLayout slotLayout;
    protected GuiManipulable draggable;

    //Dynamic Buttons
    public LinkedList<GuiElement<?>> dynamicButtons = new LinkedList<>();
    public Consumer<List<GuiElement<?>>> buttonPlacer = this::positionDynamicButtons;
    public GuiButton themeButton;
    public InfoPanel infoPanel;
    private Consumer<LinkedList<GuiElement<?>>> buttonListListener;


    public TGuiBase(Screen gui) {this.gui = gui;}

    public TGuiBase(Screen gui, ContainerSlotLayout slotLayout) {
        this.slotLayout = slotLayout;
    }

    @Override
    public void addElements(IGuiParentElement<?> parent, GuiToolkit<?> toolkit) {
        this.toolkit = toolkit;

        //Background
        if (background == null) {
            background = toolkit.createBackground(true);
        }

        if (parent.hasChild(background)) {
            parent.removeChild(background);
        }

        if (makeDraggable) {
            draggable = new GuiManipulable();
            parent.addChild(draggable);
            draggable.addChild(background);
            draggable.bindPosition(background);
            draggable.setSizeModifiers(() -> background.xSize(), () -> background.ySize());
            draggable.setDragBarHeight(dragZoneHeight);
            toolkit.jeiExclude(draggable);
        } else {
            parent.addChild(background);
        }

        //Title
        title = toolkit.createHeading(getTitle(), background, true);//setEnabled(false);

        addDynamicButtons(dynamicButtons);
        if (buttonListListener != null) {
            buttonListListener.accept(dynamicButtons);
        }
        if (buttonPlacer != null) {
            buttonPlacer.accept(dynamicButtons);
        }

        isInitialized = true;
    }

    public void addDynamicButtons(List<GuiElement<?>> dynamicButtons) {
        //Theme Button
        themeButton = toolkit.createThemeButton(background);
        dynamicButtons.add(themeButton);

        //Info Panel
        GuiElement<?> panelPosition = new GuiElement<>().setSize(12, 12);
        background.addChild(panelPosition);
        infoPanel = toolkit.createInfoPanel(background, false);
        background.removeChild(infoPanel);
        panelPosition.addChild(infoPanel);
        infoPanel.setOrigin(() -> new Point(panelPosition.xPos(), panelPosition.yPos()));
        infoPanel.setEnabled(false);
        panelPosition.setEnabledCallback(() -> infoPanel.isEnabled());
        dynamicButtons.add(panelPosition);
    }

    public void dynamicButtonPrePosition(Consumer<LinkedList<GuiElement<?>>> buttonListListener) {
        if (isInitialized) throw new IllegalStateException("dynamicButtonPrePosition must be called before template is loaded with toolkit.loadTemplate");
        this.buttonListListener = buttonListListener;
    }

    public void positionDynamicButtons(List<GuiElement<?>> dynamicButtons) {
        for (int i = 0; i < dynamicButtons.size(); i++) {
            GuiElement<?> button = dynamicButtons.get(i);
            button.setXPos(background.maxXPos() - 15);

            int finalI = i;
            button.setYPosMod(() -> {
               int index = finalI - 1;
               while (index >= 0) {
                   if (dynamicButtons.get(index).isEnabled()) {
                       break;
                   }
                   index--;
               }
               return index < 0 ? background.yPos() + 3 : dynamicButtons.get(index).maxYPos() + 1;
            });
        }
    }

    public void setButtonPlacer(Consumer<List<GuiElement<?>>> buttonPlacer) {
        this.buttonPlacer = buttonPlacer;
    }

    public void addPlayerSlots() {
        addPlayerSlots(true, false, false);
    }

    public void addPlayerSlots(boolean title, boolean armor, boolean offHand) {
        if (playerSlots != null && playerSlots.getParent() != null) {
            playerSlots.getParent().removeChild(playerSlots);
        }
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

    @Deprecated //Needs Fix (Textures broken)
    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, boolean bellowBar, SlotMover slotMover) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = GuiTexture.newSlot());
        GuiTexture bgTexture = new GuiTexture(16, 16, BCGuiSprites.get("slot_energy")).setPos(1, 1);
        powerSlot.addChild(bgTexture);

        if (slotMover != null) {
            powerSlot.addPosChangeListener((x, y) -> slotMover.setPos(x + 1 - toolkit.guiLeft(), y + 1 - toolkit.guiTop()));
        }
        GuiButton powerToggle = new GuiButton();
        powerToggle.setFillColours(0, 0x4000FF00);
        if (chargeItem != null) {
            powerToggle.onPressed(chargeItem::invert);
        }
        powerToggle.setSize(14, 14);
        powerToggle.setHoverText(element -> I18n.get("gui.bc." + (chargeItem.get() ? "charging" : "discharging") + "_item.txt"));
        GuiTexture toggleTex = new GuiTexture(14, 14, BCGuiSprites.get(bellowBar ? "vertical_discharge" : "right_discharge"));
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
            toggleTex.setMaterialSupplier(() -> BCGuiSprites.get(!chargeItem.get() ? bellowBar ? "btn_vertical_discharge" : "btn_right_discharge" : bellowBar ? "btn_vertical_charge" : "btn_right_charge"));
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

    @Deprecated //Needs Fix
    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, boolean bellowBar) {
        addEnergyItemSlot(chargeItem, bellowBar, null);
    }

    @Deprecated //Needs Fix
    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem, SlotMover slotMover) {
        addEnergyItemSlot(chargeItem, toolkit.getLayout().isTall());
    }

    @Deprecated //Needs Fix
    public void addEnergyItemSlot(@Nullable ManagedBool chargeItem) {
        addEnergyItemSlot(chargeItem, null);
    }

    public void addEnergyItemSlot(boolean chargeItem, boolean bellowBar, SlotMover slotMover) {
        if (energyBar == null) {
            throw new RuntimeException("Must call addEnergyBar before you can add an energy item slot!");
        }

        background.addChild(powerSlot = GuiTexture.newSlot());
        GuiTexture bgTexture = new GuiTexture(16, 16, BCGuiSprites.get("slots/energy")).setPos(1, 1);
        powerSlot.addChild(bgTexture);

        if (slotMover != null) {
            bgTexture.setEnabledCallback(() -> !slotMover.slot.hasItem());
            powerSlot.addPosChangeListener((x, y) -> slotMover.setPos(x + 1 - toolkit.guiLeft(), y + 1 - toolkit.guiTop()));
        }
        GuiTexture toggleTex = new GuiTexture(14, 14, BCGuiSprites.get("item_charge/" + ((bellowBar ? "vertical" : "right") + "_" + (chargeItem ? "charge" : "discharge"))));

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

    public void addEnergyItemSlot(boolean chargeItem, SlotMover slotMover) {
        addEnergyItemSlot(chargeItem, toolkit.getLayout().isTall(), slotMover);
    }

    private boolean checkInit() {
        if (!isInitialized) {
            LogHelperBC.bigError("Machine template must be initialized before applying any other operations.");
        }
        return isInitialized;
    }

    public String getTitle() {
        return gui.getTitle().getString();
    }
}
