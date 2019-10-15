package com.brandon3055.brandonscore.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 13/9/19.
 * 'temStackHandlerExtended'
 */
public class ItemStackHandlerExt extends ItemStackHandler {

    private BiPredicate<Integer, ItemStack> stackValidator = null;
    private Runnable loadListener = null;
    private Consumer<Integer> contentsChangeListener = null;

    public ItemStackHandlerExt() {}

    public ItemStackHandlerExt(int size) {
        super(size);
    }

    public ItemStackHandlerExt(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public void setStackValidator(BiPredicate<Integer, ItemStack> stackValidator) {
        this.stackValidator = stackValidator;
    }

    public void setLoadListener(Runnable loadListener) {
        this.loadListener = loadListener;
    }

    public void setContentsChangeListener(Consumer<Integer> contentsChangeListener) {
        this.contentsChangeListener = contentsChangeListener;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stackValidator == null || stackValidator.test(slot, stack);
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
}

