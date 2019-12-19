package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedNBT extends AbstractManagedData<CompoundNBT> {

    private CompoundNBT value;
    private CompoundNBT lastValue;
    protected Function<CompoundNBT, CompoundNBT> validator = null;

    public ManagedNBT(String name, CompoundNBT defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        lastValue = defaultValue.copy();
    }

    /**
     * Default empty {@link CompoundNBT}
     */
    public ManagedNBT(String name, DataFlags... flags) {
        this(name, new CompoundNBT(), flags);
    }

    public CompoundNBT set(CompoundNBT value) {
        lastValue = value.copy();
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            CompoundNBT prev = this.value;
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

    public CompoundNBT get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedNBT setValidator(Function<CompoundNBT, CompoundNBT> validator) {
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
        output.writeNBTTagCompound(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readNBTTagCompound();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundNBT compound) {
        compound.setTag(name, value);
    }

    @Override
    public void fromNBT(CompoundNBT compound) {
        value = compound.getCompoundTag(name);
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
