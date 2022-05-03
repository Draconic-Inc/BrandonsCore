package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedBool extends AbstractManagedData<Boolean> {

    private boolean value;
    protected Function<Boolean, Boolean> validator = null;

    public ManagedBool(String name, boolean defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default false
     */
    public ManagedBool(String name, DataFlags... flags) {
        this(name, false, flags);
    }

    public boolean set(boolean value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            boolean prev = this.value;
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

    public boolean get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     */
    public ManagedBool setValidator(Function<Boolean, Boolean> validator) {
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
        output.writeBoolean(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readBoolean();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putBoolean(name, value);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        value = compound.getBoolean(name);
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "=" + value + "]";
    }

    /**
     * Invert the value stored in this {@link ManagedBool} and return the result.
     *
     * @return the nwe stored value.
     */
    public boolean invert() {
        return set(!value);
    }
}
