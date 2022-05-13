package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.capability.MultiCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 18/1/21
 */
public interface IEquipmentManager {

    void addEquipCaps(ItemStack stack, MultiCapabilityProvider provider);

    LazyOptional<IItemHandlerModifiable> getInventory(LivingEntity entity);

    ItemStack findMatchingItem(Item item, LivingEntity entity);

    ItemStack findMatchingItem(Predicate<ItemStack> predicate, LivingEntity entity);

    List<ResourceLocation> getSlotIcons(LivingEntity entity);
}
