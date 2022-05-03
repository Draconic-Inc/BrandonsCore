package com.brandon3055.brandonscore.client.utils;

import com.brandon3055.brandonscore.api.TimeKeeper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 29/2/20.
 */
public class CyclingItemGroup extends CreativeModeTab {

    private Supplier<Object[]> iconSupplier;
    private int iconIndex = 0;
    private int lastIndex = 0;
    private int cycleRate = 20;
    private ItemStack icon;
    public int timeOffset = 0;
    private List<ResourceLocation> sortOrder = null;

    public CyclingItemGroup(String label, Supplier<Object[]> iconSupplier) {
        super(label);
        this.iconSupplier = iconSupplier;
    }

    public CyclingItemGroup(String label, int cycleRate, Supplier<Object[]> iconSupplier) {
        super(label);
        this.iconSupplier = iconSupplier;
        this.cycleRate = cycleRate;
    }

    public CyclingItemGroup(String label, Supplier<Object[]> iconSupplier, List<ResourceLocation> sortOrder) {
        super(label);
        this.iconSupplier = iconSupplier;
        this.sortOrder = sortOrder;
    }

    public CyclingItemGroup(String label, int cycleRate, Supplier<Object[]> iconSupplier, List<ResourceLocation> sortOrder) {
        super(label);
        this.iconSupplier = iconSupplier;
        this.cycleRate = cycleRate;
        this.sortOrder = sortOrder;
    }

    @Nonnull
    @Override
    public ItemStack makeIcon() {
        Object[] icons = iconSupplier.get();
        Object obj = icons[iconIndex % icons.length];
        ItemStack stack = obj instanceof ItemStack ? (ItemStack) obj : new ItemStack(obj instanceof Item ? (Item) obj : (Block) obj);
        if (icon == null || !stack.isEmpty()) {
            return stack;
        }
        this.iconIndex++;
        return makeIcon();
    }

    @Override
    public ItemStack getIconItem() {
        int index = ((TimeKeeper.getClientTick() + timeOffset) / cycleRate);
        if (icon == null || index != lastIndex) {
            lastIndex = index;
            this.iconIndex++;
            icon = this.makeIcon();
        }

        return icon;
    }

    public CyclingItemGroup setOffset(int timeOffset) {
        this.timeOffset = timeOffset;
        return this;
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        if (sortOrder != null) {
            NonNullList<ItemStack> sortedItems = NonNullList.create();
            for(Item item : Registry.ITEM) {
                item.fillItemCategory(this, sortedItems);
            }
            sortedItems.sort(Comparator.comparingInt(value -> sortOrder.indexOf(value.getItem().getRegistryName())));
            items.addAll(sortedItems);
        }
        else {
            super.fillItemList(items);
        }
    }
}
