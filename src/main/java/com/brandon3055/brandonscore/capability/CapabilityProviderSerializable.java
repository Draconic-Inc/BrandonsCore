package com.brandon3055.brandonscore.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 19/11/2022
 */
public class CapabilityProviderSerializable<T extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {

    protected final Capability<T> capability;
    protected final T instance;
    protected final LazyOptional<T> instanceOpt;

    public CapabilityProviderSerializable(Capability<T> capability, T instance) {
        this.capability = capability;
        this.instance = instance;
        instanceOpt = LazyOptional.of(() -> this.instance);
    }

    @Nonnull
    @Override
    public <R> LazyOptional<R> getCapability(@Nonnull Capability<R> cap, @Nullable Direction side) {
        if (capability == cap) {
            return instanceOpt.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}
