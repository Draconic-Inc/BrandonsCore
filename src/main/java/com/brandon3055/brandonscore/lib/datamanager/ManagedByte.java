package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedByte extends AbstractManagedData<Byte> {

    private byte value;
    protected Function<Byte, Byte> validator = null;

    public ManagedByte(String name, byte defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default 0
     */
    public ManagedByte(String name, DataFlags... flags) {
        this(name, (byte) 0, flags);
    }

    public byte set(byte value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            byte prev = this.value;
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

    public byte get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     */
    public ManagedByte setValidator(Function<Byte, Byte> validator) {
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
        output.writeByte(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readByte();
        notifyListeners(value);
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setByte(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getByte(name);
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }

    //=============== Helpers ===============

    /**
     * Add to the current value then return the result.
     * New value is automatically stored in this data object.
     * <br>
     * Equivalent to: 'data.value += v'
     *
     * @param add The value to add.
     * @return The new value stored in this data object.
     */
    public byte add(byte add) {
        return set((byte) (get() + add));
    }

    /**
     * Subtract to the current value then return the result.
     * New value is automatically stored in this data object.
     * <br>
     * Equivalent to: 'data.value -= v'
     *
     * @param subtract The value to subtract.
     * @return The new value stored in this data object.
     */
    public byte subtract(byte subtract) {
        return set((byte) (get() - subtract));
    }

    /**
     * Multiply to the current value by this amount then return the result.
     * New value is automatically stored in this data object.
     * <br>
     * Equivalent to: 'data.value *= v'
     *
     * @param multiplyBy The value to multiply by.
     * @return The new value stored in this data object.
     */
    public byte multiply(byte multiplyBy) {
        return set((byte) (get() * multiplyBy));
    }

    /**
     * Divide to the current value by this amount then return the result.
     * New value is automatically stored in this data object.
     * <br>
     * Equivalent to: 'data.value /= v'
     *
     * @param divideBy The value to divide by.
     * @return The new value stored in this data object.
     */
    public byte divide(byte divideBy) {
        return set((byte) (get() / divideBy));
    }

    /**
     * Reset this data to zero.
     *
     * @return zero.
     */
    public byte zero() {
        return set((byte) 0);
    }

    /**
     * Increment by 1;
     *
     * @return The new value stored in this data object.
     */
    public byte inc() {
        return add((byte) 1);
    }

    /**
     * Decrement by 1;
     *
     * @return The new value stored in this data object.
     */
    public byte dec() {
        return subtract((byte) 1);
    }
}
