package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 17/9/19.
 *
 * HANDLER = Handler / Storage / whatever you want to call your capability instance...
 */
public class OPMultiProvider implements ICapabilityProvider {

    public final LazyOptional<IOPStorage> instance;
    public final Direction facing;

    public OPMultiProvider(LazyOptional<IOPStorage> instance, @Nullable Direction facing) {
        this.instance = instance;
        this.facing = facing;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (facing == null || facing == side) {
            if (cap == CapabilityOP.OP) {
                return instance.cast();
            } else if (cap == CapabilityEnergy.ENERGY) {
                return instance.cast();
            }
        }
        return LazyOptional.empty();
    }
}
