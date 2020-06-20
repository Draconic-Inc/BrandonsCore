package com.brandon3055.brandonscore.items;

import com.brandon3055.brandonscore.capability.MultiCapabilityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated //incomplete
public interface EnergyItemImpl extends IForgeItem {

    @Override
    default MultiCapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        MultiCapabilityProvider provider = new MultiCapabilityProvider();
        //
        addAdditionalCapabilities(stack, provider);
        return provider;
    }

    default void addAdditionalCapabilities(ItemStack stack, MultiCapabilityProvider provider) {}
}
