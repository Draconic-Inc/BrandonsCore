package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedString extends AbstractManagedData<String> {

    private String value;
    protected Function<String, String> validator = null;

    public ManagedString(String name, String defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default "" (Empty String)
     */
    public ManagedString(String name, DataFlags... flags) {
        this(name, "", flags);
    }

    public String set(String value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            String prev = this.value;
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

    public String get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedString setValidator(Function<String, String> validator) {
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
        output.writeString(value);
    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readString();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString(name, value);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        value = compound.getString(name);
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
