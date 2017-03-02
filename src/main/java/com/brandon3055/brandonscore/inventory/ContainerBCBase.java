package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.blocks.TileBCBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 28/3/2016.
 * Base class for all containers. Handles syncing on syncable objects inside an attached TileBCBase.
 */
public class ContainerBCBase<T extends TileBCBase> extends Container {

    /**
     * A reference to the attached tile. This may be null if the container is not attached to a tile
     */
    public T tile;
    protected EntityPlayer player;

    public ContainerBCBase() {
    }

    public ContainerBCBase(T tile) {
        this.tile = tile;
    }

    public ContainerBCBase(EntityPlayer player, T tile) {
        this(tile);
        this.player = player;
    }

    public ContainerBCBase addPlayerSlots(int posX, int posY) {
        return addPlayerSlots(posX, posY, 4);
    }

    public ContainerBCBase addPlayerSlots(int posX, int posY, int hotbarSpacing) {
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new SlotCheckValid(player.inventory, x, posX + 18 * x, posY + 54 + hotbarSpacing));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotCheckValid(player.inventory, x + y * 9 + 9, posX + 18 * x, posY + y * 18));
            }
        }
        return this;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < this.listeners.size(); ++i) {
            IContainerListener icrafting = this.listeners.get(i);
            if (icrafting instanceof EntityPlayerMP && tile != null) {
                tile.detectAndSendChangesToPlayer(false, (EntityPlayerMP) icrafting);
            }
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if (listener instanceof EntityPlayerMP && tile != null) {
            tile.detectAndSendChangesToPlayer(true, (EntityPlayerMP) listener);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if (tile instanceof IInventory) return ((IInventory) tile).isUseableByPlayer(playerIn);
        return tile != null;
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }

    //The following are some safety checks to handle conditions vanilla normally does not have to deal with.

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        Slot slot = this.getSlot(slotID);
        if (slot != null) {
            slot.putStack(stack);
        }
    }

    @Override
    public Slot getSlot(int slotId) {
        if (slotId < inventorySlots.size() && slotId >= 0) {
            return inventorySlots.get(slotId);
        }

        return null;
    }
}
