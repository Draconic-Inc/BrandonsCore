package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.api.IDataRetainerTile;
import com.brandon3055.brandonscore.lib.IActivatableTile;
import com.brandon3055.brandonscore.lib.IChangeListener;
import com.brandon3055.brandonscore.lib.IRedstoneEmitter;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base block class form all blocks.
 */
public class BlockBCore extends Block {
    public static final String TILE_DATA_TAG = "DETileData";
    protected boolean isFullCube = true;
    protected boolean canProvidePower = false;
    public Map<Integer, String> nameOverrides = new HashMap<>();

    public BlockBCore() {
        this(Material.ROCK);
    }

    public BlockBCore(Material material) {
        super(material);
        this.setHardness(5F);
        this.setResistance(10F);
    }

    //region Rename field names
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        super.getSubBlocks(item, tab, list);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IDataRetainerTile && ItemNBTHelper.getCompound(stack).hasKey(BlockBCore.TILE_DATA_TAG)) {
            ((IDataRetainerTile) tile).readRetainedData(ItemNBTHelper.getCompound(stack).getCompoundTag(BlockBCore.TILE_DATA_TAG));
        }
    }
    //endregion

    //region Setters & Getters
    public BlockBCore setHarvestTool(String toolClass, int level) {
        this.setHarvestLevel(toolClass, level);
        return this;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if (stack.getItem() == Item.getItemFromBlock(this) && stack.getItem().getHasSubtypes()) {
            stack.setItemDamage(getMetaFromState(state));
        }

        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof IDataRetainerTile) {
            NBTTagCompound customData = new NBTTagCompound();
            ((IDataRetainerTile) tileEntity).writeRetainedData(customData);
            ItemNBTHelper.getCompound(stack).setTag(TILE_DATA_TAG, customData);
        }

        return stack;
    }

    public BlockBCore setIsFullCube(boolean value) {
        isFullCube = value;
        return this;
    }

    public BlockBCore addName(int meta, String name) {
        nameOverrides.put(meta, name);
        return this;
    }

    //endregion

    //region Interfaces

    //IRedstoneEmitter
    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof IRedstoneEmitter || canProvidePower;
        }

        return super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return canProvidePower;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof IChangeListener;
        }

        return super.shouldCheckWeakPower(state, world, pos, side);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (hasTileEntity(blockState)) {
            TileEntity tile = blockAccess.getTileEntity(pos);
            if (tile instanceof IRedstoneEmitter) {
                return ((IRedstoneEmitter) tile).getWeakPower(blockState, side);
            }
        }
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (hasTileEntity(blockState)) {
            TileEntity tile = blockAccess.getTileEntity(pos);
            if (tile instanceof IRedstoneEmitter) {
                return ((IRedstoneEmitter) tile).getStrongPower(blockState, side);
            }
        }
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IChangeListener) {
                ((IChangeListener) tile).onNeighborChange();
            }
        }
        super.neighborChanged(state, world, pos, blockIn);
    }

    //IActivatableTile

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hasTileEntity(state)) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof IActivatableTile) {
                return ((IActivatableTile) tile).onBlockActivated(state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    //endregion

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack heldStack) {
        if (te instanceof IDataRetainerTile) {
            ItemStack stack = new ItemStack(this);
            stack.setItemDamage(damageDropped(state));
            NBTTagCompound customData = new NBTTagCompound();
            ((IDataRetainerTile) te).writeRetainedData(customData);
            ItemNBTHelper.getCompound(stack).setTag(TILE_DATA_TAG, customData);
            spawnAsEntity(world, pos, stack);
            world.removeTileEntity(pos);
        }
        else {
            super.harvestBlock(world, player, pos, state, te, heldStack);
        }
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return isFullCube;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isFullCube;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(BlockBCore.TILE_DATA_TAG)) {
            tooltip.add(I18n.format("info.de.hasSavedData.txt"));
        }
    }
}

