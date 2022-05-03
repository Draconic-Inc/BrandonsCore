package com.brandon3055.brandonscore.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 31/10/19.
 *
 * TODO Can probably make this a bit more feature rich but for now this is all i need.
 */
public class ItemHandlerIOControl implements IItemHandler {
    private IItemHandler itemHandler;
    private InsertCheck insertCheck = null;
    private ExtractCheck extractCheck = null;

    public ItemHandlerIOControl(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public ItemHandlerIOControl setInsertCheck(InsertCheck insertCheck) {
        this.insertCheck = insertCheck;
        return this;
    }

    public ItemHandlerIOControl setExtractCheck(ExtractCheck extractCheck) {
        this.extractCheck = extractCheck;
        return this;
    }

    @Override
    public int getSlots() {
        return itemHandler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (insertCheck != null && !insertCheck.canInsert(slot, stack)) {
            return stack;
        }

        return itemHandler.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (extractCheck != null && !extractCheck.canExtract(slot, itemHandler.getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }

        return itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return itemHandler.isItemValid(slot, stack);
    }

    public interface InsertCheck {
        boolean canInsert(int slot, ItemStack stack);
    }

    public interface ExtractCheck {
        boolean canExtract(int slot, ItemStack stack);
    }
}
