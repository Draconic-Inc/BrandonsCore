package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedVec3D extends AbstractManagedData<Vec3D> {

    private Vec3D value;
    private Vec3D lastValue;
    protected Function<Vec3D, Vec3D> validator = null;

    public ManagedVec3D(String name, Vec3D defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.lastValue = defaultValue.copy();
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedVec3D(String name, DataFlags... flags) {
        this(name, new Vec3D(), flags);
    }

    public Vec3D set(Vec3D value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Vec3D prev = this.value;
            this.value = value;

            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                lastValue = prev.copy();
                markDirty();
                notifyListeners(value);
            }
            else {
                this.value = prev;
            }
        }

        return this.value;
    }

    public Vec3D get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedVec3D setValidator(Function<Vec3D, Vec3D> validator) {
        this.validator = validator;
        return this;
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
        output.writeDouble(value.x);
        output.writeDouble(value.y);
        output.writeDouble(value.z);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = new Vec3D();
        value.x = input.readDouble();
        value.y = input.readDouble();
        value.z = input.readDouble();
        notifyListeners(value);
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(value.x));
        list.appendTag(new NBTTagDouble(value.y));
        list.appendTag(new NBTTagDouble(value.z));
        compound.setTag(name, list);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = new Vec3D();
        if (compound.hasKey(name, 9) && compound.getTagList(name, 9).tagCount() == 3) {
            NBTTagList list = compound.getTagList(name, 9);
            value.x = list.getDoubleAt(0);
            value.x = list.getDoubleAt(1);
            value.x = list.getDoubleAt(2);
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
