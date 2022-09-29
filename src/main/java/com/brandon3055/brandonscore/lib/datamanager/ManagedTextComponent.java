package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedTextComponent extends AbstractManagedData<Component> {

    private Component value;
    protected Function<Component, Component> validator = null;

    public ManagedTextComponent(String name, @Nullable Component defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default "" (Empty String)
     */
    public ManagedTextComponent(String name, DataFlags... flags) {
        this(name, null, flags);
    }

    public Component set(@Nullable Component value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            Component prev = this.value;
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

    @Nullable
    public Component get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedTextComponent setValidator(Function<Component, Component> validator) {
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
        output.writeBoolean(value != null);
        if (value != null) {
            output.writeTextComponent(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (input.readBoolean()){
            value = input.readTextComponent();
        } else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        if (value != null) {
            compound.putString(name, Component.Serializer.toJson(value));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains(name)) {
            value = Component.Serializer.fromJson(compound.getString(name));
        } else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean notNull() {
        return value != null;
    }
}
