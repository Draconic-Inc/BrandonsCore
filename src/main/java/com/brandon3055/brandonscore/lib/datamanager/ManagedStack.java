package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 * Will finish this class if i ever find a use for it
 */
public class ManagedStack extends AbstractManagedData {

    private ItemStack value;
    private ItemStack lastValue;
    protected Function<ItemStack, ItemStack> validator = null;

    public ManagedStack(String name, ItemStack defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.lastValue = defaultValue.copy();
    }

    /**
     * Default 0
     */
    public ManagedStack(String name, DataFlags... flags) {
        this(name, ItemStack.EMPTY, flags);
    }

    public ItemStack set(ItemStack value) {
        lastValue = value.copy();
        validate();
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                this.value = value;
                markDirty();
            }
        }

        return this.value;
    }

    public ItemStack get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     */
    public void setValidator(Function<ItemStack, ItemStack> validator) {
        this.validator = validator;
    }

    @Override
    public void validate() {
        if (validator != null) {
            value = validator.apply(value);
        }
    }

    @Override
    public boolean isDirty(boolean reset) {
        if (lastValue != null && !lastValue.equals(value)) {
            if (reset) {
                lastValue = value.copy();
            }
            return true;
        }

        return super.isDirty(reset);
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeItemStack(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readItemStack();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setTag(name, value.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = new ItemStack(compound.getCompoundTag(name));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
