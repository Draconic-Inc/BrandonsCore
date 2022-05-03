package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedVector3i extends AbstractManagedData<Vec3i> {

    private Vec3i value;
    private Vec3i defaultValue;
    protected Function<Vec3i, Vec3i> validator = null;

    public ManagedVector3i(String name, @Nullable Vec3i defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.defaultValue = defaultValue == null ? null : new Vec3i(defaultValue.getX(), defaultValue.getY(), defaultValue.getZ());
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedVector3i(String name, DataFlags... flags) {
        this(name, new Vec3i(0, 0, 0), flags);
    }

    public Vec3i set(Vec3i value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Vec3i prev = this.value;
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
    public Vec3i get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedVector3i setValidator(Function<Vec3i, Vec3i> validator) {
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
    public void toNBT(CompoundTag compound) {
        CompoundTag nbt = new CompoundTag();
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
    public void fromNBT(CompoundTag compound) {
        if (!compound.contains(name, 10)) {
            value = defaultValue == null ? null : new Vec3i(defaultValue.getX(), defaultValue.getY(), defaultValue.getZ());
        } else {
            CompoundTag nbt = compound.getCompound(name);
            if (nbt.contains("null")){
                value = null;
            } else {
                value = new Vec3i(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
            }
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
