package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedEnum<T extends Enum<T>> extends AbstractManagedData<T> {
    private T value;
    public Map<Integer, T> indexToValue = new HashMap<>();
    public Map<T, Integer> valueToIndex = new HashMap<>();
    protected Function<T, T> validator = null;

    public ManagedEnum(String name, T defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        T[] v = value.getDeclaringClass().getEnumConstants();
        if (v.length > 255) {
            throw new RuntimeException("Max enum size supported by SyncableEnum is 255");
        }
        for (int i = 0; i < v.length; i++) {
            this.indexToValue.put(i, v[i]);
            this.valueToIndex.put(v[i], i);
        }
    }

    public T set(T value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            T prev = this.value;
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

    public T get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedEnum<T> setValidator(Function<T, T> validator) {
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
        output.writeByte(valueToIndex.get(value).byteValue());
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = indexToValue.get(MathHelper.clip(input.readByte() & 0xFF, 0, indexToValue.size() - 1));
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putByte(name, valueToIndex.get(value).byteValue());
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        value = indexToValue.get(MathHelper.clip(compound.getByte(name) & 0xFF, 0, indexToValue.size() - 1));
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
