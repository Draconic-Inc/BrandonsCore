package com.brandon3055.brandonscore.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 17/9/19.
 *
 * HANDLER = Handler / Storage / whatever you want to call your capability instance...
 */
public class CapabilityProviderSerializable<HANDLER> implements ICapabilitySerializable<NBTBase> {

    public final HANDLER instance;
    public final Capability<HANDLER> capability;
    public final EnumFacing facing;

    public CapabilityProviderSerializable(Capability<HANDLER> capability, HANDLER instance, @Nullable EnumFacing facing) {
        this.instance = instance;
        this.capability = capability;
        this.facing = facing;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == this.capability;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == this.capability) {
            return this.capability.cast(instance);
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return capability.writeNBT(instance, facing);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        capability.readNBT(instance, facing, nbt);
    }
}
