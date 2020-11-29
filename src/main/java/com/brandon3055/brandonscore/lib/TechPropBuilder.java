package com.brandon3055.brandonscore.lib;

import codechicken.lib.util.Copyable;
import com.brandon3055.brandonscore.api.TechLevel;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.common.ToolType;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static net.minecraftforge.common.ToolType.*;

/**
 * Created by brandon3055 on 21/5/20.
 */
public class TechPropBuilder {

    public TechLevel techLevel;
    public int miningLevel;

    private int maxStackSize = 64;
    private int maxDamage;
    private Item containerItem;
    private ItemGroup group;
    private Rarity rarity = Rarity.COMMON;
    private Food food;
    private boolean canRepair = true;
    private java.util.Map<net.minecraftforge.common.ToolType, Integer> toolClasses = Maps.newHashMap();
    private java.util.function.Supplier<java.util.concurrent.Callable<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer>> ister;


    public TechPropBuilder(TechLevel techLevel) {
        this.techLevel = techLevel;
        this.miningLevel = techLevel.getHarvestLevel();
    }

    public TechPropBuilder food(Food foodIn) {
        this.food = foodIn;
        return this;
    }

    public TechPropBuilder maxStackSize(int maxStackSizeIn) {
        if (this.maxDamage > 0) {
            throw new RuntimeException("Unable to have damage AND stack.");
        } else {
            this.maxStackSize = maxStackSizeIn;
            return this;
        }
    }

    public TechPropBuilder defaultMaxDamage(int maxDamageIn) {
        return this.maxDamage == 0 ? this.maxDamage(maxDamageIn) : this;
    }

    public TechPropBuilder maxDamage(int maxDamageIn) {
        this.maxDamage = maxDamageIn;
        this.maxStackSize = 1;
        return this;
    }

    public TechPropBuilder containerItem(Item containerItemIn) {
        this.containerItem = containerItemIn;
        return this;
    }

    public TechPropBuilder group(ItemGroup groupIn) {
        this.group = groupIn;
        return this;
    }

    public TechPropBuilder rarity(Rarity rarityIn) {
        this.rarity = rarityIn;
        return this;
    }

    public TechPropBuilder setNoRepair() {
        canRepair = false;
        return this;
    }

    public TechPropBuilder addToolType(net.minecraftforge.common.ToolType type, int level) {
        toolClasses.put(type, level);
        return this;
    }

    public TechPropBuilder setISTER(java.util.function.Supplier<java.util.concurrent.Callable<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer>> ister) {
        this.ister = ister;
        return this;
    }



    public Item.Properties build() {
        Item.Properties props = new Item.Properties();
        props.food(food);
        props.group(group);
        props.maxDamage(maxDamage);
        props.rarity(rarity);
        if (!canRepair) props.setNoRepair();
        props.setISTER(ister);
        toolClasses.forEach(props::addToolType);
        props.containerItem(containerItem);
        props.maxStackSize(maxStackSize);


        return props;
    }



    public Item.Properties pickaxeProps() {
        return build().addToolType(PICKAXE, miningLevel);
    }

    public Item.Properties axeProps() {
        return build().addToolType(AXE, miningLevel);
    }

    public Item.Properties shovelProps() {
        return build().addToolType(SHOVEL, miningLevel);
    }

    public Item.Properties staffProps() {
        return build().addToolType(PICKAXE, miningLevel).addToolType(AXE, miningLevel).addToolType(SHOVEL, miningLevel);
    }







//    @Override
//    public TechItemProps copy() {
//        TechItemProps copy = new TechItemProps(techLevel);
//        copy.maxStackSize = this.maxStackSize;
//        copy.maxDamage = this.maxDamage;
//        copy.containerItem = this.containerItem;
//        copy.group = this.group;
//        copy.rarity = this.rarity;
//        copy.food = this.food;
//        copy.canRepair = this.canRepair;
//        copy.toolClasses.putAll(this.toolClasses);
//        copy.ister = this.ister;
//        return copy;
//    }
//
//    public TechItemProps pickaxeProps() {
//        TechItemProps copy = copy();
//        return copy.addToolType(PICKAXE, miningLevel);
//    }
//
//    public TechItemProps axeProps() {
//        TechItemProps copy = copy();
//        return copy.addToolType(AXE, miningLevel);
//    }
//
//    public TechItemProps shovelProps() {
//        TechItemProps copy = copy();
//        return copy.addToolType(SHOVEL, miningLevel);
//    }
//
//    public TechItemProps staffProps() {
//        TechItemProps copy = copy();
//        return copy.addToolType(PICKAXE, miningLevel).addToolType(AXE, miningLevel).addToolType(SHOVEL, miningLevel);
//    }
//
//    //@formatter:off
//    @Override public TechItemProps food(Food foodIn) { return (TechItemProps) super.food(foodIn); }
//    @Override public TechItemProps maxStackSize(int maxStackSizeIn) { return (TechItemProps) super.maxStackSize(maxStackSizeIn); }
//    @Override public TechItemProps defaultMaxDamage(int maxDamageIn) { return (TechItemProps) super.defaultMaxDamage(maxDamageIn); }
//    @Override public TechItemProps maxDamage(int maxDamageIn) { return (TechItemProps) super.maxDamage(maxDamageIn); }
//    @Override public TechItemProps containerItem(Item containerItemIn) { return (TechItemProps) super.containerItem(containerItemIn); }
//    @Override public TechItemProps group(ItemGroup groupIn) { return (TechItemProps) super.group(groupIn); }
//    @Override public TechItemProps rarity(Rarity rarityIn) { return (TechItemProps) super.rarity(rarityIn); }
//    @Override public TechItemProps setNoRepair() { return (TechItemProps) super.setNoRepair(); }
//    @Override public TechItemProps addToolType(ToolType type, int level) { return (TechItemProps) super.addToolType(type, level); }
//    @Override public TechItemProps setISTER(Supplier<Callable<ItemStackTileEntityRenderer>> ister) { return (TechItemProps) super.setISTER(ister); }
//    //@formatter:on
}
