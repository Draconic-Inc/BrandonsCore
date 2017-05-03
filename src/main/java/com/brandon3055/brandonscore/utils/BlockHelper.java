package com.brandon3055.brandonscore.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by brandon3055 on 24/3/2016.
 * Some general block related helper functions.
 */
public class BlockHelper {

//	public void setBlock(BlockPos pos, )


    /**
     * This method returns the proper name of the block at the given position if it can be found.
     * This method is for client side use only.
     * @return the blocks name.
     */
    @SideOnly(Side.CLIENT)
    public static String getBlockName(BlockPos pos, World world) {
        IBlockState state = world.getBlockState(pos);
        ItemStack stack;

        try {
            RayTraceResult result = new RayTraceResult(RayTraceResult.Type.BLOCK, Vec3d.ZERO, EnumFacing.UP, pos);
            stack = state.getBlock().getPickBlock(state, result, world, pos, Minecraft.getMinecraft().thePlayer);
        }
        catch (Throwable ignored) {
            stack = state.getBlock().getItem(world, pos, state);
        }

        String name = "Unknown";

        if (stack != null) {
            return I18n.format(stack.getUnlocalizedName() + ".name");
        }

        return name;
    }
}
