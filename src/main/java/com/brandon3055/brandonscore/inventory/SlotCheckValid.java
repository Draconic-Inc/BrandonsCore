package com.brandon3055.brandonscore.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class SlotCheckValid extends SlotItemHandler {

    public SlotCheckValid(IItemHandler itemHandler, int id, int x, int y) {
        super(itemHandler, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    public static class IInv extends Slot {
        public IInv(IInventory itemHandler, int id, int x, int y) {
            super(itemHandler, id, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }

    }
}
