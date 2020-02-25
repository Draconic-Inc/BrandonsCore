package com.brandon3055.brandonscore.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class ItemHandlerSlotWrapper implements IItemHandlerModifiable {

    private final IItemHandlerModifiable wrapped;
    private final int[] slots;

    public ItemHandlerSlotWrapper(IItemHandlerModifiable wrapped, int[] slots) {
        this.wrapped = wrapped;
        this.slots = slots;
    }

    public ItemHandlerSlotWrapper(IItemHandlerModifiable wrapped, int fslot, int lslot) {
        this.wrapped = wrapped;
        slots = new int[lslot - fslot];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = fslot + i;
        }
    }

    @Override
    public int getSlots() {
        return slots.length;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (checkSlot(slot)) {
            return wrapped.getStackInSlot(slots[slot]);
        }

        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (checkSlot(slot)) {
            return wrapped.insertItem(slots[slot], stack, simulate);
        }

        return stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (checkSlot(slot)) {
            return wrapped.extractItem(slots[slot], amount, simulate);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (checkSlot(slot)) {
            wrapped.setStackInSlot(slots[slot], stack);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (checkSlot(slot)) {
            return wrapped.getSlotLimit(slots[slot]);
        }

        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (checkSlot(slot)) {
            return wrapped.isItemValid(slots[slot], stack);
        }

        return false;
    }

    private boolean checkSlot(int localSlot) {
        return localSlot < slots.length;
    }
}