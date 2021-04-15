package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterItem extends FilterBase {

    protected boolean whitelistItem = true;
    protected String itemName = "";
    protected int count = 0;
    protected int damage = -1;
    protected CompoundNBT nbt = null;
    protected boolean filterBlocks = false;
    protected boolean filterItems = false;
    public boolean dataChanged = false;

    public FilterItem(EntityFilter filter) {
        super(filter);
    }

    public void setWhitelistItem(boolean whitelistItem) {
        boolean prev = this.whitelistItem;
        this.whitelistItem = whitelistItem;
        getFilter().nodeModified(this);
        this.whitelistItem = prev;
    }

    public boolean isWhitelistItem() {
        return whitelistItem;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
        getFilter().nodeModified(this);
    }

    public String getItemName() {
        return itemName;
    }

    public void setCount(int count) {
        this.count = count;
        getFilter().nodeModified(this);
    }

    public int getCount() {
        return count;
    }

    public void setDamage(int damage) {
        this.damage = damage;
        getFilter().nodeModified(this);
    }

    public int getDamage() {
        return damage;
    }

    public void setNbt(CompoundNBT nbt) {
        this.nbt = nbt;
        if (nbt != null && nbt.isEmpty()) {
            this.nbt = null;
        }
        getFilter().nodeModified(this);
    }

    public CompoundNBT getNbt() {
        return nbt;
    }

    public boolean isFilterBlocks() {
        return filterBlocks;
    }

    public void setFilterItemsBlocks(boolean filterItems, boolean filterBlocks) {
        boolean prev = this.filterItems;
        boolean prevBlocks = this.filterBlocks;

        this.filterItems = filterItems;
        this.filterBlocks = filterBlocks;

        getFilter().nodeModified(this);
        this.filterItems = prev;
        this.filterBlocks = prevBlocks;
    }

    public boolean isFilterItems() {
        return filterItems;
    }

    @Override
    public boolean test(Entity entity) {
        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            if (stack.isEmpty()) {
                return !whitelistItem;
            }

            boolean match = true;

            //Check name/oreDict
            if (!itemName.isEmpty()) {
                if (itemName.contains(":")) {
                    String name = Objects.requireNonNull(stack.getItem().getRegistryName()).toString();
                    match = name.equals(itemName);
                }
                else {
                    //TODO Test This
                    Collection<ResourceLocation> tags = ItemTags.getAllTags().getMatchingTags(stack.getItem());
//                    int[] ids = OreDictionary.getOreIDs(stack);
                    match = false;
                    for (ResourceLocation tag : tags) {
                        if (tag.toString().equals(itemName)) {
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (match && count > 0) {
                match = stack.getCount() == count;
            }
            if (match && damage != -1) {
                match = stack.getDamageValue() == damage;
            }
            if (match && nbt != null) {
                match = nbt.equals(stack.getTag());
            }

            if (itemName.isEmpty()) {
                if (filterBlocks) {
                    match = stack.getItem() instanceof BlockItem;
                }
                else if (filterItems) {
                    match = !(stack.getItem() instanceof BlockItem);
                }
            }

            return match == whitelistItem;
        }
        return !whitelistItem;
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM_FILTER;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.putBoolean("include", whitelistItem);
        compound.putString("name", itemName);
        compound.putShort("count", (short) count);
        compound.putShort("damage", (short) count);
        compound.putBoolean("items", filterItems);
        compound.putBoolean("blocks", filterBlocks);
        if (nbt != null) {
            compound.put("nbt", nbt);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        super.deserializeNBT(compound);
        whitelistItem = compound.getBoolean("include");
        itemName = compound.getString("name");
        count = compound.getShort("count");
        damage = compound.getShort("damage");
        filterItems = compound.getBoolean("items");
        filterBlocks = compound.getBoolean("blocks");
        nbt = null;
        if (compound.contains("nbt", 10)) {
            nbt = compound.getCompound("nbt");
        }
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistItem);
        output.writeString(itemName);
        output.writeVarInt(count);
        output.writeVarInt(damage);
        output.writeBoolean(filterItems);
        output.writeBoolean(filterBlocks);
        output.writeCompoundNBT(nbt);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistItem = input.readBoolean();
        itemName = input.readString();
        count = input.readVarInt();
        damage = input.readVarInt();
        filterItems = input.readBoolean();
        filterBlocks = input.readBoolean();
        nbt = input.readCompoundNBT();
        dataChanged = true;
    }
}
/*
 * Include / Exclude Filtered Stacks        (Include Matching Stacks | Ignore Matching Stacks)
 * - ItemType or Ore Dictionary
 * - Count (With Wiled card option)
 * - Item Damage (With Wiled card option)
 *   - Does not apply to ore dict items
 * - NBT (WIll have an enable option Once enabled the current stack nbt will be loaded into a text field where it can be edited by the player)
 *   - Does not apply to ore dict items
 * Include / Exclude Blocks                 (Include Stacks Containing Blocks | Exclude Stacks Containing Blocks)
 *   - Option is disabled if any other options are set.
 */