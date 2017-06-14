package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedNBT extends AbstractManagedData {

	public NBTTagCompound value;
	private NBTTagCompound lastTickValue;

    public ManagedNBT(@Nonnull NBTTagCompound value) {
        this.value = value;
        this.lastTickValue = value.copy();
    }

    @Override
    public boolean detectChanges() {
        if (value.equals(lastTickValue)) {
            lastTickValue = value.copy();
            return true;
        }
        return false;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeNBTTagCompound(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readNBTTagCompound();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setTag(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getCompoundTag(name);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
