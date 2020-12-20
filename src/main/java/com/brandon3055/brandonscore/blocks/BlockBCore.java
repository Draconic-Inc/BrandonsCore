package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IDataRetainingTile;
import com.brandon3055.brandonscore.lib.*;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base block class form all blocks.
 */
public class BlockBCore extends Block implements IBCoreBlock {
    public static final String BC_TILE_DATA_TAG = "bc_tile_data";
    public static final String BC_MANAGED_DATA_FLAG = "bc_managed_data"; //Seemed like as good a place as any to put this.

    protected boolean canProvidePower = false;
    protected boolean hasSubItemTypes = false;
    protected boolean isMobResistant = false;
    public Map<Integer, String> nameOverrides = new HashMap<>();

    public BlockBCore(Block.Properties properties) {
        super(properties);
    }

    //endregion

    //region Setters & Getters

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

//        if (stack.getItem() == Item.getItemFromBlock(this) && stack.getItem().getHasSubtypes()) {
//            stack.setItemDamage(getMetaFromState(state));
//        }

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IDataRetainingTile && !BrandonsCore.proxy.isCTRLKeyDown()) {
            CompoundNBT tileData = new CompoundNBT();
            ((IDataRetainingTile) tile).writeToItemStack(tileData, false);
            if (!tileData.isEmpty()) {
                ItemNBTHelper.getCompound(stack).put(BC_TILE_DATA_TAG, tileData);
            }
        }

        if (tile instanceof INameable && ((INameable) tile).hasCustomName()) {
            stack.setDisplayName(((INameable) tile).getName());
        }

        return stack;
    }

    /**An uber function that returns all the correct values in all the correct places to make this a partial/transparent block*/
    public boolean isBlockFullCube() {
        return false;
    }

    /**
     * Adds a name mapping for the given metadata.
     * The overridden unlocalized name will be as follows.<br>
     * tile.[modid]:[nameAddedByThisMethod].name<br>
     * This also sets hasSubTypes to true.
     */
    public BlockBCore addName(int meta, String name) {
        nameOverrides.put(meta, name);
        this.setHasSubItemTypes(true);
        return this;
    }

    public BlockBCore setHasSubItemTypes(boolean hasSubItemTypes) {
        this.hasSubItemTypes = hasSubItemTypes;
        return this;
    }

    @Override
    public boolean hasSubItemTypes() {
        return hasSubItemTypes;
    }

    @Override
    public Map<Integer, String> getNameOverrides() {
        return nameOverrides;
    }

    /**
     * @return false if this block has been disabled via the mod config.
     */
    @Deprecated
    public boolean isBlockEnabled() {
        return true;//ModFeatureParser.isEnabled(this);
    }

    //endregion

    //region Interfaces

    //IRedstoneEmitter

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof IRedstoneEmitter;
        }

        return canProvidePower || super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return canProvidePower;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof IChangeListener;
        }

        return super.shouldCheckWeakPower(state, world, pos, side);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (hasTileEntity(blockState)) {
            TileEntity tile = blockAccess.getTileEntity(pos);
            if (tile instanceof IRedstoneEmitter) {
                return ((IRedstoneEmitter) tile).getWeakPower(blockState, side);
            }
        }
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (hasTileEntity(blockState)) {
            TileEntity tile = blockAccess.getTileEntity(pos);
            if (tile instanceof IRedstoneEmitter) {
                return ((IRedstoneEmitter) tile).getStrongPower(blockState, side);
            }
        }
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IChangeListener) {
                ((IChangeListener) tile).onNeighborChange(fromPos);
            }
        }
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
    }

    //IActivatableTile

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (hasTileEntity(state)) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof IActivatableTile) {
                return ((IActivatableTile) tile).onBlockActivated(state, player, handIn, hit) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
            }
        }

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    //IDataRetainingTile
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IDataRetainingTile) {
            if (stack.hasTag() && stack.getTag().contains(BC_TILE_DATA_TAG)) {
                ((IDataRetainingTile) tile).readFromItemStack(stack.getChildTag(BC_TILE_DATA_TAG));
            }
        }

        if (tile instanceof TileBCore && stack.hasDisplayName()) {
            ((TileBCore) tile).setCustomName(stack.getDisplayName().getString());
        }
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack heldStack) {
        ItemStack stack = null;

        if (te instanceof IDataRetainingTile && ((IDataRetainingTile) te).saveToItem()) {
            CompoundNBT tileData = new CompoundNBT();
            ((IDataRetainingTile) te).writeToItemStack(tileData, true);
            if (!tileData.isEmpty()) {
                stack = new ItemStack(this, 1);//, damageDropped(state));
                ItemNBTHelper.getCompound(stack).put(BC_TILE_DATA_TAG, tileData);
            }
        }

        if (te instanceof INameable && ((INameable) te).hasCustomName()) {
            if (stack == null) {
                stack = new ItemStack(this, 1);//, damageDropped(state));
            }
            stack.setDisplayName(((INameable) te).getName());
        }

        if (stack != null) {
            player.addStat(Stats.BLOCK_MINED.get(this));
            player.addExhaustion(0.005F);

            spawnAsEntity(world, pos, stack);
            //Remove tile to make sure no one else can mess with it and dupe its contents.
            world.removeTileEntity(pos);
        } else {
            super.harvestBlock(world, player, pos, state, te, heldStack);
        }
    }

    //endregion

    //region Mob Resistance

    public BlockBCore setMobResistant() {
        isMobResistant = true;
        return this;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        if (!isMobResistant) {
            return super.canEntityDestroy(state, world, pos, entity);
        }
        return entity instanceof PlayerEntity;
    }


    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if (!isMobResistant) {
            super.onBlockExploded(state, world, pos, explosion);
        }
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        if (!isMobResistant) {
            return super.canDropFromExplosion(explosionIn);
        }
        return false;
    }

    //endregion

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return !isBlockFullCube();
    }

    //Utils
    public static int getRedstonePower(IWorldReader world, BlockPos pos, Direction facing) {
        BlockState blockstate = world.getBlockState(pos);
        return blockstate.shouldCheckWeakPower(world, pos, facing) ? getStrongPower(world, pos) : blockstate.getWeakPower(world, pos, facing);
    }

    public static int getStrongPower(IWorldReader world, BlockPos pos) {
        int i = 0;
        i = Math.max(i, world.getStrongPower(pos.down(), Direction.DOWN));
        if (i >= 15) {
            return i;
        } else {
            i = Math.max(i, world.getStrongPower(pos.up(), Direction.UP));
            if (i >= 15) {
                return i;
            } else {
                i = Math.max(i, world.getStrongPower(pos.north(), Direction.NORTH));
                if (i >= 15) {
                    return i;
                } else {
                    i = Math.max(i, world.getStrongPower(pos.south(), Direction.SOUTH));
                    if (i >= 15) {
                        return i;
                    } else {
                        i = Math.max(i, world.getStrongPower(pos.west(), Direction.WEST));
                        if (i >= 15) {
                            return i;
                        } else {
                            i = Math.max(i, world.getStrongPower(pos.east(), Direction.EAST));
                            return i >= 15 ? i : i;
                        }
                    }
                }
            }
        }
    }

    public static boolean isBlockPowered(IWorldReader world, BlockPos pos) {
        if (getRedstonePower(world, pos.down(), Direction.DOWN) > 0) {
            return true;
        } else if (getRedstonePower(world, pos.up(), Direction.UP) > 0) {
            return true;
        } else if (getRedstonePower(world, pos.north(), Direction.NORTH) > 0) {
            return true;
        } else if (getRedstonePower(world, pos.south(), Direction.SOUTH) > 0) {
            return true;
        } else if (getRedstonePower(world, pos.west(), Direction.WEST) > 0) {
            return true;
        } else {
            return getRedstonePower(world, pos.east(), Direction.EAST) > 0;
        }
    }
    //

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (stack.hasTag() && stack.getTag().contains(BC_TILE_DATA_TAG)) {
            tooltip.add(new TranslationTextComponent("info.de.hasSavedData.txt"));
        }
    }

    @Override
    public boolean overrideShareTag() {
        return false;
    }

    @Override
    public CompoundNBT getNBTShareTag(ItemStack stack) {
        return stack.getTag();
    }
}

