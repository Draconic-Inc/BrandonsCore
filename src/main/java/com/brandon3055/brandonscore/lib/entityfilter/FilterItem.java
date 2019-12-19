package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.oredict.OreDictionary;

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
        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getItem();
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
                    int[] ids = OreDictionary.getOreIDs(stack);
                    match = false;
                    for (int id : ids) {
                        if (OreDictionary.getOreName(id).equals(itemName)) {
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
                match = stack.getItemDamage() == damage;
            }
            if (match && nbt != null) {
                //noinspection ConstantConditions
                match = nbt.equals(stack.getTagCompound());
            }

            if (itemName.isEmpty()) {
                if (filterBlocks) {
                    match = stack.getItem() instanceof ItemBlock;
                }
                else if (filterItems) {
                    match = !(stack.getItem() instanceof ItemBlock);
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
        compound.setBoolean("include", whitelistItem);
        compound.setString("name", itemName);
        compound.setShort("count", (short) count);
        compound.setShort("damage", (short) count);
        compound.setBoolean("items", filterItems);
        compound.setBoolean("blocks", filterBlocks);
        if (nbt != null) {
            compound.setTag("nbt", nbt);
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
        if (compound.hasKey("nbt", 10)) {
            nbt = compound.getCompoundTag("nbt");
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
        output.writeNBTTagCompound(nbt);
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
        nbt = input.readNBTTagCompound();
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