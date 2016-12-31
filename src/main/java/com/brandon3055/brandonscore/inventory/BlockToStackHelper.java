package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventoryUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BlockToStackHelper {

    public static FakePlayer harvester = null;
    public static List<ItemStack> itemCollection = null;

    public static List<ItemStack> breakAndCollect(World world, BlockPos pos, int xp) {
        return breakAndCollectWithPlayer(world, pos, null, xp);
    }

    public static List<ItemStack> breakAndCollectWithPlayer(World world, BlockPos pos, EntityPlayer player, int xp) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();

        if (!(world instanceof WorldServer)) {
            return stacks;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (player == null) {
            player = getHarvester((WorldServer) world);
        }
        itemCollection = new ArrayList<ItemStack>();

        TileEntity tile = world.getTileEntity(pos);
        if (block.removedByPlayer(state, world, pos, player, true)) {
            block.onBlockDestroyedByPlayer(world, pos, state);
            block.harvestBlock(world, player, pos, state, tile, player.getHeldItemMainhand());
//            block.dropXpOnBlockBreak(world, pos, xp);
        }

        stacks.addAll(itemCollection);
        itemCollection = null;
        return stacks;
    }

    public static void breakAndCollect(World world, BlockPos pos, InventoryDynamic inventoryDynamic, int xp) {
        breakAndCollectWithPlayer(world, pos, inventoryDynamic, null, xp);
    }

    public static void breakAndCollectWithPlayer(World world, BlockPos pos, InventoryDynamic inventoryDynamic, EntityPlayer player, int xp) {
        List<ItemStack> stacks = breakAndCollectWithPlayer(world, pos, player, xp);
        for (ItemStack stack : stacks) {
            InventoryUtils.insertItem(inventoryDynamic, stack, false);
        }
        inventoryDynamic.xp += xp;
    }

    public static FakePlayer getHarvester(WorldServer world) {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(world, new GameProfile(UUID.fromString("060e69c4-6aed-11e6-8b77-86f30ca893d3"), "[Brandons-Core]"));
            harvester.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        }

        return harvester;
    }
}
