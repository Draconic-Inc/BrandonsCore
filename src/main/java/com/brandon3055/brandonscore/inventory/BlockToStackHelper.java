package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventoryUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

    public static List<ItemStack> breakAndCollectWithPlayer(World world, BlockPos pos, PlayerEntity player, int xp) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();

        if (!(world instanceof ServerWorld)) {
            return stacks;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (player == null) {
            player = getHarvester((ServerWorld) world);
        }
        itemCollection = new ArrayList<>();

//        TileEntity tile = world.getTileEntity(pos);
//        if (block.removedByPlayer(state, world, pos, player, true, world.getFluidState(pos))) {
////            block.onPlayerDestroy(world, pos, state);
//            block.onBlockHarvested(world, pos, state, player);
//            block.harvestBlock(world, player, pos, state, tile, player.getHeldItemMainhand());
////            block.dropXpOnBlockBreak(world, pos, xp);
//        }

        //TODO I think this is good? Needs testing
        if (state.removedByPlayer(world, pos, player, true, world.getFluidState(pos))){
            state.getBlock().onPlayerDestroy(world, pos, state);
        }


        stacks.addAll(itemCollection);
        itemCollection = null;
        return stacks;
    }

    public static void breakAndCollect(World world, BlockPos pos, InventoryDynamic inventoryDynamic, int xp) {
        breakAndCollectWithPlayer(world, pos, inventoryDynamic, null, xp);
    }

    public static void breakAndCollectWithPlayer(World world, BlockPos pos, InventoryDynamic inventoryDynamic, PlayerEntity player, int xp) {
        List<ItemStack> stacks = breakAndCollectWithPlayer(world, pos, player, xp);
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()){
                InventoryUtils.insertItem(inventoryDynamic, stack, false);
            }
        }
        inventoryDynamic.xp += xp;
    }

    public static FakePlayer getHarvester(ServerWorld world) {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(world, new GameProfile(UUID.fromString("060e69c4-6aed-11e6-8b77-86f30ca893d3"), "[Brandons-Core]"));
            harvester.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        }

        return harvester;
    }
}
