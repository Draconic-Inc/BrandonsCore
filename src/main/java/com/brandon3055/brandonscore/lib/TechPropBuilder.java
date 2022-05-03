//package com.brandon3055.brandonscore.lib;
//
//import com.brandon3055.brandonscore.api.TechLevel;
//import com.google.common.collect.Maps;
//import net.minecraft.world.food.FoodProperties;
//import net.minecraft.world.item.CreativeModeTab;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.Rarity;
//
//
///**
// * Created by brandon3055 on 21/5/20.
// */
//public class TechPropBuilder {
//
//    public TechLevel techLevel;
//
//    private int maxStackSize = 64;
//    private int maxDamage;
//    private Item containerItem;
//    private CreativeModeTab group;
//    private Rarity rarity = Rarity.COMMON;
//    private FoodProperties food;
//    private boolean canRepair = true;
//
//    public TechPropBuilder(TechLevel techLevel) {
//        this.techLevel = techLevel;
//    }
//
//    public TechPropBuilder food(FoodProperties foodIn) {
//        this.food = foodIn;
//        return this;
//    }
//
//    public TechPropBuilder maxStackSize(int maxStackSizeIn) {
//        if (this.maxDamage > 0) {
//            throw new RuntimeException("Unable to have damage AND stack.");
//        } else {
//            this.maxStackSize = maxStackSizeIn;
//            return this;
//        }
//    }
//
//    public TechPropBuilder defaultMaxDamage(int maxDamageIn) {
//        return this.maxDamage == 0 ? this.maxDamage(maxDamageIn) : this;
//    }
//
//    public TechPropBuilder maxDamage(int maxDamageIn) {
//        this.maxDamage = maxDamageIn;
//        this.maxStackSize = 1;
//        return this;
//    }
//
//    public TechPropBuilder containerItem(Item containerItemIn) {
//        this.containerItem = containerItemIn;
//        return this;
//    }
//
//    public TechPropBuilder group(CreativeModeTab groupIn) {
//        this.group = groupIn;
//        return this;
//    }
//
//    public TechPropBuilder rarity(Rarity rarityIn) {
//        this.rarity = rarityIn;
//        return this;
//    }
//
//    public TechPropBuilder setNoRepair() {
//        canRepair = false;
//        return this;
//    }
//
//    public Item.Properties build() {
//        Item.Properties props = new Item.Properties();
//        props.food(food);
//        props.tab(group);
//        props.durability(maxDamage);
//        props.rarity(rarity);
//        if (!canRepair) props.setNoRepair();
//        props.craftRemainder(containerItem);
//        props.stacksTo(maxStackSize);
//
//        return props;
//    }
//}
