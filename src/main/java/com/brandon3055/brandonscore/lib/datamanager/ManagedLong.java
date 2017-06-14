package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedLong extends AbstractManagedData {

    public long value;
    public long lastTickValue;

    public ManagedLong(long value) {
        this.value = this.lastTickValue = value;
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
        output.writeLong(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readLong();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setLong(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getLong(name);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
