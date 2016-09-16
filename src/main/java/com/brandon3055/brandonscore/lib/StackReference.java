package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.BCLogHelper;
import com.google.common.base.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 9/09/2016.
 * Stores a reference to an ItemStack
 */
public class StackReference {

    private ResourceLocation stack;
    protected int metadata = 0;
    protected int stackSize = 0;
    protected NBTTagCompound nbt = null;

    public StackReference(String stackRegName, int stackSize, int metadata, NBTTagCompound nbt) {
        this.stack = new ResourceLocation(stackRegName);
        this.metadata = metadata;
        this.stackSize = stackSize;
        this.nbt = nbt;
    }

    public StackReference(String stackRegName, int stackSize, int metadata) {
        this(stackRegName, stackSize, metadata, null);
    }

    public StackReference(String stackRegName, int stackSize) {
        this(stackRegName, stackSize, 0);
    }

    public StackReference(String stackRegName) {
        this(stackRegName, 0);
    }

    public StackReference(ItemStack stack) {
        this(stack.getItem().getRegistryName().toString(), stack.stackSize, stack.getItemDamage(), stack.getTagCompound());
    }

    private StackReference() {}

    @Override
    public int hashCode() {
        return Objects.hashCode(stack, stackSize, metadata, nbt);
    }

    public ItemStack createStack() {
        Item item = Item.REGISTRY.getObject(stack);
        if (item == null) {
            return null;
        }
        else {
            ItemStack itemStack = new ItemStack(item, stackSize, metadata);
            if (nbt != null) {
                itemStack.setTagCompound(nbt.copy());
            }
            return itemStack;
        }
    }

    @Override
    public String toString() {
        return "name:" + stack + ",size:" + stackSize + ",meta:" + metadata + ",nbt:" + (nbt == null ? "{}" : nbt.toString());
    }

    public static StackReference fromString(String string) {
        if (!string.contains("name:") || !string.contains("size:") || !string.contains(",meta:") || !string.contains(",nbt:")) {
            return null;
        }

        try {
            String name = string.substring(5, string.indexOf(",size:"));
            int size = Integer.parseInt(string.substring(string.indexOf(",size:") + 6, string.indexOf(",meta:")));
            int meta = Integer.parseInt(string.substring(string.indexOf(",meta:") + 6, string.indexOf(",nbt:")));
            NBTTagCompound compound = JsonToNBT.getTagFromJson(string.substring(string.indexOf(",nbt:") + 5, string.length()));

            return new StackReference(name, size, meta, compound.hasNoTags() ? null : compound);
        }
        catch (Exception e) {
            BCLogHelper.error("An error occurred while generating a StackReference from a string");
            e.printStackTrace();
            return null;
        }
    }
}
