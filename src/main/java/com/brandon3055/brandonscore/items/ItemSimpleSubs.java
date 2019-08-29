package com.brandon3055.brandonscore.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 22/3/2016.
 * Used for simple items with sub types
 */
public class ItemSimpleSubs extends ItemBCore {

    public final Map<Integer, String> nameMap = new HashMap<Integer, String>();

    public ItemSimpleSubs(String[] names) {
        if (names.length > 0) {
            for (String s : names) {
                int meta = Integer.parseInt(s.substring(0, s.indexOf(":")));
                nameMap.put(meta, s.substring(s.indexOf(":") + 1));
            }
            setHasSubtypes(true);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            if (nameMap.size() > 0) {
                for (Integer i : nameMap.keySet()) subItems.add(new ItemStack(this, 1, i));
            }
            else {
                super.getSubItems(tab, subItems);
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (getHasSubtypes() && nameMap.containsKey(stack.getItemDamage())) {
            return (super.getTranslationKey(stack) + "." + nameMap.get(stack.getItemDamage())).replaceAll("=", ".");
        }
        else {
            return super.getTranslationKey(stack);
        }
    }
}
