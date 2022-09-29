package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */

public class ManagedVector3 extends AbstractManagedData<Vector3> {

    private Vector3 value;
    private Vector3 defaultValue;
    protected Function<Vector3, Vector3> validator = null;

    public ManagedVector3(String name, @Nullable Vector3 defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.defaultValue = defaultValue == null ? null : defaultValue.copy();
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedVector3(String name, DataFlags... flags) {
        this(name, new Vector3(), flags);
    }

    public Vector3 set(Vector3 value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Vector3 prev = this.value;
            this.value = value;

            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                markDirty();
                notifyListeners(value);
            }
            else {
                this.value = prev;
            }
        }

        return this.value;
    }

    @Nullable
    public Vector3 get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedVector3 setValidator(Function<Vector3, Vector3> validator) {
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
    public void toBytes(MCDataOutput output) {
        output.writeBoolean(value != null);
        if (value != null) {
            output.writeVector(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (input.readBoolean()){
            value = input.readVector();
        }else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        CompoundTag nbt = new CompoundTag();
        if (value == null) {
            nbt.putBoolean("null", true);
        } else {
            value.writeToNBT(compound);
        }
        compound.put(name, nbt);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (!compound.contains(name, 10)) {
            value = defaultValue == null ? null : defaultValue.copy();
        } else {
            CompoundTag nbt = compound.getCompound(name);
            if (nbt.contains("null")){
                value = null;
            } else {
                value = Vector3.fromNBT(nbt);
            }
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean notNull() {
        return value != null;
    }
}
