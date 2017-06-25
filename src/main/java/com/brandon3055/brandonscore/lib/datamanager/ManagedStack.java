package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 12/06/2017.
 * Will finish this class if i ever find a use for it
 */
public class ManagedStack extends AbstractManagedData {

    @Nonnull
	public ItemStack value;
	@Nonnull
    private ItemStack lastTickValue;

    public ManagedStack(@Nonnull ItemStack value) {
        this.value = value;
        this.lastTickValue = value.copy();
    }

    @Override
    public boolean detectChanges() {
        if (value != lastTickValue) {
            lastTickValue = value;
            return true;
        }
        return false;
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
        return String.valueOf(value);
    }
}
