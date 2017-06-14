package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedVec3D extends AbstractManagedData {

    public Vec3D vec;
    private Vec3D lastTickVec;


    public ManagedVec3D(@Nonnull Vec3D value) {
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
        output.writeDouble(vec.x);
        output.writeDouble(vec.y);
        output.writeDouble(vec.z);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        vec = new Vec3D();
        vec.x = input.readDouble();
        vec.y = input.readDouble();
        vec.z = input.readDouble();
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(vec.x));
        list.appendTag(new NBTTagDouble(vec.y));
        list.appendTag(new NBTTagDouble(vec.z));
        compound.setTag(name, list);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        vec = new Vec3D();
        if (compound.hasKey(name, 9) && compound.getTagList(name, 9).tagCount() == 3) {
            NBTTagList list = compound.getTagList(name, 9);
            vec.x = list.getDoubleAt(0);
            vec.x = list.getDoubleAt(1);
            vec.x = list.getDoubleAt(2);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(vec);
    }
}
