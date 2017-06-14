package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedString extends AbstractManagedData {

    public String value;
    private String lastTickValue;

    public ManagedString(@Nonnull String value) {
        this.value = this.lastTickValue = value;
    }

    @Override
    public boolean detectChanges() {
        if (!value.equals(lastTickValue)) {
            lastTickValue = value;
            return true;
        }
        return false;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeString(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readString();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setString(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getString(name);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
