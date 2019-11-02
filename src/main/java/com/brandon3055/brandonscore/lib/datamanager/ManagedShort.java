package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedShort extends AbstractManagedData<Short> {

    private short value;
    protected Function<Short, Short> validator = null;

    public ManagedShort(String name, short defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default 0
     */
    public ManagedShort(String name, DataFlags... flags) {
        this(name, (short) 0, flags);
    }

    public short set(short value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            short prev = this.value;
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

    public short get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedShort setValidator(Function<Short, Short> validator) {
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
        output.writeShort(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readShort();
        notifyListeners(value);
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setShort(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getShort(name);
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
    public short add(short add) {
        return set((short) (get() + add));
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
    public short subtract(short subtract) {
        return set((short) (get() - subtract));
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
    public short multiply(short multiplyBy) {
        return set((short) (get() * multiplyBy));
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
    public short divide(short divideBy) {
        return set((short) (get() / divideBy));
    }

    /**
     * Reset this data to zero.
     *
     * @return zero.
     */
    public short zero() {
        return set((short) 0);
    }

    /**
     * Increment by 1;
     *
     * @return The new value stored in this data object.
     */
    public short inc() {
        return add((short) 1);
    }

    /**
     * Decrement by 1;
     *
     * @return The new value stored in this data object.
     */
    public short dec() {
        return subtract((short) 1);
    }
}
