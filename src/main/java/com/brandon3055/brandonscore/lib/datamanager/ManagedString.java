package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedString extends AbstractManagedData {

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
        validate();
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                this.value = value;
                markDirty();
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
     */
    public void setValidator(Function<String, String> validator) {
        this.validator = validator;
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
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setString(name, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        value = compound.getString(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
