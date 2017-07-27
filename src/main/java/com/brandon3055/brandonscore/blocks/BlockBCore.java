package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.api.IDataRetainingTile;
import com.brandon3055.brandonscore.lib.IActivatableTile;
import com.brandon3055.brandonscore.lib.IChangeListener;
import com.brandon3055.brandonscore.lib.IRedstoneEmitter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
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
    protected boolean isFullCube = true;
    protected boolean canProvidePower = false;
    protected boolean hasSubItemTypes = false;
    public Map<Integer, String> nameOverrides = new HashMap<>();

    public BlockBCore() {
        this(Material.ROCK);
    }

    public BlockBCore(Material material) {
        super(material);
        this.setHardness(5F);
        this.setResistance(10F);
    }

    //region Sub Types and Names

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        super.getSubBlocks(tab, list);
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

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IDataRetainingTile) {
            ((IDataRetainingTile) tile).writeToItemStack(stack, false);
        }

        return stack;
    }

    public BlockBCore setIsFullCube(boolean value) {
        isFullCube = value;
        return this;
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

    public boolean hasSubItemTypes() {
        return hasSubItemTypes;
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IChangeListener) {
                ((IChangeListener) tile).onNeighborChange(fromPos);
            }
        }
        super.neighborChanged(state, world, pos, blockIn, fromPos);
    }

    //IActivatableTile
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hasTileEntity(state)) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof IActivatableTile) {
                return ((IActivatableTile) tile).onBlockActivated(state, playerIn, hand, facing, hitX, hitY, hitZ);
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    //IDataRetainingTile
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IDataRetainingTile) {
            ((IDataRetainingTile) tile).readFromItemStack(stack);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack heldStack) {
        if (te instanceof IDataRetainingTile && ((IDataRetainingTile) te).saveToItem()) {
            ItemStack stack = new ItemStack(this, 1, damageDropped(state));
            ((IDataRetainingTile) te).writeToItemStack(stack, true);
            spawnAsEntity(world, pos, stack);
            //Remove tile to make sure no one else can mess with it and dupe its contents.
            world.removeTileEntity(pos);
        }
        else {
            super.harvestBlock(world, player, pos, state, te, heldStack);
        }
    }

    //endregion

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
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(TileBCBase.TILE_DATA_TAG)) {
            tooltip.add(I18n.format("info.de.hasSavedData.txt"));
        }
    }

    public boolean overrideShareTag() {
        return false;
    }

    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        return stack.getTagCompound();
    }
}

