package com.brandon3055.brandonscore.lib;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
 * The format is similar to the minecraft give command but with commas instead of spaces. <br>
 * There is also optional support for forge capabilities. <br>
 * This is primarily used in Project Intelligence and modular GUI. <br><br>
 * <p>
 * Valid Formats:                                               <br>
 * mod_domain:path                                              <br>
 * mod_domain:path,[count]                                      <br>
 * mod_domain:path{NBT}                                         <br>
 * mod_domain:path{NBT},[count]                                 <br>
 * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
 * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
 * <p>
 * Created by brandon3055 on 23/01/2022
 */
public class StringyStacks {
    public static final Logger LOGGER = LogManager.getLogger("StackConverter");

    /**
     * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
     * The format is similar to the minecraft give command but with commas instead of spaces. <br>
     * There is also optional support for forge capabilities. <br>
     * This is primarily used in Project Intelligence and modular GUI. <br><br>
     * <p>
     * Valid Formats:                                               <br>
     * mod_domain:path                                              <br>
     * mod_domain:path,[count]                                      <br>
     * mod_domain:path{NBT}                                         <br>
     * mod_domain:path{NBT},[count]                                 <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
     * <p>
     *
     * @param stackString The stack string
     * @return An item stack. Will default to empty stack if stack string is invalid.
     */
    public static ItemStack fromString(final String stackString) {
        return fromString(stackString, ItemStack.EMPTY);
    }

    /**
     * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
     * The format is similar to the minecraft give command but with commas instead of spaces. <br>
     * There is also optional support for forge capabilities. <br>
     * This is primarily used in Project Intelligence and modular GUI. <br><br>
     * <p>
     * Valid Formats:                                               <br>
     * mod_domain:path                                              <br>
     * mod_domain:path,[count]                                      <br>
     * mod_domain:path{NBT}                                         <br>
     * mod_domain:path{NBT},[count]                                 <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
     * <p>
     *
     * @param stackString           The stack string
     * @param defaultIfInputInvalid The default stack to return iin the event the stack string is invalid. (This can be null)
     * @return An item stack. Will default to defaultIfInputInvalid stack if stack string is invalid.
     */
    public static ItemStack fromString(final String stackString, @Nullable ItemStack defaultIfInputInvalid) {
        if (stackString.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!stackString.contains(":")) {
            LOGGER.warn("StackReference: Was given an invalid stack string. String did not contain \":\" - " + stackString);
            return defaultIfInputInvalid;
        }

        //Check for old legacy format where NBT is placed after count and (now invalid) meta data
        if (stackString.contains("{") && stackString.contains(",") && stackString.indexOf(",") < stackString.indexOf("{")) {
            try {
                return legacyFromString(stackString, defaultIfInputInvalid);
            }
            catch (Throwable e) {
                LOGGER.warn("Failed to parse legacy stack string - " + stackString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        String stackStr = stackString;
        String itemID;
        if (stackStr.contains("{")) {
            itemID = stackStr.substring(0, stackStr.indexOf("{"));
        } else if (stackStr.contains(",")) {
            itemID = stackStr.substring(0, stackStr.indexOf(","));
        } else {
            itemID = stackStr;
        }
        stackStr = stackString.substring(itemID.length());

        CompoundNBT tagNBT = null;
        if (stackStr.startsWith("{")) {
            if (!stackString.contains("}") || stackStr.lastIndexOf("}") < stackString.indexOf("{")) {
                LOGGER.warn("Detected invalid NBT definition on stack string - " + stackString);
                return defaultIfInputInvalid;
            } else {
                String tag = stackStr.substring(0, stackStr.lastIndexOf("}") + 1);
                try {
                    tagNBT = JsonToNBT.parseTag(tag);
                }
                catch (Throwable e) {
                    LOGGER.warn("Failed to parse stack nbt from string - " + tag + ", on stack string - " + stackString + ". error: " + e.getMessage());
                    return defaultIfInputInvalid;
                }
                stackStr = stackString.substring(tag.length());
            }
        }

        int count = 1;
        if (stackStr.startsWith(",") && stackString.length() > 1) {
            try {
                count = Integer.parseInt(stackStr.substring(1));
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack size from string - " + stackStr + ", on stack string - " + stackString + ". error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        CompoundNBT stackNBT = new CompoundNBT();
        stackNBT.putString("id", itemID);
        stackNBT.putByte("Count", (byte) count);
        if (tagNBT != null && !tagNBT.isEmpty()) {
            if (tagNBT.size() == 2 && tagNBT.contains("tag", 10) && tagNBT.contains("ForgeCaps")) {
                CompoundNBT tag = tagNBT.getCompound("tag");
                if (!tag.isEmpty()) {
                    stackNBT.put("tag", tag);
                }
                CompoundNBT caps = tagNBT.getCompound("ForgeCaps");
                if (!caps.isEmpty()) {
                    stackNBT.put("ForgeCaps", caps);
                }
            } else {
                stackNBT.put("tag", tagNBT);
            }
        }

        ItemStack stack = ItemStack.of(stackNBT);
        if (stack == ItemStack.EMPTY) { //Specifically comparing to the EMPTY instance as that instance will be returned if an error occurs while loading the stack from NBT.
            return defaultIfInputInvalid;
        }
        return stack;
    }

    private static ItemStack legacyFromString(String string, @Nullable ItemStack defaultIfInputInvalid) {
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
        } else {
            stackString = workString.substring(0, workString.indexOf(splitter));
            workString = workString.substring(workString.indexOf(splitter) + splitter.length());
        }

        //Read Stack Size
        if (workString.length() > 0) {
            if (!workString.contains(splitter)) {
                countString = workString;
                workString = "";
            } else {
                countString = workString.substring(0, workString.indexOf(splitter));
                workString = workString.substring(workString.indexOf(splitter) + splitter.length());
            }
        }
        //Read Stack Meta
        if (workString.length() > 0) {
            if (!workString.contains(splitter)) {
                metaString = workString;
                workString = "";
            } else {
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
        CompoundNBT compound = null;

        if (countString.length() > 0) {
            try {
                count = Integer.parseInt(countString);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack size from string - " + countString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }
        if (metaString.length() > 0) {
            try {
                meta = Integer.parseInt(metaString);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack meta from string - " + metaString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }
        if (nbt.length() > 0) {
            try {
                compound = JsonToNBT.parseTag(nbt);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack nbt from string - " + nbt + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        ResourceLocation registryName = new ResourceLocation(stackString);
        Item item = ForgeRegistries.ITEMS.getValue(registryName);
        Block block = ForgeRegistries.BLOCKS.getValue(registryName);
        if (item == null && block == Blocks.AIR) {
            return defaultIfInputInvalid;
        } else {
            ItemStack itemStack;
            if (item != null) {
                itemStack = new ItemStack(item, count);
            } else {
                itemStack = new ItemStack(block, count);
            }
            itemStack.setDamageValue(meta);

            if (compound != null) {
                itemStack.setTag(compound);
            }
            return itemStack;
        }
    }

    public static String toString(ItemStack stack, boolean withNBT, boolean withCount, boolean withForgeCaps) {
        if (stack.isEmpty()) {
            return "";
        }

        String stackString = stack.getItem().getRegistryName().toString();

        if (withNBT || withForgeCaps) {
            CompoundNBT stackTag = stack.serializeNBT();
            CompoundNBT nbt = null;
            CompoundNBT caps = null;
            if (withNBT && stackTag.contains("tag", 10)) {
                nbt = stackTag.getCompound("tag");
            }
            if (withForgeCaps && stackTag.contains("ForgeCaps")) {
                caps = stackTag.getCompound("ForgeCaps");
            }
            CompoundNBT stringTag = null;
            if (nbt != null && caps == null) {
                stringTag = nbt;
            } else if (caps != null) {
                stringTag = new CompoundNBT();
                stringTag.put("ForgeCaps", caps);
                stringTag.put("tag", nbt == null ? new CompoundNBT() : nbt);
            }

            if (stringTag != null && !stringTag.isEmpty()) {
                stackString += stringTag.toString();
            }
        }

        if (withCount && stack.getCount() > 1) {
            stackString += "," + stack.getCount();
        }

        return stackString;
    }

    /**
     * Includes everything except capabilities.
     * */
    public static String toStringNoCaps(ItemStack stack) {
        return toString(stack, true, true, false);
    }

    /**
     * Default toString Method includes all item data including capabilities if they are present.
     */
    public static String toString(ItemStack stack) {
        return toString(stack, true, true, true);
    }

    @Deprecated
    public static ItemStack legacyStackConverter(String itemString, int count, int damage, @Nullable CompoundNBT nbt) {
        try {
            ResourceLocation itemID = new ResourceLocation(itemString);
            Item item = ForgeRegistries.ITEMS.getValue(itemID);
            Block block = ForgeRegistries.BLOCKS.getValue(itemID);
            if (item == null && block == Blocks.AIR) {
                return ItemStack.EMPTY;
            } else {
                ItemStack itemStack;
                if (item != null) {
                    itemStack = new ItemStack(item, count);
                } else {
                    itemStack = new ItemStack(block, count);
                }
                itemStack.setDamageValue(damage);

                if (nbt != null) {
                    itemStack.setTag(nbt.copy());
                }
                return itemStack;
            }
        }
        catch (Throwable e) {
            return ItemStack.EMPTY;
        }
    }

}
