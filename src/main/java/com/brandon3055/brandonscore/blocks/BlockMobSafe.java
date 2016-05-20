package com.brandon3055.brandonscore.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 22/3/2016.
 * This is a base for any block that needs to be resistant to all mobs
 */
public class BlockMobSafe extends BlockBCore {

    public BlockMobSafe(Material material) {
        super(material);
    }

    //region Resistance
    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return entity instanceof EntityPlayer;//This should allow fake players to now break this block
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }
    //endregion
}
