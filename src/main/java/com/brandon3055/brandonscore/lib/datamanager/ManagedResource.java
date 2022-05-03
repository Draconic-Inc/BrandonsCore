package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedResource extends AbstractManagedData<ResourceLocation> {

    private ResourceLocation value;
    protected Function<ResourceLocation, ResourceLocation> validator = null;

    public ManagedResource(String name, @Nullable ResourceLocation defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
    }

    /**
     * Default "" (Empty String)
     */
    public ManagedResource(String name, DataFlags... flags) {
        this(name, null, flags);
    }

    public ResourceLocation set(@Nullable ResourceLocation value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            ResourceLocation prev = this.value;
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
    public ResourceLocation get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedResource setValidator(Function<ResourceLocation, ResourceLocation> validator) {
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
            output.writeResourceLocation(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (input.readBoolean()){
            value = input.readResourceLocation();
        } else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        if (value != null) {
            compound.putString(name, value.toString());
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (compound.contains(name)) {
            value = new ResourceLocation(compound.getString(name));
        } else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
