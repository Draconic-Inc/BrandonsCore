package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.blocks.TileBCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by brandon3055 on 28/3/2016.
 * Base class for all containers. Handles syncing on syncable objects inside an attached TileBCBase.
 */
public class ContainerBCBase<T extends TileBCore> extends Container {

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
            addSlotToContainer(new SlotCheckValid.IInv(player.inventory, x, posX + 18 * x, posY + 54 + hotbarSpacing));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotCheckValid.IInv(player.inventory, x + y * 9 + 9, posX + 18 * x, posY + y * 18));
            }
        }
        return this;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tile.detectAndSendChangesToListeners(listeners);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if (listener instanceof EntityPlayerMP && tile != null) {
            tile.getDataManager().forcePlayerSync((EntityPlayerMP) listener);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        if (tile.getWorld().getTileEntity(tile.getPos()) != tile)
        {
            return false;
        }
        else
        {
            return player.getDistanceSq((double)tile.getPos().getX() + 0.5D, (double)tile.getPos().getY() + 0.5D, (double)tile.getPos().getZ() + 0.5D) <= 64.0D;
        }
    }


    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        if (tile instanceof IInventory) {
            Slot slot = getSlot(i);

            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                ItemStack result = stack.copy();

                //Transferring from tile to player
                if (i >= 36) {
                    if (!mergeItemStack(stack, 0, 36, false)) {
                        return ItemStack.EMPTY; //Return if failed to merge
                    }
                }
                else {
                    //Transferring from player to tile
                    if (!mergeItemStack(stack, 36, 36 + ((IInventory) tile).getSizeInventory(), false)) {
                        return ItemStack.EMPTY;  //Return if failed to merge
                    }
                }

                if (stack.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                }
                else {
                    slot.onSlotChanged();
                }

                slot.onTake(player, stack);

                return result;
            }
        }
        return ItemStack.EMPTY;
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

    @Override
    @SideOnly(Side.CLIENT)
    public void setAll(List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); ++i) {
            Slot slot = getSlot(i);
            if (slot != null) {
                slot.putStack(stacks.get(i));
            }
        }
    }
}
