package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 9/09/2016.
 * Stores a reference to an ItemStack which can be easily converted to and from a string.
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

    private StackReference() {
    }

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
        if (nbt == null) {
            if (metadata == 0) {
                if (stackSize == 1) {
                    return stack.toString();
                }
                else {
                    return stack + "," + stackSize;
                }
            }
            else {
                return stack + "," + stackSize + "," + metadata;
            }
        }
        else {
            return stack + "," + stackSize + "," + metadata + "," + nbt.toString();
        }
//        return "name:" + stack + ",size:" + stackSize + ",meta:" + metadata + ",nbt:" + (nbt == null ? "{}" : nbt.toString());
    }

    /**
     * @param string A stack string from which to generate a new stack reference.
     * @return A new stack reference or null if the string was invalid.<br>
     * Valid format is:<br><br>
     * <p>
     * registry name, stack size, meta, NBT<br><br>
     * <p>
     * This follows the same format as the /give command except spaces are replaced with commas.<br><br>
     * Examples:<br>
     * "minecraft:stone"                <br>
     * "minecraft:stone,64"             <br>
     * "minecraft:stone,64,3"           <br>
     * "minecraft:stone,64,3,{NBT}"     <br>
     */
    public static StackReference fromString(String string) {
        if (string.contains("name:") && string.contains("size:")) {
            return fromStringOld(string);
        }

        String workString = string;
        String splitter = ",";

        String stackString;
        String countString = "";
        String metaString = "";
        String nbt = "";

        //Read Item Registry Name
        if (!workString.contains(splitter)) {
            stackString = workString;
            workString = "";
        }
        else {
            stackString = workString.substring(0, workString.indexOf(splitter));
            workString = workString.substring(workString.indexOf(splitter) + splitter.length());
        }

        if (!stackString.contains(":")) {
            LogHelperBC.warn("StackReference: Was given an invalid stack string. String did not contain \":\" - " + string);
            return null;
        }

        //Read Stack Size
        if (workString.length() > 0) {
            if (!workString.contains(splitter)) {
                countString = workString;
                workString = "";
            }
            else {
                countString = workString.substring(0, workString.indexOf(splitter));
                workString = workString.substring(workString.indexOf(splitter) + splitter.length());
            }
        }
        //Read Stack Meta
        if (workString.length() > 0) {
            if (!workString.contains(splitter)) {
                metaString = workString;
                workString = "";
            }
            else {
                metaString = workString.substring(0, workString.indexOf(splitter));
                workString = workString.substring(workString.indexOf(splitter) + splitter.length());
            }
        }
        //Read Stack NBT
        if (workString.length() > 0) {
            nbt = workString;
        }

        int count = 1;
        int meta = 0;
        NBTTagCompound compound = null;

        if (countString.length() > 0) {
            try {
                count = Integer.parseInt(countString);
            }
            catch (Exception e) {
                LogHelperBC.warn("StackReference: Failed to parse stack size from string - " + countString + " error: " + e.getMessage());
            }
        }
        if (metaString.length() > 0) {
            try {
                meta = Integer.parseInt(metaString);
            }
            catch (Exception e) {
                LogHelperBC.warn("StackReference: Failed to parse stack meta from string - " + metaString + " error: " + e.getMessage());
            }
        }
        if (nbt.length() > 0) {
            try {
                compound = JsonToNBT.getTagFromJson(nbt);
            }
            catch (Exception e) {
                LogHelperBC.warn("StackReference: Failed to parse stack nbt from string - " + nbt + " error: " + e.getMessage());
            }
        }

        return new StackReference(stackString, count, meta, compound);
    }

    private static StackReference fromStringOld(String string) {
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
            LogHelperBC.error("An error occurred while generating a StackReference from a string");
            e.printStackTrace();
            return null;
        }
    }
}
