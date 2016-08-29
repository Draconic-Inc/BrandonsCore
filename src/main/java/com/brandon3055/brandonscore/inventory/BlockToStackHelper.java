package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventoryUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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

    public static List<ItemStack> breakAndCollect(World world, BlockPos pos) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();

        if (!(world instanceof WorldServer)) {
            return stacks;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        EntityPlayer player = getHarvester((WorldServer) world);
        itemCollection = new ArrayList<ItemStack>();

        block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), player.getHeldItemMainhand());
        world.setBlockToAir(pos);

        stacks.addAll(itemCollection);
        itemCollection = null;
        return stacks;
    }

    public static void breakAndCollect(World world, BlockPos pos, InventoryDynamic inventoryDynamic) {
        List<ItemStack> stacks = breakAndCollect(world, pos);
        for (ItemStack stack : stacks) {
//            if (stack.getItem() == Item.getItemFromBlock(Blocks.STONE)) {
//                BCLogHelper.bigInfo("Detected Stone");
//            }
            InventoryUtils.insertItem(inventoryDynamic, stack, false);
        }
    }

    public static FakePlayer getHarvester(WorldServer world) {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(world, new GameProfile(UUID.fromString("060e69c4-6aed-11e6-8b77-86f30ca893d3"), "[Brandons-Core]"));
            harvester.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        }

        return harvester;
    }
}
