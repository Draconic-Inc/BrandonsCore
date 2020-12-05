package com.brandon3055.brandonscore.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 13/9/19.
 */
public class TileItemStackHandler extends ItemStackHandler {

    private BiPredicate<Integer, ItemStack> stackValidator = null;
    private Runnable loadListener = null;
    private Consumer<Integer> contentsChangeListener = null;
    private Map<Integer, Predicate<ItemStack>> slotValidators = new HashMap<>();
    private ItemStack prevStack = ItemStack.EMPTY;
    private Supplier<Integer> stackLimit = null;


    public TileItemStackHandler() {
    }

    public TileItemStackHandler(int size) {
        super(size);
    }

    public TileItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public void setStackValidator(BiPredicate<Integer, ItemStack> stackValidator) {
        this.stackValidator = stackValidator;
    }

    public void setSlotValidator(int slot, Predicate<ItemStack> validator) {
        slotValidators.put(slot, validator);
    }

    public void setLoadListener(Runnable loadListener) {
        this.loadListener = loadListener;
    }

    public void setContentsChangeListener(Consumer<Integer> contentsChangeListener) {
        this.contentsChangeListener = contentsChangeListener;
    }

    public void setStackLimit(Supplier<Integer> stackLimit) {
        this.stackLimit = stackLimit;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        prevStack = getStackInSlot(slot);
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        prevStack = getStackInSlot(slot);
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slotValidators.containsKey(slot)) {
            return slotValidators.get(slot).test(stack);
        }
        return stackValidator == null || stackValidator.test(slot, stack);
    }

    //I dont want this handler's size to be controlled by NBT
    @Override
    public CompoundNBT serializeNBT() {
        ListNBT nbtTagList = new ListNBT();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("Slot", i);
                stacks.get(i).write(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Items", nbtTagList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        stacks.clear();
        ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.read(itemTags));
            }
        }
        onLoad();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (contentsChangeListener != null) {
            contentsChangeListener.accept(slot);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (loadListener != null) {
            loadListener.run();
        }
    }

    public ItemStack getListenerPrevStack() {
        return prevStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return stackLimit == null ? super.getSlotLimit(slot) : stackLimit.get();
    }
}

