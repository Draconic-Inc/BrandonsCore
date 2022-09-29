package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedEnum<T extends Enum<T>> extends AbstractManagedData<T> {

    private T value;
    private Class<T> enumClass;
    public Map<Integer, T> indexToValue = new HashMap<>();
    public Map<T, Integer> valueToIndex = new HashMap<>();
    protected Function<T, T> validator = null;

    public ManagedEnum(String name, @NotNull T defaultValue, DataFlags... flags) {
        this(name, defaultValue.getDeclaringClass(), defaultValue, flags);
    }

    public ManagedEnum(String name, Class<T> enumClass, @Nullable T defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.enumClass = enumClass;
        T[] v = enumClass.getEnumConstants();
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
            } else {
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
        output.writeBoolean(value == null);
        if (value != null) {
            output.writeEnum(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (!input.readBoolean()) {
            value = input.readEnum(enumClass);
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        CompoundTag nbt = new CompoundTag();
        if (value == null) {
            nbt.putBoolean("null", true);
        } else {
            nbt.putByte("value", valueToIndex.get(value).byteValue());
        }
        compound.put(name, nbt);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains(name, 10)) {
            CompoundTag nbt = compound.getCompound(name);
            if (nbt.contains("null")) {
                value = null;
            } else {
                value = indexToValue.get(MathHelper.clip(nbt.getByte("value") & 0xFF, 0, indexToValue.size() - 1));
            }
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "=" + value + "]";
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean notNull() {
        return value != null;
    }
}
