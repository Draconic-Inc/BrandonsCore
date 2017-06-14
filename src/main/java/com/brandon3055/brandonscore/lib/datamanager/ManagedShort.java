package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedShort extends AbstractManagedData {

    public short value;
    private short lastTickValue;

    public ManagedShort(short value) {
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
        output.writeShort(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readShort();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setShort(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getShort(name);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
