package com.brandon3055.brandonscore.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class SlotCheckValid extends SlotItemHandler {

    public SlotCheckValid(IItemHandler itemHandler, int id, int x, int y) {
        super(itemHandler, id, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    public static class IInv extends Slot {
        public IInv(Container itemHandler, int id, int x, int y) {
            super(itemHandler, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getSlotIndex(), stack);
        }

    }
}
