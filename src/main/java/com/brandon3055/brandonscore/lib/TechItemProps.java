package com.brandon3055.brandonscore.lib;

import codechicken.lib.util.Copyable;
import com.brandon3055.brandonscore.api.TechLevel;
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
public class TechItemProps extends Item.Properties implements Copyable<TechItemProps> {

    public TechLevel techLevel;
    public int miningLevel;

    public TechItemProps(TechLevel techLevel) {
        this.techLevel = techLevel;
        this.miningLevel = techLevel.getHarvestLevel();
    }

    @Override
    public TechItemProps copy() {
        TechItemProps copy = new TechItemProps(techLevel);
        copy.maxStackSize = this.maxStackSize;
        copy.maxDamage = this.maxDamage;
        copy.containerItem = this.containerItem;
        copy.group = this.group;
        copy.rarity = this.rarity;
        copy.food = this.food;
        copy.canRepair = this.canRepair;
        copy.toolClasses.putAll(this.toolClasses);
        copy.ister = this.ister;
        return copy;
    }

    public TechItemProps pickaxeProps() {
        TechItemProps copy = copy();
        return copy.addToolType(PICKAXE, miningLevel);
    }

    public TechItemProps axeProps() {
        TechItemProps copy = copy();
        return copy.addToolType(AXE, miningLevel);
    }

    public TechItemProps shovelProps() {
        TechItemProps copy = copy();
        return copy.addToolType(SHOVEL, miningLevel);
    }

    public TechItemProps staffProps() {
        TechItemProps copy = copy();
        return copy.addToolType(PICKAXE, miningLevel).addToolType(AXE, miningLevel).addToolType(SHOVEL, miningLevel);
    }

    //@formatter:off
    @Override public TechItemProps food(Food foodIn) { return (TechItemProps) super.food(foodIn); }
    @Override public TechItemProps maxStackSize(int maxStackSizeIn) { return (TechItemProps) super.maxStackSize(maxStackSizeIn); }
    @Override public TechItemProps defaultMaxDamage(int maxDamageIn) { return (TechItemProps) super.defaultMaxDamage(maxDamageIn); }
    @Override public TechItemProps maxDamage(int maxDamageIn) { return (TechItemProps) super.maxDamage(maxDamageIn); }
    @Override public TechItemProps containerItem(Item containerItemIn) { return (TechItemProps) super.containerItem(containerItemIn); }
    @Override public TechItemProps group(ItemGroup groupIn) { return (TechItemProps) super.group(groupIn); }
    @Override public TechItemProps rarity(Rarity rarityIn) { return (TechItemProps) super.rarity(rarityIn); }
    @Override public TechItemProps setNoRepair() { return (TechItemProps) super.setNoRepair(); }
    @Override public TechItemProps addToolType(ToolType type, int level) { return (TechItemProps) super.addToolType(type, level); }
    @Override public TechItemProps setISTER(Supplier<Callable<ItemStackTileEntityRenderer>> ister) { return (TechItemProps) super.setISTER(ister); }
    //@formatter:on
}
