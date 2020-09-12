package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.api.power.OPStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by brandon3055 on 14/8/19.
 * <p>
 * Operational Potential is the power system used by Draconic Evolution and related mods.
 * This system is an extension of Forge Energy that allows long based power transfer and storage.
 */
public class CapabilityOP {

    @CapabilityInject(IOPStorage.class)
    public static Capability<IOPStorage> OP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IOPStorage.class, new Capability.IStorage<IOPStorage>() {
            @Override
            public INBT writeNBT(Capability<IOPStorage> capability, IOPStorage instance, Direction side) {
                if (instance instanceof INBTSerializable) {
                    return ((INBTSerializable<?>) instance).serializeNBT();
                }
                else {
                    throw new IllegalArgumentException("IOPStorage instance is not serializable!");
                }
            }

            @Override
            public void readNBT(Capability<IOPStorage> capability, IOPStorage instance, Direction side, INBT nbt) {
                if (instance instanceof INBTSerializable) {
                    ((INBTSerializable) instance).deserializeNBT(nbt);
                }
                else {
                    throw new IllegalArgumentException("IOPStorage instance is not serializable!");
                }
            }
        }, () -> new OPStorage(1000));
    }
}
