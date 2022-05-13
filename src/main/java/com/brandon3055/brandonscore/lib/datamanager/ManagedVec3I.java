package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.Vec3I;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
@Deprecated
public class ManagedVec3I extends AbstractManagedData<Vec3I> {

    private Vec3I value;
    private Vec3I lastValue;
    protected Function<Vec3I, Vec3I> validator = null;

    public ManagedVec3I(String name, Vec3I defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.lastValue = defaultValue.copy();
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedVec3I(String name, DataFlags... flags) {
        this(name, new Vec3I(), flags);
    }

    public Vec3I set(Vec3I value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Vec3I prev = this.value;
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

    public Vec3I get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedVec3I setValidator(Function<Vec3I, Vec3I> validator) {
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
        output.writeInt(value.x);
        output.writeInt(value.y);
        output.writeInt(value.z);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = new Vec3I();
        value.x = input.readInt();
        value.y = input.readInt();
        value.z = input.readInt();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(value.x));
        list.add(IntTag.valueOf(value.y));
        list.add(IntTag.valueOf(value.z));
        compound.put(name, list);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        value = new Vec3I();
        if (compound.getList(name, 3).size() == 3) {
            ListTag list = compound.getList(name, 3);
            value.x = list.getInt(0);
            value.y = list.getInt(1);
            value.z = list.getInt(2);
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
