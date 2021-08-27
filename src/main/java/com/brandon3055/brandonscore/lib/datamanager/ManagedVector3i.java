package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedVector3i extends AbstractManagedData<Vector3i> {

    private Vector3i value;
    private Vector3i defaultValue;
    protected Function<Vector3i, Vector3i> validator = null;

    public ManagedVector3i(String name, @Nullable Vector3i defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.defaultValue = defaultValue == null ? null : new Vector3i(defaultValue.getX(), defaultValue.getY(), defaultValue.getZ());
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedVector3i(String name, DataFlags... flags) {
        this(name, new Vector3i(0, 0, 0), flags);
    }

    public Vector3i set(Vector3i value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Vector3i prev = this.value;
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
    public Vector3i get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedVector3i setValidator(Function<Vector3i, Vector3i> validator) {
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
            output.writeVec3i(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (input.readBoolean()){
            value = input.readVec3i();
        }else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundNBT compound) {
        CompoundNBT nbt = new CompoundNBT();
        if (value == null) {
            nbt.putBoolean("null", true);
        } else {
            nbt.putInt("x", value.getX());
            nbt.putInt("y", value.getY());
            nbt.putInt("z", value.getZ());
        }
        compound.put(name, nbt);
    }

    @Override
    public void fromNBT(CompoundNBT compound) {
        if (!compound.contains(name, 10)) {
            value = defaultValue == null ? null : new Vector3i(defaultValue.getX(), defaultValue.getY(), defaultValue.getZ());
        } else {
            CompoundNBT nbt = compound.getCompound(name);
            if (nbt.contains("null")){
                value = null;
            } else {
                value = new Vector3i(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
            }
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
