package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedInt extends AbstractManagedData {

    public int value;
    private int lastTickValue;

    public ManagedInt(int value) {
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
        output.writeInt(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readInt();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setInteger(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getInteger(name);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
