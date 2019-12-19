package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 17/9/19.
 *
 * HANDLER = Handler / Storage / whatever you want to call your capability instance...
 */
public class OPMultiProvider implements ICapabilityProvider {

    public final IOPStorage instance;
    public final Direction facing;

    public OPMultiProvider(IOPStorage instance, @Nullable Direction facing) {
        this.instance = instance;
        this.facing = facing;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityOP.OP || capability == CapabilityEnergy.ENERGY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityOP.OP) {
            return CapabilityOP.OP.cast(instance);
        }
        else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(instance);
        }
        return null;
    }
}
