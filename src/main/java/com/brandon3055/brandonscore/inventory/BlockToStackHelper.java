package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventoryUtils;
import com.mojang.authlib.GameProfile;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BlockToStackHelper {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static FakePlayer harvester = null;
    public static List<ItemStack> itemCollection = null;

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.addListener(BlockToStackHelper::entityJoinWorld);
    }

    public static void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && BlockToStackHelper.itemCollection != null && !event.isCanceled()) {
            BlockToStackHelper.itemCollection.add(((ItemEntity) event.getEntity()).getItem());
            event.setCanceled(true);
        }
    }

    public static List<ItemStack> breakAndCollect(Level world, BlockPos pos, int xp) {
        return breakAndCollectWithPlayer(world, pos, null, xp);
    }

    public static List<ItemStack> breakAndCollectWithPlayer(Level world, BlockPos pos, Player player, int xp) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();

        if (!(world instanceof ServerLevel)) {
            return stacks;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (player == null) {
            player = getHarvester((ServerLevel) world);
        }
        itemCollection = new ArrayList<>();

        BlockEntity tile = world.getBlockEntity(pos);
        if (state.onDestroyedByPlayer(world, pos, player, true, world.getFluidState(pos))){
            block.destroy(world, pos, state);
            block.playerDestroy(world, player, pos, state, tile, player.getMainHandItem());
        }

        stacks.addAll(itemCollection);
        itemCollection = null;
        return stacks;
    }

    public static void breakAndCollect(Level world, BlockPos pos, InventoryDynamic inventoryDynamic, int xp) {
        breakAndCollectWithPlayer(world, pos, inventoryDynamic, null, xp);
    }

    public static void breakAndCollectWithPlayer(Level world, BlockPos pos, InventoryDynamic inventoryDynamic, Player player, int xp) {
        List<ItemStack> stacks = breakAndCollectWithPlayer(world, pos, player, xp);
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()){
                InventoryUtils.insertItem(inventoryDynamic, stack, false);
            }
        }
        inventoryDynamic.xp += xp;
    }

    public static FakePlayer getHarvester(ServerLevel world) {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(world, new GameProfile(UUID.fromString("060e69c4-6aed-11e6-8b77-86f30ca893d3"), "[Brandons-Core]"));
            harvester.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        }

        return harvester;
    }
}
