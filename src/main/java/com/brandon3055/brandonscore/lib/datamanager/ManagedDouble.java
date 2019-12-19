package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedDouble extends AbstractManagedData<Double> {

    private double value;
    protected Function<Double, Double> validator = null;

    public ManagedDouble(String name, double defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default 0
     */
    public ManagedDouble(String name, DataFlags... flags) {
        this(name, 0D, flags);
    }

    public double set(double value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            double prev = this.value;
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

    public double get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedDouble setValidator(Function<Double, Double> validator) {
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
        output.writeDouble(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readDouble();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundNBT compound) {
        compound.setDouble(name, value);
    }

    @Override
    public void fromNBT(CompoundNBT compound) {
        value = compound.getDouble(name);
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
    public double add(double add) {
        return set(get() + add);
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
    public double subtract(double subtract) {
        return set(get() - subtract);
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
    public double multiply(double multiplyBy) {
        return set(get() * multiplyBy);
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
    public double divide(double divideBy) {
        return set(get() / divideBy);
    }

    /**
     * Reset this data to zero.
     *
     * @return zero.
     */
    public double zero() {
        return set(0);
    }

    /**
     * Increment by 1;
     *
     * @return The new value stored in this data object.
     */
    public double inc() {
        return add(1);
    }

    /**
     * Decrement by 1;
     *
     * @return The new value stored in this data object.
     */
    public double dec() {
        return subtract(1);
    }
}
