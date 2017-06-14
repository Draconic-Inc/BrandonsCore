package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.Vec3I;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedVec3I extends AbstractManagedData {

    public Vec3I vec;
    private Vec3I lastTickVec;

    public ManagedVec3I(Vec3I value) {
        this.vec = value;
        this.lastTickVec = value.copy();
    }

    @Override
    public boolean detectChanges() {
        if (!vec.equals(lastTickVec)) {
            lastTickVec.set(vec);
            return true;
        }
        return false;
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeInt(vec.x);
        output.writeInt(vec.y);
        output.writeInt(vec.z);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        vec = new Vec3I();
        vec.x = input.readInt();
        vec.y = input.readInt();
        vec.z = input.readInt();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagInt(vec.x));
        list.appendTag(new NBTTagInt(vec.y));
        list.appendTag(new NBTTagInt(vec.z));
        compound.setTag(name, list);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        vec = new Vec3I();
        if (compound.getTagList(name, 3).tagCount() == 3) {
            NBTTagList list = compound.getTagList(name, 9);
            vec.x = list.getIntAt(0);
            vec.x = list.getIntAt(1);
            vec.x = list.getIntAt(2);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(vec);
    }
}
