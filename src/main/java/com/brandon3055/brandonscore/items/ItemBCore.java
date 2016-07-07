package com.brandon3055.brandonscore.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 18/3/2016.
 */
public class ItemBCore extends Item {

    public Map<Integer, String> nameMap = new HashMap<Integer, String>();

    public ItemBCore addName(int damage, String name){
        nameMap.put(damage, name);
        return this;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (getHasSubtypes() && nameMap.containsKey(stack.getItemDamage())) {
            return super.getUnlocalizedName(stack) + "." + nameMap.get(stack.getItemDamage());
        }
        else return super.getUnlocalizedName(stack);
    }
}
