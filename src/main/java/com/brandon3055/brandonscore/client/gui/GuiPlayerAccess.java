package com.brandon3055.brandonscore.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiPopupDialogs;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by brandon3055 on 6/23/2018.
 */
public class GuiPlayerAccess extends ModularGuiContainer<ContainerPlayerAccess> {

    public String name = "";
    public BlockPos pos = new BlockPos(0, 0, 0);
    public int dimension = 0;

    public GuiPlayerAccess(ContainerPlayerAccess container,  PlayerInventory player) {
        super(container, player, new StringTextComponent("Player Access"));
        imageWidth = 220;
        imageHeight = 250;
    }

    @Override
    public void addElements(GuiElementManager manager) {
        GuiTexture bg = GuiTexture.newBCTexture(imageWidth, imageHeight);
        bg.setPos(guiLeft(), guiTop());
        manager.addChild(bg);

        GuiLabel label = new GuiLabel().setDisplaySupplier(() -> name + "'s Inventory");
        label.setSize(bg.xSize(), 10);
        label.setRelPos(8, 8);
        label.setAlignment(GuiAlign.LEFT);
        bg.addChild(label);

        GuiElement accessSlots = buildSlotElement(bg);
        accessSlots.setPos(bg.xPos() + (bg.xSize() / 2) - (accessSlots.xSize() / 2), label.maxYPos() + 2);
        LogHelperBC.dev((accessSlots.xPos() - leftPos)+" "+(accessSlots.yPos() - topPos));

        GuiLabel posLabel = new GuiLabel().setDisplaySupplier(() -> String.format("Pos: " + TextFormatting.BLUE + "X: %s, Y: %s, Z: %s, Dim: %s", pos.getX(), pos.getY(), pos.getZ(), dimension));
        posLabel.setShadow(false).setTextColour(0);
        posLabel.setAlignment(GuiAlign.LEFT);
        posLabel.setSize(accessSlots.xSize(), 12);
        posLabel.setPos(accessSlots.xPos(), accessSlots.maxYPos() + 2);
        bg.addChild(posLabel);

        GuiButton tpToPlayer = new GuiButton("Teleport to player's position");
        tpToPlayer.setVanillaButtonRender(true);
        tpToPlayer.setSize(accessSlots.xSize(), 14);
        tpToPlayer.setPos(accessSlots.xPos(), posLabel.maxYPos() + 3);
        tpToPlayer.onPressed(() -> BCoreNetwork.sendPlayerAccessButton(0));
        bg.addChild(tpToPlayer);

        GuiButton tpPlayerToYou = new GuiButton("Teleport player to your position");
        tpPlayerToYou.setVanillaButtonRender(true);
        tpPlayerToYou.setSize(accessSlots.xSize(), 14);
        tpPlayerToYou.setPos(accessSlots.xPos(), tpToPlayer.maxYPos() + 3);
        tpPlayerToYou.onPressed(() -> BCoreNetwork.sendPlayerAccessButton(1));
        bg.addChild(tpPlayerToYou);

        GuiButton clearInventory = new GuiButton("C").setTrim(false);
        clearInventory.setHoverText("Clear Player's Inventory");
        clearInventory.setVanillaButtonRender(true);
        clearInventory.setSize(18, 18);
        clearInventory.setPos(accessSlots.maxXPos() - 18, accessSlots.yPos());
        clearInventory.onPressed(() -> {
            GuiPopupDialogs.createDialog(clearInventory, GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, TextFormatting.RED + "Are you sure you want to clear " + name + "'s Inventory?\nThis cannot be undone!") //
            .setOkListener(() -> BCoreNetwork.sendPlayerAccessButton(2)) //
            .showCenter();
        });
        bg.addChild(clearInventory);


        GuiLabel label2 = new GuiLabel("Your Inventory");
        label2.setSize(bg.xSize(), 10);
        label2.setPos(label.xPos(), accessSlots.maxYPos() + 60);
        label2.setAlignment(GuiAlign.LEFT);
        bg.addChild(label2);

        GuiElement playerSlots = buildSlotElement(bg);
        playerSlots.setPos(bg.xPos() + (bg.xSize() / 2) - (playerSlots.xSize() / 2), label2.maxYPos() + 2);
        LogHelperBC.dev((playerSlots.xPos() - leftPos)+" "+(playerSlots.yPos() - topPos));
    }

    private GuiElement buildSlotElement(GuiElement bg) {
        GuiElement slotsElement = new GuiElement();
        bg.addChild(slotsElement);

        for (int i = 0; i < 4; i++) {
            slotsElement.addChild(new GuiSlotRender().setRelPos(0, i * 19));
        }

        for (int x = 0; x < 9; x++) {
            slotsElement.addChild(new GuiSlotRender().setRelPos(21 + 18 * x, 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                slotsElement.addChild(new GuiSlotRender().setRelPos(21 + 18 * x, y * 18));
            }
        }

        slotsElement.addChild(new GuiSlotRender().setRelPos(186, 54 + 3));

        slotsElement.setSize(slotsElement.getEnclosingRect());
        return slotsElement;
    }
}
