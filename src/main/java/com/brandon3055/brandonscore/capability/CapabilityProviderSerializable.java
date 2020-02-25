//package com.brandon3055.brandonscore.capability;
//
//import net.minecraft.nbt.INBT;
//import net.minecraft.util.Direction;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.ICapabilitySerializable;
//import net.minecraftforge.common.util.LazyOptional;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
///**
// * Created by brandon3055 on 17/9/19.
// *
// * HANDLER = Handler / Storage / whatever you want to call your capability instance...
// */
//public class CapabilityProviderSerializable<HANDLER> implements ICapabilitySerializable<INBT> {
//
//    public final HANDLER instance;
//    public final Capability<HANDLER> capability;
//    public final Direction facing;
//
//    public CapabilityProviderSerializable(Capability<HANDLER> capability, HANDLER instance, @Nullable Direction facing) {
//        this.instance = instance;
//        this.capability = capability;
//        this.facing = facing;
//    }
//
//
//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
//        if (capability == this.capability) {
//            return this.capability.cast(instance);
//        }
//        return null;
//    }
//
//    @Override
//    public INBT serializeNBT() {
//        return capability.writeNBT(instance, facing);
//    }
//
//    @Override
//    public void deserializeNBT(INBT nbt) {
//        capability.readNBT(instance, facing, nbt);
//    }
//}
