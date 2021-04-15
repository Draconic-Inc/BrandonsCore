package com.brandon3055.brandonscore.items;

import net.minecraft.item.Item;

/**
 * Created by brandon3055 on 18/3/2016.
 */
@Deprecated //Unless i find a use for it while porting DE
public class ItemBCore extends Item {

//    public Map<Integer, String> nameMap = new HashMap<>();

    public ItemBCore(Properties properties) {
        super(properties);
    }

//    public ItemBCore addName(int damage, String name) {
//        nameMap.put(damage, name);
//        return this;
//    }

//    @Override
//    public String getTranslationKey(ItemStack stack) {
////        if (getHasSubtypes() && nameMap.containsKey(stack.getItemDamage())) {
////            return super.getTranslationKey(stack) + "." + nameMap.get(stack.getItemDamage());
////        }
////        else
//            return super.getTranslationKey(stack);
//    }

//    /**
//     * @return false if this item has been disabled via the mod config.
//     */
//    //TODO isItemEnabled
//    public boolean isItemEnabled() {
////        return ModFeatureParser.isEnabled(this);
//        return true;
//    }
}
